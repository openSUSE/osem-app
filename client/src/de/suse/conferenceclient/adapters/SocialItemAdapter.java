package de.suse.conferenceclient.adapters;

import java.util.List;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.SocialItem;
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
	private List<SocialItem> mItems;
	
	public SocialItemAdapter(Context context, int textViewResourceId,
			List<SocialItem> objects) {
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
		return root;
	}

}
