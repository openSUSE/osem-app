/**
 * 
 */
package de.suse.conferenceclient.views;

import java.util.List;

import de.suse.conferenceclient.models.Event;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleView extends View {
	private List<Event> mEventList;
	
	public ScheduleView(Context context) {
		super(context);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public ScheduleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void setEvents(List<Event> eventList) {
		mEventList = eventList;
	}
	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
	int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
	int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
	this.setMeasuredDimension(parentWidth/2, parentHeight / 4);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvas == null) return;
		canvas.save();
		
		canvas.restore();
	}
}
