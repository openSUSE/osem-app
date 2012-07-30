/**
 * 
 */
package de.suse.conferenceclient.app;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SlidingDrawer;

public class InfoDrawer extends SlidingDrawer {
    private boolean mVertical;
	public InfoDrawer(Context context, AttributeSet attrs, int style) {
		 super(context, attrs, style);
	        setOrientation(attrs);
	    }

	public InfoDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(attrs);
	}

	private void setOrientation(AttributeSet attrs) {
		int orientation = getResources().getConfiguration().orientation;
		mVertical = (orientation == Configuration.ORIENTATION_PORTRAIT);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
		}

		final View handle = getHandle();
		final View content = getContent();
		measureChild(handle, widthMeasureSpec, heightMeasureSpec);
		int width = 0;
		
		// If we're in landscape mode, we only want to take up part of the screen,
		// but the full screen if vertical
		if (!mVertical) {
			width = (int) (widthSpecSize * 0.8);
		} else {
			width = widthSpecSize - handle.getMeasuredWidth();
		}
		
		getContent().measure(MeasureSpec.makeMeasureSpec(width, widthSpecMode), heightMeasureSpec);
		widthSpecSize = handle.getMeasuredWidth() + content.getMeasuredWidth();
		heightSpecSize = content.getMeasuredHeight();
		if (handle.getMeasuredHeight() > heightSpecSize) heightSpecSize = handle.getMeasuredHeight();
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

}