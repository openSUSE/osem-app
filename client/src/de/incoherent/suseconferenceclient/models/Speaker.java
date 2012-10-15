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

package de.incoherent.suseconferenceclient.models;

import android.graphics.Bitmap;

public class Speaker {
	private String mName, mCompany, mBio;
	private Bitmap mPhoto;
	public Speaker(String name, String company, String bio, Bitmap photo) {
		this.mName = name;
		this.mCompany = company;
		this.mBio = bio;
		this.mPhoto = photo;
	}
	
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		mName = name;
	}
	public String getCompany() {
		return mCompany;
	}
	public void setCompany(String company) {
		mCompany = company;
	}
	public String getBio() {
		return mBio;
	}
	public void setBio(String bio) {
		mBio = bio;
	}
	public Bitmap getPhoto() {
		return mPhoto;
	}
	public void setPhoto(Bitmap photo) {
		mPhoto = photo;
	}
}
