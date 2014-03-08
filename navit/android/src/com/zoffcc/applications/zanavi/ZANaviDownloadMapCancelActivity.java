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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ZANaviDownloadMapCancelActivity extends Activity
{

	public static TextView addr_view2 = null;
	TextView addr_view = null;
	private static ZANaviDownloadMapCancelActivity my_object = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		my_object = this;

		// panel linearlayout
		LinearLayout panel = new LinearLayout(this);
		panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		panel.setOrientation(LinearLayout.VERTICAL);

		// header text
		addr_view = new TextView(this);
		addr_view.setText(Navit.get_text("Stop map download?")); //TRANS
		addr_view.setGravity(Gravity.CENTER);
		addr_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		addr_view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		addr_view.setPadding(4, 10, 4, 10);

		// message
		addr_view2 = new TextView(this);
		addr_view2.setText("");
		addr_view2.setGravity(Gravity.CENTER_HORIZONTAL);
		addr_view2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		addr_view2.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		addr_view2.setPadding(4, 10, 4, 10);

		// "Yes" button
		final Button btnSearch = new Button(this);
		btnSearch.setText(Navit.get_text("Yes")); //TRANS
		btnSearch.setPadding(4, 10, 4, 10);
		btnSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f);
		btnSearch.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
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

		// "No" button
		final Button btnSearch2 = new Button(this);
		btnSearch2.setText(Navit.get_text("No")); //TRANS
		btnSearch2.setPadding(4, 10, 4, 10);
		btnSearch2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f);
		btnSearch2.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		btnSearch2.setGravity(Gravity.CENTER);
		btnSearch2.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				executeDone(0);
			}
		});

		// actually adding the views (that have layout set on them) to the panel
		panel.addView(addr_view);
		panel.addView(btnSearch);
		panel.addView(btnSearch2);
		panel.addView(addr_view2);

		// set the main view
		setContentView(panel);
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
