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

/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2008 Navit Team
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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class NavitDownloadSelectMapActivity extends ListActivity
{
	private int selected_id = -1;
	private int my_id = 0;

	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		Toolbar bar;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			ViewGroup root_view = (ViewGroup) findViewById(my_id).getParent().getParent();

			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
			bar.setTitle(Navit.get_text("download maps"));
			root_view.addView(bar, 0); // insert at top
		}
		else
		{
			ViewGroup root_view = (ViewGroup) findViewById(android.R.id.content);

			System.out.println("ZZXX22:r1=" + findViewById(android.R.id.content));
			System.out.println("ZZXX22:r2=" + findViewById(android.R.id.content).getParent());
			System.out.println("ZZXX22:r3=" + findViewById(android.R.id.content).getParent().getParent());

			View content = (View) root_view.getChildAt(0);
			System.out.println("ZZXX22:r4=" + root_view.getChildAt(0));
			System.out.println("ZZXX22:r5=" + root_view.getChildCount());
			System.out.println("ZZXX22:r6=" + root_view.getChildAt(1));

			root_view.removeAllViews();

			LinearLayout ll = new LinearLayout(this);
			ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			ll.setOrientation(LinearLayout.VERTICAL);

			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
			bar.setTitle(Navit.get_text("download maps"));
			root_view.addView(ll);

			//			int height;
			//			TypedValue tv = new TypedValue();
			//			if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
			//			{
			//				height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
			//			}
			//			else
			//			{
			//				height = bar.getHeight();
			//			}
			//
			// content.setPadding(0, height, 0, 0);

			ll.addView(bar);
			ll.addView(content);
		}

		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.p.PREF_current_theme);

		super.onCreate(savedInstanceState);
		//Log.e("Navit", "all ok");

		//TextView text_v = new TextView(this);
		//text_v.setText(Navit.get_text("Select Map to download")); // TRANS
		//text_v.setTextSize(25);
		//this.getListView().addHeaderView(text_v);

		NavitMapDownloader.init();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1_custom, NavitMapDownloader.OSM_MAP_NAME_LIST_inkl_SIZE_ESTIMATE);
		setListAdapter(adapter);
		this.getListView().setFastScrollEnabled(true);
		this.getListView().setVerticalFadingEdgeEnabled(true);
		this.getListView().setFadingEdgeLength(25);

		try
		{
			if (Build.VERSION.SDK_INT >= 16)
			{
				if (Navit.p.PREF_current_theme == Navit.DEFAULT_THEME_OLD_DARK)
				{
					this.getListView().setBackgroundColor(Color.BLACK);
				}
				else
				{
					this.getListView().setBackgroundColor(Color.WHITE);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("download-map-activity:" + e.getMessage());
		}

		my_id = this.getListView().getId();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		// Object o = this.getListAdapter().getItem(position);
		// String keyword = o.toString();
		this.selected_id = position;
		//Toast.makeText(this, "You selected: " + position + " " + keyword, Toast.LENGTH_LONG).show();
		Log.e("Navit", "p:" + position);
		Log.e("Navit", "i:" + id);

		// close this activity
		executeDone();
	}

	//	@Override
	//	public void onBackPressed()
	//	{
	//		executeDone();
	//		super.onBackPressed();
	//	}

	private void executeDone()
	{
		int real_map_id = NavitMapDownloader.OSM_MAP_NAME_ORIG_ID_LIST[Integer.parseInt(String.valueOf(this.selected_id))];
		if (real_map_id > -1)
		{
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "selected id = " + this.selected_id + " real map id = " + real_map_id);
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "selected map = " + NavitMapDownloader.z_OSM_MAPS[real_map_id]);

			// ok we have selected a map file. set flag
			NavitMapDownloader.download_active_start = true;

			Intent resultIntent = new Intent();
			resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		}
		else
		{
			// no valid map select, stay here
		}
	}

}
