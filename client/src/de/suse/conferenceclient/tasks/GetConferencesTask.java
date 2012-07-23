/**
 * 
 */
package de.suse.conferenceclient.tasks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.suse.conferenceclient.app.HTTPWrapper;
import de.suse.conferenceclient.models.Conference;

import android.os.AsyncTask;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class GetConferencesTask extends AsyncTask<Void, Void, ArrayList<Conference>> {
	public interface ConferenceListListener {
		public void handled(ArrayList<Conference> conferences);
	}
	
	private ConferenceListListener mListener;
	private String mUrl;
	
	public GetConferencesTask(String url, ConferenceListListener listener) {
		mListener = listener;
		mUrl = url + "/conferences.json";
	}
	
	@Override
	protected ArrayList<Conference> doInBackground(Void... params) {
		JSONObject reply = null;
		ArrayList<Conference> ret = new ArrayList<Conference>();
		
		try {
			reply = HTTPWrapper.get(mUrl);
			JSONArray conferences = reply.getJSONArray("conferences");
			int len = conferences.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonCon = conferences.getJSONObject(i);
				Conference newCon = new Conference();
				newCon.setGuid(jsonCon.getString("guid"));
				newCon.setName(jsonCon.getString("name"));
				newCon.setDescription(jsonCon.getString("description"));
				newCon.setYear(jsonCon.getInt("year"));
				newCon.setName(jsonCon.getString("name"));
				newCon.setDateRange(jsonCon.getString("date_range"));
				newCon.setUrl(jsonCon.getString("url"));
				ret.add(newCon);
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
    	try {Thread.sleep(13000);} catch(Exception e){};

		return ret;
	}

	protected void onPostExecute(ArrayList<Conference> conferences) {
		mListener.handled(conferences);
	}
}
