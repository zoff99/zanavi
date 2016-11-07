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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class NavitDeleteSelectMapActivity extends ListActivity
{
	private int selected_id = -1;
	private int my_id = 0;
	static String CANCELED_ID = "-canceled-";
	static boolean is_canceled = true;

	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		Toolbar bar;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			ViewGroup root_view = (ViewGroup) findViewById(my_id).getParent().getParent();

			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
			bar.setTitle(Navit.get_text("delete maps"));
			root_view.addView(bar, 0); // insert at top
		}
		else
		{
			ViewGroup root_view = (ViewGroup) findViewById(android.R.id.content);
			ListView content = (ListView) root_view.getChildAt(0);

			root_view.removeAllViews();

			LinearLayout ll = new LinearLayout(this);
			ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			ll.setOrientation(LinearLayout.VERTICAL);

			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
			bar.setTitle(Navit.get_text("delete maps"));
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
			//			content.setPadding(0, height, 0, 0);

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

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.p.PREF_current_theme);

		super.onCreate(savedInstanceState);

		NavitMapDownloader.init_ondisk_maps();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1_custom, NavitMapDownloader.OSM_MAP_NAME_LIST_ondisk);
		setListAdapter(adapter);
		this.getListView().setFastScrollEnabled(true);
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
			System.out.println("delete-map-activity:" + e.getMessage());
		}
		my_id = this.getListView().getId();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		// Get the item that was clicked
		this.selected_id = position;
		is_canceled = true;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(Navit.get_text("Do you want to delete this map?"));
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				is_canceled = false;
				dialog.dismiss();
				executeDone(is_canceled);
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
			}
		});

		builder.setCancelable(true);
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private void executeDone(boolean cancel)
	{
		Intent resultIntent = new Intent();
		if (cancel)
		{
			resultIntent.putExtra("selected_id", NavitDeleteSelectMapActivity.CANCELED_ID);
		}
		else
		{
			resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
		}
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
