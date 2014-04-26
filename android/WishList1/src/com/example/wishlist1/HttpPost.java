package com.example.wishlist1;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpPost {
	public static String ERROR = "Error.";
	//private static String URL_ADD_WISH = "http://ec2-54-186-87-220.us-west-2.compute.amazonaws.com:3000/mobile/addWish/";
	
	public static abstract class SendableObject {
		public abstract String toJSON();
		public String url; //The url to which the object should be sent.
		protected String kvPair(String key, String value) {
			return "\""+key+"\":\""+value+"\"";
		}
	}
	
	public static String post(SendableObject stuffToPost, ConnectivityManager connMgr) {
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			try {
				return doThePost(stuffToPost);
			} catch (IOException e) {
				return ERROR;
			}
		} else {
			return ERROR;
		}
	}
	
	private static String doThePost(SendableObject stuffToPost) throws IOException {
		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.
		// TODO: This length is probably too small. Is there a better way to do this?
		int len = 500;

		try {
			// The URL for the post.
			URL url = new URL(stuffToPost.url);
			// Define the connection and set initial parameters.
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			// Build the body.
			conn.setRequestProperty("Content-Type","application/json");
			conn.setRequestProperty("Accept", "application/json");
			// Start the query.
			conn.connect();
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(stuffToPost.toJSON().getBytes("UTF-8"));
			outputStream.close();
			int response = conn.getResponseCode();
			Log.d("Tatewty:response", "The response is: " + response);
			is = conn.getInputStream();

			// Convert the InputStream into a string
			String contentAsString = readIt(is, len);
			return contentAsString;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Reads an InputStream and converts it to a String.
	private static String readIt(InputStream stream, int len) throws IOException,
			UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}

}
