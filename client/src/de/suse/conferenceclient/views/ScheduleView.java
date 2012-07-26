/**
 * 
 */
package de.suse.conferenceclient.views;

import java.util.HashMap;
import java.util.List;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.ChartRenderingInfo;
import org.afree.chart.plot.PlotOrientation;
import org.afree.data.xy.XYIntervalSeries;
import org.afree.data.xy.XYIntervalSeriesCollection;

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
	private AFreeChart mChart;
	private ChartRenderingInfo mChartInfo;
	private XYIntervalSeriesCollection mCollection;
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
		mTrackAxisMap = new HashMap<String, Integer>();
		mEventMap = new HashMap<String, Event>();
	}
	
	public void setEvents(List<Event> eventList) {
		mEventList = eventList;
		mChart = ChartFactory.createXYBarChart(
				"",
				null, // X Axis label
				true, // Use dates 
				null,  // Y Axis label
				mCollection, 
				PlotOrientation.HORIZONTAL, 
				false, 
				false,
				false);

		int axisCount = 0;
		for (Event event : mEventList) {
			String trackName = event.getTrackName();
			String guid = event.getGuid();
			if (!mTrackAxisMap.containsKey(trackName)) {
				mTrackAxisMap.put(trackName, axisCount);
				axisCount++;
			}
			
			mEventMap.put(guid, event);
			long startTime = event.getDate().getTime();
            long endTime = event.getEndDate().getTime();
            XYIntervalSeries newSeries = new XYIntervalSeries(guid);
            int axis = mTrackAxisMap.get(trackName).intValue();
            newSeries.add(axis,
                          axis - 0.45D, 
                          0.45D + axis, 
                          startTime, 
                          startTime, 
                          endTime);
            mCollection.addSeries(newSeries);
		}
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
 
	}
}
