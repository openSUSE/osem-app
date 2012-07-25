/**
 * 
 */
package de.suse.conferenceclient.models;

import java.util.Date;

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
	private String mEventType;
	private String mTitle = "";
	
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


}
