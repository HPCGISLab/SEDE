package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.postgresql.util.PGobject;

import utils.TextProcessor;
import data.SliderPojo;
import data.SurveyDataPojo;
import data.SurveyPojo;
import data.Survey_Response;
import data.Survey_ResponsePojo;
import data.Survey_Response_Data;
import data.Tweet;

public class Database {
	private static Connection getConnection() throws ClassNotFoundException,
			SQLException {
		Connection connection = null;
		Class.forName("org.postgresql.Driver");
		connection = DriverManager.getConnection(DatabaseConstants.URL,
				DatabaseConstants.USER, DatabaseConstants.PASSWORD);
		return connection;
	}
	
	public static String[] getplacepolygon(String geogdata, double bufferval) {
		String[] polygonwithbounds=new String[2];
		try{
			Connection conn = getConnection();
			PreparedStatement polypst=conn.prepareStatement("select st_asewkt(st_buffer(ST_GeogFromText("+"'"+geogdata+"'"+"),"+bufferval+")),st_extent(st_buffer(ST_GeogFromText("+"'"+geogdata+"'"+"),"+bufferval+")::geometry)");
			ResultSet polyrst=polypst.executeQuery();
			polyrst.next();
			polygonwithbounds[0]=polyrst.getString(1);
			polygonwithbounds[1]=polyrst.getString(2);
			polyrst.close();
			polypst.close();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return polygonwithbounds;
	}
	public static String[] getstatepolygon(String state){
		String[] polygonwithbounds=new String[2];
		try{
			Connection conn = getConnection();
			PreparedStatement polypst=conn.prepareStatement(DatabaseConstants.GET_STATE_POLYGON);
			polypst.setString(1, state);
			ResultSet polyrst=polypst.executeQuery();
			polyrst.next();
			polygonwithbounds[0]=polyrst.getString(1);
			polygonwithbounds[1]=polyrst.getString(2);
			polyrst.close();
			polypst.close();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return polygonwithbounds;
	}
	public static String getboundingboxaspolygon(String bounds){
		String polygon=null;
		try{
			Connection conn = getConnection();
			PreparedStatement polypst=conn.prepareStatement("select st_asewkt(st_setsrid(st_makeenvelope("+bounds+"),4326))");
			ResultSet polyrst=polypst.executeQuery();
			polyrst.next();
			polygon=polyrst.getString(1);
			polyrst.close();
			polypst.close();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return polygon;
	}
	
	public static boolean checkSurveyResponseExist(String emailid, long surveyhash) {
		boolean ifexist = false;
		try {
			Connection conn = getConnection();
			PreparedStatement checksurvey = conn
					.prepareStatement(DatabaseConstants.CHECK_SURVEY_RESPONSE_EXIST);
			checksurvey.setString(1, emailid);
			checksurvey.setLong(2, surveyhash);
			ResultSet rt = checksurvey.executeQuery();
			if (rt.next()) {
				ifexist = true;
			}
			rt.close();
			checksurvey.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ifexist;
	}

	public static void updateSurvey(ArrayList<Survey_Response> responses) {
		try {
			Connection conn = getConnection();
			int categcount = responses.get(0).getResponses().size();
			String query = "update survey_response set ";
			for (int i = 1; i <= categcount; i++) {
				query += "resp_" + i + "=?,";
			}
			query = query.substring(0, query.length() - 1)
					+ " where response_id=?";
			PreparedStatement addsurveyresponse = conn.prepareStatement(query);
			for (Survey_Response sr : responses) {
				for (int i = 1; i <= sr.getResponses().size(); i++) {
					addsurveyresponse.setInt(i, sr.getResponses().get(i-1));
				}
				addsurveyresponse.setLong(categcount + 1, sr.getResponseid());
				addsurveyresponse.addBatch();
			}
			addsurveyresponse.executeBatch();
			addsurveyresponse.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createSurveyResponseForUser(String emailid, long surveyhash) {
		try {
			Connection conn = getConnection();
			PreparedStatement getsurveydata = conn
					.prepareStatement(DatabaseConstants.GET_SURVEY_DATA);
			getsurveydata.setLong(1, surveyhash);
			ResultSet surveyset = getsurveydata.executeQuery();
			surveyset.next();
			int categ = 0;
			for (int i = 5; i < 10; i++) {
				if (surveyset.getString(i) != null
						&& !surveyset.getString(i).isEmpty()) {
					categ++;
				}
			}
			String query = "insert into survey_response(survey_id,email,tweetid";
			for (int i = 0; i < categ; i++) {
				query += ",resp_" + (i + 1);
			}
			query += ")values(?,?,?,";
			for (int i = 0; i < categ; i++) {
				query += "?,";
			}
			query = query.substring(0, query.length() - 1) + ")";
			List<Tweet> tweetlist=getmapdata(surveyset.getString(2));
			if(surveyset.getBoolean(3)){
				TextProcessor.filterdicttweets(tweetlist,surveyset.getInt(4));
			}
			PreparedStatement insertsurveyresponse = conn
					.prepareStatement(query);
			for (Tweet tweet:tweetlist) {
				insertsurveyresponse.setLong(1, surveyset.getLong(1));
				insertsurveyresponse.setString(2, emailid);
				insertsurveyresponse.setLong(3, tweet.getTweetid());
				for (int i = 0; i < categ; i++) {
					insertsurveyresponse.setInt(i + 4, 0);
				}
				insertsurveyresponse.addBatch();
			}
			insertsurveyresponse.executeBatch();
			surveyset.close();
			getsurveydata.close();
			insertsurveyresponse.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void submitsurvey(SurveyPojo spoj) {
		try {
			Connection conn = getConnection();
			PreparedStatement insertsurvey = conn
					.prepareStatement(DatabaseConstants.ADD_SURVEY_DATA);
			insertsurvey.setTimestamp(1, spoj.getCreated_at());
			insertsurvey.setString(2, spoj.getEmail());
			PGobject dictinfo=new PGobject();
			JSONObject jsonobj=new JSONObject();
			jsonobj.put("query", spoj.getQuery());
			jsonobj.put("dictused", spoj.isDictused());
			jsonobj.put("dictcategid", spoj.getDictcategid());
			dictinfo.setType("jsonb");
			dictinfo.setValue(jsonobj.toJSONString());
			insertsurvey.setObject(3, dictinfo);
			insertsurvey.setLong(4, spoj.getHashcode());
			for (int i = 5; i < 10; i++) {
				insertsurvey
						.setString(i, spoj.getQuestions().get(i - 5).get(0));
				insertsurvey.setString(i + 5, spoj.getQuestions().get(i - 5)
						.get(1));
			}
			insertsurvey.setString(15,spoj.getCodebookname());
			insertsurvey.executeUpdate();
			insertsurvey.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Survey_Response_Data getSurveyResponseData(String emailid,
			long surveyhash) {
		Survey_Response_Data srdata = new Survey_Response_Data();
		try {
			Connection conn = getConnection();
			PreparedStatement getsurvey = conn
					.prepareStatement(DatabaseConstants.GET_SURVEY_RESPONSE_DATA);
			getsurvey.setString(1, emailid);
			getsurvey.setLong(2, surveyhash);
			ResultSet rst = getsurvey.executeQuery();
			SurveyPojo spj = new SurveyPojo();
			PreparedStatement getsurveydata = conn
					.prepareStatement(DatabaseConstants.GET_SURVEY_DATA);
			getsurveydata.setLong(1, surveyhash);
			ResultSet surveyset = getsurveydata.executeQuery();
			surveyset.next();
			ArrayList<ArrayList<String>> surveyquestions = new ArrayList<ArrayList<String>>();
			for (int i = 5; i < 10; i++) {
				if (surveyset.getString(i) != null
						&& !surveyset.getString(i).isEmpty()) {
					ArrayList<String> quest = new ArrayList<String>();
					quest.add(surveyset.getString(i));
					quest.add(surveyset.getString(i + 5));
					surveyquestions.add(quest);
				}
			}
			spj.setQuestions(surveyquestions);
			srdata.setSurvey_pojo(spj);
			ArrayList<Survey_Response> rsp = new ArrayList<Survey_Response>();
			while (rst.next()) {
				ArrayList<Integer> resp = new ArrayList<Integer>();
				for (int i = 1; i <= 5; i++) {
					if (rst.getString("resp_" + i) != null
							&& !rst.getString("resp_" + i).isEmpty()) {
						resp.add(rst.getInt("resp_" + i));
					}
				}
				Survey_Response srp = new Survey_Response(rst.getLong(1),
						rst.getString(2), resp);
				rsp.add(srp);
			}
			srdata.setSurvey_response(rsp);
			rst.close();
			surveyset.close();
			getsurvey.close();
			getsurveydata.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return srdata;
	}

	public static void savesurveyresponse(Survey_ResponsePojo spoj) {
		try {
			Connection conn = getConnection();
			String query = "insert into survey_response(survey_id,email,tweetid";
			for (int i = 0; i < spoj.getSurvey_response().get(0).size(); i++) {
				query += ",resp_" + (i + 1);
			}
			query += ")values(?,?,?,";
			for (int i = 0; i < spoj.getSurvey_response().get(0).size(); i++) {
				query += "?,";
			}
			query = query.substring(0, query.length() - 1) + ")";
			PreparedStatement insertsurveyresponse = conn
					.prepareStatement(query);
			for (int i = 0; i < spoj.getTweets().size(); i++) {
				insertsurveyresponse.setLong(1, spoj.getSurveyid());
				insertsurveyresponse.setString(2, spoj.getEmail());
				insertsurveyresponse
						.setLong(3, spoj.getTweets().get(i).getId());
				for (int j = 0; j < spoj.getSurvey_response().get(i).size(); j++) {
					insertsurveyresponse.setInt(j + 4, spoj
							.getSurvey_response().get(i).get(j));
				}
				insertsurveyresponse.addBatch();
			}
			insertsurveyresponse.executeBatch();
			insertsurveyresponse.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SurveyDataPojo getsurvey(long surveyhash) {
		SurveyDataPojo sdatapojo = new SurveyDataPojo();
		try {
			Connection conn = getConnection();
			SurveyPojo spoj = new SurveyPojo();
			ArrayList<Tweet> tweets = new ArrayList<Tweet>();
			PreparedStatement getsurveydata = conn
					.prepareStatement(DatabaseConstants.GET_SURVEY_DATA);
			getsurveydata.setLong(1, surveyhash);
			ResultSet surveyset = getsurveydata.executeQuery();
			surveyset.next();
			spoj.setSurveyid(surveyset.getLong(1));
			spoj.setQuery(surveyset.getString(2));
			ArrayList<ArrayList<String>> questions = new ArrayList<ArrayList<String>>();
			for (int i = 3; i < 8; i++) {
				if (surveyset.getString(i) != null
						&& !surveyset.getString(i).isEmpty()) {
					ArrayList<String> question = new ArrayList<String>();
					question.add(surveyset.getString(i));
					question.add(surveyset.getString(i + 5));
					questions.add(question);
				}
			}
			spoj.setQuestions(questions);
			sdatapojo.setSurveydata(spoj);
			System.out.println(spoj.getQuery());
			PreparedStatement gettweetquerydata = conn.prepareStatement(spoj
					.getQuery());
			ResultSet tweetset = gettweetquerydata.executeQuery();
			while (tweetset.next()) {
				Tweet tweet = new Tweet();
				tweet.setTweetid(tweetset.getLong("tweetid"));
				tweet.setBody(tweetset.getString("body"));
				tweets.add(tweet);
			}
			sdatapojo.setTweets(tweets);
			tweetset.close();
			gettweetquerydata.close();
			surveyset.close();
			getsurveydata.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sdatapojo;
	}

	public static SliderPojo getsliderdates() {
		SliderPojo intervaldata = new SliderPojo();
		try {
			Connection conn = getConnection();
			PreparedStatement getsliderintervals = conn
					.prepareStatement(DatabaseConstants.GET_SLIDER_DATA);
			ResultSet sliderset = getsliderintervals.executeQuery();
			sliderset.next();
			intervaldata.setStartdate(sliderset.getDate(1));
			intervaldata.setEnddate(sliderset.getDate(2));
			intervaldata.setInterval(sliderset.getInt(3));
			sliderset.close();
			getsliderintervals.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return intervaldata;
	}

	public static List<Tweet> getmapdata(String sqlquery) {
		List<Tweet> tweetlist = new LinkedList<Tweet>();
		try {
			Connection conn = getConnection();
			PreparedStatement pst = conn.prepareStatement(sqlquery);
			ResultSet rst = pst.executeQuery();
			while (rst.next()) {
				Tweet tweet = new Tweet();
				tweet.setTweetid(rst.getLong(1));
				tweet.setBody(rst.getString(2));
				double[] coord = { rst.getDouble(3), rst.getDouble(4) };
				tweet.setCoordinates(coord);
				tweet.setCreated_at(rst.getTimestamp(5));
				tweetlist.add(tweet);
			}
			rst.close();
			pst.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tweetlist;
	}

	public static HashMap<Long, String> getweatherData(String from, String to,
			String event_type, String event_severity) {
		HashMap<Long, String> weathermap=new HashMap<Long, String>();
		try {
			Connection conn = getConnection();
			PreparedStatement pst=null;
			if(event_severity.equalsIgnoreCase("All")){
				pst = conn.prepareStatement("select id,area_desc from us_weather_events where effective<'"+to+"' and expires>='"+from+"' and event_type='"+event_type+"' and area is not null");
			}
			else{
				pst = conn.prepareStatement("select id,area_desc from us_weather_events where effective<'"+to+"' and expires>='"+from+"' and event_type='"+event_type+"' and severity='"+event_severity+"' and area is not null");
			}
			ResultSet rst = pst.executeQuery();
			while (rst.next()) {
				weathermap.put(rst.getLong(1), rst.getString(2));
			}
			rst.close();
			pst.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return weathermap;
	}

	public static String[] getWeatherPlaceDetails(long eventid) {
		String[] placedetails=new String[2];
		try{
			Connection conn = getConnection();
			PreparedStatement pst = conn.prepareStatement(DatabaseConstants.GET_WEATHER_PLACE_DATA);
			pst.setLong(1, eventid);
			ResultSet rst = pst.executeQuery();
			if(rst.next()){
				placedetails[0]=rst.getString(1);
				placedetails[1]=rst.getString(2);
			}
			rst.close();
			pst.close();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return placedetails;
	}

	public static LinkedHashMap<Integer, String> getDictionaryCategories() {
		LinkedHashMap<Integer, String> dictdata=new LinkedHashMap<Integer, String>();
		try{
			Connection conn = getConnection();
			PreparedStatement pst = conn.prepareStatement(DatabaseConstants.GET_DICT_CATEGORIES);
			ResultSet rst = pst.executeQuery();
			while(rst.next()){
				dictdata.put(rst.getInt(1), rst.getString(2));
			}
			rst.close();
			pst.close();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return dictdata;
	}

	public static String getpattern(int categid) {
		String pattern = null;
		try{
			Connection conn = getConnection();
			PreparedStatement patternpst=conn.prepareStatement("select categ_pattern from dict_words where categ_id="+categid);
			ResultSet patternrst=patternpst.executeQuery();
			patternrst.next();
			pattern=patternrst.getString(1);
			patternrst.close();
			patternpst.close();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return pattern;
	}

	public static String transform(String polygonstring, int tosrid) {
		String polystring=null;
		try{
			Connection conn = getConnection();
			PreparedStatement polypst=conn.prepareStatement("select st_asewkt(st_transform(ST_GeomFromewkt('"+polygonstring+"'),"+tosrid+"))");
			ResultSet polyrst=polypst.executeQuery();
			if(polyrst.next()){
				polystring=polyrst.getString(1);
			}
			polypst.close();
			polyrst.close();
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return polystring;
	}

	public static String authenticate(String username, String password) {
		String email=null;
		try{
			Connection conn = getConnection();
			PreparedStatement emailpst=conn.prepareStatement(DatabaseConstants.GET_SEDE_USER_EMAIL);
			emailpst.setString(1,username);
			emailpst.setString(2,password);
			ResultSet emailrst=emailpst.executeQuery();
			if(emailrst.next()){
				email=emailrst.getString(1);
			}
			emailpst.close();
			emailrst.close();
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return email;
	}

	public static Set<String> getCommonWords() {
		Set<String> commonwords=new HashSet<String>();
		try{
			Connection conn = getConnection();
			PreparedStatement commonwordpst=conn.prepareStatement(DatabaseConstants.GET_COMMON_WORDS);
			ResultSet commonwordrst=commonwordpst.executeQuery();
			while(commonwordrst.next()){
				commonwords.add(commonwordrst.getString(1));
			}
			commonwordpst.close();
			commonwordrst.close();
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return commonwords;
	}
	
}
