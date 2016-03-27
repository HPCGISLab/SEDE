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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import utils.SpatialProcessor;
import utils.TextProcessor;
import data.MapDataRequestPojo;
import data.SliderPojo;
import data.SpatialDataPojo;
import data.SurveyDataPojo;
import data.SurveyPojo;
import data.Survey_ResponsePojo;
import data.TemporalDataPojo;
import data.TextualDataPojo;
import data.Tweet;
import data.Word;
import database.Database;

public class TweetParser {

	
	private static String getjsonresponseforpointhex(ArrayList<Tweet> tweetdat,
			ArrayList<Word> topwords, ArrayList<Double> hexdat,String sqlquery) {
		JSONObject response=new JSONObject();
		JSONArray pointarray=new JSONArray();
		JSONArray topwordsdata=new JSONArray();
		JSONArray hexdataarray=new JSONArray();
		for(Tweet twj:tweetdat){
			JSONObject tweetobj=new JSONObject();
			tweetobj.put("id",twj.getTweetid());
			tweetobj.put("x",twj.getCoordinates()[0]);
			tweetobj.put("y",twj.getCoordinates()[1]);
			tweetobj.put("tweet",twj.getBody());
			tweetobj.put("created_at",twj.getCreated_at().toString());
			pointarray.add(tweetobj);
		}
		response.put("pointdata",pointarray);
		for(Word word:topwords){
			JSONObject words=new JSONObject();
			words.put("word",word.getWord());
			words.put("count",word.getCount());
			topwordsdata.add(words);
		}
		response.put("topwords",topwordsdata);
		hexdataarray.addAll(hexdat);
		response.put("hexdata",hexdataarray);
		response.put("sqlquery", sqlquery);
		return response.toJSONString();
	}
	public static String getsearchword(HttpServletRequest request) {
		String searchword=request.getParameter("word");
		String ids=request.getParameter("ids");
		ArrayList<Integer> idlist=Database.getsearchwordids(searchword,ids);
		String jsonresponse=getjsonresponseforwordsearch(idlist);
		return jsonresponse;
	}
	private static String getjsonresponseforwordsearch(ArrayList<Integer> idlist) {
		JSONObject response=new JSONObject();
		JSONArray pointarray=new JSONArray();
		for(int id:idlist){
			pointarray.add(id);
		}
		response.put("ids", pointarray);
		return response.toJSONString();
	}
	public static String submitsurvey(HttpServletRequest request) {
		String datajson=request.getParameter("datajson");
		String resp=null;
		try{
		    ObjectMapper mapper = new ObjectMapper();
		    SurveyPojo spoj=mapper.readValue(datajson, SurveyPojo.class);
		    Date currdate=new Date();
		    int hcode=new HashCodeBuilder(17, 37).append(spoj.getEmail()).append(spoj.getQuery()).append(currdate.getTime()).toHashCode();
		    spoj.setHashcode(hcode);
		    spoj.setCreated_at(new Timestamp(currdate.getTime()));
		    Database.submitsurvey(spoj);
		    resp=String.valueOf(hcode);
		}catch(Exception e){
			e.printStackTrace();
		}
		//return response.toJSONString();
		return resp;
	}
	public static String savesurveyresponse(HttpServletRequest request) {
		String datajson=request.getParameter("datajson");
		String resp=null;
		try{
			ObjectMapper mapper = new ObjectMapper();
		    Survey_ResponsePojo spoj=mapper.readValue(datajson, Survey_ResponsePojo.class);		    
		    Database.savesurveyresponse(spoj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return "Response Saved Successfully";
	}
	public static String getsurvey(HttpServletRequest request) {
		long surveyhash=Long.parseLong(request.getParameter("surveyhash"));
		JSONObject response=new JSONObject();
		try{
			SurveyDataPojo spoj=Database.getsurvey(surveyhash);
			JSONObject surveyobj=new JSONObject();
			JSONObject tweetobj=new JSONObject();
			surveyobj.put("surveyid",spoj.getSurveydata().getSurveyid());
			JSONArray categarray=new JSONArray();
			for(ArrayList<String> categ:spoj.getSurveydata().getQuestions()){
				JSONObject catgedat=new JSONObject();
				catgedat.put("categ",categ.get(0));
				catgedat.put("desc",categ.get(1));
				categarray.add(catgedat);
			}
			surveyobj.put("categ", categarray);
			JSONArray tweetarray=new JSONArray();
			for(Tweet tweet:spoj.getTweets()){
				JSONObject tweetdat=new JSONObject();
				tweetdat.put("id",String.valueOf(tweet.getTweetid()));
				tweetdat.put("body",tweet.getBody());
				tweetarray.add(tweetdat);
			}
			tweetobj.put("tweets", tweetarray);
			response.put("surveydata", surveyobj);
			response.put("tweets",tweetarray);
		}catch(Exception e){
			e.printStackTrace();
		}
		return response.toJSONString();
	}
	public static String getsliderdates(HttpServletRequest request) {
		SliderPojo dateintervals=Database.getsliderdates();
		JSONObject sliderobj=new JSONObject();
		sliderobj.put("startdate",dateintervals.getStartdate().toString() );
		sliderobj.put("enddate",dateintervals.getEnddate().toString() );
		sliderobj.put("days",dateintervals.getInterval());
		return sliderobj.toJSONString();
	}
	public static String getmapdata(HttpServletRequest request) {
		String jsondata=request.getParameter("jsondata");
		String jsonresponse=null;
		try{
			ObjectMapper mapper = new ObjectMapper();
		    MapDataRequestPojo mpoj=mapper.readValue(jsondata, MapDataRequestPojo.class);
		    SpatialDataPojo spoj=mpoj.getSpatialdata();
			TemporalDataPojo tpoj=mpoj.getTemporaldata();
			TextualDataPojo txpoj=mpoj.getTextualdata();
			String[] hexcoordinates=mpoj.getHexmapcenters().getCoordinates().split(":");
			String sqlquery="select tweetid,body,st_x(coordinates::geometry),st_y(coordinates::geometry),created_at from tweets where created_at>="+"'"+tpoj.getFrom()+"'"+" and created_at<"+"'"+tpoj.getEnd()+"'"+" and coordinate_class<>2 ";
			ArrayList<String> wordlist=txpoj.getWords();
			if(!wordlist.isEmpty()){
				sqlquery+="and ";
				for(int i=0;i<wordlist.size();i++){
					String textparam="lower(body) like '%"+wordlist.get(i).toLowerCase()+"%' ";
					if(i!=wordlist.size()-1){
						textparam+="and ";
					}
					sqlquery+=textparam;
				}
			}
			if(spoj.getType()==2 && spoj.getState()!=null){
				sqlquery+="and coordinates && (select geom from states where lower(state_name)="+"'"+spoj.getState().toLowerCase()+"'"+")";
			}
			else if(spoj.getType()==3){
				double radmeter=1609.0;//default to 1 mile
				if(spoj.getCoordinates()!=null){
					if(spoj.getRadius()!=0.0){
						radmeter*=spoj.getRadius();
					}
					String spatialquery="and coordinates && (select st_buffer(ST_GeogFromText('POINT("+spoj.getCoordinates()[1]+" "+spoj.getCoordinates()[0]+")'),"+radmeter+"))";
					sqlquery+=spatialquery;
				}
			}
			sqlquery+=" order by created_at";
			System.out.println(sqlquery);
		    ArrayList<Tweet> tweetlist=Database.getmapdata(sqlquery);
		    ArrayList<Word> topwords=TextProcessor.gettopwords(tweetlist);
		    ArrayList<Double> hexdat=SpatialProcessor.gethexdata(hexcoordinates,tweetlist);
		    jsonresponse=getjsonresponseforpointhex(tweetlist,topwords,hexdat,sqlquery);
		}catch(Exception e){
			e.printStackTrace();
		}		
		return jsonresponse;
	}
}
