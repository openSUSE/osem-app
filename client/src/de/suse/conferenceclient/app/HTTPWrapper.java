package de.suse.conferenceclient.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Convenience wrapper around HTTP calls.
 *   
 * @author Matt Barringer <mbarringer@suse.de>
 *
 * TODO This probably doesn't work with HTTPS
 */
public class HTTPWrapper {
	public static String getRawText(String url) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		Log.d("SUSEConferences", "Get: " + url);
		HttpResponse response = client.execute(get);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode >= 200 && statusCode <= 299) {
			StringBuilder builder = new StringBuilder();
			HttpEntity responseEntity = response.getEntity();
			InputStream content = responseEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			return builder.toString();
		} else {
			throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
		}

	}
	public static JSONObject get(String url) throws IllegalStateException, SocketException, 
							UnsupportedEncodingException, IOException, JSONException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		
		HttpResponse response = client.execute(get);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode >= 200 && statusCode <= 299) {
			return HTTPWrapper.parseResponse(response);
		} else {
			throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
		}
	}
	
	private static JSONObject parseResponse(HttpResponse response) throws IllegalStateException, IOException, UnsupportedEncodingException, JSONException {
		StringBuilder builder = new StringBuilder();
		HttpEntity responseEntity = response.getEntity();
		InputStream content = responseEntity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		if (builder.length() == 0) {
			return null;
		} else {
			JSONObject ret = new JSONObject(builder.toString());
			return ret;
		}
	}
}
