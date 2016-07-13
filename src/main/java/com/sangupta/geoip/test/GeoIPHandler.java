package com.sangupta.geoip.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.sangupta.jerry.constants.HttpMimeType;
import com.sangupta.jerry.constants.HttpStatusCode;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ResponseUtils;

public class GeoIPHandler extends AbstractHandler {
	
	private DatabaseReader reader;
	
	private Gson gson;
	
	public GeoIPHandler(File geoIPDatabaseFile) throws IOException {
		this.reader = new DatabaseReader.Builder(geoIPDatabaseFile).withCache(new CHMCache()).build();
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		this.gson = gsonBuilder.setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String ipAddress = request.getParameter("ipAddress");
		
		if(AssertUtils.isEmpty(ipAddress)) {
			// via a reverse proxy header
			ipAddress = request.getHeader("X-Forwarded-For");
		}
		
		if(AssertUtils.isEmpty(ipAddress)) {
			// direct client
			ipAddress = request.getRemoteAddr();
		}
		
		try {
			InetAddress inetAddress = InetAddress.getByName(ipAddress);
			Map<String, Object> map = new HashMap<>();
			
			map.put("ipAddress", ipAddress);
			map.put("cityResponse", reader.city(inetAddress));
//			map.put("countryResponse", reader.country(inetAddress));
//			map.put("domainResponse", reader.domain(inetAddress));
//			map.put("enterpriseResponse", reader.enterprise(inetAddress));
//			map.put("ispResponse", reader.isp(inetAddress));
			
			String json = this.gson.toJson(map);

			response.setStatus(HttpStatusCode.OK);
			ResponseUtils.sendResponse(response, json, HttpMimeType.JSON);
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			
			response.setStatus(HttpStatusCode.INTERNAL_SERVER_ERROR);
			ResponseUtils.sendResponse(response, sw.toString(), HttpMimeType.TEXT_PLAIN);
			
		} finally {
			baseRequest.setHandled(true);
		}
	}

}
