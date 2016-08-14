import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.postgis.LinearRing;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

public class EnvironDataExtractor {

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException, ParseException {
		Connection connection = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		Class.forName("org.postgresql.Driver");
		connection = DriverManager.getConnection(
				"jdbc:postgresql://localhost:5432/twitter_db", "sede",
				"dataexplorer");
		connection.setAutoCommit(false);
		((org.postgresql.PGConnection) connection).addDataType("geometry",
				Class.forName("org.postgis.PGgeometry"));
		long startfeedid = 0;
		// Check if there are any data inserted, if not start from beginning
		// else get the max val
		PreparedStatement maxfeedidpst = connection
				.prepareStatement("select max(feed_id) from us_weather_events");
		PreparedStatement feeddatapst = connection
				.prepareStatement("select feed_data from us_weather_feed where id=?");
		PreparedStatement checkeventexistspst = connection
				.prepareStatement("select id from us_weather_events where id=?");
		PreparedStatement inserteventpst = connection
				.prepareStatement("insert into us_weather_events(id,feed_id,event_type,title,summary,effective,expires,urgency,severity,area_desc,area) values(?,?,?,?,?,?,?,?,?,?,?)");
		ResultSet maxfeedidrs = maxfeedidpst.executeQuery();
		maxfeedidrs.next();
		startfeedid = maxfeedidrs.getLong(1);
		maxfeedidrs.close();
		maxfeedidpst.close();
		// if there is no data in the us_weather_events table then get it from
		// us_weather_feed
		if (startfeedid == 0) {
			PreparedStatement minfeedidpst = connection
					.prepareStatement("select min(id) from us_weather_feed");
			ResultSet minfeedidrs = minfeedidpst.executeQuery();
			minfeedidrs.next();
			startfeedid = minfeedidrs.getLong(1);
			minfeedidrs.close();
			minfeedidpst.close();
		} else {
			startfeedid += 1;
		}
		// start reading feed data continuously
		while (true) {
			feeddatapst.setLong(1, startfeedid);
			ResultSet feeddatarst = feeddatapst.executeQuery();
			if (feeddatarst.next()) {
				String feeddata = feeddatarst.getString(1);
				Reader feeddatareader = new StringReader(feeddata);
				Abdera abdera = new Abdera();
				Parser parser = abdera.getParser();
				Document<Feed> feeddoc = null;
				Feed feed = null;
				try {
					feeddoc = parser.parse(feeddatareader);
					feed = feeddoc.getRoot();
					feed.getEntries().size();
				} catch (Exception ex) {
					startfeedid += 1;
					continue;
				}
				ArrayList<Weather_Event> weather_event_list = new ArrayList<Weather_Event>();
				for (Entry entry : feed.getEntries()) {
					Weather_Event evnt = new Weather_Event();
					boolean canbeinserted = true;
					for (Element elem : entry.getElements()) {
						String elementname = elem.getQName().getLocalPart();
						if (elementname.equals("id")) {
							long elemid = new HashCodeBuilder(17, 37).append(
									elem.getText().split("=")[1]).toHashCode();
							// Check if this already exists in us_weather_events
							// table
							checkeventexistspst.setLong(1, elemid);
							ResultSet checkeventexistsrst = checkeventexistspst
									.executeQuery();
							if (checkeventexistsrst.next()) {
								checkeventexistsrst.close();
								canbeinserted = false;
								break;
							} else {
								evnt.setId(elemid);
								checkeventexistsrst.close();
							}
						} else if (elementname.equals("event")) {
							evnt.setEvent_type(elem.getText());
						} else if (elementname.equals("title")) {
							evnt.setTitle(elem.getText());
						} else if (elementname.equals("summary")) {
							evnt.setSummary(elem.getText());
						} else if (elementname.equals("effective")) {
							evnt.setEffective(new Timestamp(sdf.parse(
									elem.getText()).getTime()));
						} else if (elementname.equals("expires")) {
							evnt.setExpires(new Timestamp(sdf.parse(
									elem.getText()).getTime()));
						} else if (elementname.equals("urgency")) {
							evnt.setUrgency(elem.getText());
						} else if (elementname.equals("severity")) {
							evnt.setSeverity(elem.getText());
						} else if (elementname.equals("areaDesc")) {
							evnt.setArea_desc(elem.getText());
						} else if (elementname.equals("polygon")) {
							String polystring = elem.getText();
							if (polystring != null && !polystring.isEmpty()) {
								String[] coordarray = polystring.split(" ");
								Point[] points = new Point[coordarray.length];
								for (int i = 0; i < coordarray.length; i++) {
									String[] coordinates = coordarray[i]
											.split(",");
									points[i] = new Point(
											Double.parseDouble(coordinates[1]),
											Double.parseDouble(coordinates[0]));
								}
								LinearRing lring = new LinearRing(points);
								LinearRing[] linearrings = { lring };
								PGgeometry coordinategeom = new PGgeometry(
										new Polygon(linearrings));
								evnt.setPolygon(coordinategeom);
							}
						}
					}
					if (canbeinserted) {
						evnt.setFeedid(startfeedid);
						weather_event_list.add(evnt);
					}
				}
				for (Weather_Event event : weather_event_list) {
					inserteventpst.setLong(1, event.getId());
					inserteventpst.setLong(2, event.getFeedid());
					inserteventpst.setString(3, event.getEvent_type());
					inserteventpst.setString(4, event.getTitle());
					inserteventpst.setString(5, event.getSummary());
					inserteventpst.setTimestamp(6, event.getEffective());
					inserteventpst.setTimestamp(7, event.getExpires());
					inserteventpst.setString(8, event.getUrgency());
					inserteventpst.setString(9, event.getSeverity());
					inserteventpst.setString(10, event.getArea_desc());
					inserteventpst.setObject(11, event.getPolygon());
					inserteventpst.addBatch();
				}

				inserteventpst.executeBatch();
				connection.commit();
				startfeedid += 1;
			}
			feeddatarst.close();
		}
	}

}
