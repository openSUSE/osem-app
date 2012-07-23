/**
 * 
 */
package de.suse.conferenceclient.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */

public class Conference {
	private String mGuid = "";
	private String mSqlId = "-1";
	private String mName = "";
	private String mDescription = "";
	private int mYear = 2012;
	private String mDateRange = "";
	private String mUrl = "";
	
	private List<Event> mEvents;
	private List<Room> mRooms;
	private List<Speaker> mSpeakers;
	private Venue mVenue;
	
	public Conference() {
		mEvents = new ArrayList<Event>();
		mRooms = new ArrayList<Room>();
		mSpeakers = new ArrayList<Speaker>();
		mVenue = new Venue();
	}

	public String getGuid() {
		return mGuid;
	}

	public void setGuid(String guid) {
		this.mGuid = guid;
	}

	public String getSqlId() {
		return mSqlId;
	}

	public void setSqlId(String sqlId) {
		this.mSqlId = sqlId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public int getYear() {
		return mYear;
	}

	public void setYear(int year) {
		this.mYear = year;
	}

	public String getDateRange() {
		return mDateRange;
	}

	public void setDateRange(String dateRange) {
		this.mDateRange = dateRange;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public void setUrl(String url) {
		this.mUrl = url;
	}

	public List<Event> getEvents() {
		return mEvents;
	}

	public void setEvents(List<Event> events) {
		this.mEvents = events;
	}

	public List<Room> getRooms() {
		return mRooms;
	}

	public void setRooms(List<Room> rooms) {
		this.mRooms = rooms;
	}

	public List<Speaker> getSpeakers() {
		return mSpeakers;
	}

	public void setSpeakers(List<Speaker> speakers) {
		this.mSpeakers = speakers;
	}

	public Venue getVenue() {
		return mVenue;
	}

	public void setVenue(Venue venue) {
		this.mVenue = venue;
	}
}
