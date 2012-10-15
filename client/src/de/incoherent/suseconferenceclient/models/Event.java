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

package de.incoherent.suseconferenceclient.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Event implements Comparable<Event> {
	private String mGuid = "";
	private Date mDate;
	private Date mEndDate;
	private TimeZone mTimeZone;
	
	private boolean mInMySchedule = false;
	private boolean mMetaInformation = false;
	
	private int mLength = 0;
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
	private long mConferenceId = -1;
	
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
	public long getConferenceId() {
		return mConferenceId;
	}
	public void setConferenceId(long conferenceId) {
		mConferenceId = conferenceId;
	}
	public boolean isInMySchedule() {
		return mInMySchedule;
	}
	public void setInMySchedule(boolean inMySchedule) {
		mInMySchedule = inMySchedule;
	}
	
	@Override
	public int compareTo(Event another) {
		return mDate.compareTo(another.getDate());
	}
	
	public TimeZone getTimeZone() {
		return mTimeZone;
	}
	public void setTimeZone(TimeZone timeZone) {
		mTimeZone = timeZone;
	}
	public boolean isMetaInformation() {
		return mMetaInformation;
	}
	public void setMetaInformation(boolean metaInformation) {
		mMetaInformation = metaInformation;
	}


}
