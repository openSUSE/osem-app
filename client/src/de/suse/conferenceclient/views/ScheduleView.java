/**
 * 
 */
package de.suse.conferenceclient.views;

import android.text.format.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
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
	
	private class DisplayItem implements Comparable<DisplayItem> {
		float mX, mY;
		String mLabel;
		int mLength;
		RectF mBox;
		int mTrackColor;
		Event mEvent;
		private String mGuid;
		private StaticLayout mLayout;
		
		public DisplayItem(String label) {
			this.mX = 0;
			this.mY = 0;
			this.mLabel = label;
		}
		
		public void setLayout(TextPaint painter, int boxSize) {
			mLayout = new StaticLayout(mLabel,
									painter,
									boxSize,
									Layout.Alignment.ALIGN_NORMAL,
									1, 5.0f, false);
		}
		public StaticLayout getLayout() {
			return mLayout;
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

		@Override
		public int compareTo(DisplayItem another) {
			return mLabel.compareTo(another.getLabel());
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
	private int MAGIC_MULTIPLIER = 6;
	private int MAGIC_HOUR = 60 * MAGIC_MULTIPLIER;
	private int EVENT_BOX_HEIGHT = 30;
	private int SUSE_GREEN = 0;
	private final int TRACK_COLOR_BOX_WIDTH = 20;
	private boolean mVertical, mIsMoving;
	private RectF mTopHeader, mLeftBox, mTopLeftBox;
	private float mWindowWidth = 0;
	private float mWindowHeight = 0;
	private float mLeftColumnStartX = 0;
	private float mLeftColumnStartY = 30;
	private float mLeftColumnWidth = 0;
	private float mTopRowItemStartX = 0;
	private float mTopRowItemStartY = 30;
	private float mMaximumScrollWidth = 0;
	private float mMaximumScrollHeight = 0;
	private float mEndRightEdge = 0;
	private float mEndBottomEdge = 0;
	private float mHourHeaderHeight = 40;
	private float mRoomStartText = 0;
	private float mTranslateX = 0;
	private float mTranslateY = 0;
	private float mStartX = 0;
	private float mStartY = 0;
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
		if (mVertical) {
			mLeftColumnStartX = 30;
			mLeftColumnStartY = 60;
			MAGIC_MULTIPLIER = 6;
			MAGIC_HOUR = 60 * MAGIC_MULTIPLIER;
		}
		SUSE_GREEN = getResources().getColor(R.color.light_suse_green);
		mIsMoving = false;
		
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
		mHeaderPainter.setColor(getResources().getColor(R.color.suse_grey));

		mHourPainter = new Paint();
		mHourPainter.setAntiAlias(true);
		mHourPainter.setTextSize(20);
		mHourPainter.setTextAlign(Paint.Align.CENTER);
		mHourPainter.setTypeface(Typeface.DEFAULT_BOLD);
		mHourPainter.setColor(getResources().getColor(R.color.dark_suse_green));
		
		mRoomPainter = new Paint();
		mRoomPainter.setAntiAlias(true);
		mRoomPainter.setTextSize(20);
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
		mTopHeader = new RectF(0,0, getWidth(), mHourHeaderHeight);
		mLeftBox = new RectF(0,0, mTopRowItemStartX, getHeight());
		mTopLeftBox = new RectF(0,0, 0,0);
		mEventDisplayList = new ArrayList<DisplayItem>();
		mStartDate = null;
		mEndDate = null;
		mGestureDetector = new GestureDetector(context, new TouchDetector());
		setEvents(new ArrayList<Event>(), true);
	}
	
	public void setOnEventClickListener(OnEventClickListener listener) {
		this.mListener = listener;
	}
	
	public void setEvents(List<Event> eventList, boolean fullSchedule) {
		mEventList = eventList;
		mEventMap.clear();
		mTimeList.clear();
		mRoomList.clear();
		mEventDisplayList.clear();
		mStartDate = null;
		mEndDate = null;

		if (mVertical) {
			setVerticalView(eventList, fullSchedule);
		} else {
			setHorizontalView(eventList, fullSchedule);
		}
		invalidate();
	}

	private void setVerticalView(List<Event> eventList, boolean fullSchedule) {
		HashMap<String, Float> roomMap = new HashMap<String, Float>();
		HashMap<String, Float> hourMap = new HashMap<String, Float>();
		mRoomPainter.setTextAlign(Paint.Align.CENTER);
		mTopRowItemStartX = 0;
		mTopRowItemStartY = 30;
		mLeftColumnStartX = 30;
		mLeftColumnStartY = 60;
		
		java.text.DateFormat timeFormatter = DateFormat.getTimeFormat(getContext());
		Calendar cal = new GregorianCalendar();
		float boxSize = 100;
		if (eventList.size() > 0) {
			timeFormatter.setTimeZone(eventList.get(0).getTimeZone());
			cal.setTimeZone(eventList.get(0).getTimeZone());
		}

		// Room titles along the top
		for (Event event : mEventList) {		
			if (mStartDate == null || mStartDate.after(event.getDate()))
				mStartDate = event.getDate();

			if (mEndDate == null || mEndDate.before(event.getEndDate()))
				mEndDate = event.getEndDate();
			
			String room = event.getRoomName();
			if (!roomMap.containsKey(room)) {
				roomMap.put(room, mTopRowItemStartX);
				DisplayItem newRoom = new DisplayItem(room);
				float roomWidth = mRoomPainter.measureText(room);
				if (roomWidth > boxSize)
					boxSize = roomWidth;
				newRoom.setX(0);
				newRoom.setY(mTopRowItemStartY);
				mRoomList.add(newRoom);
			}
		}

		// Build up the times on the left
		if (mStartDate != null && mEndDate != null) {
			Iterator<Date> i = new HourIterator(mStartDate, mEndDate);
			int hourCount = 0;
	    	while(i.hasNext())
	    	{
	    		Date date = i.next();
	    		cal.setTime(date);
	    		int hour = cal.get(Calendar.HOUR_OF_DAY);
	    		String hourText = timeFormatter.format(date);
	    		float hourTextSize = mHourPainter.measureText(hourText);
	    		if (hourTextSize > mTopRowItemStartX) {
	    			mTopRowItemStartX = hourTextSize + 20;
	    		}
	    		float x = mLeftColumnStartX;
	    		float y = mLeftColumnStartY + (MAGIC_HOUR * hourCount);

	    		hourMap.put(Integer.toString(hour), y);    		
	    		DisplayItem newHour = new DisplayItem(hourText);
	    		newHour.setX(x);
	    		newHour.setY(y);
	    		newHour.setBox(new RectF(x, y, 50, 50));

	    		mTimeList.add(newHour);
	    		hourCount++;
	    	}
	    	mLeftColumnWidth = mTopRowItemStartX;
	    	
	    	mEndBottomEdge = mLeftColumnStartY + (MAGIC_HOUR * hourCount);
		}
		
//		mTopRowItemStartX = mTopRowItemStartX + boxSize;
		Collections.sort(mRoomList);
		for (DisplayItem room : mRoomList) {
			roomMap.put(room.getLabel(), mTopRowItemStartX);
			room.setX(mTopRowItemStartX + (boxSize / 2));
			mTopRowItemStartX = mTopRowItemStartX + boxSize;
		}
		
		mEndRightEdge = mTopRowItemStartX;
        mMaximumScrollWidth = mTopRowItemStartX;
        
		for (Event event : mEventList) {
			cal.setTime(event.getDate());
			int minute = cal.get(Calendar.MINUTE);
			float y = hourMap.get(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
			y += (minute * MAGIC_MULTIPLIER);
			
			float x = roomMap.get(event.getRoomName());
			DisplayItem newEvent = new DisplayItem(event.getTitle());
			newEvent.setX(x);
			newEvent.setY(y);
			newEvent.setEvent(event);
			newEvent.setGuid(event.getGuid());
			int length = event.getLength();
			newEvent.setLength(length);
			
			float boxX = x;
			float boxY = y;
			float boxRx = x + boxSize;
			float boxRy = y + (length * MAGIC_MULTIPLIER) - 10;

			newEvent.setBox(new RectF(boxX, boxY, boxRx, boxRy));
			newEvent.setLayout(mLabelTextPainter, (int) boxSize - TRACK_COLOR_BOX_WIDTH - 20);
			newEvent.setTrackColor(Color.parseColor(event.getColor()));
			if (fullSchedule || event.isInMySchedule() || event.isMetaInformation())
				mEventDisplayList.add(newEvent);
		}

	}
	private void setHorizontalView(List<Event> eventList, boolean fullSchedule) {
		mLeftColumnStartX = 0;
		mLeftColumnStartY = 30;
		mLeftColumnWidth = 0;

		java.text.DateFormat timeFormatter = DateFormat.getTimeFormat(getContext());
		Calendar cal = new GregorianCalendar();

		if (eventList.size() > 0) {
			timeFormatter.setTimeZone(eventList.get(0).getTimeZone());
			cal.setTimeZone(eventList.get(0).getTimeZone());
		}

		HashMap<String, Integer> roomMap = new HashMap<String, Integer>();
		HashMap<String, Float> hourMap = new HashMap<String, Float>();
		// Room texts are right-aligned
		mRoomStartText = mLeftColumnStartX - 10;

		// Build up the list of rooms on the left
		for (Event event : mEventList) {		
			if (mStartDate == null || mStartDate.after(event.getDate()))
				mStartDate = event.getDate();

			if (mEndDate == null || mEndDate.before(event.getEndDate()))
				mEndDate = event.getEndDate();
			
			String room = event.getRoomName();
			if (!roomMap.containsKey(room)) {
				// Make sure that the time display doesn't overlap
				float roomSize = mRoomPainter.measureText(room);
				if (roomSize > mLeftColumnStartX) {
					mLeftColumnStartX = roomSize + 25;
					mRoomStartText = mLeftColumnStartX - 10;
				}
				
				roomMap.put(room, 0);
				DisplayItem newRoom = new DisplayItem(room);
				newRoom.setX(mRoomStartText);
				mRoomList.add(newRoom);
			}
	    	mLeftColumnWidth = mLeftColumnStartX;

		}
		
		// Now sort the rooms alphabetically, and assign the correct Y values
		Collections.sort(mRoomList);
		int roomY = 50;
		for (DisplayItem room : mRoomList) {
			roomMap.put(room.getLabel(), roomY);
			room.setX(mRoomStartText);
			room.setY(roomY + (EVENT_BOX_HEIGHT / 2));
			roomY += 50;
		}

		
		// Now build up the timeline list
		if (mStartDate != null && mEndDate != null) {
			Iterator<Date> i = new HourIterator(mStartDate, mEndDate);
			int hourCount = 0;
	    	while(i.hasNext())
	    	{
	    		Date date = i.next();
	    		cal.setTime(date);
	    		int hour = cal.get(Calendar.HOUR_OF_DAY);
	    		String hourText = timeFormatter.format(date);
	    		
	    		float x = mLeftColumnStartX + (MAGIC_HOUR * hourCount);
	    		float rx = x + MAGIC_HOUR;
	    		
	    		hourMap.put(Integer.toString(hour), x);    		
	    		DisplayItem newHour = new DisplayItem(hourText);
	    		// The time is centered, so put it in the middle of the box
	    		newHour.setX(x + MAGIC_HOUR / 2);
	    		newHour.setY(mLeftColumnStartY);
	    		newHour.setBox(new RectF(x, mLeftColumnStartY, rx, mLeftColumnStartY + mHourHeaderHeight));
	    		// Don't let the user scroll too far right
	    		if (!i.hasNext()) {
	    			mEndRightEdge = rx;
	    	        mMaximumScrollWidth = mEndRightEdge - mWindowWidth;
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
			float x = hourMap.get(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
			x += (minute * MAGIC_MULTIPLIER);
			
			int y = roomMap.get(event.getRoomName());
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
			if (fullSchedule || event.isInMySchedule() || event.isMetaInformation())
				mEventDisplayList.add(newEvent);
		}
	}
	@Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mWindowWidth = w;
        mWindowHeight = h;
        mMaximumScrollWidth = mEndRightEdge - mWindowWidth;
        mMaximumScrollHeight = mEndBottomEdge - mWindowHeight;
		mTopHeader.set(0, 0, mEndRightEdge, mHourHeaderHeight);
		mLeftBox.set(0,0, mLeftColumnWidth, h);
		mTopLeftBox.set(0,0, mLeftColumnWidth, mHourHeaderHeight);
		Log.d("SUSEConferences", "mTopLeftBox is now "+ mTopLeftBox);
    }
	
	private void drawEvent(Canvas canvas, DisplayItem event) {
		RectF box = event.getBox();
		RectF colorBox = new RectF(box.left, box.top, box.left + TRACK_COLOR_BOX_WIDTH, box.bottom);
		
		// Background
		canvas.drawRoundRect(box, 5, 5, mBoxBackgroundPainter);

		// Track color
		mBoxColorPainter.setColor(event.getTrackColor());
		canvas.drawRoundRect(colorBox, 5, 5, mBoxColorPainter);
		
		// Outline box
		mBoxPainter.setColor(SUSE_GREEN);
		mBoxPainter.setStyle(Paint.Style.STROKE);
		canvas.drawRoundRect(box, 5, 5, mBoxPainter);

		Log.d("SUSEConferences", "CenterX: " + box.centerX());
		// Text
		if (mVertical) {
			canvas.save();
			canvas.translate(box.centerX(), box.top + 10);
			event.getLayout().draw(canvas);
			canvas.restore();
		} else {
			float avail = box.right - box.left - 40;
			String label = (String) TextUtils.ellipsize(event.getLabel(), mLabelTextPainter, avail, TextUtils.TruncateAt.END);
			canvas.drawText(label, box.centerX(), box.centerY(), mLabelTextPainter);
		}
	}
	
	private void drawHour(Canvas canvas, DisplayItem event) {
		RectF displayBox = event.getBox();
		canvas.drawText(event.getLabel(), event.getX(), event.getY(), mHourPainter);
		canvas.drawLine(displayBox.left, displayBox.top, displayBox.left, canvas.getHeight(), mLinePainter);
	}

	private void drawVertical(Canvas canvas) {
		canvas.save();
        canvas.translate(mTranslateX, mTranslateY);

		for (DisplayItem eventItem : mEventDisplayList) {
			drawEvent(canvas, eventItem);
		}
		canvas.restore();

		// Set the header backgrounds
		canvas.drawRect(mTopHeader, mHeaderPainter);
		canvas.drawRect(mLeftBox, mHeaderPainter);
		canvas.save();
		canvas.translate(0, mTranslateY);
		for (DisplayItem hourItem : mTimeList) {
			drawHour(canvas, hourItem);
		}
		canvas.restore();
		canvas.save();
		canvas.translate(mTranslateX, 0);
		for (DisplayItem roomItem : mRoomList) {
			canvas.drawText(roomItem.getLabel(), roomItem.getX(), roomItem.getY(), mRoomPainter);
		}
		canvas.restore();
		canvas.save();
		// Finally, a cosmetic fix so the hours don't scroll past the time header, and the time
		// doesn't scroll past the room column
		canvas.drawRect(mTopLeftBox, mHeaderPainter);
		canvas.restore();
	}
	
	
	private void drawHorizontal(Canvas canvas) {
		canvas.save();
        canvas.translate(mTranslateX, mTranslateY);

		for (DisplayItem eventItem : mEventDisplayList) {
			drawEvent(canvas, eventItem);
		}
		canvas.restore();

		// Set the header backgrounds
		canvas.drawRect(mTopHeader, mHeaderPainter);
		canvas.save();
		canvas.translate(mTranslateX, 0);
		for (DisplayItem hourItem : mTimeList) {
			drawHour(canvas, hourItem);
		}
		canvas.restore();

		canvas.drawRect(mLeftBox, mHeaderPainter);
		canvas.save();
		canvas.translate(0, mTranslateY);
		for (DisplayItem roomItem : mRoomList) {
			canvas.drawText(roomItem.getLabel(), roomItem.getX(), roomItem.getY(), mRoomPainter);
		}
		canvas.restore();
		

	}
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvas == null) return;
		if (mVertical)
			drawVertical(canvas);
		else
			drawHorizontal(canvas);
		
	}

	// Touch handling
    private class TouchDetector extends SimpleOnGestureListener
    {
			@Override
            public boolean onDown(MotionEvent event)
            {
				mStartX =  event.getX();
				mStartY = event.getY();
				return true;
            }
			
            @Override
            public boolean onSingleTapUp(MotionEvent event)
            {
            	Log.d("SUSEConferences", "singleTapUp");
            	if (mIsMoving) {
            		return true;
            	}
            	float translatedX = event.getX() - mTranslateX;
            	float translatedY = event.getY() - mTranslateY;
            	
            	for (DisplayItem eventItem : mEventDisplayList) {
            		if (eventItem.inBox(translatedX, translatedY)) {
            			if (eventItem.getEvent().isMetaInformation())
            				break;
            			if (mListener != null)
            				mListener.clicked(eventItem.getEvent());
            		}
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
            
            Log.d("SUSEConferences", "ACTION_MOVE");
            mIsMoving = true;
            float transX = mTranslateX - (mStartX - event.getX());
            float transY = mTranslateY - (mStartY - event.getY());
            
            // Keep them from going too far right
            if (transX > -mMaximumScrollWidth)
            	mTranslateX = transX;
            
            // Or too far below
            if (transY > -mMaximumScrollHeight)
            	mTranslateY = transY;
            
            // Don't let the user go too far left
            if (mTranslateX > 0)
            	mTranslateX = 0;

            // Or too far above
            if (mTranslateY > 0)
            	mTranslateY = 0;
            
            mStartX = event.getX();
            mStartY = event.getY();

            invalidate();
        	break;
        case MotionEvent.ACTION_UP:
        	if (mIsMoving) {
        		mIsMoving = false;
        		break;
        	}
        	mIsMoving = false;
        default:
	    	mGestureDetector.onTouchEvent(event);
	    	break;

    	}
    	return true;
    }
    
}
