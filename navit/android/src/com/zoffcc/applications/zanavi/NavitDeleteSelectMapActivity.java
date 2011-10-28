/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 Zoff <zoff@zoff.cc>
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

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NavitDeleteSelectMapActivity extends ListActivity
{
	private int selected_id = -1;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		NavitMapDownloader.init_ondisk_maps();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, NavitMapDownloader.OSM_MAP_NAME_LIST_ondisk);
		setListAdapter(adapter);
		this.getListView().setFastScrollEnabled(true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		this.selected_id = position;
		// close this activity
		executeDone();
	}

	private void executeDone()
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
