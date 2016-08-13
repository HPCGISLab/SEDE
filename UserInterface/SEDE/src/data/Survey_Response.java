package data;

import java.util.ArrayList;

public class Survey_Response {
	private long responseid;
	private String tweetbody;
	private ArrayList<Integer> responses;
	public long getResponseid() {
		return responseid;
	}
	public void setResponseid(long responseid) {
		this.responseid = responseid;
	}
	public String getTweetbody() {
		return tweetbody;
	}
	
	public Survey_Response(){
		
	}
	
	public Survey_Response(long responseid, String tweetbody,
			ArrayList<Integer> responses) {
		this.responseid = responseid;
		this.tweetbody = tweetbody;
		this.responses = responses;
	}
	public void setTweetbody(String tweetbody) {
		this.tweetbody = tweetbody;
	}
	public ArrayList<Integer> getResponses() {
		return responses;
	}
	public void setResponses(ArrayList<Integer> responses) {
		this.responses = responses;
	}
}
