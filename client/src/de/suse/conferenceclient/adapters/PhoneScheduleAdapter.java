package de.suse.conferenceclient.adapters;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.Speaker;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PhoneScheduleAdapter extends ArrayAdapter<PhoneScheduleAdapter.ScheduleItem> {
	public static class ScheduleItem {
		boolean mIsHeader = false;
		String mHeaderTitle = null;
		Event mEvent = null;
		int mColor = 0;
		boolean mIsEmpty = true;
		boolean mConflict;
		
		public ScheduleItem(String headerTitle) {
			this.mHeaderTitle = headerTitle;
			mIsHeader = true;
		}
		
		public ScheduleItem(Event event, boolean isEmpty) {
			this.mEvent = event;
			this.mColor = Color.parseColor(event.getColor());
			this.mIsEmpty = isEmpty;
			this.mConflict = false;
		}

		public boolean conflicts() {
			return mConflict;
		}
		
		public void setConflict(boolean conflict) {
			mConflict = conflict;
		}
		
		public boolean isEmpty() {
			return mIsEmpty;
		}
		public int getColor() {
			return mColor;
		}

		public boolean isHeader() {
			return mIsHeader;
		}

		public void setIsHeader(boolean isHeader) {
			mIsHeader = isHeader;
		}

		public String getHeaderTitle() {
			return mHeaderTitle;
		}

		public void setHeaderTitle(String headerTitle) {
			mHeaderTitle = headerTitle;
		}

		public Event getEvent() {
			return mEvent;
		}

		public void setEvent(Event event) {
			mEvent = event;
		}
	}
	
	private int mResource;
	private Context mContext;
	private int mHeaderTextColor, mHeaderBackColor;
	private LayoutInflater mLayoutInflator;
	private List<ScheduleItem> mItems;
    private java.text.DateFormat mTimeFormatter;
    private Boolean mFullSchedule = true;
	public PhoneScheduleAdapter(Context context, boolean fullSchedule, int resource, int headerTextColor, int headerBackColor, List<ScheduleItem> itemList) {
		super(context, resource, itemList);
		this.mItems = itemList;
		this.mFullSchedule = fullSchedule;
		this.mContext = context;
		this.mLayoutInflator = LayoutInflater.from(context);
		this.mResource = resource;
		this.mHeaderTextColor = headerTextColor;
		this.mHeaderBackColor = headerBackColor;
		this.mTimeFormatter = DateFormat.getTimeFormat(context);
	}
	
	@Override
	public void clear() {
		mItems.clear();
	}
	
	public void setList(List<ScheduleItem> list) {
		mItems = list;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View root;
        ScheduleItem item = mItems.get(position);
        Event event = item.getEvent();
        if (convertView == null) {
            root = mLayoutInflator.inflate(mResource, null);
        } else {
            root = convertView;
        }

        ImageView favoriteView = (ImageView) root.findViewById(R.id.eventFavorited);
        TextView titleText = (TextView) root.findViewById(R.id.titleTextView);
        TextView speakerText = (TextView) root.findViewById(R.id.speakerPhoneTextView);
        TextView trackText = (TextView) root.findViewById(R.id.trackTextView);        
        TextView roomText = (TextView) root.findViewById(R.id.roomTextView);
        LinearLayout timeLayout = (LinearLayout) root.findViewById(R.id.timeLayout);
        TextView timeText = (TextView) root.findViewById(R.id.timeText);
    	root.setBackgroundColor(mHeaderBackColor);
    	
        if (item.isHeader()) {
        	titleText.setText(item.getHeaderTitle());
        	titleText.setTextColor(mHeaderTextColor);
        	speakerText.setVisibility(View.GONE);
        	trackText.setVisibility(View.GONE);
        	roomText.setVisibility(View.GONE);
        	timeLayout.setVisibility(View.GONE);
        	timeText.setVisibility(View.GONE);
    		favoriteView.setVisibility(View.GONE);
        } else {
          	
        	if (!mFullSchedule || !event.isInMySchedule()) {
        		favoriteView.setVisibility(View.GONE);
        	} else {
        		favoriteView.setVisibility(View.VISIBLE);
        	}
        	
        	timeLayout.setVisibility(View.VISIBLE);
        	timeText.setVisibility(View.VISIBLE);
        	if (item.conflicts()) {
        		timeText.setTextColor(Color.RED);
        	} else {
        		timeText.setTextColor(Color.BLACK);
        	}
        	String time = mTimeFormatter.format(event.getDate());
        	if (!DateFormat.is24HourFormat(mContext) && mTimeFormatter.getCalendar().get(Calendar.HOUR) < 10)
        		time = "0" + time;
        	timeText.setText(time);
        	
        	if (item.isEmpty()) {
	        	titleText.setTextColor(Color.BLACK);
	        	titleText.setText("Empty Slot");
	        	speakerText.setVisibility(View.GONE);
	        	trackText.setVisibility(View.GONE);
	        	roomText.setVisibility(View.GONE);
        	} else {
	            titleText.setText(event.getTitle());
	            titleText.setEllipsize(TruncateAt.MARQUEE);
	        	titleText.setTextColor(Color.BLACK);
	
	        	speakerText.setVisibility(View.VISIBLE);
	        	roomText.setVisibility(View.VISIBLE);
	        	String roomStr= event.getRoomName() + ", " + event.getLength() + " minutes";
	        	roomText.setText(roomStr);
	        	trackText.setVisibility(View.VISIBLE);
	        	trackText.setText(event.getTrackName());
	        	trackText.setTextColor(item.getColor());
	
	            List<Speaker> speakers = event.getSpeakers();
	            String speakersStr;
	            if (speakers.size() == 0) {
	            	speakersStr = "Unknown";
	            } else {
	            	speakersStr = speakers.get(0).getName();
	            }
	            
	            if (speakers.size() >= 2) {
	            	speakersStr = speakersStr + ", " + speakers.get(1).getName();
	            } 
	            
	            if (speakers.size() > 2) {
	            	speakersStr = speakersStr + " and others";
	            }
	            
	            speakerText.setText(speakersStr);
        	}
        }
        

        return root;
	}

}
