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

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ZANaviNormalDonateActivity extends ActionBarActivity
{

	static boolean on_amazon_device = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.PREF_current_theme);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_normal_donate);

		android.support.v7.widget.Toolbar bar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar2nd);
		bar.setTitle(Navit.get_text("Donate"));
		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		((Button) findViewById(R.id.b1_donate_001)).setText(Navit.get_text("buy Donate Version"));
		((Button) findViewById(R.id.b1_donate_002)).setText(Navit.get_text("buy large-map Donate Version"));
		((Button) findViewById(R.id.b1_donate_003)).setText(Navit.get_text("Donate with the offcial Donate App"));

		((TextView) findViewById(R.id.t1_donate_001)).setText(Navit.get_text("_long_text_donate_version_"));
		((TextView) findViewById(R.id.t1_donate_002)).setText(Navit.get_text("_long_text_large_map_donate_version_"));
		((TextView) findViewById(R.id.t1_donate_003)).setText(Navit.get_text("_long_text_donate_app_"));

		if (on_amazon_device)
		{
			findViewById(R.id.b1_donate_003).setVisibility(View.INVISIBLE);
			findViewById(R.id.t1_donate_003).setVisibility(View.INVISIBLE);
		}
	}

	public void on_buy_donate_version(View arg0)
	{
		try
		{
			try
			{
				PackageInfo pkgInfo;
				String url_to_donate_app = "https://play.google.com/store/apps/details?id=com.zoffcc.applications.zanavi_donate";
				if (on_amazon_device)
				{
					url_to_donate_app = "http://www.amazon.com/gp/mas/dl/android?p=com.zoffcc.applications.zanavi_donate";
				}

				try
				{
					// is the donate version installed?
					pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_donate", 0);
					// System.out.println("pkginfo donate=" + pkgInfo);
					if (pkgInfo.versionCode > 0)
					{
						System.out.println("## donate version installed ##");
						Toast.makeText(this, Navit.get_text("ZANavi Donate Version already installed"), Toast.LENGTH_LONG).show();
						return;
					}
				}
				catch (NameNotFoundException e1)
				{
					e1.printStackTrace();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}

				try
				{
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url_to_donate_app));
					startActivity(i);
				}
				catch (Exception e3)
				{
				}
			}
			catch (Exception e)
			{
			}
		}
		catch (Exception e)
		{
		}
	}

	public void on_buy_large_map_donate_version(View arg0)
	{
		try
		{
			try
			{
				PackageInfo pkgInfo;
				String url_to_donate_app = "https://play.google.com/store/apps/details?id=com.zoffcc.applications.zanavi_largemap_donate";
				if (on_amazon_device)
				{
					url_to_donate_app = "http://www.amazon.com/gp/mas/dl/android?p=com.zoffcc.applications.zanavi_largemap_donate";
				}

				try
				{
					// is the donate version installed?
					pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_largemap_donate", 0);
					// System.out.println("pkginfo donate=" + pkgInfo);
					if (pkgInfo.versionCode > 0)
					{
						System.out.println("## large map donate version installed ##");
						Toast.makeText(this, Navit.get_text("ZANavi large map Donate Version already installed"), Toast.LENGTH_LONG).show();
						return;
					}
				}
				catch (NameNotFoundException e1)
				{
					e1.printStackTrace();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}

				try
				{
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url_to_donate_app));
					startActivity(i);
				}
				catch (Exception e3)
				{
				}
			}
			catch (Exception e)
			{
			}
		}
		catch (Exception e)
		{
		}
	}

	public void on_start_donate_app(View arg0)
	{
		try
		{
			if (on_amazon_device)
			{
				Toast.makeText(this, Navit.get_text("There is no Donate App on Amazon-device"), Toast.LENGTH_LONG).show();
			}
			else
			{

				PackageInfo pkgInfo;
				String url_to_donate_app = "https://play.google.com/store/apps/details?id=com.zoffcc.applications.zanavi_any_donate";

				try
				{
					// is the donate app installed?
					pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_any_donate", 0);
					// System.out.println("pkginfo donate=" + pkgInfo);
					if (pkgInfo.versionCode > 0)
					{
						System.out.println("## donate app installed ##");
						// start the donate app

						Intent intent_donateapp = new Intent(Intent.ACTION_MAIN);
						intent_donateapp.setPackage("com.zoffcc.applications.zanavi_any_donate");
						intent_donateapp.setClassName("com.zoffcc.applications.zanavi_any_donate", "com.zoffcc.applications.zanavi_any_donate.ZANaviAnyDonateActivity");
						Toast.makeText(this, Navit.get_text("starting Donate App"), Toast.LENGTH_LONG).show();
						startActivity(intent_donateapp);
						return;
					}
				}
				catch (NameNotFoundException e1)
				{
					e1.printStackTrace();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}

				try
				{
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url_to_donate_app));
					startActivity(i);
				}
				catch (Exception e3)
				{
				}
			}
		}
		catch (Exception e)
		{
		}
	}
}
