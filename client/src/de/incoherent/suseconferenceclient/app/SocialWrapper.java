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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import de.incoherent.suseconferenceclient.Config;
import de.incoherent.suseconferenceclient.models.SocialItem;
import de.incoherent.suseconferenceclient.R;

public class SocialWrapper {
	public static ArrayList<SocialItem> getTwitterItems(Context context, String tag, int maximum) {
		String twitterSearch = "http://search.twitter.com/search.json?q=" + tag;
		ArrayList<SocialItem> socialItems = new ArrayList<SocialItem>();
		
		// TODO Android 2.2 thinks that "Wed, 19 Sep 2012 16:35:43 +0000" is invalid
		// with this formatter
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.twitter_icon);
		try {
			JSONObject result = HTTPWrapper.get(twitterSearch);
			JSONArray items = result.getJSONArray("results");
			int len = items.length();
			if ((len > 0) && (maximum > 0) && (len > maximum))
				len = maximum;
			
			for (int i = 0; i < len; i++) {
				JSONObject jsonItem = items.getJSONObject(i);
				Bitmap image = HTTPWrapper.getImage(jsonItem.getString("profile_image_url"));			
				Date formattedDate = new Date();
				try {
					formattedDate = formatter.parse(jsonItem.getString("created_at"));
				} catch (ParseException e) {
					Log.d("SUSEConferences", "Invalid date string: " + jsonItem.getString("created_at"));
					e.printStackTrace();
				}
				String user = jsonItem.getString("from_user");
				SocialItem newItem = new SocialItem(SocialItem.TWITTER,
													user,
													jsonItem.getString("text"),
													formattedDate,
													DateUtils.formatDateTime(context,
												 	         formattedDate.getTime(),
												 	         DateUtils.FORMAT_SHOW_WEEKDAY
												 	        |DateUtils.FORMAT_NUMERIC_DATE
															|DateUtils.FORMAT_SHOW_TIME
															|DateUtils.FORMAT_SHOW_DATE),
													image,
													icon);
				String link = "http://twitter.com/" + user + "/status/" + jsonItem.getString("id_str");
				newItem.setLink(link);
				socialItems.add(newItem);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return socialItems;
	}

	public static ArrayList<SocialItem> getGooglePlusItems(Context context, String tag, int maximum) {
		String twitterSearch = "https://www.googleapis.com/plus/v1/activities?orderBy=recent&query=" + tag + "&key=" + Config.PLUS_KEY;
		Log.d("SUSEConferences", "Google search: " + twitterSearch);
		ArrayList<SocialItem> socialItems = new ArrayList<SocialItem>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.google_icon);

		try {
			JSONObject result = HTTPWrapper.get(twitterSearch);
			JSONArray items = result.getJSONArray("items");
			int len = items.length();
			if ((len > 0) && (maximum > 0) && (len > maximum))
				len = maximum;

			for (int i = 0; i < len; i++) {
				JSONObject jsonItem = items.getJSONObject(i);
				JSONObject actorItem = jsonItem.getJSONObject("actor");
				JSONObject imageItem = actorItem.getJSONObject("image");
				JSONObject objectItem = jsonItem.getJSONObject("object");
				Bitmap image = HTTPWrapper.getImage(imageItem.getString("url"));
				String content = Html.fromHtml(objectItem.getString("content")).toString();
				Date formattedDate = new Date();
				try {
					formattedDate = formatter.parse(jsonItem.getString("published"));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				SocialItem newItem = new SocialItem(SocialItem.GOOGLE,
													actorItem.getString("displayName"),
													content,
													formattedDate,
													DateUtils.formatDateTime(context,
												 	         formattedDate.getTime(),
												 	         DateUtils.FORMAT_SHOW_WEEKDAY
												 	        |DateUtils.FORMAT_NUMERIC_DATE
															|DateUtils.FORMAT_SHOW_TIME
															|DateUtils.FORMAT_SHOW_DATE),
													image,
													icon);
				newItem.setLink(jsonItem.getString("url"));
				socialItems.add(newItem);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return socialItems;
	}
}
