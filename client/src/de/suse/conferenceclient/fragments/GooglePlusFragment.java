/**
 * 
 */
package de.suse.conferenceclient.fragments;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.Config;
import de.suse.conferenceclient.R;
import de.suse.conferenceclient.adapters.SocialItemAdapter;
import de.suse.conferenceclient.app.HTTPWrapper;
import de.suse.conferenceclient.models.SocialItem;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class GooglePlusFragment extends SherlockListFragment {
	private String mSearchTag;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void setSearch(String searchTag) {
		mSearchTag = searchTag;
		GetSearchTask task = new GetSearchTask();
		task.execute(searchTag);
	}
	
	private class GetSearchTask extends AsyncTask<String, Void, List<SocialItem>> {
		@Override
		protected List<SocialItem> doInBackground(String... params) {
			String searchTag = params[0];
			String twitterSearch = "https://www.googleapis.com/plus/v1/activities?orderBy=recent&query=" + searchTag + "&key=" + Config.PLUS_KEY;
			List<SocialItem> socialItems = new ArrayList<SocialItem>();
			
			try {
				JSONObject result = HTTPWrapper.get(twitterSearch);
				JSONArray items = result.getJSONArray("items");
				int len = items.length();
				for (int i = 0; i < len; i++) {
					JSONObject jsonItem = items.getJSONObject(i);
					JSONObject actorItem = jsonItem.getJSONObject("actor");
					JSONObject imageItem = actorItem.getJSONObject("image");
					JSONObject objectItem = jsonItem.getJSONObject("object");
					Bitmap image = HTTPWrapper.getImage(imageItem.getString("url"));
					String content = Html.fromHtml(objectItem.getString("content")).toString();
					SocialItem newItem = new SocialItem(actorItem.getString("displayName"),
														content,
														jsonItem.getString("published"),
														image);

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
		
		protected void onPostExecute(List<SocialItem> items) {
			SocialItemAdapter adapter = new SocialItemAdapter(getActivity(), R.layout.social_item, items);
			setListAdapter(adapter);
		}
	}
}
