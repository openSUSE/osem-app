/**
 * 
 */
package de.suse.conferenceclient.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	
	private static final String conferencesTableCreate = "CREATE TABLE conferences ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "guid VARCHAR, "
			+ "name VARCHAR, "
			+ "description VARCHAR, "
			+ "year INTEGER, "
			+ "dateRange VARCHAR)";
	
	private static final String venueTableCreate = "CREATE TABLE venues (" 
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "guid VARCHAR, "
			+ "name VARCHAR, "
			+ "address VARCHAR)";
	
	private static final String roomsTableCreate = "CREATE TABLE rooms ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "guid VARCHAR, "
			+ "venue_id INTEGER, "
			+ "name VARCHAR, "
			+ "description VARCHAR)";
	
	private static final String speakerTableCreate = "CREATE TABLE speakers ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "guid VARCHAR, "
			+ "name VARCHAR, " 
			+ "company VARCHAR, "
			+ "biography VARCHAR, "
			+ "photo_guid VARCHAR)";
	
	private static final String typeTableCreate = "CREATE TABLE eventTypes ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "name VARCHAR)";
	
	private static final String eventTableCreate = "CREATE TABLE events ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "guid VARCHAR, "
			+ "type_id INTEGER, "
			+ "speaker_id INTEGER, "
			+ "room_id INTEGER, "
			+ "date DATETIME, "
			+ "length INTEGER, "
			+ "language VARCHAR, "
			+ "abstract VARCHAR, "
			+ "url_list VARCHAR)";
	
	private static final String myEventTableCreate = "CREATE TABLE myEvents ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "conference_id INTEGER, "
			+ "event_id INTEGER)";
	
	public DatabaseHelper(Context context) {
		super(context, "SUSEConferences", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("SUSEConferences", "Creating database");
		db.execSQL(conferencesTableCreate);
		db.execSQL(venueTableCreate);
		db.execSQL(roomsTableCreate);
		db.execSQL(speakerTableCreate);
		db.execSQL(typeTableCreate);
		db.execSQL(eventTableCreate);
		db.execSQL(myEventTableCreate);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}


}
