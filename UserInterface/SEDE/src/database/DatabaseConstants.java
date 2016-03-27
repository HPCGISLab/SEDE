package database;

public class DatabaseConstants {
	public static final String URL = "jdbc:postgresql://localhost:5432/eb_db";
	public static final String USER = "jajayaku";
	public static final String PASSWORD = "twitter";
	public static final String GET_TWEETDATA="select id,tweet,st_x(coordinates::geometry),st_y(coordinates::geometry) from tweet_and_time where posted_time>=? and posted_time<? and coordinates && st_makeenvelope(?,?,?,?)";
	public static final String GET_TWEETDATA_WITHWORDFILTER="select id from tweet_and_time where id in";
	public static final String ADD_SURVEY_DATA="INSERT INTO survey(created_at, created_email,dataquery,survey_hash, categ_1, categ_2, categ_3, categ_4, categ_5, desc_1, desc_2, desc_3, desc_4, desc_5) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	public static final String GET_SURVEY_DATA="select survey_id,dataquery,categ_1, categ_2, categ_3, categ_4, categ_5, desc_1, desc_2, desc_3, desc_4, desc_5 from survey where survey_hash=?;";
	public static final String GET_SLIDER_DATA="select min(created_at)::date,max(created_at)::date+1,max(created_at)::date-min(created_at)::date +1 from tweets;";
	
}
