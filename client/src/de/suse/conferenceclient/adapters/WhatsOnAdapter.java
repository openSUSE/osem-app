/**
 * 
 */
package de.suse.conferenceclient.adapters;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.models.Event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class WhatsOnAdapter extends ArrayAdapter<Event> {
	private List<Event> mEvents;
	private LayoutInflater mInflater;
	private int mResource;
	private SimpleDateFormat mTimeFormatter, mDateFormatter;
	private static final String TIME_FORMAT = "HH:mm";
	private static final String DATE_FORMAT = "MMM d";
	/**
	 * @param context
	 * @param resource
	 * @param textViewResourceId
	 * @param objects
	 */
	public WhatsOnAdapter(Context context, int textViewResourceId, List<Event> objects) {
		super(context, textViewResourceId, objects);
		this.mInflater = LayoutInflater.from(context);
		this.mEvents = objects;
		this.mResource = textViewResourceId;
		this.mTimeFormatter = new SimpleDateFormat(TIME_FORMAT);
		this.mDateFormatter = new SimpleDateFormat(DATE_FORMAT);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View root;
		Event event = mEvents.get(position);
		if (convertView == null) {
			root = mInflater.inflate(mResource, null);
		} else {
			root = convertView;
		}
		TextView title = (TextView) root.findViewById(R.id.titleTextView);
		TextView room = (TextView) root.findViewById(R.id.roomTextView);
		TextView date = (TextView) root.findViewById(R.id.dateTextView);
		TextView time = (TextView) root.findViewById(R.id.timeTextView);
		title.setText(event.getTitle());
		room.setText(event.getRoomName());
		time.setText(mTimeFormatter.format(event.getDate()));
		date.setText(mDateFormatter.format(event.getDate()));
		return root;
	}
}
