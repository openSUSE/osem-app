package de.incoherent.suseconferenceclient.app;

import de.incoherent.suseconferenceclient.activities.HomeActivity;
import de.suse.conferenceclient.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
