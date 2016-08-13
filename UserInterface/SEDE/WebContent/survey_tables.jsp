<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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
<script src="js/jquery/jquery.js"></script>
<script>
	function submitsurvey() {
		if(!($('#codebookname')).val()){
			return false;
		}
		var obj = {};
		obj.email = $('#email').val();
		obj.query = $('#query').val();
		obj.dictused = $('#dictused').val();
		obj.dictcategid = $('#dictcateg').val();
		var surveyquestions = [];
		for (var i = 1; i <= 5; i++) {
			surveyquestions.push([ $("#code" + i).val(),
					$("#code" + i + "comm").val() ]);
		}
		obj.questions = surveyquestions;
		obj.codebookname=$('#codebookname').val();
		document.surveyform.datajson.value = JSON.stringify(obj);
		return true;
	}
</script>
</head>
<body>
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
	<div class="panel panel-default">
		<div class="panel-heading">Total Tweets :
			${fn:escapeXml(param.tweetcount)}</div>
	</div>
	<div class="container">
		<table class="table table-bordered">
			<thead>
				<tr>
					<th class="col-md-1">Code#</th>
					<th class="col-md-3">Code</th>
					<th class="col-md-8">Description</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col-md-1">1</td>
					<td class="col-md-2"><input type="text" class="form-control"
						id="code1" maxlength="20"></td>
					<td class="col-md-8"><textarea class="form-control" rows="2"
							id="code1comm"></textarea></td>
				</tr>
				<tr>
					<td class="col-md-1">2</td>
					<td class="col-md-2"><input type="text" class="form-control"
						id="code2" maxlength="20"></td>
					<td class="col-md-8"><textarea class="form-control" rows="2"
							id="code2comm"></textarea></td>
				</tr>
				<tr>
					<td class="col-md-1">3</td>
					<td class="col-md-2"><input type="text" class="form-control"
						id="code3" maxlength="20"></td>
					<td class="col-md-8"><textarea class="form-control" rows="2"
							id="code3comm"></textarea></td>
				</tr>
				<tr>
					<td class="col-md-1">4</td>
					<td class="col-md-2"><input type="text" class="form-control"
						id="code4" maxlength="20"></td>
					<td class="col-md-8"><textarea class="form-control" rows="2"
							id="code4comm"></textarea></td>
				</tr>
				<tr>
					<td class="col-md-1">5</td>
					<td class="col-md-2"><input type="text" class="form-control"
						id="code5" maxlength="20"></td>
					<td class="col-md-8"><textarea class="form-control" rows="2"
							id="code5comm"></textarea></td>
				</tr>
			</tbody>
		</table>
		<div class="row">
			<div class="col-md-12">
				<label style="padding-right: 1%">Codebook name</label><input type="text" id="codebookname" required="required" maxlength="20">
			</div>	
		</div>
		<div class="row">
			<div class="col-md-2 col-md-offset-4">
				<form name="surveyform"
					action="/SEDE/SEDEServlet"
					onsubmit="return submitsurvey()" method="post">
					<input type="hidden" name="action" value="submitsurvey"> <input
						type="hidden" name="datajson" value=""> <input
						type="submit" class="btn btn-primary" value="Submit">
				</form>
			</div>
		</div>
	</div>
	<input type="hidden" value='${fn:escapeXml(param.emailsurvey)}'
		id="email">
	<input type="hidden" value='${fn:escapeXml(param.sqlquerysurvey)}'
		id="query">
	<input type="hidden" value='${fn:escapeXml(param.dictusedsurvey)}'
		id="dictused">
	<input type="hidden" value='${fn:escapeXml(param.dictcategidsurvey)}'
		id="dictcateg">
</body>
</html>
