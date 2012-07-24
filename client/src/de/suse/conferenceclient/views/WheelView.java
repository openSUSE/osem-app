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
	private WheelView me;
	private static Matrix mMatrix;
	private static Bitmap mBitmap;
	private static final Paint mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
	private float mCenterX, mCenterY;
	private float direction = 0;
	private float startX, startY;
	private float startDirection=0;
	private GestureDetector mDetector;
	
	public WheelView(Context context) {
		super(context);
		setup(context);
	}

	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}
	
	private void setup(Context context) {
		me = this;
		mMatrix = new Matrix();
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
		mDetector = new GestureDetector(context, new WheelGestureDetector());
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
	    float angle = (float) angle(mCenterX, mCenterY, startX, startY, x,
	            y);
	    direction = (float) Math.toDegrees(angle) * -1 + startDirection;
	    Log.d("SUSEConferences", "Direction: " + direction);
	    this.invalidate();
	}
	
    private class WheelGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	me.post(new WheelAnimation(startDirection + 10));
        	return true;
        }
    }
	private class WheelAnimation implements Runnable {
        public WheelAnimation(float endDirection) {
               if (direction != endDirection) {
            	   direction += 1;
            	   startDirection = direction;
            	   invalidate();
            	   me.post(this);
               }
        }
 
        @Override
        public void run() {

        }
    }
}
