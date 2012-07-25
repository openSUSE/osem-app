/**
 * 
 */
package de.suse.conferenceclient.views;

import de.suse.conferenceclient.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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

public class WheelView extends View {
	public interface OnLaunch {
		public void launchActivity(int activity);
	}
	
	public static int ACTIVITY_MYSCHEDULE = 1;
	public static int ACTIVITY_SCHEDULE = 2;
	public static int ACTIVITY_MAPS = 3;
	public static int ACTIVITY_SOCIAL = 4;
	
	private WheelView me;
	private static Matrix mMatrix;
	private static Bitmap mBitmap;
	private static final Paint mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
	private float mCenterX, mCenterY;
	private float direction = 0;
	private float startX, startY;
	private float startDirection=0;
	private GestureDetector mDetector;
	private OnLaunch mListener;
	private int mSelectedActivity = -1;
	
	public WheelView(Context context) {
		super(context);
		setup(context);
	}

	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}
	
	public void setOnLaunchListener(OnLaunch listener) {
		mListener = listener;
	}
	
	private void setup(Context context) {
		me = this;
		mListener = null;
		mMatrix = new Matrix();
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
//		mDetector = new GestureDetector(context, new WheelGestureDetector());
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (canvas == null) return;

		canvas.save();
		canvas.rotate(direction, mCenterX, mCenterY);
		canvas.drawBitmap(mBitmap, mMatrix, mPainter);
		canvas.restore();
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {           
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            start(x, y);
            break;
        case MotionEvent.ACTION_MOVE:
            move(x, y);
            break;
        case MotionEvent.ACTION_UP:
            startDirection = direction;
            snap();
            break;
        }
        
//        mDetector.onTouchEvent(event);
        return true;
    }

    public double angle(float centerX,
    					float centerY,
    					float x1,
    					float y1,
    					float x2,
    					float y2) {
        double angle1 = Math.atan2(y1 - centerY, x1 - centerX);
        double angle2 = Math.atan2(y2 - centerY, x2 - centerX);
        return angle1 - angle2;
    }
    
	private void start(float x, float y) {
	    mCenterX = this.getWidth() / 2;
	    mCenterY = this.getHeight() / 2;
	    startX = x;
	    startY = y;
	}
	
	private void move(float x, float y) {
	    float angle = (float) angle(mCenterX, mCenterY, startX, startY, x, y);
	    direction = (float) Math.toDegrees(angle) * -1 + startDirection;
//	    if (direction >= 360.0)
//	    	direction = 0;
//	    else if (direction <= -360.0)
//	    	direction = 0;
	    Log.d("SUSEConferences", "Direction: " + direction);
	    this.invalidate();
	}
	
	// TODO there must be a better way to do this!
	private void snap() {
		int rounded = Math.round(startDirection);
		
		if (rounded >= 0 && rounded <= 45) {
			// It's the dashboard
			me.post(new WheelAnimation(0));
		} else if (rounded >=46 && rounded <= 135) {
			// It's the Schedule
			me.post(new WheelAnimation(90));
			mSelectedActivity = ACTIVITY_SCHEDULE;
		} else if (rounded >= 136 && rounded <= 225) {
			// It's the Social 
			me.post(new WheelAnimation(180));
			mSelectedActivity = ACTIVITY_SOCIAL;
		} else if (rounded >= 226 && rounded <= 315) {
			// It's My Schedule
			me.post(new WheelAnimation(270));
			mSelectedActivity = ACTIVITY_MYSCHEDULE;
		}  else if (rounded >= 316 && rounded <= 360) {
			// Looped back to the dashboard
			me.post(new WheelAnimation(360));
		} else if (rounded <= 0 && rounded >= -45) {
			// Dashboard again
			me.post(new WheelAnimation(0));
		} else if (rounded <= -45 && rounded >= -135) {
			// My Schedule
			me.post(new WheelAnimation(-90));
			mSelectedActivity = ACTIVITY_MYSCHEDULE;
		} else if (rounded <= -136 && rounded >= -225) {
			//Social
			me.post(new WheelAnimation(-180));
			mSelectedActivity = ACTIVITY_SOCIAL;
		} else if (rounded <= -226 && rounded >= -315) {
			// Schedule
			me.post(new WheelAnimation(-270));
			mSelectedActivity = ACTIVITY_SCHEDULE;
		} else if (rounded <= -316 && rounded >= -360) {
			// And Dashboard yet again
			me.post(new WheelAnimation(-360));
		}
	}
//    private class WheelGestureDetector extends SimpleOnGestureListener {
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        	me.post(new WheelAnimation(startDirection + 10));
//        	return true;
//        }
//    }
//    
	private void handleSelection() {
    	if (mSelectedActivity != -1) {
    		if (mListener != null) {
    			startDirection = 0;
    			direction = 0;
    			invalidate();
    			mListener.launchActivity(mSelectedActivity);
    			mSelectedActivity = -1;
    		}
    	}

	}
	private class WheelAnimation implements Runnable {
		
		private float endDirection;
        public WheelAnimation(float endDirection) {
        	this.endDirection = endDirection;
        }
 
        @Override
        public void run() {
        	Log.d("SUSEConferences", "Animating to " + endDirection);
            if (Math.round(direction) != endDirection) {
            	Log.d("SUSEConferences", "Direction is " + direction);
         	   if (direction > endDirection)
         		   direction -= 1f;
         	   else
         		   direction += 1f;
         	   
         	   startDirection = direction;
         	   invalidate();
         	   me.post(this);
            } else {
            	if (endDirection == 360 || endDirection == -360) {
            		startDirection = 0;
            		endDirection = 0;
            	}
            	handleSelection();
            }
        }
	}
}