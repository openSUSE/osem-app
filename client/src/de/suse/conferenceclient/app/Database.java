/**
 * 
 */
package de.suse.conferenceclient.app;

import de.suse.conferenceclient.models.Conference;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
		String where = "guid = " + guid;
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
}
