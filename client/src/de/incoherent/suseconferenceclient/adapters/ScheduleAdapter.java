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
package de.incoherent.suseconferenceclient.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.incoherent.suseconferenceclient.models.Event;
import de.incoherent.suseconferenceclient.models.Speaker;
import de.incoherent.suseconferenceclient.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ScheduleAdapter extends ArrayAdapter<ScheduleAdapter.ScheduleItem> implements SectionIndexer {
	// Use a ViewHolder to save all of the ID lookup time 
	private static class ViewHolder {
        ImageView favoriteView;
        TextView titleText;
        TextView speakerText;
        TextView trackText;
        TextView roomText;
        LinearLayout timeLayout;
        TextView timeText;
	}
	
	public static class ScheduleItem {
		boolean mIsHeader = false;
		String mHeaderTitle = null;
		String mDay = "";
		Event mEvent = null;
		int mColor = 0;
		boolean mIsEmpty = true;
		boolean mConflict;
		
		// TODO The day string is used for the fast scroll display.  It would be nice to show the entire title
		public ScheduleItem(String headerTitle, String day) {
			this.mHeaderTitle = headerTitle;
			this.mDay = day;
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

		public String getDayString() {
			return mDay;
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
    private int mSUSEGreen;
    private Object[] mIndexerSections;
    private int[] mIndexerPositionsForSection;
    private int[] mIndexerSectionsForPosition;
    
	public ScheduleAdapter(Context context,
							    boolean fullSchedule,
							    int resource,
							    int headerTextColor, 
							    int headerBackColor,
							    List<ScheduleItem> itemList) {
		super(context, resource, itemList);
		this.mItems = itemList;
		this.mFullSchedule = fullSchedule;
		this.mContext = context;
		this.mLayoutInflator = LayoutInflater.from(context);
		this.mResource = resource;
		this.mHeaderTextColor = headerTextColor;
		this.mHeaderBackColor = headerBackColor;
		this.mTimeFormatter = DateFormat.getTimeFormat(context);
		this.mSUSEGreen = context.getResources().getColor(R.color.dark_suse_green);
		
		List<Object> sections = new ArrayList<Object>();
		List<Integer> sectionPositions = new ArrayList<Integer>();
		mIndexerSectionsForPosition = new int[itemList.size()];
		int size = itemList.size();
		int currentSection = 0;

		for (int i = 0; i < size; i++) {
			ScheduleItem item = itemList.get(i);
			if (item.isHeader()) {
				sections.add(item.getDayString());
				sectionPositions.add(i);
				mIndexerSectionsForPosition[i] = currentSection;
				currentSection++;
			} else {
				mIndexerSectionsForPosition[i] = currentSection;
			}
		}
		
		mIndexerSections = sections.toArray();
		mIndexerPositionsForSection =  new int[sectionPositions.size()];
		for(int i = 0; i < mIndexerPositionsForSection.length; i++)
			mIndexerPositionsForSection[i] = sectionPositions.get(i);

	}
	
	@Override
	public void clear() {
		mItems.clear();
	}
	
	public void setList(List<ScheduleItem> list) {
		mItems = list;
	}
	
	private boolean eventWithinRange(Date now, Event event) {
		Date start = event.getDate();
		Date end = event.getEndDate();
		return !(now.before(start) || now.after(end));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View root;
        int scheduleHour = 0;
        int scheduleHour24 = 0;
        ViewHolder viewHolder;
        ScheduleItem item = mItems.get(position);
        Event event = item.getEvent();
        
        if (convertView == null) {
            root = mLayoutInflator.inflate(mResource, null);
            viewHolder = new ViewHolder();
            viewHolder.favoriteView = (ImageView) root.findViewById(R.id.eventFavorited);
            viewHolder.titleText = (TextView) root.findViewById(R.id.titleTextView);
            viewHolder.speakerText = (TextView) root.findViewById(R.id.speakerPhoneTextView);
            viewHolder.trackText = (TextView) root.findViewById(R.id.trackTextView);        
            viewHolder.roomText = (TextView) root.findViewById(R.id.roomTextView);
            viewHolder.timeLayout = (LinearLayout) root.findViewById(R.id.timeLayout);
            viewHolder.timeText = (TextView) root.findViewById(R.id.timeText);
            root.setTag(viewHolder);
        } else {
            root = convertView;
            viewHolder = (ViewHolder) root.getTag();
        }
        
        Date now = new Date();
    	root.setBackgroundColor(mHeaderBackColor);
    	
        if (item.isHeader()) {
        	viewHolder.titleText.setText(item.getHeaderTitle());
        	viewHolder.titleText.setTypeface(null, Typeface.BOLD);
        	viewHolder.titleText.setTextColor(mHeaderTextColor);
        	viewHolder.speakerText.setVisibility(View.GONE);
        	viewHolder.trackText.setVisibility(View.GONE);
        	viewHolder.roomText.setVisibility(View.GONE);
        	viewHolder.timeLayout.setVisibility(View.GONE);
        	viewHolder.timeText.setVisibility(View.GONE);
        	viewHolder.favoriteView.setVisibility(View.GONE);
        } else if (event.isMetaInformation()) {
        	viewHolder.titleText.setText(event.getTitle());
        	viewHolder.titleText.setTypeface(null, Typeface.NORMAL);
        	viewHolder.titleText.setTextColor(Color.GRAY);        	
        	viewHolder.favoriteView.setVisibility(View.GONE);
        	viewHolder.speakerText.setVisibility(View.GONE);
        	viewHolder.trackText.setVisibility(View.GONE);
        	viewHolder.roomText.setVisibility(View.GONE);
        	viewHolder.timeLayout.setVisibility(View.VISIBLE);
        	viewHolder.timeText.setVisibility(View.VISIBLE);
        	if (eventWithinRange(now, event))
        		viewHolder.timeText.setTextColor(this.mSUSEGreen);
        	else
        		viewHolder.timeText.setTextColor(Color.BLACK);
        	mTimeFormatter.setTimeZone(event.getTimeZone());
        	String time = mTimeFormatter.format(event.getDate());
        	
        	// While this means that the user's time locale preferences aren't
        	// *strictly* respected, it's necessary to make all of the hours two digits
        	// to maintain the column width
        	scheduleHour = mTimeFormatter.getCalendar().get(Calendar.HOUR);
        	scheduleHour24 = mTimeFormatter.getCalendar().get(Calendar.HOUR_OF_DAY);

        	if (!DateFormat.is24HourFormat(mContext) &&  scheduleHour < 10 && scheduleHour > 0)
        		time = "0" + time;
        	
        	// One more work around for certain locales (Germany, for example) where 24 hour
        	// clocks don't mean double digit hours
        	if (DateFormat.is24HourFormat(mContext) && scheduleHour24 < 10 && !time.startsWith("0"))
        		time = "0" + time;
        	
        	viewHolder.timeText.setText(time);
        } else {
        	if (!mFullSchedule || !event.isInMySchedule()) {
        		viewHolder.favoriteView.setVisibility(View.GONE);
        	} else {
        		viewHolder.favoriteView.setVisibility(View.VISIBLE);
        	}
        	
        	viewHolder.timeLayout.setVisibility(View.VISIBLE);
        	viewHolder.timeText.setVisibility(View.VISIBLE);
        	if (eventWithinRange(now, event)) {
        		viewHolder.timeText.setTextColor(this.mSUSEGreen);
        	} else {
	        	if (item.conflicts()) {
	        		viewHolder.timeText.setTextColor(Color.RED);
	        	} else {
	        		viewHolder.timeText.setTextColor(Color.BLACK);
	        	}
        	}

        	mTimeFormatter.setTimeZone(event.getTimeZone());
        	String time = mTimeFormatter.format(event.getDate());
        	scheduleHour = mTimeFormatter.getCalendar().get(Calendar.HOUR);
        	scheduleHour24 = mTimeFormatter.getCalendar().get(Calendar.HOUR_OF_DAY);

        	if (!DateFormat.is24HourFormat(mContext) &&  scheduleHour < 10 && scheduleHour > 0)
        		time = "0" + time;
        	if (DateFormat.is24HourFormat(mContext) && scheduleHour24 < 10 && !time.startsWith("0"))
        		time = "0" + time;

        	viewHolder.timeText.setText(time);
        	viewHolder.titleText.setTypeface(null, Typeface.NORMAL);

        	if (item.isEmpty()) {
        		viewHolder.titleText.setTextColor(Color.GRAY);
        		viewHolder.titleText.setText("Empty Slot");
        		viewHolder.speakerText.setVisibility(View.GONE);
        		viewHolder.trackText.setVisibility(View.GONE);
        		viewHolder.roomText.setVisibility(View.GONE);
        	} else {
        		viewHolder.titleText.setText(event.getTitle());
        		viewHolder.titleText.setEllipsize(TruncateAt.MARQUEE);
        		viewHolder.titleText.setTextColor(Color.BLACK);

        		viewHolder.speakerText.setVisibility(View.VISIBLE);
        		viewHolder.roomText.setVisibility(View.VISIBLE);
	        	String roomStr= event.getRoomName() + ", " + event.getLength() + " minutes";
	        	viewHolder.roomText.setText(roomStr);
	        	viewHolder.trackText.setVisibility(View.VISIBLE);
	        	viewHolder.trackText.setText(event.getTrackName());
	        	viewHolder.trackText.setTextColor(item.getColor());
	
	            List<Speaker> speakers = event.getSpeakers();
	            String speakersStr = "";
	            if (speakers.size() > 0) {
	            	speakersStr = speakers.get(0).getName();
	            }
	            
	            if (speakers.size() >= 2) {
	            	speakersStr = speakersStr + ", " + speakers.get(1).getName();
	            } 
	            
	            if (speakers.size() > 2) {
	            	speakersStr = speakersStr + " and others";
	            }
	            
	            if (speakers.size() == 0)
	            	viewHolder.speakerText.setVisibility(View.GONE);
	            else
	            	viewHolder.speakerText.setText(speakersStr);
        	}
        }
        

        return root;
	}

	@Override
	public int getPositionForSection(int section) {
		return mIndexerPositionsForSection[section];
	}

	@Override
	public int getSectionForPosition(int position) {
//		return mIndexerSectionsForPosition[position];
		return 0;
	}

	@Override
	public Object[] getSections() {
		return mIndexerSections;
	}

}
