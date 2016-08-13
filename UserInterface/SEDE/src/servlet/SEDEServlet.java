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
			String responsejson = submitsurvey(request);
			String url = request.getScheme() + "://" + request.getServerName()
					+ ":" + request.getServerPort()
					+ "/SEDE/login/survey_response.jsp?surveyid="
					+ responsejson;
			request.setAttribute("url", url);
			request.getRequestDispatcher("survey_created.jsp").forward(request,
					response);
		} else {
			String responseJSON = null;
			if (action.equalsIgnoreCase("getsliderdates")) {
				responseJSON = getsliderdates(request);
			} else if (action.equalsIgnoreCase("submitsurvey")) {
				responseJSON = submitsurvey(request);
			} else if (action.equalsIgnoreCase("savesurveyresponse")) {
				responseJSON = savesurveyresponse(request);
			} else if (action.equalsIgnoreCase("getsurvey")) {
				responseJSON = getsurvey(request);
			} else if (action.equalsIgnoreCase("getmapdatafordisplay")) {
				responseJSON = getmapdatafordisplay(request);
			} else if (action.equalsIgnoreCase("getsurveyresponse")) {
				responseJSON = getsurveyresponse(request);
			} else if (action.equalsIgnoreCase("updatesurveyresponse")) {
				responseJSON = updatesurveyresponse(request);
			} else if (action.equalsIgnoreCase("getweatherevents")) {
				responseJSON = getweatherevents(request);
			} else if (action.equalsIgnoreCase("getdictionarycategories")) {
				responseJSON = getdictionarycategories(request);
			}
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			OutputStream os = response.getOutputStream();
			os.write(responseJSON.getBytes());
			os.flush();
			os.close();
		}
	}

	private String getweatherevents(HttpServletRequest request) {
		return TweetParser.getweatherevents(request);
	}

	private String updatesurveyresponse(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return TweetParser.updatesurveyresponse(request);
	}

	private String getsurveyresponse(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return TweetParser.getsurveyresponse(request);
	}

	private String getmapdatafordisplay(HttpServletRequest request) {
		return TweetParser.getmapdatafordisplay(request);
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

	private String getsliderdates(HttpServletRequest request) {
		return TweetParser.getsliderdates(request);
	}

	private String getdictionarycategories(HttpServletRequest request) {
		return TweetParser.getdictionarycategories(request);
	}
}
