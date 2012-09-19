/**
 * 
 */
package de.incoherent.suseconferenceclient;

import de.incoherent.suseconferenceclient.app.Database;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class SUSEConferences extends Application {
	private static SUSEConferences mInstance;
    private static Database mDb;
	private long mActiveId;
	
	public SUSEConferences() {
		mInstance = this;
	}
	
    public static Context getContext()
    {
    	return mInstance;
    }
    
    @Override
    public void onCreate() {
	    super.onCreate();
	    mDb = Database.getInstance(this);
	    mDb.open();
    	SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
    	mActiveId = settings.getLong("active_conference", -1);
    }
    
    public static Database getDatabase() {
        return mDb;
    }

    public long getActiveId() {
    	return mActiveId;
    }
    
    public void setActiveId(long id) {
    	this.mActiveId = id; 
    }
}
