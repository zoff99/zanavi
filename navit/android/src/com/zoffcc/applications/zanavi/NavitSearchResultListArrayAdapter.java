/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2013 Zoff <zoff@zoff.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.zanavi;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavitSearchResultListArrayAdapter extends BaseExpandableListAdapter
{
	private Context context;
	private Map<Integer, List<search_result_entry>> list;

	public static class search_result_entry
	{
		private int type;
		private String name;
		private int global_pos;
		final static private String[] typename = { "-S-", "Street", "Town", "POI", "-E-" };

		public search_result_entry(int type, String name, int global_pos)
		{
			this.type = type; // 1 --> street
								// 2 --> town
								// 3 --> POI
			this.name = name;
			this.global_pos = global_pos;
		}

		public int getgpos()
		{
			return global_pos;
		}

		public String getName()
		{
			return name;
		}

		public int gettype()
		{
			return type;
		}

		public static String gettype_name(int my_type)
		{
			return Navit.get_text(typename[my_type]); // TRANS
		}

	}

	public NavitSearchResultListArrayAdapter(Context context, Map<Integer, List<search_result_entry>> list)
	{
		this.context = context;
		this.list = list;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		List<search_result_entry> l = list.get(groupPosition + 1);
		return l.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		// List<search_result_entry> l = list.get(groupPosition);
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		List<search_result_entry> l = list.get(groupPosition + 1);
		// System.out.println("LL:groupp=" + (groupPosition + 1) + " cpos=" + childPosition + " l=" + l.size());

		search_result_entry entry = l.get(childPosition);

		//if (convertView != null)
		//{
		// --- we need a new view object every time!! otherwise it will be mixed up !! ---

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (entry.gettype() == 1)
		{
			convertView = inflater.inflate(R.layout.search_result_item_street, null);

			ImageView icon_view = (ImageView) convertView.findViewById(R.id.icon);
			if (Navit.p.PREF_current_theme == Navit.DEFAULT_THEME_OLD_LIGHT)
			{
				// leave it as it is
			}
			else
			{
				// thanks to: http://stackoverflow.com/questions/17841787/invert-colors-of-drawable-android
				Drawable drawable_homeicon = icon_view.getResources().getDrawable(R.drawable.roadicon);
				float[] colorMatrix_Negative = { -1.0f, 0, 0, 0, 255, //red
						0, -1.0f, 0, 0, 255, //green
						0, 0, -1.0f, 0, 255, //blue
						0, 0, 0, 1.0f, 0 //alpha  
				};

				ColorFilter colorFilter_Negative = new ColorMatrixColorFilter(colorMatrix_Negative);
				drawable_homeicon.setColorFilter(colorFilter_Negative);
				icon_view.setImageDrawable(drawable_homeicon);
			}
		}
		else if (entry.gettype() == 3)
		{
			convertView = inflater.inflate(R.layout.search_result_item_poi, null);

			ImageView icon_view = (ImageView) convertView.findViewById(R.id.icon);
			if (Navit.p.PREF_current_theme == Navit.DEFAULT_THEME_OLD_LIGHT)
			{
				// leave it as it is
			}
			else
			{
				Drawable drawable_homeicon = icon_view.getResources().getDrawable(R.drawable.poiicon);
				float[] colorMatrix_Negative = { -1.0f, 0, 0, 0, 255, //red
						0, -1.0f, 0, 0, 255, //green
						0, 0, -1.0f, 0, 255, //blue
						0, 0, 0, 1.0f, 0 //alpha  
				};

				ColorFilter colorFilter_Negative = new ColorMatrixColorFilter(colorMatrix_Negative);
				drawable_homeicon.setColorFilter(colorFilter_Negative);
				icon_view.setImageDrawable(drawable_homeicon);
			}
		}
		else
		{
			convertView = inflater.inflate(R.layout.search_result_item_town, null);

			ImageView icon_view = (ImageView) convertView.findViewById(R.id.icon);
			if (Navit.p.PREF_current_theme == Navit.DEFAULT_THEME_OLD_LIGHT)
			{
				// leave it as it is
			}
			else
			{
				Drawable drawable_homeicon = icon_view.getResources().getDrawable(R.drawable.townicon);
				float[] colorMatrix_Negative = { -1.0f, 0, 0, 0, 255, //red
						0, -1.0f, 0, 0, 255, //green
						0, 0, -1.0f, 0, 255, //blue
						0, 0, 0, 1.0f, 0 //alpha  
				};

				ColorFilter colorFilter_Negative = new ColorMatrixColorFilter(colorMatrix_Negative);
				drawable_homeicon.setColorFilter(colorFilter_Negative);
				icon_view.setImageDrawable(drawable_homeicon);
			}
		}

		// R.layout.search_result_item_poi --> for POI result
		//}

		TextView text = (TextView) convertView.findViewById(R.id.toptext);
		text.setText(entry.getName());

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		//System.out.println("LL:groupp=" + (groupPosition + 1));
		List<search_result_entry> l = list.get(groupPosition + 1);
		//System.out.println("LL:list=" + l.size());
		return l.size();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return search_result_entry.gettype_name(groupPosition + 1);
	}

	@Override
	public int getGroupCount()
	{
		return 3;
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return (groupPosition);
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.search_result_list_header, null);
		}
		TextView groupName = (TextView) convertView.findViewById(R.id.articleHeaderTextView);
		groupName.setText(search_result_entry.gettype_name(groupPosition + 1) + " (" + getChildrenCount(groupPosition) + ")");
		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}
}
