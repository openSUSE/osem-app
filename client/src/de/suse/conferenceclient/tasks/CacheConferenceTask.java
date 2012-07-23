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
		HashMap<String, Room> roomsMap = new HashMap<String, Room>();
		
		try {
			JSONObject eventTypesReply = HTTPWrapper.get(eventTypesUrl);
			JSONArray eventTypes = eventTypesReply.getJSONArray("event_types");
			int eventTypesLen = eventTypes.length();
			for (int i = 0; i < eventTypesLen; i++) {
				JSONObject type = eventTypes.getJSONObject(i);
				eventTypesMap.put(type.getString("guid"), type.getString("name"));
			}
			
			JSONObject roomsReply = HTTPWrapper.get(roomsUrl);
			JSONArray rooms = roomsReply.getJSONArray("rooms");
			int roomsLen = roomsReply.length();
			for (int i = 0; i < roomsLen; i++) {
				JSONObject room = rooms.getJSONObject(i);
				Room newRoom = new Room();
				newRoom.setGuid(room.getString("guid"));
				newRoom.setName(room.getString("name"));
				newRoom.setDescription(room.getString("description"));
				roomsMap.put(newRoom.getGuid(), newRoom);
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


		return null;
	}

	protected void onPostExecute() {
		
	}

}
