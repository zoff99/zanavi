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

import java.util.Iterator;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NavitAddressResultListActivity extends ListActivity
{

	private int selected_id = -1;
	private int selected_id_passthru = -1;
	private Boolean is_empty = true;
	public String[] result_list = new String[] { "loading results ..." };
	public static int mode = 1; // 0 .. hide towns if streets/housenumbers available
								// 1 .. show towns and streets and housenumbers
								// 2 .. show only towns

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//Log.e("Navit", "all ok");

		Navit.Navit_Address_Result_Struct tmp = new Navit.Navit_Address_Result_Struct();

		Log.e("Navit", "########### full result count: " + Navit.NavitAddressResultList_foundItems.size());

		if (mode == 0)
		{
			// show "town names" as results only when we dont have any street names in resultlist
			if ((Navit.search_results_streets > 0) || (Navit.search_results_streets_hn > 0))
			{
				// clear out towns from result list
				for (Iterator<Navit.Navit_Address_Result_Struct> k = Navit.NavitAddressResultList_foundItems.iterator(); k.hasNext();)
				{
					tmp = k.next();
					if (tmp.result_type.equals("TWN"))
					{
						k.remove();
					}
				}
			}
		}
		else if (mode == 1)
		{
			// fine, show them all
		}
		else if (mode == 2)
		{
			// show only town names
			// clear out streets and housenumbers from result list
			for (Iterator<Navit.Navit_Address_Result_Struct> k = Navit.NavitAddressResultList_foundItems.iterator(); k.hasNext();)
			{
				tmp = k.next();
				if (tmp.result_type.equals("STR"))
				{
					k.remove();
				}
				else if (tmp.result_type.equals("SHN"))
				{
					k.remove();
				}
			}
		}

		Log.e("Navit", "########### final result count: " + Navit.NavitAddressResultList_foundItems.size());

		this.result_list = new String[Navit.NavitAddressResultList_foundItems.size()];
		int j = 0;
		for (Iterator<Navit.Navit_Address_Result_Struct> i = Navit.NavitAddressResultList_foundItems.iterator(); i.hasNext();)
		{
			tmp = i.next();
			this.result_list[j] = tmp.addr;
			j++;
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result_list);
		setListAdapter(adapter);
		this.getListView().setFastScrollEnabled(true);
		is_empty = true;

		// ListActivity has a ListView, which you can get with:
		ListView lv = getListView();
		// Then you can create a listener like so:
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
			{
				onLongListItemClick(v, pos, id);
				return true;
			}

		});

	}

	public void add_item_(String item)
	{
		if (item == null)
		{
			// empty item?
			return;
		}

		if (this.is_empty)
		{
			// clear dummy text, and add this item
			this.result_list = new String[1];
			this.result_list[0] = item;
		}
		else
		{
			// add the item to the end of the list
			String[] tmp_list = this.result_list;
			this.result_list = new String[tmp_list.length + 1];
			for (int i = 0; i < tmp_list.length; i = i + 1)
			{
				this.result_list[i] = tmp_list[i];
			}
			this.result_list[tmp_list.length] = item;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result_list);
		setListAdapter(adapter);
		this.is_empty = false;
	}

	protected void onLongListItemClick(View v, int pos, long id)
	{
		Log.e("Navit", "long click id=" + id + " pos=" + pos);
		// remember what pos we clicked
		this.selected_id_passthru = pos;

		Intent search_intent = new Intent(this, NavitMapPreviewActivity.class);

		search_intent.putExtra("lat", Navit.NavitAddressResultList_foundItems.get(pos).lat);
		search_intent.putExtra("lon", Navit.NavitAddressResultList_foundItems.get(pos).lon);
		search_intent.putExtra("q", Navit.NavitAddressResultList_foundItems.get(pos).addr);
		this.startActivityForResult(search_intent, Navit.NavitMapPreview_id);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case Navit.NavitMapPreview_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					Log.e("Navit", "*activity ready*");
					int sel_id = Integer.parseInt(data.getStringExtra("selected_id"));
					Log.e("Navit", "*activity ready* sel_id=" + sel_id);

					if (sel_id == 1)
					{
						// user wants to set as destination
						this.selected_id = this.selected_id_passthru;
						// close this activity
						executeDone();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		// --- OLD --- set as target
		// --- OLD --- set as target
		// **this.selected_id = position;
		// Log.e("Navit", "p:" + position);
		// Log.e("Navit", "i:" + id);
		// **executeDone();
		// --- OLD --- set as target
		// --- OLD --- set as target

		// --- NEW --- preview map
		// --- NEW --- preview map
		onLongListItemClick(v, position, id);
		// --- NEW --- preview map
		// --- NEW --- preview map
	}

	//	@Override
	//	public void onBackPressed()
	//	{
	//		executeDone();
	//		super.onBackPressed();
	//	}

	private void executeDone()
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

}
