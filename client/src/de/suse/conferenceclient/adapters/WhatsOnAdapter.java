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
	private SimpleDateFormat mFormatter;
	private static final String FORMAT = "MMM dd HH:mm";
	
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
		this.mFormatter = new SimpleDateFormat(FORMAT);
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
		TextView time = (TextView) root.findViewById(R.id.timeTextView);
		title.setText(event.getTitle());
		room.setText(event.getRoomName());
		time.setText(mFormatter.format(event.getDate()));
		return root;
	}
}
