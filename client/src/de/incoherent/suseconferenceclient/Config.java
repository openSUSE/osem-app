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
package de.incoherent.suseconferenceclient;

/*
 * This class is used to keep track of API keys, which
 * should not be put into git.
 */

public class Config {
	public static String MAPS_KEY = "04fPcVba6EsK5DoDqf4l6ZgTMoN7QHm9AuZpWhQ";
	public static String TWITTER_KEY = "XYZ";
	public static String PLUS_KEY = "AIzaSyAZvokqUnIi9BxpohDBCLsPc4bR8IK5mhw";
	public static String BASE_URL = "http://incoherent.de/conferences";
	// The level at which the map only displays the marker for the conference
	// venue, and not food/drinks/etc.  The greater the number, the more
	// zoomed in it is.
	public static final int MAGIC_ZOOM_LEVEL = 14;
}
