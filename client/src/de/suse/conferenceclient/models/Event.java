/**
 * 
 */
package de.suse.conferenceclient.models;

import java.util.Date;
import java.util.List;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Event {
	private String mGuid = "";
	private Date mDate;
	private int mLength;
	private String mLanguage = "";
	private String mAbstract = "";
	private String mUrlList = "";
	private List<Speaker> mSpeakers;
	private Room mRoom;
	private String mEventType;
	
	public String getGuid() {
		return mGuid;
	}
	public void setGuid(String guid) {
		mGuid = guid;
	}
	public Date getDate() {
		return mDate;
	}
	public void setDate(Date date) {
		mDate = date;
	}
	public int getLength() {
		return mLength;
	}
	public void setLength(int length) {
		mLength = length;
	}
	public String getLanguage() {
		return mLanguage;
	}
	public void setLanguage(String language) {
		mLanguage = language;
	}
	public String getAbstract() {
		return mAbstract;
	}
	public void setAbstract(String abstract1) {
		mAbstract = abstract1;
	}
	public String getUrlList() {
		return mUrlList;
	}
	public void setUrlList(String urlList) {
		mUrlList = urlList;
	}
	public List<Speaker> getSpeakers() {
		return mSpeakers;
	}
	public void setSpeakers(List<Speaker> speakers) {
		mSpeakers = speakers;
	}
	public Room getRoom() {
		return mRoom;
	}
	public void setRoom(Room room) {
		mRoom = room;
	}
	public String getEventType() {
		return mEventType;
	}
	public void setEventType(String eventType) {
		mEventType = eventType;
	}

}
