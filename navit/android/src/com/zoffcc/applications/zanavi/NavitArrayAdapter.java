/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2012 Zoff <zoff@zoff.cc>
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NavitArrayAdapter extends BaseAdapter
{
	List<String> l;
	Context c;

	public NavitArrayAdapter(Context context, List<String> objects)
	{
		this.l = objects;
		this.c = context;
	}

	@Override
	public int getCount()
	{
		return l.size();
	}

	@Override
	public Object getItem(int i)
	{
		return l.get(i);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View itemView = null;

		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.recentdest_list, null);
		}
		else
		{
			itemView = convertView;
		}
		TextView text = (TextView) itemView.findViewById(R.id.text);
		text.setText(l.get(position));

		return itemView;
	}

}
