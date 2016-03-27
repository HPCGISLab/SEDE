package data;

import java.util.ArrayList;

public class Survey_ResponsePojo {
	private long surveyid;
	private String email;
	private ArrayList<ArrayList<Integer>> survey_response;
	private ArrayList<ResponseTweets> tweets;
	public long getSurveyid() {
		return surveyid;
	}
	public void setSurveyid(long surveyid) {
		this.surveyid = surveyid;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public ArrayList<ArrayList<Integer>> getSurvey_response() {
		return survey_response;
	}
	public void setSurvey_response(ArrayList<ArrayList<Integer>> survey_response) {
		this.survey_response = survey_response;
	}
	public ArrayList<ResponseTweets> getTweets() {
		return tweets;
	}
	public void setTweets(ArrayList<ResponseTweets> tweets) {
		this.tweets = tweets;
	}
	
}
