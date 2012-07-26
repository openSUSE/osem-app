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

public class ScheduleView extends View {
	private class HourIterator
	   implements Iterator<Date>, Iterable<Date>
	{
	    private Calendar end = Calendar.getInstance();
	    private Calendar current = Calendar.getInstance();

	    public HourIterator(Date start, Date end)
	    {
	        this.end.setTime(end);
//	        this.end.add(Calendar.HOUR_OF_DAY, 1);
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
		
		public DisplayItem(String label) {
			this.mX = 0;
			this.mY = 0;
			this.mLabel = label;
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


	}
	private Paint mHourPainter, mRoomPainter, mLinePainter;
	private Paint mBoxPainter, mLabelPainter, mBoxColorPainter;
	private Paint mHeaderPainter;
	private TextPaint mLabelTextPainter;
	
	private List<Event> mEventList;
	private List<DisplayItem> mTimeList;
	private List<DisplayItem> mRoomList;
	private List<DisplayItem> mEventDisplayList;
	
	// Pixel width of "one minute" on the timeline
	private int MAGIC_MULTIPLIER = 4;
	private int MAGIC_HOUR = 60 * MAGIC_MULTIPLIER;
	private int EVENT_BOX_HEIGHT = 30;
	private int mScreenW = 100;
	private int mScreenH = 100;
	private float mHourStartX = 0;
	private float mHourStartY = 30;
	private float mMaximumScrollwidth = 0;
	private float mEndTimeEdge = 0;
	private RectF mHourHeader;
	private float mHourHeaderHeight = 40;
	private float mRoomStartText = 0;
	private float mTranslateX = 0;
	private float mStartX = 0;
	private GestureDetector mGestureDetector;
	Date mStartDate, mEndDate;
	
	private HashMap<String, Integer> mTrackAxisMap;
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
		
		mBoxPainter = new Paint();
		mBoxPainter.setAntiAlias(true);
		mBoxPainter.setTextSize(20);
		mBoxPainter.setColor(getResources().getColor(R.color.light_suse_grey));
		mBoxPainter.setStyle(Paint.Style.STROKE);
		
		mBoxColorPainter = new Paint();
		mBoxColorPainter.setStyle(Paint.Style.FILL);
		mBoxColorPainter.setAntiAlias(true);

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
		mHourPainter.setColor(Color.BLACK);
		
		mRoomPainter = new Paint();
		mRoomPainter.setAntiAlias(true);
		mRoomPainter.setTextSize(15);
		mRoomPainter.setColor(Color.GRAY);
		mRoomPainter.setTextAlign(Paint.Align.RIGHT);

		mLabelPainter = new Paint();
		mLabelPainter.setAntiAlias(true);
		mLabelPainter.setTextSize(15);
		mLabelPainter.setColor(Color.BLACK);
		mLabelPainter.setTypeface(Typeface.DEFAULT_BOLD);
		mLabelPainter.setTextAlign(Paint.Align.CENTER);
		
		mLabelTextPainter = new TextPaint();
		mLabelTextPainter.setAntiAlias(true);
		mLabelTextPainter.setTextSize(15);
		mLabelTextPainter.setTypeface(Typeface.DEFAULT_BOLD);
		mLabelTextPainter.setTextAlign(Paint.Align.CENTER);
		
		mTrackAxisMap = new HashMap<String, Integer>();
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
	
	public void setEvents(List<Event> eventList) {
		mEventList = eventList;   
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
			int length = event.getLength();
			newEvent.setLength(length);
			
			float boxX = x;
			float boxY = y;
			float boxRx = x + (length * MAGIC_MULTIPLIER);
			float boxRy = y + EVENT_BOX_HEIGHT;
			newEvent.setBox(new RectF(boxX, boxY, boxRx, boxRy));
			
			newEvent.setTrackColor(Color.parseColor(event.getColor()));
			mEventDisplayList.add(newEvent);
		}
		
		invalidate();
	}
//	
//	@Override 
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
//	int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
//	int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
//	this.setMeasuredDimension(parentWidth/2, parentHeight / 4);
//	}

	@Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mMaximumScrollwidth = mEndTimeEdge - w;
		mHourHeader.set(0, 0, mEndTimeEdge, mHourHeaderHeight);
    }
	
	private void drawEvent(Canvas canvas, DisplayItem event) {
		RectF box = event.getBox();
		RectF colorBox = new RectF(box.left, box.top, box.left + 20, box.bottom);
		
		// Background
		mBoxPainter.setColor(Color.WHITE);
		mBoxPainter.setStyle(Paint.Style.FILL);
		canvas.drawRoundRect(box, 5, 5, mBoxPainter);

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
        case MotionEvent.ACTION_UP:
            mGestureDetector.onTouchEvent(event);
            break;
	    default:
	    	mGestureDetector.onTouchEvent(event);
	    	break;

    	}
    	return true;
    }
    
}
