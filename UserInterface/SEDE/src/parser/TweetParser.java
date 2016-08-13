/*
Copyright (c) 2014 High-Performance Computing and GIS (HPCGIS) Laboratory. All rights reserved.
Use of this source code is governed by a BSD-style license that can be found in the LICENSE file.
Authors and contributors: Jayakrishnan Ajayakumar (jajayaku@kent.edu);Eric Shook (eshook@kent.edu)
 */
package parser;

//Parser to parse tweets
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.TextProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.DictDataPojo;
import data.MapDataRequestPojo;
import data.SliderPojo;
import data.SpatialDataPojo;
import data.SurveyDataPojo;
import data.SurveyPojo;
import data.Survey_Response;
import data.Survey_ResponsePojo;
import data.Survey_Response_Data;
import data.TemporalDataPojo;
import data.TextualDataPojo;
import data.Tweet;
import data.Word;
import database.Database;

public class TweetParser {

	public static String submitsurvey(HttpServletRequest request) {
		String datajson = request.getParameter("datajson");
		String resp = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			SurveyPojo spoj = mapper.readValue(datajson, SurveyPojo.class);
			Date currdate = new Date();
			int hcode = new HashCodeBuilder(17, 37).append(spoj.getEmail())
					.append(spoj.getQuery()).append(spoj.getCodebookname())
					.append(currdate.getTime()).toHashCode();
			spoj.setHashcode(hcode);
			spoj.setCreated_at(new Timestamp(currdate.getTime()));
			Database.submitsurvey(spoj);
			resp = String.valueOf(hcode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public static String savesurveyresponse(HttpServletRequest request) {
		String datajson = request.getParameter("datajson");
		try {
			ObjectMapper mapper = new ObjectMapper();
			Survey_ResponsePojo spoj = mapper.readValue(datajson,
					Survey_ResponsePojo.class);
			Database.savesurveyresponse(spoj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Response Saved Successfully";
	}

	public static String getsurvey(HttpServletRequest request) {
		long surveyhash = Long.parseLong(request.getParameter("surveyhash"));
		JSONObject response = new JSONObject();
		try {
			SurveyDataPojo spoj = Database.getsurvey(surveyhash);
			JSONObject surveyobj = new JSONObject();
			JSONObject tweetobj = new JSONObject();
			surveyobj.put("surveyid", spoj.getSurveydata().getSurveyid());
			JSONArray categarray = new JSONArray();
			for (ArrayList<String> categ : spoj.getSurveydata().getQuestions()) {
				JSONObject catgedat = new JSONObject();
				catgedat.put("categ", categ.get(0));
				catgedat.put("desc", categ.get(1));
				categarray.add(catgedat);
			}
			surveyobj.put("categ", categarray);
			JSONArray tweetarray = new JSONArray();
			for (Tweet tweet : spoj.getTweets()) {
				JSONObject tweetdat = new JSONObject();
				tweetdat.put("id", String.valueOf(tweet.getTweetid()));
				tweetdat.put("body", tweet.getBody());
				tweetarray.add(tweetdat);
			}
			tweetobj.put("tweets", tweetarray);
			response.put("surveydata", surveyobj);
			response.put("tweets", tweetarray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toJSONString();
	}

	public static String getsliderdates(HttpServletRequest request) {
		SliderPojo dateintervals = Database.getsliderdates();
		JSONObject sliderobj = new JSONObject();
		sliderobj.put("startdate", dateintervals.getStartdate().toString());
		sliderobj.put("enddate", dateintervals.getEnddate().toString());
		sliderobj.put("days", dateintervals.getInterval());
		return sliderobj.toJSONString();
	}

	public static String getsurveyresponse(HttpServletRequest request) {
		Long surveyhash = Long.parseLong(request.getParameter("surveyhash"));
		String email = request.getParameter("email");
		if (!Database.checkSurveyResponseExist(email, surveyhash)) {
			Database.createSurveyResponseForUser(email, surveyhash);
		}
		Survey_Response_Data srd = Database.getSurveyResponseData(email,
				surveyhash);
		JSONObject surveyjson = new JSONObject();
		JSONArray surveydetails = new JSONArray();
		JSONArray surveyresponse = new JSONArray();
		for (ArrayList<String> questions : srd.getSurvey_pojo().getQuestions()) {
			surveydetails.add(questions);
		}
		surveyjson.put("surveydetails", surveydetails);
		for (Survey_Response srp : srd.getSurvey_response()) {
			JSONObject responsedat = new JSONObject();
			responsedat.put("responseid", srp.getResponseid());
			responsedat.put("body", srp.getTweetbody());
			JSONArray responses = new JSONArray();
			for (Integer resp : srp.getResponses()) {
				responses.add(resp);
			}
			responsedat.put("responses", responses);
			surveyresponse.add(responsedat);
		}
		surveyjson.put("surveyresponse", surveyresponse);
		return surveyjson.toJSONString();
	}

	public static String updatesurveyresponse(HttpServletRequest request) {
		String datajson = request.getParameter("datajson");
		String resp = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			Survey_Response_Data srd = mapper.readValue(datajson,
					Survey_Response_Data.class);
			Database.updateSurvey(srd.getSurvey_response());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Response Saved Successfully";
	}

	public static String getweatherevents(HttpServletRequest request) {
		String from = request.getParameter("from");
		String to = request.getParameter("to");
		String event_type = request.getParameter("event_type");
		String event_severity = request.getParameter("event_severity");
		HashMap<Long, String> weatherdata = Database.getweatherData(from, to,
				event_type, event_severity);
		JSONArray weatherdatalist = new JSONArray();
		for (Long id : weatherdata.keySet()) {
			JSONObject weatherobj = new JSONObject();
			weatherobj.put("id", id);
			weatherobj.put("area_desc", weatherdata.get(id));
			weatherdatalist.add(weatherobj);
		}
		return weatherdatalist.toJSONString();
	}

	public static String getdictionarycategories(HttpServletRequest request) {
		LinkedHashMap<Integer, String> dictdata = Database
				.getDictionaryCategories();
		JSONArray dictionarylist = new JSONArray();
		for (Integer id : dictdata.keySet()) {
			JSONObject dictobj = new JSONObject();
			dictobj.put("id", id);
			dictobj.put("category", dictdata.get(id));
			dictionarylist.add(dictobj);
		}
		return dictionarylist.toJSONString();
	}

	public static String getmapdatafordisplay(HttpServletRequest request) {
		String jsondata = request.getParameter("jsondata");
		String jsonresponse = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			MapDataRequestPojo mpoj = mapper.readValue(jsondata,
					MapDataRequestPojo.class);
			SpatialDataPojo spoj = mpoj.getSpatialdata();
			TemporalDataPojo tpoj = mpoj.getTemporaldata();
			TextualDataPojo txpoj = mpoj.getTextualdata();
			DictDataPojo dpoj = mpoj.getDictdata();
			String sqlquery = "select tweetid,body,st_x(coordinates::geometry),st_y(coordinates::geometry),created_at from tweets where created_at>="
					+ "'"
					+ tpoj.getFrom()
					+ "'"
					+ " and created_at<"
					+ "'"
					+ tpoj.getEnd() + "'" + " and coordinate_class<=1 ";
			sqlquery += " and st_within(coordinates::geometry,st_geomfromewkt('"
					+ spoj.getGeometry() + "')) ";
			ArrayList<String> wordlist = txpoj.getWords();
			if (!wordlist.isEmpty()) {
				sqlquery += "and ";
				for (int i = 0; i < wordlist.size(); i++) {
					String textparam = "lower(body) like '%"
							+ wordlist.get(i).toLowerCase() + "%' ";
					if (i != wordlist.size() - 1) {
						textparam += "and ";
					}
					sqlquery += textparam;
				}
			}
			sqlquery += " order by created_at";
			List<Tweet> tweetlist = Database.getmapdata(sqlquery);
			// if dictionary is used
			if (dpoj.isUsed()) {
				TextProcessor.filterdicttweets(tweetlist, dpoj.getCategid());
			}
			List<Word> topwords = TextProcessor.gettopwords(tweetlist);
			jsonresponse = getjsonresponseformapdata(tweetlist, topwords,
					sqlquery, dpoj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonresponse;
	}

	private static String getjsonresponseformapdata(List<Tweet> tweetlist,
			List<Word> topwords, String sqlquery, DictDataPojo dpoj) {
		JSONObject response = new JSONObject();
		JSONArray pointarray = new JSONArray();
		JSONArray topwordsdata = new JSONArray();
		JSONObject dictinfo = new JSONObject();
		for (Tweet twj : tweetlist) {
			JSONObject tweetobj = new JSONObject();
			tweetobj.put("id", twj.getTweetid().toString());
			tweetobj.put("x", twj.getCoordinates()[0]);
			tweetobj.put("y", twj.getCoordinates()[1]);
			tweetobj.put("tweet", twj.getBody());
			tweetobj.put("created_at", twj.getCreated_at().toString());
			pointarray.add(tweetobj);
		}
		response.put("pointdata", pointarray);
		for (Word word : topwords) {
			JSONObject words = new JSONObject();
			words.put("word", word.getWord());
			words.put("count", word.getCount());
			words.put("common", word.isCommon());
			topwordsdata.add(words);
		}
		dictinfo.put("used", dpoj.isUsed());
		dictinfo.put("categid", dpoj.getCategid());
		response.put("topwords", topwordsdata);
		response.put("sqlquery", sqlquery);
		response.put("dictinfo", dictinfo);
		return response.toJSONString();
	}

	public static String authenticate(String username, String password) {
		String email = Database.authenticate(username, password);
		return email;
	}
}
