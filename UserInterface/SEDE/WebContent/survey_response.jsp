<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<meta name="description" content="">
<meta name="author" content="">

<title>SEDE - Socio-Environmental Data Explorer</title>

<!-- Bootstrap core CSS -->
<link href="css/bootstrap.min.css" rel="stylesheet">

<!-- Custom styles for this template -->
<link href="css/dashboard.css" rel="stylesheet">

<!-- custom style for SEDE -->
<link href="css/sede.css" rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
<!--[if lt IE 9]><script src="../../assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
<script src="js/ie-emulation-modes-warning.js"></script>
<script charset="utf-8" src="js/d3.min.js" type="text/javascript"></script>
<script src="js/jquery/jquery.js"></script>
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/dt/dt-1.10.12/datatables.min.css"/>
<script type="text/javascript" src="https://cdn.datatables.net/v/dt/dt-1.10.12/datatables.min.js"></script>
<script>
	var tweets, categ, surveyid, email, surveyjson, surveydetails, surveyresponse;
	function getsurvey(surveyidhash,emailid) {
		var myVar = setInterval(updateinterval, 60000);
		email = emailid;
		var http = new XMLHttpRequest();
		http.open("POST", "/SEDE/SEDEServlet",
				false);
		http.setRequestHeader("Content-type",
				"application/x-www-form-urlencoded");
		http.send("action=getsurveyresponse&surveyhash=" + surveyidhash
				+ "&email=" + email);
		surveyjson = JSON.parse(http.responseText);
		surveydetails = surveyjson.surveydetails;
		surveyresponse = surveyjson.surveyresponse;
		var tooltip = d3.select("body").append("div").style("position",
				"absolute").style("z-index", "10")
				.style("visibility", "hidden").text("").attr("class",
						"span2 well").attr("id", "tooltipdiv");
		var header = [ "Sl#", "Tweet" ];
		for (var i = 0; i < surveydetails.length; i++) {
			header.push(surveydetails[i]);
		}
		var container = d3.select("body").append("div").attr("class",
				"container");
		var table = container.append("table").attr("class",
				"table table-striped").attr("id","myTable");
		var headerrow = table.append("thead").append("tr");
		var headerdata = headerrow.selectAll("th").data(header).enter().append(
				"th").text(function(d, i) {
			if (i < 2)
				return d;
			else
				return d[0];
		}).on('mouseover', function(d, i) {
			if (i >= 2) {
				tooltip.text(d[1]);
				return tooltip.style("visibility", "visible");
			}
		}).on('mouseout', function(d, i) {
			if (i >= 2) {
				tooltip.text("");
				return tooltip.style("visibility", "hidden");
			}
		}).on(
				'mousemove',
				function(d, i) {
					if (i >= 2) {
						return tooltip.style("top", (event.pageY - 10) + "px")
								.style("left", (event.pageX + 10) + "px");
					}
				}).attr("class", function(d, i) {
			if (i == 0) {
				return "col-md-1";
			}
			if (i == 1) {
				return "col-md-6";
			} else {
				return "col-md-1";
			}
		});
		var tbody = table.append("tbody");
		var trows = tbody.selectAll("tr").data(surveyresponse).enter().append(
				"tr");
		var tdata = trows.selectAll("td").data(function(h, k) {
			var rowobj = [];
			for (var ind = 0; ind < header.length; ind++)
				rowobj.push(k);
			rowobj[0] = k;
			rowobj[1] = h.body;
			return rowobj;
		}).enter().append("td").html(function(s, j) {
			if (j < 2)
				return s;
			else {
				var respid = surveyresponse[s].responseid;
				var resp = surveyresponse[s].responses[j - 2];
				if (resp == 0)
					return "<input type ='checkbox' name="+respid+">";
				else if (resp == 1) {
					return "<input type ='checkbox' name="+respid+" checked>";
				}
			}
		});
		$('#myTable').DataTable( {
		    fixedHeader: true
		} );
		var formdata = container.append("form").attr("name", "surveyresp")
				.attr("action",
						"/SEDE/SEDEServlet")
				.attr("method", "post").on("submit", function() {
					updatesurveyresponse();
				});
		formdata.append("input").attr("type", "hidden")
				.attr("name", "datajson").attr("value", "");
		formdata.append("input").attr("type", "hidden").attr("name", "action")
				.attr("value", "updatesurveyresponse");
		formdata.append("button").attr("class", "btn btn-primary").text(
				"Submit");
	}
	
	function updateinterval(){
		var http = new XMLHttpRequest();
		http.open("POST", "/SEDE/SEDEServlet",
				false);
		http.setRequestHeader("Content-type",
				"application/x-www-form-urlencoded");
		http.send("action=updatesurveyresponse&datajson=" + JSON.stringify(getresponsedata()));
	}
	
	function getresponsedata(){
		var survey_response_data={};
		var responsedata = [];
		for (var i = 0; i < surveyresponse.length; i++) {
			var Survey_Response = {};
			Survey_Response.responseid = surveyresponse[i].responseid;
			var rows = document.getElementsByName(surveyresponse[i].responseid
					.toString());
			rowdata = [];
			for (var j = 0; j < rows.length; j++)
				rowdata.push(+rows[j].checked);
			Survey_Response.responses = rowdata;
			responsedata.push(Survey_Response);
		}
		survey_response_data.survey_response=responsedata;
		return survey_response_data;
	}
	function updatesurveyresponse() {
		var survey_response_data=getresponsedata();
		document.surveyresp.datajson.value = JSON.stringify(survey_response_data);
	}
</script>
</head>
<body onload="getsurvey('${requestScope.surveyid}','${requestScope.email}')">
	<nav class="navbar navbar-inverse navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed"
					data-toggle="collapse" data-target="#navbar" aria-expanded="false"
					aria-controls="navbar">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#">SEDE - Socio-Environmental Data
					Explorer</a>
			</div>
			<div id="navbar" class="navbar-collapse collapse">
				<ul class="nav navbar-nav navbar-right">
					<li><a href="#">Settings</a></li>
					<li><a href="#">About</a></li>
					<li><a href="#">Help</a></li>
				</ul>
				<form class="navbar-form navbar-right">
					<input type="text" class="form-control" placeholder="Search...">
				</form>
			</div>
		</div>
	</nav>
</body>
</html>
