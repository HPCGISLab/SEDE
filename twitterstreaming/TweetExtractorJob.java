import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import twitter4j.*;
import java.sql.Timestamp;
import org.postgis.PGgeometry;
import org.postgis.Point;
import java.util.*;
//Class to Process raw twitter data and distribute them to logical tables.
public class TweetExtractorJob {

	/**
 * 	 * @param args
 * 	 	 */
	public static void main(String[] args) {
	    Connection connection = null;
            int minid=0;
            try{
                 Class.forName("org.postgresql.Driver");
                 connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/twitter_db","jajayaku", "twitter");
                 connection.setAutoCommit(false);
                 ((org.postgresql.PGConnection)connection).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
                 PreparedStatement getminidrawtweet=connection.prepareStatement("select min(id) from raw_tweet");
                 PreparedStatement getmaxidtweets=connection.prepareStatement("select max(id) from tweets");
                 PreparedStatement getrawtweetdata=connection.prepareStatement("select id,body,created_at from raw_tweet where id>=? and id<? and body->>'lang'='en'");
                 PreparedStatement inserttweets=connection.prepareStatement("insert into tweets(tweetid,id,uid,body,created_at,coordinates,coordinate_class,tweet_type,rt_enabled,retweet_count,likes) values(?,?,?,?,?,?,?,?,?,?,?)");
                 PreparedStatement insertuserinteractions=connection.prepareStatement("insert into user_interactions(reference_tweetid,originating_tweet_id,originating_user_id) values (?,?,?)");
                 PreparedStatement insertusermentions=connection.prepareStatement("insert into user_mentions(tweetid,userid) values (?,?)");
                  PreparedStatement insertusers=connection.prepareStatement("insert into users(uid,url,name,screen_name,description,created_at,verified,user_location,user_location_coordinates,favourites_count,followers_count)values(?,?,?,?,?,?,?,?,?,?,?)");
                 String user_list_query="select uid from users where uid in(";
                 String tweet_list_query="select tweetid from tweets where tweetid in(";
                 ResultSet maxidtweetset=getmaxidtweets.executeQuery();
                 if(!maxidtweetset.next()){
                     ResultSet minidrawset=getminidrawtweet.executeQuery();
                     if(minidrawset.next())
                         minid=minidrawset.getInt(1);
                     minidrawset.close();
                     getminidrawtweet.close();
                 }
                 else{
                     minid=maxidtweetset.getInt(1)+1;
                 }
                 while(true){
                     getrawtweetdata.setInt(1,minid);
                     getrawtweetdata.setInt(2,minid+10000);
                     ResultSet rawtweetset=getrawtweetdata.executeQuery();
                     HashMap<Long,Users> userdetails=new HashMap<Long,Users>();
                     HashMap<Long,Tweet> tweetdetails=new HashMap<Long,Tweet>();
                     HashMap<Long,UserInteraction> userinterdetails=new HashMap<Long,UserInteraction>();
                     HashMap<Long,ArrayList<UserMention>> usermendetails=new HashMap<Long,ArrayList<UserMention>>();
                     int records=0;
                     while(rawtweetset.next()){
                         int rawtweetid=rawtweetset.getInt(1);
                         Status status=TwitterObjectFactory.createStatus(rawtweetset.getString(2));
                         Timestamp created=rawtweetset.getTimestamp(3);
                         //Retreive tweet details
                         long tweetid=status.getId();
                         if(!tweetdetails.containsKey(tweetid)){ 
                             //Retreive user details
                             if(!userdetails.containsKey(status.getUser().getId())){
                                 //Add to hashmap
                                 Users user=new Users(status.getUser().getId(),status.getUser().getURL(),status.getUser().getName(),status.getUser().getScreenName(),status.getUser().getDescription(),status.getUser().getCreatedAt(),status.getUser().isVerified(),status.getUser().getLocation(),null,status.getUser().getFavouritesCount(),status.getUser().getFollowersCount());
                                 userdetails.put(status.getUser().getId(),user);                                                                                                                     }
                             long uid=status.getUser().getId();
                             String body=status.getText();
                             body=body.replaceAll("(\\r|\\n|\\r\\n)+", " ");
                             int coordinateflag=0;
                             double coordinates[]=new double[2];
                             if(status.getGeoLocation()!=null){
                                 coordinates[0]=status.getGeoLocation().getLongitude();
                                 coordinates[1]=status.getGeoLocation().getLatitude();
                             }
                             else{
                                 coordinateflag=1;
                                 GeoLocation[][] geolocs=status.getPlace().getBoundingBoxCoordinates();
                                 coordinates[0]=(geolocs[0][0].getLongitude()+geolocs[0][1].getLongitude()+geolocs[0][2].getLongitude()+geolocs[0][3].getLongitude())/4.0;
                                 coordinates[1]=(geolocs[0][0].getLatitude()+geolocs[0][1].getLatitude()+geolocs[0][2].getLatitude()+geolocs[0][3].getLatitude())/4.0;
                             }
                             PGgeometry coordinategeom=new PGgeometry(new Point(coordinates[0],coordinates[1]));
                             boolean rt_enabled=false;
                             if(body.startsWith("RT @"))
                                 rt_enabled=true;
                             int retweet_count=status.getRetweetCount();
                             int likes=status.getFavoriteCount();
                             int tweettype=0;
                             if(status.getRetweetedStatus()!=null)
                                 tweettype=2;
                             else if(status.getInReplyToStatusId()!=-1)
                                 tweettype=1;
                             Tweet tweet=new Tweet(tweetid,rawtweetid,uid,body,created,coordinategeom,coordinateflag,tweettype,rt_enabled,retweet_count,likes);
                             tweetdetails.put(tweetid,tweet);
                             //user_interactions
                             if(tweettype!=0){
                                 long originating_tweet_id=0;
                                 long originating_user_id=0;
                                 if(tweettype==1){
                                     originating_tweet_id=status.getInReplyToStatusId();
                                     originating_user_id=status.getInReplyToUserId();
                                 }
                                 else if(tweettype==2){
                                     originating_tweet_id=status.getRetweetedStatus().getId();
                                     originating_user_id=status.getRetweetedStatus().getUser().getId();
                                 }
                                 UserInteraction userinter=new UserInteraction(tweetid,originating_tweet_id,originating_user_id);
                                 userinterdetails.put(tweetid,userinter);                    
                             }
                             //user_mentions
                             UserMentionEntity[] usermentions=status.getUserMentionEntities();
                             if(usermentions!=null){
                                 Set<Long> idset=new HashSet<Long>();
                                 ArrayList<UserMention> usermenlist=new ArrayList<UserMention>();
                                 for (UserMentionEntity usr:usermentions){
                                     if(!idset.contains(usr.getId())){
                                         idset.add(usr.getId());
                                         usermenlist.add(new UserMention(tweetid,usr.getId()));
                                     }
                                 }
                                 usermendetails.put(tweetid,usermenlist);
                             }
                         }
                         records++;
                     }
                     if(records!=0){
                         String userids="";
                         for(Long keys:userdetails.keySet()){
                             userids+=keys+",";
                         }
                         userids=userids.substring(0,userids.length()-1);
                         PreparedStatement getusers=connection.prepareStatement(user_list_query+userids+")");
                         ResultSet userset=getusers.executeQuery();
                         while(userset.next()){
                             if(userdetails.containsKey(userset.getLong(1))){
                                 userdetails.remove(userset.getLong(1));
                             }
                         }
                         userset.close();
                         getusers.close();
                         for(Long keys:userdetails.keySet()){
                             Users user=userdetails.get(keys);
                             insertusers.setLong(1,user.getUid());
                             insertusers.setString(2,user.getUrl());
                             insertusers.setString(3,user.getName());
                             insertusers.setString(4,user.getScreenName());
                             insertusers.setString(5,user.getDescription());
                             insertusers.setTimestamp(6,user.getCreatedAt());
                             insertusers.setBoolean(7,user.isVerified());
                             insertusers.setString(8,user.getLocation());
                             insertusers.setObject(9,user.getLocationCoordinates());
                             insertusers.setInt(10,user.getFavouritesCount());
                             insertusers.setInt(11,user.getFollowersCount());
                             insertusers.addBatch();
                         }
                         String tweetids="";
                         for(Long keys:tweetdetails.keySet()){
                             tweetids+=keys+",";
                         }
                         tweetids=tweetids.substring(0,tweetids.length()-1);
                         PreparedStatement gettweets=connection.prepareStatement(tweet_list_query+tweetids+")");
                         ResultSet tweetset=gettweets.executeQuery();
                         while(tweetset.next()){
                             if(tweetdetails.containsKey(tweetset.getLong(1))){
                                 tweetdetails.remove(tweetset.getLong(1)); 
                                 usermendetails.remove(tweetset.getLong(1));
                                 userinterdetails.remove(tweetset.getLong(1));
                             }
                         }
                         tweetset.close();
                         gettweets.close();
                         for(Long keys:tweetdetails.keySet()){
                             Tweet tweet=tweetdetails.get(keys);
                             UserInteraction userinter=userinterdetails.get(keys);
                             ArrayList<UserMention> usermentionlist=usermendetails.get(keys);
                             inserttweets.setLong(1,tweet.getTweetId());
                             inserttweets.setInt(2,tweet.getRawTweetId());
                             inserttweets.setLong(3,tweet.getUid());
                             inserttweets.setString(4,tweet.getBody());
                             inserttweets.setTimestamp(5,tweet.getCreated());
                             inserttweets.setObject(6,tweet.getGeometry());
                             inserttweets.setInt(7,tweet.getCoordinateFlag());
                             inserttweets.setInt(8,tweet.getTweetType());
                             inserttweets.setBoolean(9,tweet.isRtenabled());
                             inserttweets.setInt(10,tweet.getRetweetCount());
                             inserttweets.setInt(11,tweet.getLikes());
                             inserttweets.addBatch();
                             if(userinter!=null){ 
                                 insertuserinteractions.setLong(1,userinter.getTweetId());
                                 insertuserinteractions.setLong(2,userinter.getOriginatingTweetId());
                                 insertuserinteractions.setLong(3,userinter.getOriginatingUserId());
                                 insertuserinteractions.addBatch();
                             }
                             if(usermentionlist!=null){
                                 for (UserMention umen:usermentionlist){
                                     insertusermentions.setLong(1,umen.getTweetId());
                                     insertusermentions.setLong(2,umen.getUserId());
                                     insertusermentions.addBatch(); 
                                 }
                             }
                         }
                         insertusers.executeBatch();
                         inserttweets.executeBatch();
                         insertuserinteractions.executeBatch();
                         insertusermentions.executeBatch();
                         connection.commit();
                     }
                     rawtweetset.close();
                     maxidtweetset=getmaxidtweets.executeQuery();
                     maxidtweetset.next();
                     minid=maxidtweetset.getInt(1)+1;
                     maxidtweetset.close();
                     if(records==0)
                         Thread.sleep(10*60*1000);
                 }
            }
            catch (SQLException e) {
                SQLException current = e;
                do {
                    current.printStackTrace();
                   } while ((current = current.getNextException()) != null);
            }
            catch(Exception e){
                e.printStackTrace();
            }
	}
}

