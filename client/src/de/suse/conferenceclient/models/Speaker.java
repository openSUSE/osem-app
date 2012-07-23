/**
 * 
 */
package de.suse.conferenceclient.models;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Speaker {
	private String mGuid = "";
	private String mName = "";
	private String mCompany = "";
	private String mBiography = "";
	private String mPhotoGuid = "";
	
	public String getGuid() {
		return mGuid;
	}
	public void setGuid(String guid) {
		mGuid = guid;
	}
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		mName = name;
	}
	public String getCompany() {
		return mCompany;
	}
	public void setCompany(String company) {
		mCompany = company;
	}
	public String getBiography() {
		return mBiography;
	}
	public void setBiography(String biography) {
		mBiography = biography;
	}
	public String getPhotoGuid() {
		return mPhotoGuid;
	}
	public void setPhotoGuid(String photoGuid) {
		mPhotoGuid = photoGuid;
	}
	
	public static HashMap<String, Speaker> parseJSON(JSONObject json) throws JSONException {
		HashMap<String, Speaker> speakerMap = new HashMap<String, Speaker>();
		JSONArray speakers = json.getJSONArray("speakers");
		int speakersLen = speakers.length();
		for (int i = 0; i < speakersLen; i++) {
			JSONObject speaker = speakers.getJSONObject(i);
			Speaker newSpeaker= new Speaker();
			newSpeaker.setGuid(speaker.getString("guid"));
			newSpeaker.setName(speaker.getString("name"));
			newSpeaker.setCompany(speaker.getString("company"));
			newSpeaker.setBiography(speaker.getString("biography"));
			speakerMap.put(newSpeaker.getGuid(), newSpeaker);
		}
		return speakerMap;
	}
}
