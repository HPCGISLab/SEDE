package database;

public class DatabaseConstants {
	public static final String URL = "jdbc:postgresql://localhost:5432/twitter_db";
	public static final String USER = "sede";
	public static final String PASSWORD = "dataexplorer";
	public static final String ADD_SURVEY_DATA="INSERT INTO survey(created_at, created_email,datainfo,survey_hash, categ_1, categ_2, categ_3, categ_4, categ_5, desc_1, desc_2, desc_3, desc_4, desc_5,survey_name) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);";
	public static final String GET_SURVEY_DATA="select survey_id,datainfo->>'query',datainfo->>'dictused',datainfo->>'dictcategid',categ_1, categ_2, categ_3, categ_4, categ_5, desc_1, desc_2, desc_3, desc_4, desc_5 from survey where survey_hash=?;";
	public static final String GET_SURVEY_RESPONSE_DATA="select response_id,body,resp_1,resp_2,resp_3,resp_4,resp_5 from survey_response s,tweets t where email=? and survey_id in (select survey_id from survey where survey_hash=?) and t.tweetid=s.tweetid order by response_id";
	public static final String CHECK_SURVEY_RESPONSE_EXIST="select * from survey_response where email=? and survey_id in(select survey_id from survey where survey_hash=?)";
	public static final String GET_SLIDER_DATA="select min(created_at)::date,max(created_at)::date+1,max(created_at)::date-min(created_at)::date +1 from tweets;";
	public static final String GET_STATE_POLYGON="select st_asewkt(ST_SetSRID(geom,4326)) as state_geom,st_extent(geom) from states where state_name=? group by state_geom;";
	public static final String GET_WEATHER_PLACE_DATA="select st_asewkt(ST_SetSRID(area::geometry,4326)) as weathergeom,st_extent(area::geometry) from us_weather_events where id=? group by weathergeom";
	public static final String GET_SEDE_USER_EMAIL="select email from sede_users where user_name=? and password=?";
	public static final String GET_DICT_CATEGORIES="select categ_id,category from dict_categories order by category;";
	public static final String GET_COMMON_WORDS = "select word from stopwords";
}
