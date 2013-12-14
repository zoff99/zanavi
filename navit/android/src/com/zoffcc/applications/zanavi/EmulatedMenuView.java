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

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EmulatedMenuView extends ListView
{
	public static String[] MenuItemsList2 = null;
	public static int[] MenuItemsIdMapping2 = null;
	public String[] MenuItemsList = null;
	public int[] MenuItemsIdMapping = null;
	Context c;
	Navit main_app;

	public EmulatedMenuView(Context context, Navit main)
	{
		super(context);
		c = context;
		main_app = main;

		MenuItemsList = new String[1];
		MenuItemsIdMapping = new int[1];
		for (int i = 0; i < 1; i++)
		{
			MenuItemsList[i] = "_";
			MenuItemsIdMapping[i] = i;
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, MenuItemsList);
		setAdapter(adapter);
		setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{
				Object o = getItemAtPosition(position);
				String str = (String) o;//As you are using Default String Adapter
				//System.out.println("aagtl:mitem=" + str + " pos=" + position);
				setVisibility(View.INVISIBLE);
				main_app.onOptionsItemSelected_wrapper(MenuItemsIdMapping[position]);
			}
		});
		this.setBackgroundColor(Color.BLACK);
	}

	void set_adapter(String[] ml, int[] mid)
	{
		for (int i = 0; i < MenuItemsList.length; i++)
		{
			MenuItemsList[i] = null;
		}
		MenuItemsList = null;
		MenuItemsIdMapping = null;

		MenuItemsList = ml;
		MenuItemsIdMapping = mid;

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, MenuItemsList);
		setAdapter(adapter);
	}
}
