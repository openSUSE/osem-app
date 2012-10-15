/*******************************************************************************
 * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Matt Barringer <matt@incoherent.de> - initial API and implementation
 ******************************************************************************/
package de.incoherent.suseconferenceclient.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Convenience wrapper around HTTP calls.
 *   
 * TODO This probably doesn't work with HTTPS
 */

public class HTTPWrapper {
	
	public static Bitmap getImage(String url) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		Log.d("SUSEConferences", "Get: " + url);
		HttpResponse response = client.execute(get);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode >= 200 && statusCode <= 299) {
			final HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            InputStream inputStream = null;
	            try {
	                inputStream = entity.getContent(); 
	                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	                return bitmap;
	            } finally {
	                if (inputStream != null) {
	                    inputStream.close();  
	                }
	                entity.consumeContent();
	            }
	        } else {
	        	throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
	        }
		} else {
        	throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
        }
	}
	
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
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		DefaultHttpClient client = new DefaultHttpClient();
		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

		HttpGet get = new HttpGet(url);
		HttpResponse response = httpClient.execute(get);
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
