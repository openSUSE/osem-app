/**
 * 
 */
package de.suse.conferenceclient.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.views.ScheduleView;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleActivity extends SherlockFragmentActivity {
    private Database db;
    private long conferenceId;
    
    private HashMap<String, List<Event>> dailyEvents;
    private List<TextView> dailyTextViews;
    private int mDarkText, mLightText;
    private ScheduleView mScheduleView;
    private TextView mAgendaTitle;
	@TargetApi(9)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_agenda);
        
        mScheduleView = (ScheduleView) findViewById(R.id.scheduleView);
        mAgendaTitle = (TextView) findViewById(R.id.agendaTextView);
        dailyEvents = new HashMap<String, List<Event>>();
        dailyTextViews = new ArrayList<TextView>();
        mDarkText = getResources().getColor(R.color.dark_suse_green);
        mLightText = getResources().getColor(R.color.light_suse_green);
        
        GregorianCalendar cal = new GregorianCalendar();
        LinearLayout daysLayout = (LinearLayout) findViewById(R.id.datesLayout);
        
		this.db = SUSEConferences.getDatabase();
		this.conferenceId = getIntent().getLongExtra("conferenceId", -1);
		List<Event> eventList = db.getScheduleTitles(conferenceId);

		for (Event event : eventList) {
			cal.setTime(event.getDate());
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
			TextView newDay = new TextView(this);
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
		day.setTextColor(mDarkText);
		for (TextView view : dailyTextViews) {
			if (view != day) {
				view.setTextColor(mLightText);
			}
		}
		
		String text = day.getText().toString();
		mAgendaTitle.setText(getResources().getString(R.string.agendaFor) + " " + text);
		mScheduleView.setEvents(dailyEvents.get(text));
	}
	
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}
