import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class EnvironJob {

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException, ParseException, IOException, InterruptedException,
			java.text.ParseException {
		Connection connection = null;
		Class.forName("org.postgresql.Driver");
		connection = DriverManager.getConnection(
				"jdbc:postgresql://localhost:5432/twitter_db", "sede",
				"dataexplorer");
		PreparedStatement pst = connection
				.prepareStatement("insert into us_weather_feed(feed_data,created_at) values(?,?)");
		PreparedStatement pst1 = connection
				.prepareStatement("select max(created_at) from us_weather_feed");
		while (true) {
			ResultSet rs = pst1.executeQuery();
			Abdera abdera = new Abdera();
			Parser parser = abdera.getParser();
			URL url = new URL("https://alerts.weather.gov/cap/us.php?x=0");
			String feedresult = url.toString();
			Document<Feed> doc = null;
			String feeddata = null;
			try {
				doc = parser.parse(url.openStream(), feedresult);
				feeddata = IOUtils.toString(url.openStream(),
						Charset.defaultCharset());
			} catch (Exception e) {
				Date currentdate = new Date();
				System.out.println("Exception occured at " + currentdate);
				continue;
			}

			Feed feed = doc.getRoot();
			// Wrong format date of the form 2016-07-07T21:37:00-0-3:00 to be
			// ignored
			int hyphens = StringUtils.countMatches(feed.getUpdatedElement()
					.getText(), "-");
			Date updated_date = null;
			if (hyphens == 3) {
				updated_date = feed.getUpdated();
			} else {
				continue;
			}

			Feed feed = doc.getRoot();
			// Wrong format date of the form 2016-07-07T21:37:00-0-3:00 to be
			// ignored
			int hyphens = StringUtils.countMatches(feed.getUpdatedElement()
					.getText(), "-");
			Date updated_date = null;
			if (hyphens == 3) {
				updated_date = feed.getUpdated();
			} else {
				continue;
			}
			Date updated_t = null;
			Calendar cal = Calendar.getInstance();
			cal.setTime(updated_date);
			if (!cal.getTimeZone().getID().equals("America/New_York")) {
				System.out.println("Changed timezone "
						+ cal.getTimeZone().getID());
				Calendar calendar = Calendar.getInstance(TimeZone
						.getTimeZone("America/New_York"));
				calendar.setTime(updated_date);
				updated_t = calendar.getTime();
			} else {
				updated_t = updated_date;
			}
			while (rs.next()) {
				Timestamp max_date = rs.getTimestamp(1);
				if (max_date == null
						|| updated_t.getTime() > max_date.getTime()) {
					pst.setString(1, feeddata);
					pst.setTimestamp(2, new Timestamp(updated_t.getTime()));
					pst.execute();
				}
			}
		}
	}

}
