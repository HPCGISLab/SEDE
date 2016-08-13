<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
<script
	src="http://maps.google.com/maps/api/js?key=AIzaSyAhI5aCDY751jcmskx5SYzmtkikR5omDOM&libraries=visualization"></script>
<script charset="utf-8" src="js/d3.min.js" type="text/javascript"></script>
<link rel="stylesheet" href="js/noUiSlider/nouislider.min.css">
<script src="js/jquery/jquery.js"></script>
<script src="js/noUiSlider/nouislider.min.js"></script>
<script src="js/moment.js"></script>
<script src="js/hexbin.js"></script>
<script src="states.json"></script>
<!--  script src="js/counties.json"></script>-->
<style>
svg {
	position: absolute;
}

.hexagons path {
	stroke: #fff;
}

.county path {
	stroke-width: 1.0;
	stroke: black;
}

.redfill {
	fill: red;
}
</style>
<script type="text/javascript">
	var rectselector, sliderdata, gmap, slider, sliderstartend = [], sliderinterval, sedeserviceendpoint = "/SEDE/SEDEServlet", spatialserviceendpoint = "/SEDE/SpatialServlet", mapviewportbounds, geocoder = new google.maps.Geocoder(), mapresponsejson, pointdata, hexmapdata, countiesdata, topwordsdata, overlay, projection, layer, hexbins, worldmapbounds, datareceived = false, searchwordset, searchcoordinates, sqlquery, hexlatlng, heatmap = new google.maps.visualization.HeatmapLayer();
	var color = d3.scale.linear().domain([ 0, 20 ]).range(
			[ "white", "steelblue" ]).interpolate(d3.interpolateLab);
	/*Get data from database based on url and parameter value*/
	function getdatafromserver(url, params) {
		var http = new XMLHttpRequest();
		http.open("POST", url, false);
		http.setRequestHeader("Content-type",
				"application/x-www-form-urlencoded");
		http.send(params);
		return JSON.parse(http.responseText);
	}

	/*Get box in coordinate pixel*/
	function getboxinpixels(bounds, isdiv) {
		var lowerleft = projection.fromLatLngToContainerPixel(bounds
				.getSouthWest());
		var upperright = projection.fromLatLngToContainerPixel(bounds
				.getNorthEast());
		if (isdiv) {
			lowerleft = projection.fromLatLngToDivPixel(bounds.getSouthWest());
			upperright = projection.fromLatLngToDivPixel(bounds.getNorthEast());
		}
		var top = [ lowerleft.x, upperright.y ];
		var width = upperright.x - lowerleft.x;
		var height = lowerleft.y - upperright.y;
		return [ top, width, height ];
	}
	/*Utility function to get current slider values*/
	function getcurrentslidervalues() {
		var sliderarr = slider.noUiSlider.get();
		sliderarr[0] = Math.floor(sliderarr[0]);
		sliderarr[1] = Math.floor(sliderarr[1]);
		return sliderarr;
	}

	/*Set svg across overlay*/
	function setsvgforbounds() {
		var bounds = gmap.getBounds();
		var box = getboxinpixels(bounds, true);
		/*Set svg dimensions*/
		layer.attr("height", box[2] + "px").attr("width", box[1] + "px").style(
				"left", box[0][0] + "px").style("top", box[0][1] + "px");
	}
	/*To transform point data*/
	function transform(d) {
		d = new google.maps.LatLng(d.y, d.x);
		d = projection.fromLatLngToContainerPixel(d);
		return d3.select(this).attr("cx", d.x).attr("cy", d.y);
	}
	/*function to display maps based on user selected choice*/
	function displaymap() {
		setsvgforbounds();
		layer.selectAll("*").remove();
		if (heatmap.getMap() != null) {
			heatmap.setMap(null);
		}
		var dispselected = d3.select('input[name="disp"]:checked').node().value;
		if (dispselected == "Point") {
			displaypoints();
		} else if (dispselected == "Hex") {
			displayhexmaps();
		} else if (dispselected == "Heat") {
			displayheatmaps();
		}
	}
	/*Display point data*/
	function displaypoints() {
		//Display point data over here
		layer.selectAll("circle").data(mapresponsejson.pointdata).each(
				transform).enter().append("circle").each(transform)
				.attr("r", 3).style("fill", "red");
	}
	//Display hexmaps
	function displayhexmaps() {
		//Display hexmap data over here
		layer.append("g").attr("class", "hexagons").selectAll("path").data(
				hexbins(mapresponsejson.pointdata)).enter().append("path")
				.attr("d", function(d) {
					return hexbins.hexagon();
				}).attr("transform", function(d) {
					return "translate(" + d.x + "," + d.y + ")";
				}).style("fill", function(d) {
					return color(d.length);
				});
	}
	//Display Heatmaps uses google heatmaps
	function displayheatmaps() {
		if (mapresponsejson && mapresponsejson.pointdata.length != 0) {
			var latlngarray = [];
			mapresponsejson.pointdata.forEach(function(d) {
				latlngarray.push(new google.maps.LatLng(d.y, d.x));
			});
			var mvcarray = new google.maps.MVCArray(latlngarray);
			heatmap.setData(mvcarray);
			heatmap.setMap(gmap);
		}
	}
	/*Initializes and setsup the home screen*/
	function initialize() {
		sedeserviceendpoint = "${pageContext.request.scheme}" + "://"
				+ "${pageContext.request.serverName}" + ":"
				+ "${pageContext.request.serverPort}" + sedeserviceendpoint;
		spatialserviceendpoint = "${pageContext.request.scheme}" + "://"
				+ "${pageContext.request.serverName}" + ":"
				+ "${pageContext.request.serverPort}" + spatialserviceendpoint;
		/*Set the map*/
		gmap = new google.maps.Map(d3.select("#map").node(), {
			zoom : 2,
			center : new google.maps.LatLng(35.7803, 16.7871),
			mapTypeId : google.maps.MapTypeId.ROADMAP
		});
		rectselector = new google.maps.Rectangle({
			editable : true,
			draggable : true,
			visible : false
		});
		rectselector.setMap(gmap);
		/*Set the overlayview for svg*/
		overlay = new google.maps.OverlayView();
		/*When overlay is added*/
		overlay.onAdd = function() {
			/*Create svg layer for drawing*/
			layer = d3.select(this.getPanes().overlayLayer).append("svg");
			worldmapbounds = gmap.getBounds();
			mapviewportbounds = worldmapbounds;
		};
		/*When map is panned/zoomed/center changed or bounds changed*/
		overlay.draw = function() {
			projection = this.getProjection();
			//Check if we have data for drawing
			if (mapresponsejson) {
				//set svg according to current bounds
				displaymap();
				/* setsvgforbounds();
				layer.selectAll("*").remove();
				displaymap(); */
			}
		};
		/*Set overlay to map*/
		overlay.setMap(gmap);
		/*Set the time slider*/
		slider = document.getElementById('slider');
		console.log();
		/*Request time slider dates from database*/
		sliderdata = getdatafromserver(sedeserviceendpoint,
				"action=getsliderdates");
		sliderstartend.push(sliderdata.startdate);
		sliderstartend.push(sliderdata.enddate);
		var selects = d3.select("#state").selectAll("option").data(usstates)
				.enter().append("option").attr("value", function(d) {
					return d.name;
				}).text(function(d) {
					return d.name;
				});
		$("#start").val(moment(sliderdata.startdate).format('YYYY-MM-DD'));
		$("#start").prop('min',
				moment(sliderdata.startdate).format('YYYY-MM-DD'));
		$("#start").prop(
				'max',
				moment(sliderdata.enddate).subtract(1, 'days').format(
						'YYYY-MM-DD'));
		$("#end").val(
				moment(sliderdata.startdate).add(1, 'days')
						.format('YYYY-MM-DD'));
		$("#end").prop(
				'min',
				moment(sliderdata.startdate).add(1, 'days')
						.format('YYYY-MM-DD'));
		$("#end").prop('max', moment(sliderdata.enddate).format('YYYY-MM-DD'));
		sliderinterval = parseInt(sliderdata.days);
		noUiSlider.create(slider, {
			start : [ 0, 1 ],
			margin : 1,
			connect : true,
			range : {
				'min' : 0,
				'max' : sliderinterval
			}
		});
		slider.noUiSlider.on('end', function() {
			sliderchange();
		});
		searchwordset = d3.set();
		updatedictionary();
	}

	function sliderchange() {
		var slidervals = getcurrentslidervalues();
		$("#start").val(
				moment(sliderstartend[0]).add(slidervals[0], 'days').format(
						'YYYY-MM-DD'));
		$("#end").val(
				moment(sliderstartend[0]).add(slidervals[1], 'days').format(
						'YYYY-MM-DD'));
		$("#start").prop(
				'max',
				moment($("#end").val()).subtract(1, 'days')
						.format('YYYY-MM-DD'));
		$("#end").prop('min',
				moment($("#start").val()).add(1, 'days').format('YYYY-MM-DD'));
		updateweatherevent();
	}
	//Update the LIWC dictionary
	function updatedictionary() {
		var parameter = "action=getdictionarycategories";
		var dictdata = getdatafromserver(sedeserviceendpoint, parameter);
		d3.select("#dictionary").selectAll("option").data(dictdata).enter()
				.append("option").attr("value", function(d) {
					return d.id;
				}).text(function(d) {
					return d.category;
				});
	}
	//update the topwords table
	function updatetopwordstable() {
		var howmany = parseInt(d3.select("#topwords").node().value);
		d3.select("#topwordstable").selectAll("tbody").remove();
		var topwordslist = [];
		for (var k = 0; k < mapresponsejson.topwords.length; k++) {
			if (topwordslist.length == howmany) {
				break;
			} else {
				if ($("#hidecommon").is(':checked')) {
					if (!(mapresponsejson.topwords[k].common)) {
						topwordslist.push(mapresponsejson.topwords[k]);
					}
				} else {
					topwordslist.push(mapresponsejson.topwords[k]);
				}
			}
		}
		var rows = d3.select('#topwordstable').append("tbody").selectAll("tr")
				.data(topwordslist).enter().append("tr");
		var wordcolumn = rows.append("td");
		var countcolumn = rows.append("td");
		wordcolumn.html(function(d) {
			return d.word;
		});
		countcolumn.html(function(d) {
			return d.count;
		});
	}
	//remove word from textual filter
	function removeword(word) {
		searchwordset.remove(word);
		updateuserwordstable();
	}
	//update textual filter table
	function updateuserwordstable() {
		d3.select("#userselectedtable").selectAll("tbody").remove();
		var rows = d3.select("#userselectedtable").append("tbody").selectAll(
				"tr").data(searchwordset.values()).enter().append("tr");
		var wordcolumn = rows.append("td");
		var removecolumn = rows.append("td");
		wordcolumn.html(function(d) {
			return d;
		});
		removecolumn.append("button").text("Remove").attr("onclick",
				function(d) {
					return "removeword(" + "\"" + d + "\"" + ")";
				});
	}
	//save words searched by user and update the user selected words table
	function searchwords() {
		var wordsearched = d3.select("#wordsearch").node().value;
		if (wordsearched.length != 0 && !searchwordset.has(wordsearched)) {
			searchwordset.add(wordsearched);
			d3.select("#wordsearch").node().value = "";
			updateuserwordstable();
		}
	}
	//when calendar is changed
	function lowerdatechange() {
		if ((moment($("#start").val()).isAfter(moment(sliderdata.enddate)))
				|| (moment($("#start").val())
						.isBefore(moment(sliderdata.startdate)))) {
			sliderchange();
		} else {
			$("#end").prop(
					'min',
					moment($("#start").val()).add(1, 'days').format(
							'YYYY-MM-DD'));
			if (moment($("#end").val()).isBefore(moment($("#start").val()))
					|| moment($("#end").val())
							.isSame(moment($("#start").val()))) {
				$("#end").val(
						moment($("#start").val()).add(1, 'days').format(
								'YYYY-MM-DD'));
			}
			var lowersliderval = moment.duration(
					moment($("#start").val()).diff(moment(sliderstartend[0])))
					.asDays();
			var uppersliderval = moment.duration(
					moment($("#end").val()).diff(moment(sliderstartend[0])))
					.asDays();
			slider.noUiSlider.set([ lowersliderval, uppersliderval ]);
			updateweatherevent();
		}
	}
	//when calendar is changed
	function upperdatechange() {
		if (moment($("#end").val()).isAfter(moment(sliderdata.enddate))
				|| moment($("#end").val()).isBefore(
						moment(sliderdata.startdate))) {
			sliderchange();
		} else {
			$("#start").prop(
					'max',
					moment($("#end").val()).subtract(1, 'days').format(
							'YYYY-MM-DD'));
			if (moment($("#start").val()).isAfter(moment($("#end").val()))
					|| moment($("#end").val())
							.isSame(moment($("#start").val()))) {
				$("#start").val(
						moment($("#end").val()).subtract(1, 'days').format(
								'YYYY-MM-DD'));
			}
			var lowersliderval = moment.duration(
					moment($("#start").val()).diff(moment(sliderstartend[0])))
					.asDays();
			var uppersliderval = moment.duration(
					moment($("#end").val()).diff(moment(sliderstartend[0])))
					.asDays();
			slider.noUiSlider.set([ lowersliderval, uppersliderval ]);
			updateweatherevent();
		}
	}
	//search using google maps geocoding feature
	function getplacedatafromgmap(searchkey) {
		if (searchkey.length == 0) {
			searchcoordinates = null;
			return;
		}
		geocoder.geocode({
			'address' : searchkey
		}, function(results, status) {
			if (status === google.maps.GeocoderStatus.OK) {
				searchcoordinates = [ results[0].geometry.location.lat(),
						results[0].geometry.location.lng() ];
			} else {
				searchcoordinates = null;
			}
		});
	}
	//search for a place name
	function searchplace() {
		var searchkey = $("#location").val();
		getplacedatafromgmap(searchkey);
	}
	//utility function to get bounds from boundstring of the form xmin,ymin,xmax,ymax
	function boundsfromcoordinates(boundstring) {
		var arr = boundstring.split(",");
		return new google.maps.LatLngBounds(new google.maps.LatLng(
				parseFloat(arr[1]), parseFloat(arr[0])),
				new google.maps.LatLng(parseFloat(arr[3]), parseFloat(arr[2])));
	}
	//utility function to get boundstring from google.maps.LatLngBounds
	function coordinatesfrombounds(bounds) {
		return bounds.getSouthWest().lng() + "," + bounds.getSouthWest().lat()
				+ "," + bounds.getNorthEast().lng() + ","
				+ bounds.getNorthEast().lat();
	}
	//Check if two bounds are equal
	function boundequality(boundA, boundB) {
		if ((boundA.getNorthEast().lat() == boundB.getNorthEast().lat())
				&& (boundA.getNorthEast().lng() == boundB.getNorthEast().lng())
				&& (boundA.getSouthWest().lat() == boundB.getSouthWest().lat())
				&& (boundA.getSouthWest().lng() == boundB.getSouthWest().lng()))
			return true;
		else
			return false;
	}
	//update hexmap data with new bounds
	function updatehexdata() {
		var boxdata = getboxinpixels(gmap.getBounds(), false);
		hexbins = d3.hexbin().size([ boxdata[1], boxdata[2] ]).radius(15);
		hexbins
				.x(function(d) {
					return projection
							.fromLatLngToContainerPixel(new google.maps.LatLng(
									d.y, d.x)).x;
				});
		hexbins
				.y(function(d) {
					return projection
							.fromLatLngToContainerPixel(new google.maps.LatLng(
									d.y, d.x)).y;
				});
	}
	//update data with new params
	function update_data() {
		var formData;
		var spatialparameters;
		var candospatialquery = true;
		var spatialchoice = $('input[name=spatialradio]:checked').val();
		//All tweets		
		if (spatialchoice == "1") {
			//we have already the bounds for world map use that
			var ne = worldmapbounds.getNorthEast();
			var sw = worldmapbounds.getSouthWest();
			spatialparameters = "choice=1&boundingbox=" + sw.lng() + ","
					+ sw.lat() + "," + ne.lng() + "," + ne.lat();
		}
		//State based tweets
		else if (spatialchoice == "2") {
			if ($("#state").val()) {
				spatialparameters = "choice=2&state=" + $("#state").val();
			} else {
				candospatialquery = false;
			}
		}
		//Location based tweets
		else if (spatialchoice == "3") {
			if (searchcoordinates) {
				var buffer = $("#buffer").val();
				spatialparameters = "choice=3&buffer=" + buffer
						+ "&coordinates=" + searchcoordinates[1] + " "
						+ searchcoordinates[0];
			} else {
				candospatialquery = false;
			}
		}
		//Shape file upload tweets
		else if (spatialchoice == "4") {
			if ($('#shapefile').val()) {
				formData = new FormData();
				var buffer = $("#buffer_file").val();
				formData.append("choice", "4");
				formData.append("buffer", buffer);
				formData.append("shapefile", document
						.getElementById("shapefile").files[0]);
				if ($('#prjfile').val()) {
					formData.append("prjfile", document
							.getElementById("prjfile").files[0]);
				}
			} else {
				candospatialquery = false;
			}
		}
		//Rectangle selection
		else if (spatialchoice == "5") {
			spatialparameters = "choice=5&rectbounds="
					+ coordinatesfrombounds(rectselector.getBounds());
		}
		//Weather event selection
		else if (spatialchoice == "6") {
			if ($('#events').val()) {
				spatialparameters = "choice=6&eventid=" + $('#events').val();
			} else {
				candospatialquery = false;
			}
		}
		//If we can do the spatial query then proceed
		if (candospatialquery) {
			var request = new XMLHttpRequest();
			request.open("POST", spatialserviceendpoint, false);
			if (spatialchoice == "4") {
				request.send(formData);
			} else {
				request.setRequestHeader("Content-type",
						"application/x-www-form-urlencoded");
				request.send(spatialparameters);
			}
			//handle error cases here
			var spatialresponsejson = JSON.parse(request.responseText);
			var temporaldata = {};
			temporaldata.from = $("#start").val();
			temporaldata.end = $("#end").val();
			var textualdata = {};
			textualdata.words = searchwordset.values();
			var spatialdata = {};
			spatialdata.geometry = spatialresponsejson.polygon;
			var dictdata = {};
			if ($("#usedict").is(':checked')) {
				dictdata.used = true;
				dictdata.categid = $("#dictionary").val();
			} else {
				dictdata.used = false;
			}
			var jsondata = {};
			jsondata.temporaldata = temporaldata;
			jsondata.textualdata = textualdata;
			jsondata.spatialdata = spatialdata;
			jsondata.dictdata = dictdata;
			var request = "action=getmapdatafordisplay&jsondata="
					+ JSON.stringify(jsondata);
			var fitboundsneeded = true;
			if (mapresponsejson
					&& boundequality(mapresponsejson.bounds,
							boundsfromcoordinates(spatialresponsejson.bounds))) {
				fitboundsneeded = false;
			}
			mapresponsejson = getdatafromserver(sedeserviceendpoint, request);
			mapresponsejson.bounds = boundsfromcoordinates(spatialresponsejson.bounds);
			updatetopwordstable();
			updatehexdata();
			if (fitboundsneeded)
				gmap.fitBounds(mapresponsejson.bounds);
			else
				displaymap();
		}
	}
	//When different spatial filter are chosen
	function spatialselectchange() {
		var spatialchoice = $('input[name=spatialradio]:checked').val();
		//For all tweets spatial choice
		if (spatialchoice == "1") {
			$("#state").prop("disabled", true);
			$("#state").val("default");
			$("#location").prop("disabled", true);
			$("#buffer").prop("disabled", true);
			$("#location").val("");
			$("#shapefile").prop("disabled", true);
			$("#prjfile").prop("disabled", true);
			$("#buffer_file").prop("disabled", true);
			$("#recenterbutton").prop("disabled", true);
			rectselector.setVisible(false);
			$("#event_type").prop("disabled", true);
			$("#event_type").val("default");
			$("#event_severity").prop("disabled", true);
			$("#event_severity").val("default");
			$("#events").prop("disabled", true);
			$("#events").val("default");
		}
		//US state selection 
		else if (spatialchoice == "2") {
			$("#state").prop("disabled", false);
			$("#location").prop("disabled", true);
			$("#buffer").prop("disabled", true);
			$("#location").val("");
			$("#shapefile").prop("disabled", true);
			$("#prjfile").prop("disabled", true);
			$("#buffer_file").prop("disabled", true);
			$("#recenterbutton").prop("disabled", true);
			rectselector.setVisible(false);
			$("#event_type").prop("disabled", true);
			$("#event_type").val("default");
			$("#event_severity").prop("disabled", true);
			$("#event_severity").val("default");
			$("#events").prop("disabled", true);
			$("#events").val("default");
		}
		//Location search selection
		else if (spatialchoice == "3") {
			$("#state").prop("disabled", true);
			$("#state").val("default");
			$("#location").prop("disabled", false);
			$("#buffer").prop("disabled", false);
			$("#shapefile").prop("disabled", true);
			$("#prjfile").prop("disabled", true);
			$("#buffer_file").prop("disabled", true);
			$("#recenterbutton").prop("disabled", true);
			rectselector.setVisible(false);
			$("#event_type").prop("disabled", true);
			$("#event_type").val("default");
			$("#event_severity").prop("disabled", true);
			$("#event_severity").val("default");
			$("#events").prop("disabled", true);
			$("#events").val("default");
		}
		//Upload Shape file selection
		else if (spatialchoice == "4") {
			$("#state").prop("disabled", true);
			$("#state").val("default");
			$("#location").prop("disabled", true);
			$("#buffer").prop("disabled", true);
			$("#shapefile").prop("disabled", false);
			$("#buffer_file").prop("disabled", false);
			$("#prjfile").prop("disabled", false);
			$("#recenterbutton").prop("disabled", true);
			rectselector.setVisible(false);
			$("#event_type").prop("disabled", true);
			$("#event_type").val("default");
			$("#event_severity").prop("disabled", true);
			$("#event_severity").val("default");
			$("#events").prop("disabled", true);
			$("#events").val("default");
		}
		//Rectangle Selection
		else if (spatialchoice == "5") {
			$("#state").prop("disabled", true);
			$("#state").val("default");
			$("#location").prop("disabled", true);
			$("#buffer").prop("disabled", true);
			$("#shapefile").prop("disabled", true);
			$("#prjfile").prop("disabled", true);
			$("#buffer_file").prop("disabled", true);
			$("#recenterbutton").prop("disabled", false);
			recenterrectangle();
			rectselector.setVisible(true);
			$("#event_type").prop("disabled", true);
			$("#event_type").val("default");
			$("#event_severity").prop("disabled", true);
			$("#event_severity").val("default");
			$("#events").prop("disabled", true);
			$("#events").val("default");
		}
		//weather events selection
		else if (spatialchoice == "6") {
			$("#state").prop("disabled", true);
			$("#state").val("default");
			$("#location").prop("disabled", true);
			$("#buffer").prop("disabled", true);
			$("#shapefile").prop("disabled", true);
			$("#prjfile").prop("disabled", true);
			$("#buffer_file").prop("disabled", true);
			$("#recenterbutton").prop("disabled", true);
			rectselector.setVisible(false);
			$("#event_type").prop("disabled", false);
			$("#event_type").val("default");
			$("#event_severity").prop("disabled", false);
			$("#event_severity").val("default");
			$("#events").prop("disabled", true);
			$("#events").val("default");
		}
	}
	function gotosurvey() {
		if (mapresponsejson && mapresponsejson.pointdata.length != 0) {
			document.addsurvey.sqlquerysurvey.value = mapresponsejson.sqlquery;
			document.addsurvey.dictusedsurvey.value = mapresponsejson.dictinfo.used;
			document.addsurvey.dictcategidsurvey.value = mapresponsejson.dictinfo.categid;
			document.addsurvey.tweetcount.value = mapresponsejson.pointdata.length;
			return true;
		}
		return false;
	}
	//recenter the selection rectangle
	function recenterrectangle() {
		var mapcenter = gmap.getCenter();
		var mapbounds = gmap.getBounds();
		var southwestcorner = new google.maps.LatLng(
				mapbounds.getSouthWest().lat()
						+ ((mapcenter.lat() - mapbounds.getSouthWest().lat()) / 2.0),
				mapbounds.getSouthWest().lng()
						+ ((mapcenter.lng() - mapbounds.getSouthWest().lng()) / 2.0));
		var northeastcorner = new google.maps.LatLng(
				mapbounds.getNorthEast().lat()
						- ((mapbounds.getNorthEast().lat() - mapcenter.lat()) / 2.0),
				mapbounds.getNorthEast().lng()
						- ((mapbounds.getNorthEast().lng() - mapcenter.lng()) / 2.0));
		var boundforrect = new google.maps.LatLngBounds(southwestcorner,
				northeastcorner);
		rectselector.setBounds(boundforrect);
	}
	//toggle dictionary selection
	function usedictionary() {
		if ($("#usedict").is(':checked')) {
			$("#dictionary").prop("disabled", false);
		} else {
			$("#dictionary").prop("disabled", true);
		}
	}
	//update weather event selection with new values
	function updateweatherevent() {
		if ($("#event_type").val() && $("#event_severity").val()) {
			if (!($("#events").val())) {
				$("#events").prop("disabled", false);
			}
			//update all weather events
			d3.select("#events").selectAll("option").remove();
			d3.select("#events").append("option").attr("value", "default")
					.attr("disabled", "disabled").attr("selected", "selected")
					.text("Select Event Areas");
			var parameter = "action=getweatherevents&from=" + $("#start").val()
					+ "&to=" + $("#end").val() + "&event_type="
					+ $("#event_type").val() + "&event_severity="
					+ $("#event_severity").val();
			var weatherdata = getdatafromserver(sedeserviceendpoint, parameter);
			d3.select("#events").selectAll("option").data(weatherdata).enter()
					.append("option").attr("value", function(d) {
						return d.id;
					}).text(function(d) {
						return d.area_desc;
					});
		}
	}
	//to download tweets in csv form
	function exportcsv() {
		if (mapresponsejson && mapresponsejson.pointdata.length != 0) {
			var csv = "Tweetid,Latitude,Longitude,Date,Tweet" + '\n';
			mapresponsejson.pointdata.forEach(function(d) {
				csv += "'" + d.id + "'" + ',' + d.y + ',' + d.x + ','
						+ moment(d.created_at).format("MM-DD-YYYY") + ',' + '"'
						+ d.tweet.replace(/"/g, '\\"') + '"' + '\n';
			});
			csv = 'data:application/csv;charset=utf-8,'
					+ encodeURIComponent(csv);
			var a = document.createElement('a');
			a.href = csv;
			a.target = '_blank';
			a.download = 'map.csv';
			a.id = 'csv';
			document.body.appendChild(a);
			a.click();
			var elem = document.getElementById('csv');
			elem.parentNode.removeChild(elem);
		}
	}
	//to download tweets in kml format
	function exportkml() {
		if (mapresponsejson && mapresponsejson.pointdata.length != 0) {
			var kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://earth.google.com/kml/2.2\">\n<Document>\n<name>Map</name>\n";
			mapresponsejson.pointdata.forEach(function(d) {
				kml += "<Placemark>\n<name></name>\n<description>"
						+ escape(d.tweet) + "\n"
						+ moment(d.created_at).format("MM-DD-YYYY")
						+ "</description>\n<Point>\n<coordinates>" + d.x + ","
						+ d.y + "," + '0'
						+ "</coordinates>\n</Point>\n</Placemark>\n";
			});
			kml += "</Document>\n</kml>";
			kml = 'data:Application/octet-stream,' + encodeURIComponent(kml);
			var a = document.createElement('a');
			a.href = kml;
			a.target = '_blank';
			a.download = 'map.kml';
			a.id = 'kml';
			document.body.appendChild(a);
			a.click();
			var elem = document.getElementById('kml');
			elem.parentNode.removeChild(elem);
		}
	}
</script>
</head>
<body onload="initialize()">
	<!-- Navigation bar -->
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
	<div class="container-fluid fill">
		<div class="row fill">
			<div class="col-md-8 main fill" id="map"></div>
			<div class="col-md-4 col-md-offset-8 controls sidebar">
				<div class="row">
					<div class="col-md-12">
						<h3>Spatial Filter</h3>
						<div class="row">
							<div class="col-md-3">
								<label><input type="radio" name="spatialradio"
									checked="checked" value="1" onclick="spatialselectchange()">
									All Tweets</label>
							</div>
						</div>
						<br>
						<div class="row">
							<div class="col-md-3">
								<label><input type="radio" name="spatialradio" value="2"
									onclick="spatialselectchange()"> US States</label>
							</div>
							<div class="col-md-6">
								<select class="form-control" id="state" disabled="disabled">
									<option value="default" disabled selected>Select State</option>
								</select>
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-3">
								<label><input type="radio" name="spatialradio" value="3"
									onclick="spatialselectchange()"> Location</label>
							</div>
							<div class="col-md-4">
								<input type="text" class="form-control" id="location"
									disabled="disabled" onblur="searchplace()">
							</div>
							<div class="col-md-3">Buffer(miles):</div>
							<div class="col-md-2">
								<input type="text" class="form-control" id="buffer"
									disabled="disabled" value="1">
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-3">
								<label><input type="radio" name="spatialradio" value="4"
									onclick="spatialselectchange()"> Upload shp</label>
							</div>
							<div class="col-md-4">
								<input type="file" class="form-control" id="shapefile"
									disabled="disabled">
							</div>
							<div class="col-md-3">Buffer(miles):</div>
							<div class="col-md-2">
								<input type="text" class="form-control" id="buffer_file"
									disabled="disabled" value="0">
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-3">
								<span style="padding-left: 17%;"><b>Upload Prj</b></span>
							</div>
							<div class="col-md-4">
								<input type="file" class="form-control" id="prjfile"
									disabled="disabled">
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-5">
								<label><input type="radio" name="spatialradio" value="5"
									onclick="spatialselectchange()"> Rectangle Selection</label>
							</div>
							<div class="col-md-3">
								<button type="button" class="btn btn-primary"
									id="recenterbutton" onclick="recenterrectangle()"
									disabled="disabled">re-center</button>
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-4">
								<label><input type="radio" name="spatialradio" value="6"
									onclick="spatialselectchange()"> Weather Events</label>
							</div>
							<div class="col-md-4">
								<select class="form-control" id="event_type" disabled="disabled"
									onchange="updateweatherevent()">
									<option value="default" disabled selected>Select Event
										Type</option>
									<option>Tornado Warning</option>
									<option>Flood Warning</option>
									<option>Flash Flood Warning</option>
									<option>Severe Thunderstorm Watch</option>
								</select>
							</div>
							<div class="col-md-4">
								<select class="form-control" id="event_severity"
									disabled="disabled" onchange="updateweatherevent()">
									<option value="default" disabled selected>Select
										Severity</option>
									<option value="Moderate">Moderate</option>
									<option value="Severe">Severe</option>
									<option value="Extreme">Extreme</option>
									<option value="Minor">Minor</option>
									<option value="All">All</option>
								</select>
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-4">
								<span style="padding-left: 13%;"><b>Event areas</b></span>
							</div>
							<div class="col-md-8">
								<select class="form-control" id="events" disabled="disabled">
									<option value="default" disabled selected>Select Event
										Areas</option>
								</select>
							</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<h3>Temporal Filter</h3>
						<div class="row">
							<div class="col-md-12">
								<div id="slider"></div>
							</div>
						</div>
						<br>
						<div class="row">
							<div class="col-md-6">
								<input type="date" id="start" onchange="lowerdatechange()"
									min="2015-05-11" max="2016-05-20">
							</div>
							<div class="col-md-6">
								<input type="date" id="end" onchange="upperdatechange()"
									min="2015-05-11" max="2016-05-20">
							</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<h3>Textual Filter</h3>
						<div class="row">
							<div class="col-md-12">
								<div class="row">
									<div class="col-md-2">Top</div>
									<div class="col-md-3">
										<select class="form-control" id="topwords"
											onchange="updatetopwordstable()">
											<option value="5" selected="selected">5</option>
											<option value="10">10</option>
											<option value="15">15</option>
										</select>
									</div>
									<div class="col-md-7">
										<div class="checkbox">
											<label><input type="checkbox" id="hidecommon"
												onchange="updatetopwordstable()">Hide common words</label>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-md-6 col-md-offset-2">
								<table class="table table-striped" id="topwordstable">
									<thead>
										<tr>
											<th>Word</th>
											<th>Count</th>
										</tr>
									</thead>
								</table>
							</div>
						</div>
						<div class="row">
							<div class="col-md-12">
								<div class="row">
									<div class="col-md-6">
										<input type="text" class="form-control" id="wordsearch">
									</div>
									<div class="col-md-6">
										<button type="button" class="btn btn-primary"
											onclick="searchwords()">Add Word</button>
									</div>
								</div>
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-6 col-md-offset-2">
								<table class="table table-striped" id="userselectedtable">
									<thead>
										<tr>
											<th>Word</th>
											<th>Remove</th>
										</tr>
									</thead>
								</table>
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-md-4">
								<label><input type="checkbox" id="usedict"
									onchange="usedictionary()"> Use Dictionary</label>
							</div>
							<div class="col-md-4">
								<select class="form-control" id="dictionary" disabled="disabled">
								</select>
							</div>
						</div>
					</div>
				</div>

				<div class="row">
					<div class="col-md-12">
						<h3>Display Options</h3>
						<div class="row">
							<div class="col-md-12">
								<label class="radio-inline"><input type="radio"
									name="disp" value="Point" checked="checked"
									onclick="displaymap()">Points</label> <label
									class="radio-inline"><input type="radio" name="disp"
									value="Hex" onclick="displaymap()">HexMap</label> <label
									class="radio-inline"> <input type="radio" name="disp"
									value="Heat" onclick="displaymap()">HeatMap
								</label>
							</div>
						</div>
					</div>
				</div>
				<br>
				<div class="row">
					<div class="col-md-12">
						<h3>Export Options</h3>
						<div class="row">
							<div class="col-md-2">
								<button type="button" class="btn btn-primary"
									onclick="exportcsv()">CSV</button>
							</div>
							<div class="col-md-2">
								<button type="button" class="btn btn-primary"
									onclick="exportkml()">KML</button>
							</div>
						</div>
					</div>
				</div>
				<br> <br>
				<div class="row">
					<div class="col-md-6">
						<form action="survey_tables.jsp" name="addsurvey"
							onsubmit="return gotosurvey()" method="post">
							<input type="hidden" name="emailsurvey"
								value="${fn:escapeXml(requestScope.email)}"> <input
								type="hidden" name="sqlquerysurvey" value=""><input
								type="hidden" name="dictusedsurvey" value=""><input
								type="hidden" name="dictcategidsurvey" value="">
							<!-- <input
								type="hidden" name="spatialdat" value=""> <input
								type="hidden" name="temporaldat" value=""> <input
								type="hidden" name="textualdat" value=""> -->
							<input type="hidden" name="tweetcount" value=""> <input
								type="submit" class="btn btn-primary" value="Create Code Book">
						</form>
					</div>
					<div class="col-md-6">
						<button type="button" class="btn btn-primary"
							onclick="update_data()">Update</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
