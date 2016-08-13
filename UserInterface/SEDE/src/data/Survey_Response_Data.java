package data;

import java.util.ArrayList;

public class Survey_Response_Data {
	private ArrayList<Survey_Response> survey_response;
    private SurveyPojo survey_pojo;
	public SurveyPojo getSurvey_pojo() {
		return survey_pojo;
	}

	public void setSurvey_pojo(SurveyPojo survey_pojo) {
		this.survey_pojo = survey_pojo;
	}

	public ArrayList<Survey_Response> getSurvey_response() {
		return survey_response;
	}

	public void setSurvey_response(ArrayList<Survey_Response> survey_response) {
		this.survey_response = survey_response;
	}
}
