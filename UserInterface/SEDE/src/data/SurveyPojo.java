package data;

import java.sql.Timestamp;
import java.util.ArrayList;

public class SurveyPojo {
	private long surveyid;
    public long getSurveyid() {
		return surveyid;
	}
	public void setSurveyid(long surveyid) {
		this.surveyid = surveyid;
	}
	private String email;
    private String query;
    private ArrayList<ArrayList<String>> questions;
    private int hashcode;
    private Timestamp created_at;
	public Timestamp getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}
	public int getHashcode() {
		return hashcode;
	}
	public void setHashcode(int hashcode) {
		this.hashcode = hashcode;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public ArrayList<ArrayList<String>> getQuestions() {
		return questions;
	}
	public void setQuestions(ArrayList<ArrayList<String>> questions) {
		this.questions = questions;
	}
    
}
