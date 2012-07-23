/**
 * 
 */
package de.suse.conferenceclient.tasks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.suse.conferenceclient.app.HTTPWrapper;
import de.suse.conferenceclient.models.Conference;
import de.suse.conferenceclient.models.Room;
import de.suse.conferenceclient.models.Speaker;
import android.os.AsyncTask;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class CacheConferenceTask extends AsyncTask<Void, Void, Void> {
	public interface CacheConferenceListener {
		public void onConferenceCached(Conference conference);
	}
	
	private CacheConferenceListener mListener;
	private Conference mConference;
	
	public CacheConferenceTask(Conference conference, CacheConferenceListener listener) {
		this.mListener = listener;
		this.mConference = conference;
	}

	@Override
	protected Void doInBackground(Void... params) {
		String url = mConference.getUrl();
		String eventsUrl = url + "/events.json";
		String eventTypesUrl = url + "/event_types.json";
		String roomsUrl = url + "/rooms.json";
		String speakersUrl = url + "/speakers.json";
		String tracksUrl = url + "/tracks.json";
		String venueUrl = url + "/venue.json";
		HashMap<String, String> eventTypesMap = new HashMap<String, String>();
		
		try {
			JSONObject eventTypesReply = HTTPWrapper.get(eventTypesUrl);
			JSONArray eventTypes = eventTypesReply.getJSONArray("event_types");
			int eventTypesLen = eventTypes.length();
			for (int i = 0; i < eventTypesLen; i++) {
				JSONObject type = eventTypes.getJSONObject(i);
				eventTypesMap.put(type.getString("guid"), type.getString("name"));
			}
			
			JSONObject roomsReply = HTTPWrapper.get(roomsUrl);
			HashMap<String, Room> roomsMap = Room.parseJSON(roomsReply);
			
			JSONObject speakerReply = HTTPWrapper.get(speakersUrl);
			HashMap<String, Speaker> speakerMap = Speaker.parseJSON(speakerReply);
			
			
			
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


		return null;
	}

	protected void onPostExecute() {
		
	}

}
