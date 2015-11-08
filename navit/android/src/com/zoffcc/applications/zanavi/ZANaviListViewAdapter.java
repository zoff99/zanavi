package com.zoffcc.applications.zanavi;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoffcc.applications.zanavi.ZANaviListViewAdapter.ListViewItem;

public class ZANaviListViewAdapter extends ArrayAdapter<ListViewItem>
{

	public static class ListViewItem
	{
		public final String distanct_short; // the distance in short form [String] 
		public final Drawable icon; // the drawable for the ListView item ImageView
		public final String title; // the text for the ListView item title
		public final String description; // the text for the ListView item description
		public final float lat;
		public final float lon;

		public ListViewItem(String distanct_short, Drawable icon, String title, String description, float lat, float lon)
		{
			this.distanct_short = distanct_short;
			this.icon = icon;
			this.title = title;
			this.description = description;
			this.lat = lat;
			this.lon = lon;
		}
	}

	public ZANaviListViewAdapter(Context context, List<ListViewItem> items)
	{
		super(context, R.layout.listview_item, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder;
		if (convertView == null)
		{
			// inflate the GridView item layout
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.listview_item, parent, false);
			// initialize the view holder
			viewHolder = new ViewHolder();
			viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
			viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
			viewHolder.road_book_short_distance_text = (TextView) convertView.findViewById(R.id.road_book_short_distance_text);
			convertView.setTag(viewHolder);
		}
		else
		{
			// recycle the already inflated view
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// update the item view
		ListViewItem item = getItem(position);
		viewHolder.ivIcon.setImageDrawable(item.icon);
		viewHolder.tvTitle.setText(item.title);
		viewHolder.tvDescription.setText(item.description);
		viewHolder.road_book_short_distance_text.setText(item.distanct_short);
		return convertView;
	}

	/**
	 * The view holder design pattern prevents using findViewById()
	 * repeatedly in the getView() method of the adapter.
	 * 
	 * @see http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
	 */
	private static class ViewHolder
	{
		ImageView ivIcon;
		TextView tvTitle;
		TextView tvDescription;
		TextView road_book_short_distance_text;
	}
}
