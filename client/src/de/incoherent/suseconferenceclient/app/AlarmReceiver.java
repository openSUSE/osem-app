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

import de.incoherent.suseconferenceclient.activities.HomeActivity;
import de.incoherent.suseconferenceclient.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/*
 * Android 2.x/3.x don't provide a sufficiently reliable way to add
 * events to the user's calendar, so we set alarms to remind users
 * that the event is coming up, if they've pressed the "Add to Calendar" button.
 */
public class AlarmReceiver extends BroadcastReceiver {
	// TODO launch the event details activity on click
	@SuppressWarnings({"deprecation" })
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent notificationIntent = new Intent(context, HomeActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		String title = intent.getStringExtra("title");
		String room = intent.getStringExtra("room");
		String time = intent.getStringExtra("timetext");
		String message = time + ", " + room;
		
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,
													 null,
													 System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = context.getResources().getColor(R.color.dark_suse_green);
		notification.ledOffMS = 1000;
		notification.ledOnMS = 300;
		notification.setLatestEventInfo(context, title, message, contentIntent);
		manager.notify(13572, notification);
	}

}
