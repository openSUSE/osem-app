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

package de.incoherent.suseconferenceclient.app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
	
	private static final String conferencesTableCreate = "CREATE TABLE conferences ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "guid VARCHAR, "
			+ "name VARCHAR, "
			+ "description VARCHAR, "
			+ "year INTEGER, "
			+ "venue_id INTEGER,"
			+ "social_tag VARCHAR, "
			+ "dateRange VARCHAR, "
			+ "lastUpdated INTEGER DEFAULT 0, "
			+ "is_cached INTEGER DEFAULT 0,"
			+ "url VARCHAR)";
	
	private static final String venueTableCreate = "CREATE TABLE venues (" 
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "guid VARCHAR, "
			+ "name VARCHAR, "
			+ "offline_map VARCHAR, "
			+ "offline_map_bounds VARCHAR, "
			+ "address VARCHAR, "
			+ "info_text VARCHAR)";
	
	private static final String mapPointsTableCreate = "CREATE TABLE points ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "venue_id INTEGER, "
			+ "type VARCHAR, "
			+ "lat VARCHAR,"
			+ "lon VARCHAR, "
			+ "name VARCHAR, "
			+ "address VARCHAR, "
			+ "description VARCHAR)";
	
	private static final String mapPolygonTableCreate = "CREATE TABLE mapPolygons ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "venue_id INTEGER, "
			+ "name VARCHAR, "
			+ "label VARCHAR, "
			+ "lineColor INTEGER, "
			+ "fillColor INTEGER, "
			+ "pointList VARCHAR)";
	
	private static final String roomsTableCreate = "CREATE TABLE rooms ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "guid VARCHAR, "
			+ "venue_id INTEGER, "
			+ "name VARCHAR, "
			+ "description VARCHAR)";
	
	private static final String tracksTableCreate = "CREATE TABLE tracks ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "guid VARCHAR, "
			+ "conference_id INTEGER, "
			+ "color VARCHAR, "
			+ "name VARCHAR)";
	
	private static final String speakerTableCreate = "CREATE TABLE speakers ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "guid VARCHAR, "
			+ "name VARCHAR, " 
			+ "company VARCHAR, "
			+ "biography VARCHAR, "
			+ "photo_guid VARCHAR)";
	
	private static final String eventTableCreate = "CREATE TABLE events ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "guid VARCHAR, "
			+ "conference_id INTEGER, "
			+ "room_id INTEGER, "
			+ "track_id INTEGER, "
			+ "my_schedule INTEGER, "
			+ "date DATETIME, "
			+ "length INTEGER, "
			+ "type VARCHAR, "
			+ "title VARCHAR, "
			+ "language VARCHAR, "
			+ "abstract VARCHAR, "
			+ "url_list VARCHAR)";
	
	private static final String speakerEventTableCreate = "CREATE TABLE eventSpeakers ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "speaker_id INTEGER, "
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
		db.execSQL(eventTableCreate);
		db.execSQL(tracksTableCreate);
		db.execSQL(speakerEventTableCreate);
		db.execSQL(mapPointsTableCreate);
		db.execSQL(mapPolygonTableCreate);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1) {
			db.execSQL("ALTER TABLE venues ADD offline_map VARCHAR");
			db.execSQL("ALTER TABLE venues ADD offline_map_bounds VARCHAR");
			db.execSQL("ALTER TABLE conferences ADD is_cached INTEGER");
			db.execSQL("ALTER TABLE conferences ADD url VARCHAR");
			db.execSQL("UPDATE conferences SET is_cached=1");
		}
		
	}

	public void clearDatabase(SQLiteDatabase db, long conferenceId) {
		long venueId = -1;
		String sql = "SELECT venue_id FROM conferences WHERE _id=" + conferenceId;
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			venueId = c.getLong(0);
		}
		c.close();
		
		db.execSQL("DELETE FROM venues WHERE _id=" + venueId);
		db.execSQL("DELETE FROM points WHERE venue_id="+ venueId);
		db.execSQL("DELETE FROM mapPolygons WHERE venue_id=" + venueId);
		db.execSQL("DELETE FROM rooms WHERE venue_id=" + venueId);
		db.execSQL("DELETE FROM tracks WHERE conference_id=" + conferenceId);
		db.execSQL("DELETE FROM events WHERE conference_id=" + conferenceId);
		// TODO Delete speakers and eventSpeakers entries
	}

}
