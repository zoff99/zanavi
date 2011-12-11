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

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.retain.dialog.RenameDialog;
import com.retain.dialog.RenameHandlerInterface;
import com.zoffcc.applications.zanavi.Navit.Navit_Point_on_Map;

public class NavitRecentDestinationActivity extends ListActivity
{
	private int selected_id = -1;
	private int my_id = 0;
	private String[] context_items = null;
	static Navit_Point_on_Map t = null;
	static int t_position = -1;
	static int t_size = -1;
	static Boolean refresh_items = false;
	static NavitRecentDestinationActivity my = null;
	private static ArrayList<String> listview_items = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		my = this;

		context_items = new String[] { Navit.get_text("delete Destination"), Navit.get_text("rename Destination") }; // TRANS

		listview_items.clear();
		String[] t = new String[Navit.map_points.size()];
		try
		{
			int j = 0;
			for (j = Navit.map_points.size() - 1; j >= 0; j--)
			{
				t[Navit.map_points.size() - j - 1] = Navit.map_points.get(j).point_name;
			}
			for (j = 0; j < t.length; j++)
			{
				listview_items.add(t[j]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			t = new String[1];
			t[0] = "* Error *";
			listview_items.add(t[0]);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listview_items);
		this.setListAdapter(adapter);
		this.getListView().setFastScrollEnabled(true);
		registerForContextMenu(this.getListView());
		my_id = this.getListView().getId();
	}

	public static Handler handler1 = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.getData().getInt("what") == 1)
			{
				refresh_items_real();
			}
		}
	};

	public static void refresh_items()
	{
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("what", 1);
		msg.setData(b);
		handler1.sendMessage(msg);
	}

	public static void refresh_items_real()
	{
		String[] t = new String[Navit.map_points.size()];
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) my.getListAdapter();
		listview_items.clear();
		adapter.notifyDataSetChanged();
		try
		{
			int j = 0;
			for (j = Navit.map_points.size() - 1; j >= 0; j--)
			{
				t[Navit.map_points.size() - j - 1] = Navit.map_points.get(j).point_name;
			}
			for (j = 0; j < t.length; j++)
			{
				listview_items.add(t[j]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			t = new String[1];
			t[0] = "* Error *";
			listview_items.add(t[0]);
		}
		adapter.notifyDataSetChanged();
		refresh_items = false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		if (my_id != 0)
		{
			if (v.getId() == my_id)
			{
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				menu.setHeaderTitle(Navit.map_points.get(Navit.map_points.size() - info.position - 1).point_name);
				String[] menuItems = context_items;
				for (int i = 0; i < menuItems.length; i++)
				{
					menu.add(Menu.NONE, i, i, menuItems[i]);
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String menuItemName = context_items[menuItemIndex];
		t_position = info.position;
		t_size = Navit.map_points.size();
		String listItemName = Navit.map_points.get(t_size - t_position - 1).point_name;

		switch (menuItemIndex)
		{
		case 0:
			// delete item
			Navit.map_points.remove(t_size - t_position - 1);
			// save it
			Navit.write_map_points();
			// refresh
			refresh_items = true;
			refresh_items();
			break;
		case 1:
			// rename item
			NavitRecentDestinationActivity.t = Navit.map_points.get(t_size - t_position - 1);
			String title = Navit.get_text("Rename Destination"); //TRANS
			RenameDialog rd = new RenameDialog(this, title, t.point_name, new RenameHandlerInterface.OnRenameItemListener()
			{
				@Override
				public void onRenameItem(String newname)
				{
					NavitRecentDestinationActivity.t.point_name = newname;
					Navit.map_points.set(t_size - t_position - 1, NavitRecentDestinationActivity.t);
					System.out.println("new=" + newname);
					// save it
					Navit.write_map_points();
					// refresh
					refresh_items = true;
					refresh_items();
				}
			}

			);
			rd.show();
			break;
		}
		return true;
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
