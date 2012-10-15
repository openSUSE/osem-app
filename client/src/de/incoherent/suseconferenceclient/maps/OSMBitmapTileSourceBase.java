/*******************************************************************************
 * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Matt Barringer <matt@incoherent.de> - initial API and implementation
 ******************************************************************************/

package de.incoherent.suseconferenceclient.maps;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

/*
 * In order to set the downloaded GEMF file as the tile source, extend
 * the abstract BitmapTileSourceBase class
 */
public class OSMBitmapTileSourceBase extends BitmapTileSourceBase {
	public OSMBitmapTileSourceBase(String name, string resourceId,
			int minimumZoomLevel, int maximumZoomLevel, int tileSize, String imageExtension) {
		super(name, resourceId, minimumZoomLevel, maximumZoomLevel, tileSize, imageExtension);
	}
}
