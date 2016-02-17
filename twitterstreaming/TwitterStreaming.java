import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import twitter4j.FilterQuery;
import twitter4j.RawStreamListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import org.postgresql.util.PGobject;
import java.sql.Timestamp;
import java.util.Date;
//Class for Listening to Twitter stream and to persist the data to database
public class TwitterStreaming {
	public static void main(String[] args) throws TwitterException {
		// Creating shared object. We want to do the database insert asynchronosly
		final BlockingQueue<String> sharedQueue = new LinkedBlockingQueue<String>();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("GcBGvHQS8UMrJgKFNxP4AS4vt");
		cb.setOAuthConsumerSecret("aZUrAmWegvjcvYpK42RKkkWQONpBoVnlREOVBCxTow8vKv2SgH");
		cb.setOAuthAccessToken("2954121790-cj3eWCRi0Zr6sQpoENREviBjBOUKn3QlypE5KUj");
		cb.setOAuthAccessTokenSecret("gNOFaW9FXIbu4vPNwxGkUNm0HbtmQKJVKkIoj5UzNVNUq");
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
				.getInstance();
		RawStreamListener listener = new RawStreamListener() {

			@Override
			public void onException(Exception ex) {
				logException(ex);
			}

			@Override
			public void onMessage(String rawJson) {
                            try{
                                //When message is recieved its added to the sync queue
				sharedQueue.put(rawJson);
                            }
                            catch(InterruptedException e){
                                logException(e);
                            }
			}
		};
		FilterQuery fq = new FilterQuery();
                //All the geo tweets
		double[][] bb = { { -180, -90.0 }, { 180.0, 90.0 } };
		fq.locations(bb);
		twitterStream.addListener(listener);
		twitterStream.filter(fq);
                //Takes data from queue and persist to database
		Thread consumerThread = new Thread(new Runnable() {

			@Override
			public void run() {
                                Connection connection = null;
                                PreparedStatement pst=null;
				try {
                                        Class.forName("org.postgresql.Driver");
                                        connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/twitter_db","jajayaku", "twitter");
                                        pst = connection.prepareStatement("insert into raw_tweet(body,created_at) values(?,?)");
					while (true) {
						String data = sharedQueue.poll();
						if (data != null && !data.startsWith("{\"limit")) {
                                                        data=data.replace("\\u0000", "");
							PGobject dataObject = new PGobject();
				                        dataObject.setType("jsonb");
				                        dataObject.setValue(data);
							pst.setObject(1, dataObject);
                                                        pst.setTimestamp(2,new Timestamp(new Date().getTime()));
							pst.executeUpdate();
						}
					}
				} catch (ClassNotFoundException | SQLException e) {
                                        e.printStackTrace();
					logException(e);
                                        try{
                                            pst.close();
                                            connection.close(); 
                                        }catch(Exception ex){
                                            logException(ex);
                                        }
				}

			}
		});
		consumerThread.start();
		try {
			consumerThread.join();
		} catch (InterruptedException ex) {
                        ex.printStackTrace();
			logException(ex);
		}
	}

	public static void logException(Exception ex) {
		String message = ex.getMessage();
		try {
			FileWriter fw = new FileWriter(
					"/home/jajayaku/twitterjavastreamer/exceptions/exception.txt",
					true);
			fw.write(message);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
