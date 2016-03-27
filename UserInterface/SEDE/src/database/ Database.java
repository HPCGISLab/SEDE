package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import data.MapDataRequestPojo;
import data.SliderPojo;
import data.SpatialDataPojo;
import data.SurveyDataPojo;
import data.SurveyPojo;
import data.Survey_ResponsePojo;
import data.TemporalDataPojo;
import data.TextualDataPojo;
import data.Tweet;

public class Database {
	private static Connection getConnection() throws ClassNotFoundException, SQLException{
		Connection connection = null;
		Class.forName("org.postgresql.Driver");
		connection = DriverManager.getConnection(DatabaseConstants.URL,DatabaseConstants.USER, DatabaseConstants.PASSWORD);
		return connection;
	}
	
	public static ArrayList<Integer> getsearchwordids(String searchword,
			String ids) {
		ArrayList<Integer> arr=new ArrayList<Integer>();
		try{
			Connection conn=getConnection();
			PreparedStatement tweetdatwordpst=conn.prepareStatement(DatabaseConstants.GET_TWEETDATA_WITHWORDFILTER+"("+ids+") and lower(tweet) like '%"+searchword.toLowerCase()+"%'");
	        ResultSet tweetwordset=tweetdatwordpst.executeQuery();
			while(tweetwordset.next()){
				int id=tweetwordset.getInt(1);
				arr.add(id);
			}
			tweetwordset.close();
			tweetdatwordpst.close();
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return arr;
	}

	public static void submitsurvey(SurveyPojo spoj) {
		try{
			Connection conn=getConnection();
			PreparedStatement insertsurvey=conn.prepareStatement(DatabaseConstants.ADD_SURVEY_DATA);
			insertsurvey.setTimestamp(1, spoj.getCreated_at());
			insertsurvey.setString(2, spoj.getEmail());
			insertsurvey.setString(3, spoj.getQuery());
			insertsurvey.setLong(4,spoj.getHashcode());
			for(int i=5;i<10;i++){
				insertsurvey.setString(i,spoj.getQuestions().get(i-5).get(0));
				insertsurvey.setString(i+5,spoj.getQuestions().get(i-5).get(1));
			}
			insertsurvey.executeUpdate();
			insertsurvey.close();
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public static void savesurveyresponse(Survey_ResponsePojo spoj) {
		try{
			Connection conn=getConnection();
			String query="insert into survey_response(survey_id,email,tweetid";
		    for(int i=0;i<spoj.getSurvey_response().get(0).size();i++){
		    	query+=",resp_"+(i+1);
		    }
		    query+=")values(?,?,?,";
		    for(int i=0;i<spoj.getSurvey_response().get(0).size();i++){
		    	query+="?,";
		    }
		    query=query.substring(0, query.length() - 1) + ")";
		    PreparedStatement insertsurveyresponse=conn.prepareStatement(query);
		    for(int i=0;i<spoj.getTweets().size();i++){
		    	insertsurveyresponse.setLong(1, spoj.getSurveyid());
		    	insertsurveyresponse.setString(2, spoj.getEmail());
		    	insertsurveyresponse.setLong(3,spoj.getTweets().get(i).getId());
		    	for(int j=0;j<spoj.getSurvey_response().get(i).size();j++){
		    		insertsurveyresponse.setInt(j+4,spoj.getSurvey_response().get(i).get(j));
		    	}
		    	insertsurveyresponse.addBatch();
		    }
		    insertsurveyresponse.executeBatch();
		    insertsurveyresponse.close();
		    conn.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static SurveyDataPojo getsurvey(long surveyhash) {
		SurveyDataPojo sdatapojo=new SurveyDataPojo();
		try{
			Connection conn=getConnection();
			SurveyPojo spoj=new SurveyPojo();
			ArrayList<Tweet> tweets=new ArrayList<Tweet>();
			PreparedStatement getsurveydata=conn.prepareStatement(DatabaseConstants.GET_SURVEY_DATA);
			getsurveydata.setLong(1,surveyhash);
			ResultSet surveyset=getsurveydata.executeQuery();
			surveyset.next();
			spoj.setSurveyid(surveyset.getLong(1));
			spoj.setQuery(surveyset.getString(2));
			ArrayList<ArrayList<String>> questions=new ArrayList<ArrayList<String>>();
			for(int i=3;i<8;i++){
				if(surveyset.getString(i)!=null&&!surveyset.getString(i).isEmpty()){
					ArrayList<String> question=new ArrayList<String>();
					question.add(surveyset.getString(i));
					question.add(surveyset.getString(i+5));
					questions.add(question);
				}
			}
			spoj.setQuestions(questions);
			sdatapojo.setSurveydata(spoj);
			System.out.println(spoj.getQuery());
			PreparedStatement gettweetquerydata=conn.prepareStatement(spoj.getQuery());
			ResultSet tweetset=gettweetquerydata.executeQuery();
			while(tweetset.next()){
				Tweet tweet=new Tweet();
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
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return sdatapojo;
	}

	public static SliderPojo getsliderdates() {
		SliderPojo intervaldata=new SliderPojo();
		try{
			Connection conn=getConnection();
			PreparedStatement getsliderintervals=conn.prepareStatement(DatabaseConstants.GET_SLIDER_DATA);
			ResultSet sliderset=getsliderintervals.executeQuery();
			sliderset.next();
			intervaldata.setStartdate(sliderset.getDate(1));
			intervaldata.setEnddate(sliderset.getDate(2));
			intervaldata.setInterval(sliderset.getInt(3));
			sliderset.close();
			getsliderintervals.close();
			conn.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return intervaldata;
	}

	public static ArrayList<Tweet> getmapdata(String sqlquery) {
		ArrayList<Tweet> tweetlist=new ArrayList<Tweet>();
		try{
			Connection conn=getConnection();
			PreparedStatement pst=conn.prepareStatement(sqlquery);
			ResultSet rst=pst.executeQuery();
			while(rst.next()){
				Tweet tweet=new Tweet();
				tweet.setTweetid(rst.getLong(1));
				tweet.setBody(rst.getString(2));
				double []coord={rst.getDouble(3),rst.getDouble(4)};
				tweet.setCoordinates(coord);
				tweet.setCreated_at(rst.getTimestamp(5));
				tweetlist.add(tweet);
			}
			rst.close();
			pst.close();
			conn.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return tweetlist;
	}
}
