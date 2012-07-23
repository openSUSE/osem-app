/**
 * 
 */
package de.suse.conferenceclient.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Room {
	private String mName = "";
	private String mGuid = "";
	private String mDescription = "";
	
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
	public String getGuid() {
		return mGuid;
	}
	public void setGuid(String guid) {
		this.mGuid = guid;
	}
	public String getDescription() {
		return mDescription;
	}
	public void setDescription(String description) {
		this.mDescription = description;
	}
	
	public static HashMap<String, Room> parseJSON(JSONObject json) throws JSONException {
		HashMap<String, Room> roomsMap = new HashMap<String, Room>();
		JSONArray rooms = json.getJSONArray("rooms");
		int roomsLen = rooms.length();
		for (int i = 0; i < roomsLen; i++) {
			JSONObject room = rooms.getJSONObject(i);
			Room newRoom = new Room();
			newRoom.setGuid(room.getString("guid"));
			newRoom.setName(room.getString("name"));
			newRoom.setDescription(room.getString("description"));
			roomsMap.put(newRoom.getGuid(), newRoom);
		}
		
		return roomsMap;
	}

}
