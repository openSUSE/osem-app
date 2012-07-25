/**
 * 
 */
package de.suse.conferenceclient.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.suse.conferenceclient.models.Conference;
import de.suse.conferenceclient.models.Event;
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
	
	public long insertTrack(String guid, String name, long conferenceId) {
		ContentValues values = new ContentValues();
		values.put("guid", guid);
		values.put("name", name);
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
	
	public List<Event> getScheduleTitles(long conferenceId) {
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");  

		List<Event> eventList = new ArrayList<Event>();
		String[] columns = {"_id", "guid", "title", "date"};
		String where = "conference_id = " + conferenceId;
		Cursor c = db.query("events", columns, where, null, null, null, null);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			Event newEvent = new Event();
			newEvent.setGuid(c.getString(1));
			newEvent.setTitle(c.getString(2));
			try {  
			    Date date = format.parse(c.getString(3));  
			    newEvent.setDate(date);
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
