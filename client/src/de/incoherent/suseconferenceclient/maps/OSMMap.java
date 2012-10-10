package de.incoherent.suseconferenceclient.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.models.Venue;
import de.incoherent.suseconferenceclient.models.Venue.MapPoint;
import de.incoherent.suseconferenceclient.models.Venue.MapPolygon;

public class OSMMap implements MapInterface {
	private Context mContext;
	private OSMMapView mMapView = null;
	private File mOfflineMap;
	private MyLocationOverlay mLocationOverlay;
	private ArrayList<OSMOverlayItem> mOverlays = new ArrayList<OSMOverlayItem>();
	
	public OSMMap(Context context, File offlineMap) {
		mContext = context;
		mOfflineMap = offlineMap;
	}
	
	@Override
	public View getView() {
		return mMapView;
	}

	@Override
	public void setupMap(Venue venue) {
		if (mOfflineMap == null) {
			mMapView = new OSMMapView(mContext);
			mMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
		} else {
			GEMFFileArchive archive;
			try {
				archive = GEMFFileArchive.getGEMFFileArchive(mOfflineMap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mMapView = null;
				return;
			} catch (IOException e) {
				e.printStackTrace();
				mMapView = null;
				return;
			}
			
			IArchiveFile[] archiveFiles = new IArchiveFile[1];
			archiveFiles[0] = archive;
	        OSMBitmapTileSourceBase bitmapTileSourceBase = new OSMBitmapTileSourceBase("OfflineMap", null, 13, 18, 256, ".png");
	        MapTileModuleProviderBase[] mapTileProviders = new MapTileModuleProviderBase[2];
	        mapTileProviders[0] = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(mContext),
	        													 bitmapTileSourceBase,
	        													 archiveFiles);
	        mapTileProviders[1] = new MapTileDownloader(TileSourceFactory.MAPQUESTOSM);
	        MapTileProviderArray gemfTileProvider = new MapTileProviderArray(bitmapTileSourceBase,
	        																 null,
	        																 mapTileProviders);
	        TilesOverlay tilesOverlay = new TilesOverlay(gemfTileProvider, mContext);
	        gemfTileProvider.setUseDataConnection(false);
	
	        mMapView = new OSMMapView(mContext, 
											 256, new DefaultResourceProxyImpl(mContext), 
											 gemfTileProvider);
			mMapView.getOverlays().add(tilesOverlay);
		}
		
		mMapView.setBuiltInZoomControls(true);
		mMapView.setClickable(true);
		mMapView.setMultiTouchControls(true); 
		mMapView.getController().setZoom(15);

		if (venue == null)
			return;
	
		Drawable venueDrawable = mContext.getResources().getDrawable(R.drawable.venue_marker);
		Drawable foodDrawable = mContext.getResources().getDrawable(R.drawable.food_marker);
		Drawable drinkDrawable = mContext.getResources().getDrawable(R.drawable.drink_marker);
		Drawable elecDrawable = mContext.getResources().getDrawable(R.drawable.electronics_marker);
		Drawable partyDrawable = mContext.getResources().getDrawable(R.drawable.party_marker);

		MapController controller =  mMapView.getController();
		for (MapPoint point : venue.getPoints()) {
			GeoPoint mapPoint = new GeoPoint(point.getLat(), point.getLon());
			OSMOverlayItem overlay = null;
			if (point.getType() == MapPoint.TYPE_VENUE)
				 overlay = new OSMOverlayItem(point.getAddress(), point.getName(), mapPoint);
			else
				overlay = new OSMOverlayItem(point.getDescription(), point.getName(), mapPoint);
			
			switch (point.getType()) {
			case MapPoint.TYPE_VENUE:
				overlay.setMarker(venueDrawable);
				controller.setCenter(mapPoint);
				controller.setZoom(18);
				break;
			case MapPoint.TYPE_FOOD:
				overlay.setMarker(foodDrawable);
				break;
			case MapPoint.TYPE_DRINK:
				overlay.setMarker(drinkDrawable);
				break;
			case MapPoint.TYPE_PARTY:
				overlay.setMarker(partyDrawable);
				break;
			case MapPoint.TYPE_ELECTRONICS:
				overlay.setMarker(elecDrawable);
				break;
			}

			mOverlays.add(overlay);
		}
		
		ResourceProxy proxy = new DefaultResourceProxyImpl(mContext);
		OSMMapOverlay mapOverlays = new OSMMapOverlay(mOverlays, new OnItemGestureListener<OSMOverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(final int index, final OSMOverlayItem item) {
            	AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            	dialog.setTitle(item.getTitle());
            	dialog.setMessage(item.getSnippet());
            	dialog.show();
                return true;
            }

            @Override
            public boolean onItemLongPress(final int index, final OSMOverlayItem item) {
                return false;
            }
        }, proxy);
//
//		for (MapPolygon polygon : venue.getPolygons()) {
//			List<MapPoint> points = polygon.getPoints();
//			GeoPoint[] pathPoints = new GeoPoint[points.size()];
//			for (int i = 0; i < points.size(); i++) {
//				MapPoint point = points.get(i);
//				pathPoints[i] = new GeoPoint(point.getLat(), point.getLon());
//			}
//			GoogleMapPolygonOverlay newOverlay;
//			newOverlay = new GoogleMapPolygonOverlay(pathPoints, polygon.getLineColor(), polygon.getFillColor());
//			overlays.add(newOverlay);
//		}
//		overlays.add(mMapOverlays);
		
		
//		mLocationOverlay = new MyLocationOverlay(mContext, mMapView);
//		mLocationOverlay.enableMyLocation();
//		mLocationOverlay.enableCompass();
//		overlays.add(mLocationOverlay);
		
		mMapView.getOverlays().add(mapOverlays);
		mapOverlays.doPopulate();
		mMapView.postInvalidate();

	}

	@Override
	public void enableLocation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disableLocation() {
		// TODO Auto-generated method stub
		
	}

}
