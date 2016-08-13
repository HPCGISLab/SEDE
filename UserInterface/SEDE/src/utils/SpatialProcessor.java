package utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.nocrala.tools.gis.data.esri.shapefile.ShapeFileReader;
import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;
import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

import com.fasterxml.jackson.databind.ObjectMapper;

import data.EPSGPojo;
import database.Database;

public class SpatialProcessor {
	
	public static String processSpatialRequest(HttpServletRequest request) {
		String responseJson = null;
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			String choice = request.getParameter("choice");
			if (choice.equals("1")) {
				responseJson = processAllTweetsSpatialData(request);
			} else if (choice.equals("2")) {
				responseJson = processStateTweetsSpatialData(request);
			} else if (choice.equals("3")) {
				responseJson = processPlaceTweetsSpatialData(request);
			} else if (choice.equals("5")) {
				responseJson = processRectTweetsSpatialData(request);
			}else if (choice.equals("6")) {
				responseJson = processWeatherTweetsSpatialData(request);
			}
		} else {
			responseJson = processShapefileTweetsSpatialData(request);
		}
		return responseJson;
	}

	private static String processWeatherTweetsSpatialData(
			HttpServletRequest request) {
		long eventid=Long.parseLong(request.getParameter("eventid"));
		String[] placedetails=Database.getWeatherPlaceDetails(eventid);
		String boxdat = placedetails[1].replace("BOX(", "").replace(")", "")
				.replaceAll(" ", ",");
		JSONObject respobj = new JSONObject();
		respobj.put("polygon", placedetails[0]);
		respobj.put("bounds", boxdat);
		return respobj.toJSONString();
	}

	private static String processShapefileTweetsSpatialData(
			HttpServletRequest request) {
		String resp = null;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		Geometry polygon = null;
		String buffer = null;
		String epsg=null;
		double bufferval=0;
		try {
			List<FileItem> items = upload.parseRequest(request);
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();
				if (item.isFormField()) {
					if (item.getFieldName()!=null&&item.getFieldName().equals("buffer")) {
						buffer = item.getString();
					}
				} else {
					if(item.getFieldName()!=null&&item.getFieldName().equals("shapefile"))
					    polygon = readShapeFile(item.getInputStream());
					else if(item.getFieldName()!=null&&item.getFieldName().equals("prjfile"))
						epsg=getEPSG(item.getInputStream());
				}
			}
			if(polygon==null){
				resp="error";
			}
			else{
				String polygonstring=null;
				if(buffer!=null&&!buffer.isEmpty()){
					bufferval=Double.parseDouble(buffer)*1609.34;
				}
				if(epsg!=null&&!epsg.equals("4326")){
					polygon.setSrid(Integer.parseInt(epsg));
					polygonstring=Database.transform(polygon.toString(),4326);
				}
				else{
					polygon.setSrid(4326);
					polygonstring=polygon.toString();
				}
				String[] placedat = Database.getplacepolygon(polygonstring, bufferval);
				String boxdat = placedat[1].replace("BOX(", "").replace(")", "")
						.replaceAll(" ", ",");
				JSONObject respobj = new JSONObject();
				respobj.put("polygon", placedat[0]);
				respobj.put("bounds", boxdat);
				resp=respobj.toJSONString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			resp = "error";
		}
		return resp;
	}

	private static String getEPSG(InputStream inputStream) {
		String prjstring=null;
		String epsg=null;
		try {
			prjstring=IOUtils.toString(inputStream,"utf-8");
			URL epsgurl=new URL("http://prj2epsg.org/search.json?mode=wkt&terms="+prjstring);
			InputStream resp=epsgurl.openStream();
			String response=IOUtils.toString(resp,"utf-8");
			ObjectMapper mapper = new ObjectMapper();
			EPSGPojo epoj=mapper.readValue(response, EPSGPojo.class);
			if(epoj.getCodes().size()!=0){
				epsg= epoj.getCodes().get(0).getCode();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return epsg;
	}

	private static Geometry readShapeFile(InputStream inputStream)
			throws InvalidShapeFileException, IOException {
		Geometry response = null;
		ValidationPreferences pref=new ValidationPreferences();
		pref.setAllowUnlimitedNumberOfPointsPerShape(true);
		pref.setAllowBadContentLength(true);
		pref.setAllowBadRecordNumbers(true);
		ShapeFileReader r = new ShapeFileReader(inputStream,pref);
		ShapeFileHeader h = r.getHeader();
		AbstractShape s;
		ArrayList<Geometry> allshapes = new ArrayList<Geometry>();
		while ((s = r.next()) != null) {
			switch (s.getShapeType()) {
			case POINT:
				PointShape aPoint = (PointShape) s;
				Point point = new Point(aPoint.getX(), aPoint.getY());
				point.setSrid(4326);
				allshapes.add(point);
				break;
			case POLYGON:
				PolygonShape aPolygon = (PolygonShape) s;
				LinearRing lin[] = new LinearRing[aPolygon.getNumberOfParts()];
				for (int i = 0; i < aPolygon.getNumberOfParts(); i++) {
					PointData[] pnts = aPolygon.getPointsOfPart(i);
					Point pt[] = new Point[pnts.length];
					for (int j = 0; j < pnts.length; j++) {
						pt[j] = new Point(pnts[j].getX(), pnts[j].getY());
					}
					lin[i] = new LinearRing(pt);
				}
				Polygon pg = new Polygon(lin);
				allshapes.add(pg);
				break;
			case POLYLINE:
				PolylineShape aPolyline = (PolylineShape) s;
				ArrayList<Point> pointlist = new ArrayList<Point>();
				for (int i = 0; i < aPolyline.getNumberOfParts(); i++) {
					PointData[] pnts = aPolyline.getPointsOfPart(i);
					for (int j = 0; j < pnts.length; j++) {
						pointlist
								.add(new Point(pnts[j].getX(), pnts[j].getY()));
					}
				}
				LineString ls = new LineString(
						pointlist.toArray(new Point[pointlist.size()]));
				allshapes.add(ls);
				break;
			default:
				response = null;
			}
		}
		if (allshapes.size() == 1) {
			response = allshapes.get(0);
		} else if (allshapes.size() > 1) {
			if (allshapes.get(0) instanceof Point) {
				MultiPoint mp = new MultiPoint(
						allshapes.toArray(new Point[allshapes.size()]));
				response = mp;
			} else if (allshapes.get(0) instanceof Polygon) {
				MultiPolygon mp = new MultiPolygon(
						allshapes.toArray(new Polygon[allshapes.size()]));
				response = mp;
			} else if (allshapes.get(0) instanceof LineString) {
				MultiLineString mp = new MultiLineString(
						allshapes.toArray(new LineString[allshapes.size()]));
				response = mp;
			}
		}
		return response;
	}

	private static String processRectTweetsSpatialData(
			HttpServletRequest request) {
		String bounds = request.getParameter("rectbounds");
		String polygonstring = Database.getboundingboxaspolygon(bounds);
		JSONObject respobj = new JSONObject();
		respobj.put("polygon", polygonstring);
		respobj.put("bounds", bounds);
		return respobj.toJSONString();
	}

	private static String processPlaceTweetsSpatialData(
			HttpServletRequest request) {
		String coordinates = request.getParameter("coordinates");
		String geogdata = "SRID=4326;POINT(" + coordinates + ")";
		String buffer = request.getParameter("buffer");
		if (buffer == null || buffer.isEmpty() || buffer.equals("0")) {
			buffer = "1";
		}
		double bufferval = Double.parseDouble(buffer) * 1609.34;
		String[] placedat = Database.getplacepolygon(geogdata, bufferval);
		String boxdat = placedat[1].replace("BOX(", "").replace(")", "")
				.replaceAll(" ", ",");
		JSONObject respobj = new JSONObject();
		respobj.put("polygon", placedat[0]);
		respobj.put("bounds", boxdat);
		return respobj.toJSONString();
	} 

	private static String processStateTweetsSpatialData(
			HttpServletRequest request) {
		String state = request.getParameter("state");
		String[] statedat = Database.getstatepolygon(state);
		String boxdat = statedat[1].replace("BOX(", "").replace(")", "")
				.replaceAll(" ", ",");
		JSONObject respobj = new JSONObject();
		respobj.put("polygon", statedat[0]);
		respobj.put("bounds", boxdat);
		return respobj.toJSONString();
	}

	private static String processAllTweetsSpatialData(HttpServletRequest request) {
		String bounds = request.getParameter("boundingbox");
		String polygonstring = Database.getboundingboxaspolygon(bounds);
		JSONObject respobj = new JSONObject();
		respobj.put("polygon", polygonstring);
		respobj.put("bounds", bounds);
		return respobj.toJSONString();
	}
}
