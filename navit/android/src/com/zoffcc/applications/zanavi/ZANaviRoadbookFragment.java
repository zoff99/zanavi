/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2015 Zoff <zoff@zoff.cc>
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
import java.util.List;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.zoffcc.applications.zanavi.ZANaviListViewAdapter.ListViewItem;

public class ZANaviRoadbookFragment extends ListFragment
{
	private List<ListViewItem> mItems = null; // ListView items list
	private ZANaviListViewAdapter adapter = null;

	private boolean active = false;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		// remove the dividers from the ListView of the ListFragment
		try
		{
			// getListView().setDivider(null);
			getListView().setDividerHeight(0);
			getListView().setFastScrollEnabled(true);
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// initialize the items list
		mItems = new ArrayList<ListViewItem>();
		Resources resources = getResources();
		// initialize and set the list adapter
		adapter = new ZANaviListViewAdapter(getActivity(), mItems);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		// System.out.println("FRAG:onListItemClick:" + position + " " + id);

		//		try
		//		{
		//			final ZANaviListViewAdapter.ListViewItem l2 = adapter.getItem(position);
		//			//
		//			Navit.follow_button_off();
		//			Navit.animate_bottom_bar_down();
		//
		//			new AsyncTask<Void, Void, String>()
		//			{
		//				@Override
		//				protected String doInBackground(Void... params)
		//				{
		//					{
		//						try
		//						{
		//							Navit.set_zoom_level_no_draw(Navit.Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL);
		//							Thread.sleep(20);
		//							Navit.show_geo_on_screen_no_draw(l2.lat, l2.lon);
		//							Thread.sleep(200);
		//							Navit.draw_map();
		//						}
		//						catch (Exception e)
		//						{
		//						}
		//					}
		//					return "";
		//				}
		//
		//				@Override
		//				protected void onPostExecute(String msg)
		//				{
		//
		//				}
		//			}.execute(null, null, null);
		//		}
		//		catch (Exception e)
		//		{
		//		}

		try
		{
			final ZANaviListViewAdapter.ListViewItem l2 = adapter.getItem(position);
			//
			Navit.follow_button_off();
			Navit.animate_bottom_bar_down();

			final Thread temp_work_thread_2 = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						Navit.show_geo_on_screen_no_draw(l2.lat, l2.lon);
						Thread.sleep(500);
						Navit.set_zoom_level_no_draw(Navit.Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL);
						Thread.sleep(120);
						Navit.draw_map();
					}
					catch (Exception e2)
					{
						e2.printStackTrace();
					}
				}
			};
			temp_work_thread_2.start();
		}
		catch (Exception e)
		{
		}
	}

	public synchronized void reload_items(List<ListViewItem> new_items)
	{
		if (active)
		{
			adapter.clear();
			try
			{
				adapter.addAll(new_items);
			}
			catch (java.lang.NoSuchMethodError ee)
			{
				// compatibility method ----------
				int item;
				for (item = 0; item < new_items.size(); item++)
				{
					adapter.add(new_items.get(item));
				}
				// compatibility method ----------
			}
			adapter.notifyDataSetChanged();
		}
		else
		{
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		active = true;

		// fragment is now loaded and ready to use
		try
		{
			getListView().setDividerHeight(NavitGraphics.dp_to_px(1));
		}
		catch (Exception e)
		{
		}

		try
		{
			getListView().setDivider(Navit.res_.getDrawable(R.drawable.horizontal_divider));
		}
		catch (Exception e)
		{
		}

		try
		{
			Navit.cwthr.CallbackGeoCalc2(13, 0, 12345, 0);
		}
		catch (Exception eerb1)
		{

		}
	}

	@Override
	public void onDestroyView()
	{
		active = false;
		super.onDestroyView();
	}

}
