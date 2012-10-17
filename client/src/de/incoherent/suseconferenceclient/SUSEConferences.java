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

package de.incoherent.suseconferenceclient;

import de.incoherent.suseconferenceclient.app.Database;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/*
 * Do this to provide a simple way to grab the Database
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
    }
    
    public static Database getDatabase() {
        return mDb;
    }
}
