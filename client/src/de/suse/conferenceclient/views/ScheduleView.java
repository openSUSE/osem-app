/**
 * 
 */
package de.suse.conferenceclient.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.ChartRenderingInfo;
import org.afree.chart.axis.DateAxis;
import org.afree.chart.axis.SymbolAxis;
import org.afree.chart.labels.StandardCategoryItemLabelGenerator;
import org.afree.chart.labels.StandardXYItemLabelGenerator;
import org.afree.chart.labels.XYItemLabelGenerator;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYBarRenderer;
import org.afree.data.xy.XYDataset;
import org.afree.data.xy.XYIntervalSeries;
import org.afree.data.xy.XYIntervalSeriesCollection;
import org.afree.graphics.SolidColor;
import org.afree.graphics.geom.RectShape;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.models.Event;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */

// TODO scrolling along the Y axis does not work

public class ScheduleView extends View {
	public interface OnEventClickListener {
		public void clicked(Event event);
	}
	
	private class HourIterator
	   implements Iterator<Date>, Iterable<Date>
	{
	    private Calendar end = Calendar.getInstance();
	    private Calendar current = Calendar.getInstance();

	    public HourIterator(Date start, Date end)
	    {
	        this.end.setTime(end);
	        this.current.setTime(start);
	        this.current.add(Calendar.HOUR_OF_DAY, -1);
	    }

	    public boolean hasNext()
	    {
	        return !current.after(end);
	    }

	    public Date next()
	    {
	        current.add(Calendar.HOUR_OF_DAY, 1);
	        return current.getTime();
	    }

	    public void remove()
	    {
	        throw new UnsupportedOperationException(
	           "Cannot remove");
	    }

	    public Iterator<Date> iterator()
	    {
	        return this;
	    }
	}
	
	private class DisplayItem {
		float mX, mY;
		String mLabel;
		int mLength;
		RectF mBox;
		int mTrackColor;
		Event mEvent;
		private String mGuid;
		
		public DisplayItem(String label) {
			this.mX = 0;
			this.mY = 0;
			this.mLabel = label;
		}
		
		public boolean inBox(float x, float y) {
			return mBox.contains(x, y);
		}

		public float getX() {
			return mX;
		}
		public void setX(float x) {
			mX = x;
		}
		public float getY() {
			return mY;
		}
		public void setY(float y) {
			mY = y;
		}
		public String getLabel() {
			return mLabel;
		}

		public int getLength() {
			return mLength;
		}

		public void setLength(int length) {
			mLength = length;
		}

		public RectF getBox() {
			return mBox;
		}

		public void setBox(RectF box) {
			mBox = box;
		}

		public int getTrackColor() {
			return mTrackColor;
		}

		public void setTrackColor(int trackColor) {
			mTrackColor = trackColor;
		}

		public Event getEvent() {
			return mEvent;
		}

		public void setEvent(Event event) {
			mEvent= event;
		}

		public String getGuid() {
			return mGuid;
		}

		public void setGuid(String guid) {
			mGuid = guid;
		}


	}
	private Paint mHourPainter, mRoomPainter, mLinePainter;
	private Paint mBoxPainter, mLabelPainter, mBoxColorPainter;
	private Paint mHeaderPainter, mBoxBackgroundPainter;
	private TextPaint mLabelTextPainter;
	
	private List<Event> mEventList;
	private List<DisplayItem> mTimeList;
	private List<DisplayItem> mRoomList;
	private List<DisplayItem> mEventDisplayList;
	
	// Pixel width of "one minute" on the timeline
	private int MAGIC_MULTIPLIER = 4;
	private int MAGIC_HOUR = 60 * MAGIC_MULTIPLIER;
	private int EVENT_BOX_HEIGHT = 30;
	private boolean mVertical;
	private RectF mHourHeader;
	private float mWindowWidth = 0;
	private float mWindowHeight = 0;
	private float mHourStartX = 0;
	private float mHourStartY = 30;
	private float mMaximumScrollwidth = 0;
	private float mEndTimeEdge = 0;
	private float mHourHeaderHeight = 40;
	private float mRoomStartText = 0;
	private float mTranslateX = 0;
	private float mStartX = 0;
	private OnEventClickListener mListener = null;
	private GestureDetector mGestureDetector;
	Date mStartDate, mEndDate;
	
	private HashMap<String, Event> mEventMap;
	
	public ScheduleView(Context context) {
		super(context);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public ScheduleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO draw a different layout in portrait mode
		mVertical = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
		
		mBoxPainter = new Paint();
		mBoxPainter.setAntiAlias(true);
		mBoxPainter.setTextSize(20);
		mBoxPainter.setColor(getResources().getColor(R.color.bright_suse_green));
		mBoxPainter.setStyle(Paint.Style.STROKE);
		
		mBoxColorPainter = new Paint();
		mBoxColorPainter.setStyle(Paint.Style.FILL);
		mBoxColorPainter.setAntiAlias(true);

		mBoxBackgroundPainter = new Paint();
		mBoxBackgroundPainter.setStyle(Paint.Style.FILL);
		mBoxBackgroundPainter.setAntiAlias(false);
		mBoxBackgroundPainter.setColor(getResources().getColor(R.color.light_suse_grey));

		mLinePainter = new Paint();
		mLinePainter.setAntiAlias(true);
		mLinePainter.setColor(getResources().getColor(R.color.light_suse_grey));
		mLinePainter.setStyle(Paint.Style.FILL);
		mLinePainter.setPathEffect(new DashPathEffect(new float[] {5,5}, 0));
		
		mHeaderPainter = new Paint();
		mHeaderPainter.setAntiAlias(true);
		mHeaderPainter.setStyle(Paint.Style.FILL);
		mHeaderPainter.setColor(getResources().getColor(R.color.light_suse_grey));

		mHourPainter = new Paint();
		mHourPainter.setAntiAlias(true);
		mHourPainter.setTextSize(20);
		mHourPainter.setTextAlign(Paint.Align.CENTER);
		mHourPainter.setTypeface(Typeface.DEFAULT_BOLD);
		mHourPainter.setColor(getResources().getColor(R.color.dark_suse_green));
		
		mRoomPainter = new Paint();
		mRoomPainter.setAntiAlias(true);
		mRoomPainter.setTextSize(15);
		mRoomPainter.setColor(getResources().getColor(R.color.dark_suse_green));
		mRoomPainter.setTextAlign(Paint.Align.RIGHT);

		mLabelPainter = new Paint();
		mLabelPainter.setAntiAlias(true);
		mLabelPainter.setTextSize(15);
		mLabelPainter.setColor(getResources().getColor(R.color.dark_suse_green));
		mLabelPainter.setTypeface(Typeface.DEFAULT_BOLD);
		mLabelPainter.setTextAlign(Paint.Align.CENTER);
		
		mLabelTextPainter = new TextPaint();
		mLabelTextPainter.setAntiAlias(true);
		mLabelTextPainter.setTextSize(15);
		mLabelTextPainter.setTypeface(Typeface.DEFAULT_BOLD);
		mLabelTextPainter.setColor(getResources().getColor(R.color.dark_suse_green));
		mLabelTextPainter.setTextAlign(Paint.Align.CENTER);
		
		mEventMap = new HashMap<String, Event>();
		mTimeList = new ArrayList<DisplayItem>();
		mRoomList = new ArrayList<DisplayItem>();
		mHourHeader = new RectF(0,0, getWidth(), mHourHeaderHeight);
		
		mEventDisplayList = new ArrayList<DisplayItem>();
		mStartDate = null;
		mEndDate = null;
		mGestureDetector = new GestureDetector(context, new TouchDetector());
		setEvents(new ArrayList<Event>());
	}
	
	public void setOnEventClickListener(OnEventClickListener listener) {
		this.mListener = listener;
	}
	
	public void setEvents(List<Event> eventList) {
		mEventList = eventList;
		mEventMap.clear();
		mTimeList.clear();
		mRoomList.clear();
		mEventDisplayList.clear();
		mStartDate = null;
		mEndDate = null;
		
		Calendar cal = new GregorianCalendar();
		HashMap<String, Integer> roomYMap = new HashMap<String, Integer>();
		HashMap<String, Float> hourXMap = new HashMap<String, Float>();
		
		// Build up the list of rooms on the left
		int roomY = 50;
		for (Event event : mEventList) {		
			if (mStartDate == null || mStartDate.after(event.getDate()))
				mStartDate = event.getDate();

			if (mEndDate == null || mEndDate.before(event.getEndDate()))
				mEndDate = event.getEndDate();
			
			String room = event.getRoomName();
			if (!roomYMap.containsKey(room)) {
				
				// Make sure that the time display doesn't overlap
				float roomSize = mRoomPainter.measureText(room);
				if (roomSize > mHourStartX)
					mHourStartX = roomSize + 25;

				roomYMap.put(room, roomY);
				DisplayItem newRoom = new DisplayItem(room);
				newRoom.setX(10);
				newRoom.setY(roomY + (EVENT_BOX_HEIGHT / 2));
				mRoomList.add(newRoom);
				roomY += 50;
			}
		}
		
		// Room texts are right-aligned
		mRoomStartText = mHourStartX - 10;
		
		// Now build up the timeline list
		if (mStartDate != null && mEndDate != null) {
			Iterator<Date> i = new HourIterator(mStartDate, mEndDate);
			int hourCount = 0;
	    	while(i.hasNext())
	    	{
	    		Date date = i.next();
	    		cal.setTime(date);
	    		int hour = cal.get(Calendar.HOUR_OF_DAY);
	    		String hourText = Integer.toString(hour);
	    		float x = mHourStartX + (MAGIC_HOUR * hourCount);
	    		float rx = x + MAGIC_HOUR;
	    		
	    		hourXMap.put(hourText, x);
	    		if (hour < 10)
	    			hourText = "0" + hourText;
	    		hourText = hourText + ":00";
	    		
	    		DisplayItem newHour = new DisplayItem(hourText);
	    		// The time is centered, so put it in the middle of the box
	    		newHour.setX(x + MAGIC_HOUR / 2);
	    		newHour.setY(mHourStartY);
	    		newHour.setBox(new RectF(x, mHourStartY, rx, mHourStartY + mHourHeaderHeight));
	    		// Don't let the user scroll too far right
	    		if (!i.hasNext()) {
	    			mEndTimeEdge = rx;
	    	        mMaximumScrollwidth = mEndTimeEdge - mWindowWidth;
	    	        // This is really only useful for My Schedule - 
	    	        // if the user un-favorites an item that's far to the right,
	    	        // we want to cleanly reset their scroll width
	    	        mTranslateX = 0;
	    		}
	    		mTimeList.add(newHour);
	    		hourCount++;
	    	}
		}
		
		// Finally, our actual events
		for (Event event : mEventList) {
			cal.setTime(event.getDate());
			int minute = cal.get(Calendar.MINUTE);
			float x = hourXMap.get(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
			x += (minute * MAGIC_MULTIPLIER);
			
			int y = roomYMap.get(event.getRoomName());
			DisplayItem newEvent = new DisplayItem(event.getTitle());
			newEvent.setX(x);
			newEvent.setY(y);
			newEvent.setEvent(event);
			newEvent.setGuid(event.getGuid());
			int length = event.getLength();
			newEvent.setLength(length);
			
			float boxX = x;
			float boxY = y;
			float boxRx = x + (length * MAGIC_MULTIPLIER) - 10;
			float boxRy = y + EVENT_BOX_HEIGHT;
			newEvent.setBox(new RectF(boxX, boxY, boxRx, boxRy));
			
			newEvent.setTrackColor(Color.parseColor(event.getColor()));
			mEventDisplayList.add(newEvent);
		}
		
		invalidate();
	}

	@Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mWindowWidth = w;
        mWindowHeight = w;
        mMaximumScrollwidth = mEndTimeEdge - mWindowWidth;
		mHourHeader.set(0, 0, mEndTimeEdge, mHourHeaderHeight);
    }
	
	private void drawEvent(Canvas canvas, DisplayItem event) {
		RectF box = event.getBox();
		RectF colorBox = new RectF(box.left, box.top, box.left + 20, box.bottom);
		
		// Background
		canvas.drawRoundRect(box, 5, 5, mBoxBackgroundPainter);

		// Track color
		mBoxColorPainter.setColor(event.getTrackColor());
		canvas.drawRoundRect(colorBox, 5, 5, mBoxColorPainter);
		
		// Outline box
		mBoxPainter.setColor(getResources().getColor(R.color.light_suse_green));
		mBoxPainter.setStyle(Paint.Style.STROKE);
		canvas.drawRoundRect(box, 5, 5, mBoxPainter);

		// Text
		float avail = box.right - box.left - 40;
		String label = (String) TextUtils.ellipsize(event.getLabel(), mLabelTextPainter, avail, TextUtils.TruncateAt.END);
		canvas.drawText(label, box.centerX(), box.centerY() + 5, mLabelTextPainter);
	}
	
	private void drawHour(Canvas canvas, DisplayItem event) {
		
		RectF displayBox = event.getBox();
		canvas.drawText(event.getLabel(), event.getX(), event.getY(), mHourPainter);
		canvas.drawLine(displayBox.left, displayBox.top, displayBox.left, canvas.getHeight(), mLinePainter);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvas == null) return;
		
		canvas.save();
        canvas.translate(mTranslateX, 0);
        
		// Set the header background
		canvas.drawRect(mHourHeader, mHeaderPainter);

		for (DisplayItem hourItem : mTimeList) {
			drawHour(canvas, hourItem);
		}
		for (DisplayItem roomItem : mRoomList) {
			canvas.drawText(roomItem.getLabel(), mRoomStartText, roomItem.getY(), mRoomPainter);
		}
		
		for (DisplayItem eventItem : mEventDisplayList) {
			drawEvent(canvas, eventItem);
		}
		canvas.restore();
	}

	// Touch handling
    private class TouchDetector extends SimpleOnGestureListener
    {
			@Override
            public boolean onDown(MotionEvent event)
            {
                    mStartX =  event.getX();
                    return true;
            }
			
            @Override
            public boolean onSingleTapUp(MotionEvent event)
            {
            	float translatedX = event.getX() - mTranslateX;
            	
            	for (DisplayItem eventItem : mEventDisplayList) {
            		if (eventItem.inBox(translatedX, event.getY()))
            			if (mListener != null)
            				mListener.clicked(eventItem.getEvent());
            	}

            	return true;
            }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {               
    	switch(event.getAction()) {
        case MotionEvent.ACTION_MOVE:
            if (event.getPointerCount() > 1)
                break;
            
            float transX = mTranslateX - (mStartX - event.getX());

            // Keep them from going too far right            
            if (transX > -mMaximumScrollwidth)
            	mTranslateX = transX;
            
            // Don't let the user go too far left
            if (mTranslateX > 0)
            	mTranslateX = 0;

            mStartX = event.getX();
            invalidate();
        	break;
	    default:
	    	mGestureDetector.onTouchEvent(event);
	    	break;

    	}
    	return true;
    }
    
}
