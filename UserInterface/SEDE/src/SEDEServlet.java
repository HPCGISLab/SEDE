package servlets;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import parser.TweetParser;

/**
 * Servlet implementation class SEDEServlet
 */
public class SEDEServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SEDEServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");		
		if (action.equalsIgnoreCase("submitsurvey")) {
			String responsejson=submitsurvey(request);
			String url="http://geostor.geog.kent.edu:8080/SEDE/survey_response.jsp?surveyid="+responsejson;
			request.setAttribute("url",url);
			request.getRequestDispatcher("survey_created.jsp").forward(request, response);
		} 
		else {
			String responseJSON = null;
			if (action.equalsIgnoreCase("getsliderdates")) {
				responseJSON = getsliderdates(request);
			} /*
			 * else if (action.equalsIgnoreCase("getpointhexdata")) {
			 * responseJSON = getpointhexdata(request); }
			 */else if (action.equalsIgnoreCase("searchword")) {
				responseJSON = getsearchword(request);
			} else if (action.equalsIgnoreCase("submitsurvey")) {
				responseJSON = submitsurvey(request);
			} else if (action.equalsIgnoreCase("savesurveyresponse")) {
				responseJSON = savesurveyresponse(request);
			} else if (action.equalsIgnoreCase("getsurvey")) {
				responseJSON = getsurvey(request);
			} else if (action.equalsIgnoreCase("getmapdata")) {
				responseJSON = getmapdata(request);
			}
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			OutputStream os = response.getOutputStream();
			os.write(responseJSON.getBytes());
			os.flush();
			os.close();
		}
	}

	private String getmapdata(HttpServletRequest request) {
		return TweetParser.getmapdata(request);
	}

	private String getsurvey(HttpServletRequest request) {
		return TweetParser.getsurvey(request);
	}

	private String savesurveyresponse(HttpServletRequest request) {
		return TweetParser.savesurveyresponse(request);
	}

	private String submitsurvey(HttpServletRequest request) {
		return TweetParser.submitsurvey(request);
	}

	private String getsearchword(HttpServletRequest request) {
		return TweetParser.getsearchword(request);
	}

	/*
	 * private String getpointhexdata(HttpServletRequest request) { return
	 * TweetParser.getpointhexdata(request); }
	 */

	private String getsliderdates(HttpServletRequest request) {
		return TweetParser.getsliderdates(request);
	}

}
