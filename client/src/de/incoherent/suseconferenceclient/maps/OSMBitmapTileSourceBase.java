package de.incoherent.suseconferenceclient.maps;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

public class OSMBitmapTileSourceBase extends BitmapTileSourceBase {

	public OSMBitmapTileSourceBase(String aName, string aResourceId,
			int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels,
			String aImageFilenameEnding) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
				aImageFilenameEnding);
	}

}
