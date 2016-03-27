package data;

import java.util.ArrayList;

public class SurveyDataPojo {
    private ArrayList<Tweet> tweets;
    private SurveyPojo surveydata;
	public ArrayList<Tweet> getTweets() {
		return tweets;
	}
	public void setTweets(ArrayList<Tweet> tweets) {
		this.tweets = tweets;
	}
	public SurveyPojo getSurveydata() {
		return surveydata;
	}
	public void setSurveydata(SurveyPojo surveydata) {
		this.surveydata = surveydata;
	}
}
