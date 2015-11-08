/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2014 Zoff <zoff@zoff.cc>
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
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ZANaviDownloadMapCancelActivity extends ActionBarActivity
{

	public static TextView addr_view2 = null;
	TextView addr_view = null;
	TextView info_view = null;
	private static ZANaviDownloadMapCancelActivity my_object = null;
	static ProgressBar pg = null;
	static ProgressBar[] pg_speed = null;
	static Drawable[] pg_speed_d = null;
	static TextView[] pg_speed_txt = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.p.PREF_current_theme);

		super.onCreate(savedInstanceState);

		my_object = this;

		setContentView(R.layout.activity_download_cancel_activity);

		android.support.v7.widget.Toolbar bar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar2nd);
		bar.setTitle(Navit.get_text("Download"));
		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		//		// header text
		addr_view = (TextView) findViewById(R.id.addr_view);
		addr_view.setText(Navit.get_text("Stop map download?")); //TRANS
		addr_view.setGravity(Gravity.CENTER);
		addr_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		//		addr_view.setPadding(4, 10, 4, 10);
		//
		//		// info text
		info_view = (TextView) findViewById(R.id.info_view);
		info_view.setText(Navit.get_text("press HOME to download in the background")); //TRANS
		info_view.setGravity(Gravity.CENTER);
		info_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		//		info_view.setPadding(4, 10, 4, 10);
		//
		//		// message
		addr_view2 = (TextView) findViewById(R.id.addr_view2);
		addr_view2.setText("");
		addr_view2.setGravity(Gravity.CENTER_HORIZONTAL);
		addr_view2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		//		addr_view2.setPadding(4, 10, 4, 10);
		//
		//		// "Yes" button
		final Button btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setText(Navit.get_text("Yes")); //TRANS
		//		btnSearch.setPadding(4, 10, 4, 10);
		btnSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f);
		btnSearch.setGravity(Gravity.CENTER);
		btnSearch.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				// ----- service stop -----
				// ----- service stop -----
				System.out.println("Navit:map download cancel dialog -> stop ZANaviMapDownloaderService ---------");
				try
				{
					ZANaviMapDownloaderService.stop_downloading();
				}
				catch (Exception e)
				{
				}

				try
				{
					Navit.getBaseContext_.stopService(Navit.ZANaviMapDownloaderServiceIntent);
				}
				catch (Exception e)
				{
				}
				// ----- service stop -----
				// ----- service stop -----

				executeDone(1);
			}
		});

		//		// "No" button
		final Button btnSearch2 = (Button) findViewById(R.id.btnSearch2);

		btnSearch2.setText(Navit.get_text("No")); //TRANS
		//		btnSearch2.setPadding(4, 10, 4, 10);
		btnSearch2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f);
		btnSearch2.setGravity(Gravity.CENTER);
		btnSearch2.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				executeDone(0);
			}
		});

		pg = (ProgressBar) findViewById(R.id.mapdownload_prog_bar);
		pg.setProgress(0);

		pg_speed = new ProgressBar[NavitMapDownloader.MULTI_NUM_THREADS_MAX];
		pg_speed_d = new Drawable[NavitMapDownloader.MULTI_NUM_THREADS_MAX];
		pg_speed_txt = new TextView[NavitMapDownloader.MULTI_NUM_THREADS_MAX];

		Resources r = getResources();
		int pg_i = 0;
		int pg_speed_id = 0;
		for (pg_i = 0; pg_i < NavitMapDownloader.MULTI_NUM_THREADS_MAX; pg_i++)
		{
			try
			{
				pg_speed[pg_i] = null;
				pg_speed_id = r.getIdentifier("mapdownload_speed_bar_t00" + (pg_i + 1), "id", "com.zoffcc.applications.zanavi");
				pg_speed[pg_i] = (ProgressBar) findViewById(pg_speed_id);
				//if (pg_i >= NavitMapDownloader.MULTI_NUM_THREADS)
				//{
				pg_speed[pg_i].setVisibility(View.INVISIBLE);
				//}
				pg_speed_d[pg_i] = pg_speed[pg_i].getProgressDrawable();

				pg_speed_txt[pg_i] = (TextView) findViewById(r.getIdentifier("mapdownload_speed_text_t00" + (pg_i + 1), "id", "com.zoffcc.applications.zanavi"));
				pg_speed_txt[pg_i].setVisibility(View.INVISIBLE);
			}
			catch (Exception e2)
			{
				System.out.println("PGB:EE3=" + e2.getMessage());
			}
		}
	}

	public static Handler canceldialog_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 0:
				try
				{
					String text1 = msg.getData().getString("text");
					ZANaviDownloadMapCancelActivity.addr_view2.setText(text1);
					ZANaviDownloadMapCancelActivity.addr_view2.postInvalidate();
				}
				catch (Exception e)
				{
					//e.printStackTrace();
				}
				break;
			case 1:
				try
				{
					// clear this activity (map is ready)
					ZANaviDownloadMapCancelActivity.addr_view2.setText("");
					ZANaviDownloadMapCancelActivity.addr_view2.postInvalidate();
				}
				catch (Exception e)
				{
					//e.printStackTrace();
				}

				try
				{
					my_object.executeDone(1);
				}
				catch (Exception e)
				{
					//e.printStackTrace();
				}
				break;
			case 2:
				try
				{
					ZANaviDownloadMapCancelActivity.pg.setProgress(msg.getData().getInt("pg"));
					// System.out.println("PG_percent=" + msg.getData().getInt("pg"));
					// ZANaviDownloadMapCancelActivity.pg.postInvalidate();
				}
				catch (Exception e)
				{
					// System.out.println("PG_percent:EE:" + e.getMessage());
				}
				break;
			case 3:
				try
				{
					final int max_kb_per_sec = 3000;
					int t_num = msg.getData().getInt("threadnum");
					int speed = msg.getData().getInt("speed_kb_per_sec");

					// System.out.println("PGB:num=" + t_num);

					if (speed == -1)
					{
						pg_speed[t_num].setVisibility(View.INVISIBLE);
					}
					else if (speed == -2)
					{
						pg_speed[t_num].setVisibility(View.VISIBLE);
					}
					else
					{
						try
						{
							int progress = (int) (((float) speed / (float) max_kb_per_sec) * 100.f);
							if (progress > 100)
							{
								progress = 100;
							}
							pg_speed_d[t_num].setLevel(progress * 100);
							pg_speed[t_num].setProgress(progress);

							if (pg_speed[t_num].getVisibility() == View.INVISIBLE)
							{
								pg_speed[t_num].setVisibility(View.VISIBLE);
							}
						}
						catch (Exception e)
						{
							System.out.println("PGB:EE2=" + e.getMessage());
						}
					}
				}
				catch (Exception e0)
				{
				}
				break;

			case 4:
				try
				{
					int t_num = msg.getData().getInt("threadnum");
					String srv = msg.getData().getString("srv");

					// System.out.println("PGB:srvname=" + srv + " #" + t_num);

					if (srv.compareTo("-1") == 0)
					{
						pg_speed_txt[t_num].setVisibility(View.INVISIBLE);
					}
					else if (srv.compareTo("-2") == 0)
					{
						pg_speed_txt[t_num].setVisibility(View.VISIBLE);
					}
					else
					{
						pg_speed_txt[t_num].setText(srv + ":");
						
						if (pg_speed_txt[t_num].getVisibility() == View.INVISIBLE)
						{
							pg_speed_txt[t_num].setVisibility(View.VISIBLE);
						}
					}
				}
				catch (Exception e)
				{
					System.out.println("PGB:EESRV=" + e.getMessage());
				}
				break;
			}
		}
	};

	private void executeDone(int code)
	{
		System.out.println("Navit:map download cancel dialog -> executeDone()");

		Intent resultIntent = new Intent();
		if (code == 0)
		{
			setResult(Activity.RESULT_CANCELED, resultIntent);
		}
		else
		{
			setResult(Activity.RESULT_OK, resultIntent);
		}

		finish();
	}
}
