/**
 * 
 */
package de.suse.conferenceclient.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.views.ScheduleView;
import de.suse.conferenceclient.views.ScheduleView.OnEventClickListener;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
@TargetApi(9)
public class ScheduleFragment extends SherlockFragment implements OnEventClickListener {
	public interface OnEventListener {
		public void eventClicked(Event event);
	}
	
	public interface OnGetEventsListener {
		public List<Event> getEvents();
	}
	
    private Database db;
    private long conferenceId;
    
    private HashMap<String, List<Event>> dailyEvents;
    private List<TextView> dailyTextViews;
    private TextView mActiveDay;
    private int mDarkText, mLightText;
    private ScheduleView mScheduleView;
    private TextView mAgendaTitle;
    private boolean mMySchedule = false;
    private List<Event> mEventList;
    private OnEventListener mListener;
    private OnGetEventsListener mGetEventsListener;
    
    public ScheduleFragment() {
    }

    public ScheduleFragment(long conferenceId, boolean mySchedule) {
    	Log.d("SUSEConferences", "new ScheduleFragment");
        this.mMySchedule = mySchedule;
		this.conferenceId = conferenceId;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mGetEventsListener = (OnGetEventsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnGetEventsListener");
        }
        
        try {
            mListener = (OnEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnEventListener");
        }
    }
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		getActivity().getActionBar().setTitle("Schedule");
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agenda, container, false);
        mScheduleView = (ScheduleView) view.findViewById(R.id.scheduleView);
        mScheduleView.setOnEventClickListener(this);
        mAgendaTitle = (TextView) view.findViewById(R.id.agendaTextView);
        dailyEvents = new HashMap<String, List<Event>>();
        dailyTextViews = new ArrayList<TextView>();
        mDarkText = getResources().getColor(R.color.dark_suse_green);
        mLightText = getResources().getColor(R.color.light_suse_green);
        return view;
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	mEventList = mGetEventsListener.getEvents();
    	setEventList(null);
    }
    
    public void toggleFavorite(boolean checked, Event event) {
		if (mMySchedule) {
			String text = mActiveDay.getText().toString();
			List<Event> eventList = dailyEvents.get(text);

			if (checked) {
				eventList.add(event);
			} else {
				eventList.remove(event);
				mEventList.remove(event);
			}
			// TODO orientation
			mScheduleView.setEvents(eventList, false);
		}
    }
    
    public void setEventList(List<Event> eventList) {
    	if (eventList != null)
    		mEventList = eventList;
    	
    	View view = getView();
    	Log.d("SUSEConferences", "setEventList");
        GregorianCalendar cal = new GregorianCalendar();
        LinearLayout daysLayout = (LinearLayout) view.findViewById(R.id.datesLayout);
		this.db = SUSEConferences.getDatabase();

		for (Event event : mEventList) {
			cal.setTime(event.getDate());
			cal.setTimeZone(event.getTimeZone());
			String month = cal.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.SHORT, Locale.getDefault());
			String day = String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH));
			String dayMonth = month + " " + day;
			
			if (!dailyEvents.containsKey(dayMonth)) {
				List<Event> newList = new ArrayList<Event>();
				newList.add(event);
				dailyEvents.put(dayMonth, newList);
			} else {
				List<Event> list = dailyEvents.get(dayMonth);
				list.add(event);
			}
		}
		
		// Now sort the date keys so they look appropriate
		Vector<String> dateVector = new Vector<String>(dailyEvents.keySet()); 
		Collections.sort(dateVector);
		for (String day : dateVector) {
			TextView newDay = new TextView(getActivity());
			newDay.setText(day);
			newDay.setTextSize(25);
			newDay.setClickable(true);
			newDay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setDay((TextView) v);
				}
			});
			
			newDay.setPadding(0, 0, 10, 0);
			daysLayout.addView(newDay);
			dailyTextViews.add(newDay);
		}
		
		if (dailyTextViews.size() > 0)
			setDay(dailyTextViews.get(0));
    }
    
	private void setDay(TextView day) {
		mActiveDay = day;
		day.setTextColor(mDarkText);
		for (TextView view : dailyTextViews) {
			if (view != day) {
				view.setTextColor(mLightText);
			}
		}
		
		String text = day.getText().toString();
		if (mMySchedule)
			mAgendaTitle.setText(getResources().getString(R.string.myAgendaFor) + " " + text);
		else
			mAgendaTitle.setText(getResources().getString(R.string.agendaFor) + " " + text);

		// TODO orientation for the calendar
		mScheduleView.setEvents(dailyEvents.get(text), false);
	}

	@Override
	public void clicked(Event event) {
		this.mListener.eventClicked(event);
	}
}
