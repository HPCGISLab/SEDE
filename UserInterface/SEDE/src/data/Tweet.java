package data;

import java.sql.Timestamp;

public class Tweet {
    private long tweetid;
    private long id;
    private String body;
    private Timestamp created_at;
    private double coordinates[];
    private int coordinate_class;
    private int tweet_type;
    private boolean rt_enabled;
    private int retweet_count;
    private int likes;
	public Long getTweetid() {
		return tweetid;
	}
	public void setTweetid(long tweetid) {
		this.tweetid = tweetid;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Timestamp getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}
	public double[] getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(double[] coordinates) {
		this.coordinates = coordinates;
	}
	public int getCoordinate_class() {
		return coordinate_class;
	}
	public void setCoordinate_class(int coordinate_class) {
		this.coordinate_class = coordinate_class;
	}
	public int getTweet_type() {
		return tweet_type;
	}
	public void setTweet_type(int tweet_type) {
		this.tweet_type = tweet_type;
	}
	public boolean isRt_enabled() {
		return rt_enabled;
	}
	public void setRt_enabled(boolean rt_enabled) {
		this.rt_enabled = rt_enabled;
	}
	public int getRetweet_count() {
		return retweet_count;
	}
	public void setRetweet_count(int retweet_count) {
		this.retweet_count = retweet_count;
	}
	public int getLikes() {
		return likes;
	}
	public void setLikes(int likes) {
		this.likes = likes;
	}
    
}
