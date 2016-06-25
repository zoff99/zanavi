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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.zoffcc.applications.zanavi.NavitSearchResultListArrayAdapter.search_result_entry;

public class NavitAddressResultListActivity extends ExpandableListActivity
{
	// public static NavitAddressResultListActivity self_ = null;
	NavitSearchResultListArrayAdapter adapter_ = null;
	private int selected_id = -1;
	private int selected_id_passthru = -1;
	static NavitAddressResultListActivity NavitAddressResultListActivity_s = null;
	// private Boolean is_empty = true;
	// public ArrayList<HashMap<Integer, search_result_entry>> result_list;
	public Map<Integer, List<search_result_entry>> result_list;

	public static int mode = 1; // 0 .. hide towns if streets/housenumbers available
								// 1 .. show towns and streets and housenumbers
								// 2 .. show only towns

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		Toolbar bar;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			ViewGroup root_view = (ViewGroup) findViewById(android.R.id.list).getParent().getParent();

			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
			bar.setTitle(Navit.get_text("Search results"));
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
			bar.setTitle(Navit.get_text("Search results"));
			root_view.addView(ll);

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

		// Override how this activity is animated into view
		// The new activity is pulled in from the left and the current activity is kept still
		// This has to be called before onCreate
		overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

		NavitAddressResultListActivity_s = this;
		Navit.search_list_ready = true;
		System.out.println("SCREENSHOT:this");

		//Log.e("Navit", "all ok");

		Navit.Navit_Address_Result_Struct tmp = new Navit.Navit_Address_Result_Struct();

		Log.e("Navit", "########### full result count: " + Navit.NavitAddressResultList_foundItems.size());

		//		if (mode == 0)
		//		{
		//			// show "town names" as results only when we dont have any street names in resultlist
		//			if ((Navit.search_results_streets > 0) || (Navit.search_results_streets_hn > 0))
		//			{
		//				// clear out towns from result list
		//				for (Iterator<Navit.Navit_Address_Result_Struct> k = Navit.NavitAddressResultList_foundItems.iterator(); k.hasNext();)
		//				{
		//					tmp = k.next();
		//					if (tmp.result_type.equals("TWN"))
		//					{
		//						k.remove();
		//					}
		//				}
		//			}
		//		}
		//		else if (mode == 1)
		//		{
		//			// fine, show them all
		//		}
		//		else if (mode == 2)
		//		{
		//			// show only town names
		//			// clear out streets and housenumbers from result list
		//			for (Iterator<Navit.Navit_Address_Result_Struct> k = Navit.NavitAddressResultList_foundItems.iterator(); k.hasNext();)
		//			{
		//				tmp = k.next();
		//				if (tmp.result_type.equals("STR"))
		//				{
		//					k.remove();
		//				}
		//				else if (tmp.result_type.equals("SHN"))
		//				{
		//					k.remove();
		//				}
		//			}
		//		}

		Log.e("Navit", "########### final result count: " + Navit.NavitAddressResultList_foundItems.size());

		// this.result_list = new String[Navit.NavitAddressResultList_foundItems.size()];
		this.result_list = new LinkedHashMap<Integer, List<search_result_entry>>();
		ArrayList<search_result_entry> l1 = new ArrayList<search_result_entry>();
		ArrayList<search_result_entry> l2 = new ArrayList<search_result_entry>();
		ArrayList<search_result_entry> l3 = new ArrayList<search_result_entry>();

		int j = 0;
		for (Iterator<Navit.Navit_Address_Result_Struct> i = Navit.NavitAddressResultList_foundItems.iterator(); i.hasNext();)
		{
			tmp = i.next();
			if (tmp.result_type.equals("TWN"))
			{
				l2.add(new search_result_entry(2, tmp.addr, j)); // town
			}
			else if (tmp.result_type.equals("POI"))
			{
				l3.add(new search_result_entry(3, tmp.addr, j)); // POI
			}
			else
			{
				l1.add(new search_result_entry(1, tmp.addr, j)); // street or housenumber
			}
			// [j] = tmp.addr;
			j++;
		}

		this.result_list.put(1, l1);
		this.result_list.put(2, l2);
		this.result_list.put(3, l3);

		//System.out.println("LL:in:l1=" + l1.size() + " l2=" + l2.size() + " l3=" + l3.size());

		// self_ = this;

		// adapter_ = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result_list);
		adapter_ = new NavitSearchResultListArrayAdapter(this, result_list);

		setListAdapter(adapter_);
		// this.getListView().setFastScrollEnabled(true);
		// ** // is_empty = true;

		// this.getExpandableListView().setBackgroundColor(Color.rgb(0, 0, 0));

		// ListActivity has a ListView, which you can get with:
		ExpandableListView lv = getExpandableListView();
		lv.setOnChildClickListener(this);
		// lv.setFastScrollEnabled(true);

		if (Navit.CIDEBUG == 1)
		{
			try
			{
				lv.expandGroup(0);
				lv.expandGroup(1);
				lv.expandGroup(2);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// Then you can create a listener like so:
		//		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		//		{
		//			@Override
		//			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
		//			{
		//				//onLongListItemClick(v, pos, id);
		//				//return true;
		//			}
		//
		//		});

	}

	@Override
	protected void onPause()
	{
		// Whenever this activity is paused (i.e. looses focus because another activity is started etc)
		// Override how this activity is animated out of view
		// The new activity is kept still and this activity is pushed out to the left
		overridePendingTransition(R.anim.hold, R.anim.push_out_to_right);
		super.onPause();
	}

	protected void onLongListItemClick(View v, int gpos, int pos2, long id)
	{
		// get the global resultposition from the result object
		int pos = this.result_list.get(gpos + 1).get(pos2).getgpos();

		Log.e("Navit", "long click id=" + id + " gpos=" + gpos + " pos=" + pos);
		// remember what pos we clicked
		this.selected_id_passthru = pos;

		Intent search_intent = new Intent(this, NavitMapPreviewActivity.class);

		if (Navit.use_index_search)
		{
			//Log.e("Navit", "long click lat=" + Navit.NavitAddressResultList_foundItems.get(pos).lat);
			//Log.e("Navit", "long click lon=" + Navit.NavitAddressResultList_foundItems.get(pos).lon);
			//Log.e("Navit", "long click lat=" + Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(pos).lat));
			//Log.e("Navit", "long click lon=" + Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(pos).lon));
			search_intent.putExtra("lat", (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(pos).lat));
			search_intent.putExtra("lon", (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(pos).lon));
		}
		else
		{
			search_intent.putExtra("lat", Navit.NavitAddressResultList_foundItems.get(pos).lat);
			search_intent.putExtra("lon", Navit.NavitAddressResultList_foundItems.get(pos).lon);
		}
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
						executeDone("set");
					}
					else if (sel_id == 2)
					{
						// "back"
					}
					else if (sel_id == 3)
					{
						// show destination on map
						this.selected_id = this.selected_id_passthru;
						executeDone("view");
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
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
	{
		// v.setBackgroundResource(R.color.background_material_dark);

		onLongListItemClick(v, groupPosition, childPosition, id);
		return true;
	}

	private void executeDone(String what)
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
		if (what.equals("view"))
		{
			resultIntent.putExtra("what", "view");
		}
		else if (what.equals("set"))
		{
			resultIntent.putExtra("what", "set");
		}
		else
		{
			resultIntent.putExtra("what", "-");
		}
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	static void force_done()
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("what", "-");
		NavitAddressResultListActivity_s.setResult(Activity.RESULT_OK, resultIntent);
		NavitAddressResultListActivity_s.finish();
	}
}
