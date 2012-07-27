/**
 * 
 */
package de.suse.conferenceclient.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.suse.conferenceclient.models.Conference;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.Speaker;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
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
	
	public boolean isEventInMySchedule(long eventId) {
		boolean ret = false;
		Log.d("SUSEConferences", "Checking if "+ eventId + " is in My Schedule");
		Cursor c = db.rawQuery("SELECT _id FROM myEvents WHERE event_id=" + eventId, null);
		if (c.getCount() > 0) {
			Log.d("SUSEConferences", "It is!");
			ret = true;
		} else
			Log.d("SUSEConference", "It isn't!");
		
		c.close();
		return ret;
	}
	
	public void removeEventFromMySchedule(long eventId) {
		Log.d("SUSEConferences", "Removing " + eventId + " from My Schedule");
		String sql = "event_id=" + eventId;
		db.delete("myEvents", sql, null);
	}
	
	public void addEventToMySchedule(long eventId, long conferenceId) {
		Log.d("SUSEConferences", "Adding " + eventId + " " + conferenceId+ " to My Schedule");

		ContentValues values = new ContentValues();
		values.put("event_id", eventId);
		values.put("conference_id", conferenceId);
		db.insert("myEvents", null, values);
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
		long insertId = db.insert("conferences", null, values);
		return insertId;
	}
	
	public long insertVenue(String guid, String name, String address) {
		ContentValues values = new ContentValues();
		values.put("guid", guid);
		values.put("name", name);
		values.put("address", address);
		long insertId = db.insert("venues", null, values);
		return insertId;
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
//		select name from events where date >= datetime('now', 'localtime') limit 2;
		String sql = "SELECT events._id, events.guid, events.title, events.date, events.length, "
				   + "rooms.name, events.track_id, events.abstract FROM events INNER JOIN rooms ON rooms._id = events.room_id "
				   + "WHERE events.date >= datetime(\'now\', \'localtime\') AND events.conference_id = " + conferenceId + " LIMIT 2";
		return doEventsQuery(sql);
	}
	
	public List<Event> getMyScheduleTitles(long conferenceId) {
		// First get the list of IDs we need
		// TODO subqueries?
		String eventsSql = "SELECT event_id FROM myEvents WHERE conference_id=" + conferenceId;
		Cursor c = db.rawQuery(eventsSql, null);
		List<String> idList = new ArrayList<String>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			idList.add(c.getString(0));
		}
		
		String ids = TextUtils.join(",", idList);
		if (ids.isEmpty())
			return new ArrayList<Event>();
		
		String sql = "SELECT events._id, events.guid, events.title, events.date, events.length, "
				   + "rooms.name, events.track_id, events.abstract FROM events INNER JOIN rooms ON rooms._id = events.room_id "
				   + "WHERE events._id IN (" + ids + ")";
		return doEventsQuery(sql);
	}
	
	public List<Event> getScheduleTitles(long conferenceId) {

		String sql = "SELECT events._id, events.guid, events.title, events.date, events.length, "
				   + "rooms.name, events.track_id, events.abstract FROM events INNER JOIN rooms ON rooms._id = events.room_id "
				   + "WHERE events.conference_id = " + conferenceId;
		return doEventsQuery(sql);
	}
	
	private List<Event> doEventsQuery(String sql) {
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");  

		List<Event> eventList = new ArrayList<Event>();		
		Cursor c = db.rawQuery(sql, null);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			Event newEvent = new Event();
			long sqlId = c.getLong(0);
			newEvent.setSqlId(sqlId);
			newEvent.setGuid(c.getString(1));
			newEvent.setTitle(c.getString(2));
			try {  
			    Date date = format.parse(c.getString(3));  
			    newEvent.setDate(date);
			    GregorianCalendar cal = new GregorianCalendar();
			    cal.setTime(date);
			    cal.add(GregorianCalendar.MINUTE, c.getInt(4));
			    newEvent.setLength(c.getInt(4));
			    newEvent.setEndDate(cal.getTime());
			    newEvent.setRoomName(c.getString(5));
			    // TODO this should be merged into a subquery if possible
			    long trackId = c.getLong(6);
			    newEvent.setAbstract(c.getString(7));
			    Cursor d = db.rawQuery("SELECT _id, color, name FROM tracks WHERE _id=" + trackId, null);
			    if (d.moveToFirst()) {
			    	newEvent.setColor(d.getString(1));
			    	newEvent.setTrackName(d.getString(2));
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
