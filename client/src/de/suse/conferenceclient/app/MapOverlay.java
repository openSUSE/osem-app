/**
 * 
 */
package de.suse.conferenceclient.app;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class MapOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	
	public MapOverlay(Context context, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.mContext = context;
	}
		
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}
	
	public static Drawable boundDrawable(Drawable d) {
		return boundCenterBottom(d);
	}
	
	public void addOverlay(OverlayItem overlayItem) {
		mOverlays.add(overlayItem);
	}
	
	public void addOverlays(ArrayList<OverlayItem> overlays) {
		mOverlays.addAll(overlays);
	}
	
	public void removeOverlay(OverlayItem overlayItem) {
		mOverlays.remove(overlayItem);
	}
	
	public void clearOverlays() {
		mOverlays.clear();
	}
	
	public void doPopulate() {
		populate();
		setLastFocusedIndex(-1);
	}
	
	@Override
	public int size() {
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}
}
