/**
 * 
 */
package de.suse.conferenceclient.app;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleViewPager extends ViewPager {

	/**
	 * @param context
	 * @param attrs
	 */
	public ScheduleViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	private boolean enabled;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }
  
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }
 
        return false;
    }
 
    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
