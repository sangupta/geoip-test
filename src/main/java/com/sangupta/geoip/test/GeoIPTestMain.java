package com.sangupta.geoip.test;

import java.io.File;

import org.eclipse.jetty.server.Server;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.CheckUtils;
import com.sangupta.jerry.util.StringUtils;

public class GeoIPTestMain {

	private static final int HTTP_PORT = 8300;
	
	public static void main(String[] args) throws Exception {
		if(AssertUtils.isEmpty(args) || args.length != 2) {
			usage();
			return;
		}
		
		String probablePort = args[0];
		String fileName = args[1]; // "GeoLite2-City.mmdb";
		
		final int port = StringUtils.getIntValue(probablePort, HTTP_PORT);
		final File dbFile = new File(fileName);
		CheckUtils.checkFileExists(dbFile);
		
		final long startTime = System.currentTimeMillis();
		
    	// create the server
    	Server server = new Server(port);
    	
		// set the request handler
        server.setHandler(new GeoIPHandler(dbFile));
  
        // start the server
        server.start();
        
        final long endTime = System.currentTimeMillis();
        System.out.println("GeoIP test server started in " + (endTime - startTime) + " milliseconds, on port " + port + ".");
        
        // join to server thread
        server.join();
	}

	private static void usage() {
		System.out.println("Usage: $ java -jar geoip-test-server.jar <port> <dbfile>");
		System.out.println();
		System.out.println("\tport\tThe HTTP port number to run the server on, default is 4100");
		System.out.println();
		System.out.println("\tdbFile\tThe MaxMind GeoIP-2 database file path");
	}
	
}
