/**
 * 
 */
package de.suse.conferenceclient.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import de.suse.conferenceclient.models.Conference;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.Speaker;
import de.suse.conferenceclient.models.Venue;
import de.suse.conferenceclient.models.Venue.MapPoint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Database access wrapper
 * 
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */

public class Database {
	private DatabaseHelper helper;
	private SQLiteDatabase db;
	private static Database instance = null;

	public final static Database getInstance(Context ctx) {
		if (instance == null)
			instance = new Database(ctx);
		return instance;
	}

	private Database(Context context) {
		helper = new DatabaseHelper(context);
	}
	
	public void open() throws SQLException {
		db = helper.getWritableDatabase();
	}

	public void close() {
		helper.close();
	}
	
	public void setConferenceVenue(long venueId, long conferenceId) {
		Log.d("SUSEConferences", "Setting venue_id to " + venueId + " where conference is " + conferenceId);
		ContentValues values = new ContentValues();
		values.put("venue_id", venueId);
		db.update("conferences", values, "_id = " + conferenceId, null);		
	}
	
	public Conference getConference(long conferenceId) {
		Conference newConference = null;
		String sql = "SELECT guid, name, description, year, social_tag, dateRange FROM conferences WHERE _id=" + conferenceId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			newConference = new Conference();
			newConference.setGuid(c.getString(0));
			newConference.setName(c.getString(1));
			newConference.setDescription(c.getString(2));
			newConference.setYear(c.getInt(3));
			newConference.setSocialTag(c.getString(4));
			newConference.setDateRange(c.getString(5));
			newConference.setSqlId(conferenceId);
		}
		
		return newConference;
	}
	
	public long getLastUpdateTime(long conferenceId) {
		long time = 0;
		String sql = "SELECT lastUpdated FROM conferences WHERE _id=" + conferenceId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			time = c.getLong(0);
		}
		c.close();
		return time;
	}
	
	public void setLastUpdateTime(long conferenceId, long time) {
		String sql = "_id=" + conferenceId;
		ContentValues values = new ContentValues();
		values.put("lastUpdated", time);
		db.update("conferences", values, sql, null);
	}
	public long getConferenceVenue(long conferenceId) {
		long id = -1;
		String sql = "SELECT venue_id FROM conferences WHERE _id=" + conferenceId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			id = c.getLong(0);
		}
		c.close();
		return id;
	}
	
	public Venue getVenueInfo(long venueId) {
		Venue venue = null;
		String sql = "SELECT name, address, info_text FROM venues WHERE _id=" + venueId;
		String pointSql = "SELECT type, lat, lon, name, address, description FROM points WHERE venue_id=" + venueId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			venue = new Venue(c.getString(0), c.getString(1), c.getString(2));
		}
		c.close();
		
		c = db.rawQuery(pointSql, null);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String typeStr = c.getString(0);
			int lat = getLatLon(c.getString(1));
			int lon = getLatLon(c.getString(2));
			int type = 0;
			if (typeStr.equals("venue"))
				type = MapPoint.TYPE_VENUE;
			else if (typeStr.equals("food"))
				type = MapPoint.TYPE_FOOD;
			else if (typeStr.equals("drink"))
				type = MapPoint.TYPE_DRINK;
			else if (typeStr.equals("electronics"))
				type = MapPoint.TYPE_ELECTRONICS;
			MapPoint newPoint = venue.new MapPoint(type, lat, lon);
			newPoint.setName(c.getString(3));
			newPoint.setAddress(c.getString(4));
			newPoint.setDescription(c.getString(5));			
			venue.addPoint(newPoint);
		}
		return venue;
	}
	
	public int getLatLon(String str) {
		double val = Double.parseDouble(str);
		int ret = (int) (val * 1E6);
		return ret;
	}
	
	public void toggleEventInMySchedule(long eventId, int val) {
		Log.d("SUSEConferences", "Toggling " + eventId + " in My Schedule");
		String sql = "_id=" + eventId;
		ContentValues values = new ContentValues();
		values.put("my_schedule", val);
		db.update("events", values, sql, null);
	}
		
	public long getConferenceIdFromGuid(String guid) {
		String[] columns = {"_id"};
		String where = "guid = \"" + guid + "\"";
		Cursor c = db.query("conferences", columns, where, null, null, null, null);
		if (c.getCount() == 0) {
			return -1;
		}
		c.moveToNext();
		long id = c.getLong(0);
		c.close();
		return id;
	}

	public long addConference(Conference conference) {
		ContentValues values = new ContentValues();
		values.put("guid", conference.getGuid());
		values.put("name", conference.getName());
		values.put("year", conference.getYear());
		values.put("dateRange", conference.getDateRange());
		values.put("description", conference.getDescription());
		values.put("social_tag", conference.getSocialTag());
		long insertId = db.insert("conferences", null, values);
		return insertId;
	}
	
	public long insertVenue(String guid, String name, String address, String infoText) {
		ContentValues values = new ContentValues();
		values.put("guid", guid);
		values.put("name", name);
		values.put("address", address);
		values.put("info_text", infoText);
		long insertId = db.insert("venues", null, values);
		return insertId;
	}
	
	public void insertVenuePoint(long venueId,
								 String lat,
								 String lon,
								 String type,
								 String name,
								 String address,
								 String description) {
		ContentValues values = new ContentValues();
		values.put("venue_id", venueId);
		values.put("type", type);
		values.put("lat", lat);
		values.put("lon", lon);
		values.put("name", name);
		values.put("address", address);
		values.put("description", description);
		db.insert("points", null, values);
	}
	
	public long insertRoom(String guid, String name, String description, long venueId) {
		ContentValues values = new ContentValues();
		values.put("guid", guid);
		values.put("name", name);
		values.put("description", description);
		values.put("venue_id", venueId);
		long insertId = db.insert("rooms", null, values);
		return insertId;
	}
	
	public long insertTrack(String guid, String name, String color, long conferenceId) {
		ContentValues values = new ContentValues();
		values.put("guid", guid);
		values.put("name", name);
		values.put("color", color);
		values.put("conference_id", conferenceId);
		long insertId = db.insert("tracks", null, values);
		return insertId;
	}

	public long insertSpeaker(String guid, String name, String company, String biography, String photoGuid) {
		ContentValues values = new ContentValues();
		values.put("guid", guid);
		values.put("name", name);
		values.put("company", company);
		values.put("biography", biography);
		values.put("photo_guid", photoGuid);
		long insertId = db.insert("speakers", null, values);
		return insertId;
	}

	public long insertEvent(String guid,
							long conferenceId,
							long roomId,
							long trackId,
							String date,
							int length,
							String type,
							String language,
							String title,
							String abs,
							String urlList) {
		Log.d("SUSEConferences", "Inserting event: " + abs);
		ContentValues values = new ContentValues();
		values.put("guid", guid);
		values.put("conference_id", conferenceId);
		values.put("room_id", roomId);
		values.put("track_id", trackId);
		values.put("my_schedule", 0);
		values.put("date", date);
		values.put("length", length);
		values.put("type", type);
		values.put("title", title);
		values.put("language", language);
		values.put("abstract", abs);
		values.put("url_list", urlList);
		long insertId = db.insert("events", null, values);
		return insertId;
	}
	
	public void insertEventSpeaker(long speakerId, long eventId) {
		ContentValues values = new ContentValues();
		values.put("speaker_id", speakerId);
		values.put("event_id", eventId);
		db.insert("eventSpeakers", null, values);
	}
		
	public List<Event> getNextTwoEvents(long conferenceId) {
		List<Event> eventList = getScheduleTitles(conferenceId);
		Collections.sort(eventList);
		if (eventList.size() >= 2)
			return eventList.subList(0,2);
		else
			return eventList;
	}
	
	public List<Event> getMyScheduleTitles(long conferenceId) {
		String sql = "SELECT events._id, events.guid, events.title, events.date, events.length, "
				   + "rooms.name, events.track_id, events.abstract, events.my_schedule FROM events INNER JOIN rooms ON rooms._id = events.room_id "
				   + "WHERE events.my_schedule=1 AND events.conference_id = " + conferenceId + " ORDER BY julianday(events.date) ASC";
		return doEventsQuery(sql, conferenceId);
	}
	
	public List<Event> getScheduleTitles(long conferenceId) {
		String sql = "SELECT events._id, events.guid, events.title, events.date, events.length, "
				   + "rooms.name, events.track_id, events.abstract, events.my_schedule FROM events INNER JOIN rooms ON rooms._id = events.room_id "
				   + "WHERE events.conference_id = " + conferenceId + " ORDER BY julianday(events.date) ASC";
		return doEventsQuery(sql, conferenceId);
	}
	
	public Event getEvent(long conferenceId, long eventId) {
		String sql = "SELECT events._id, events.guid, events.title, events.date, events.length, "
				   + "rooms.name, events.track_id, events.abstract, events.my_schedule FROM events INNER JOIN rooms ON rooms._id = events.room_id "
				   + "WHERE events._id = " + eventId;
		List<Event> events = doEventsQuery(sql, conferenceId);
		if (events.size() == 0)
			return null;
		
		Event e = events.get(0);
		return e;
	}
	
	private List<Event> doEventsQuery(String sql, long conferenceId) {
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

		List<Event> eventList = new ArrayList<Event>();		
		Cursor c = db.rawQuery(sql, null);
	    Log.d("SUSEConferences", "doEventsQuery:  " + sql);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			Event newEvent = new Event();
			newEvent.setConferenceId(conferenceId);
			long sqlId = c.getLong(0);
			newEvent.setSqlId(sqlId);
			newEvent.setGuid(c.getString(1));
			newEvent.setTitle(c.getString(2));
			try {
				String time = c.getString(3);
			    Date date = format.parse(time);
			    String tzOffset = time.substring(time.length() - 5);
			    TimeZone tz = TimeZone.getTimeZone("GMT"+tzOffset);
			    newEvent.setTimeZone(tz);
			    newEvent.setDate(date);

			    GregorianCalendar cal = new GregorianCalendar();
			    cal.setTime(date);
			    cal.setTimeZone(tz);
			    cal.add(GregorianCalendar.MINUTE, c.getInt(4));
			    newEvent.setLength(c.getInt(4));
			    newEvent.setEndDate(cal.getTime());
			    newEvent.setRoomName(c.getString(5));
			    
			    // TODO this should be merged into a subquery if possible
			    long trackId = c.getLong(6);
			    newEvent.setAbstract(c.getString(7));
			    newEvent.setInMySchedule(c.getInt(8) != 0);
			    Cursor d = db.rawQuery("SELECT _id, color, name FROM tracks WHERE _id=" + trackId, null);
			    if (d.moveToFirst()) {
			    	newEvent.setColor(d.getString(1));
			    	newEvent.setTrackName(d.getString(2));
			    	if (d.getString(2).equalsIgnoreCase("meta")) {
			    		newEvent.setMetaInformation(true);
			    	}
			    }
			    d.close();

			    // Get the speakers
			    d = db.rawQuery("SELECT speakers._id, speakers.name, speakers.company, speakers.biography, speakers.photo_guid " +
			    					   " FROM speakers INNER JOIN eventSpeakers ON eventSpeakers.speaker_id = speakers._id WHERE eventSpeakers.event_id=" + sqlId, null);
			    d.moveToFirst();
		        while (d.isAfterLast() == false) {
		        	Speaker newSpeaker = new Speaker(d.getString(1),
		        									 d.getString(2),
		        									 d.getString(3),
		        									 null);
		        	newEvent.addSpeaker(newSpeaker);
		        	d.moveToNext();
		        }
		        d.close();
		        eventList.add(newEvent);
			} catch (ParseException e) {  
			    // TODO Auto-generated catch block  
			    e.printStackTrace();  
			}
		}
		c.close();
		return eventList;
	}
}
