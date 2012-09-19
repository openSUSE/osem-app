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
package de.incoherent.suseconferenceclient.adapters;

import java.util.ArrayList;
import de.incoherent.suseconferenceclient.models.SocialItem;
import de.incoherent.suseconferenceclient.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SocialItemAdapter extends ArrayAdapter<SocialItem> {
	private LayoutInflater mInflater;
	private int mResource;
	private ArrayList<SocialItem> mItems;
	
	public SocialItemAdapter(Context context, int textViewResourceId,
			ArrayList<SocialItem> objects) {
		super(context, textViewResourceId, objects);
		this.mInflater = LayoutInflater.from(context);
		this.mResource = textViewResourceId;
		this.mItems = objects;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View root;
		SocialItem item = mItems.get(position);
		if (convertView == null) {
			root = mInflater.inflate(mResource, null);
		} else {
			root = convertView;
		}
		
		ImageView picture = (ImageView) root.findViewById(R.id.userPicture);
		picture.setImageBitmap(item.getUserImage());
		TextView username = (TextView) root.findViewById(R.id.userNameTextView);
		username.setText(item.getUserName());
		TextView message = (TextView) root.findViewById(R.id.messageTextView);
		message.setText(item.getMessage());
		TextView time = (TextView) root.findViewById(R.id.timeTextView);
		time.setText(item.getDatestamp());
		ImageView typeIcon = (ImageView) root.findViewById(R.id.typeIcon);
		typeIcon.setImageBitmap(item.getTypeIcon());
		return root;
	}

}
