package servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import parser.TweetParser;

/**
 * Servlet implementation class LoginServlet
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    //all browser requests
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action=getActionFromURL(request);
		response.sendRedirect(request.getContextPath()+"/login.jsp?action="+action);
	}

	private String getActionFromURL(HttpServletRequest request) {
		if(request.getRequestURL().toString().contains("home")){
			return "home";
		}
		else if(request.getRequestURL().toString().contains("survey_response")){
			return "surveyid:"+request.getParameter("surveyid");
		}
		return null;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	//calls from login page
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username=request.getParameter("username");
		String password=request.getParameter("password");
		String action=request.getParameter("action");
		String useremail=authenticate(username,password);
		if(useremail!=null&&(action!=null&&!action.isEmpty())){
			if(action.equals("home")){
				request.setAttribute("email",useremail);
				request.getRequestDispatcher("/index.jsp").forward(request, response);
			}
			else if(action.contains("surveyid")){
				request.setAttribute("email",useremail);
				request.setAttribute("surveyid",action.split(":")[1]);
				request.getRequestDispatcher("/survey_response.jsp").forward(request, response);
			}
		}
		else{
			response.sendRedirect(request.getContextPath()+"/login.jsp?action="+action);
		}
	}

	private String authenticate(String username, String password) {
		return TweetParser.authenticate(username,password);
	}
}
