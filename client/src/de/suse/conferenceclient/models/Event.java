/**
 * 
 */
package de.suse.conferenceclient.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Event {
	private String mGuid = "";
	private Date mDate;
	private Date mEndDate;

	private int mLength;
	private String mLanguage = "";
	private String mAbstract = "";
	private String mUrlList = "";
	private String mEventType;
	private String mTitle = "";
	private String mTrackName = "";
	private String mRoomName = "";
	private String mColor = "#ffffff";
	private List<Speaker> mSpeakers;
	
	private long mSqlId = -1;
	
	public Event() {
		mSpeakers = new ArrayList<Speaker>();
	}
	public String getTrackName() {
		return mTrackName;
	}
	public void setTrackName(String trackName) {
		this.mTrackName = trackName;
	}
	
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
	public Date getEndDate() {
		return mEndDate;
	}
	public void setEndDate(Date endDate) {
		this.mEndDate = endDate;
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

	public String getEventType() {
		return mEventType;
	}
	public void setEventType(String eventType) {
		mEventType = eventType;
	}
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String title) {
		mTitle = title;
	}
	public String getRoomName() {
		return mRoomName;
	}
	public void setRoomName(String roomName) {
		mRoomName = roomName;
	}
	public String getColor() {
		return mColor;
	}
	public void setColor(String color) {
		mColor = color;
	}
	public long getSqlId() {
		return mSqlId;
	}
	public void setSqlId(long sqlId) {
		mSqlId = sqlId;
	}
	public List<Speaker> getSpeakers() {
		return mSpeakers;
	}
	public void addSpeaker(Speaker speaker) {
		mSpeakers.add(speaker);
	}
	public void setSpeakers(List<Speaker> speakers) {
		mSpeakers = speakers;
	}


}
