/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2016 Zoff <zoff@zoff.cc>
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;
import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.zoffcc.applications.logging.Logging;
import com.zoffcc.applications.logging.Logging.AsyncResponse;

@SuppressLint("NewApi")
public class ZANaviMainIntroActivityStatic extends AppCompatActivity implements AsyncResponse
{
	private ViewPager viewPager;
	private MyViewPagerAdapter myViewPagerAdapter;
	private LinearLayout dotsLayout;
	private TextView[] dots;
	ArrayList<Integer> layouts = new ArrayList<Integer>();
	ArrayList<Integer> colors = new ArrayList<Integer>();
	ArrayList<String> title_txt = new ArrayList<String>();
	ArrayList<Spanned> desc_txt = new ArrayList<Spanned>();
	ArrayList<Integer> icon_res = new ArrayList<Integer>();
	ArrayList<Integer> id_ = new ArrayList<Integer>();
	private Button btnSkip, btnNext;
	private int progress = 0;
	private ArgbEvaluator argbEvaluator = null;

	ProgressDialog progressDialog2;

	ArrayList<String> disk_locations = new ArrayList<String>();
	ArrayList<String> disk_locations_long = new ArrayList<String>();
	ArrayList<String> disk_locations_path = new ArrayList<String>();

	final private static int ID_CRASH = 100;
	final private static int ID_PERM = 101;
	final private static int ID_FIRST = 102;
	final private static int ID_UPD = 103;
	final private static int ID_INFO = 104;
	final private static int ID_NOMAPS = 105;
	final private static int ID_INDEX = 106;

	static boolean custom_path_not_needed = false;
	static int currently_selected = -1;
	private static int slide_press = -1;

	static String screen_id_to_name(int id)
	{
		switch (id)
		{
		case ID_CRASH:
			return "ID_CRASH";
		case ID_PERM:
			return "ID_PERM";
		case ID_FIRST:
			return "ID_FIRST";
		case ID_UPD:
			return "ID_UPD";
		case ID_INFO:
			return "ID_INFO";
		case ID_NOMAPS:
			return "ID_NOMAPS";
		case ID_INDEX:
			return "ID_INDEX";
		default:
			return "*unknown*";
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		System.out.println("ZANaviMainIntroActivity:" + "onCreate");

		if (Build.VERSION.SDK_INT >= 11)
		{
			argbEvaluator = new ArgbEvaluator();
		}

		// Making notification bar transparent
		if (Build.VERSION.SDK_INT >= 21)
		{
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}

		setContentView(R.layout.intro_slide_static);

		viewPager = (ViewPager) findViewById(R.id.view_pager);
		dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
		btnSkip = (Button) findViewById(R.id.btn_skip);
		btnNext = (Button) findViewById(R.id.btn_next);

		btnNext.setText(Navit.get_text("OK"));

		if (Navit.intro_flag_crash)
		{
			System.out.println("flags:" + "intro_flag_crash:true " + "starting crashlog screen");

			layouts.add(R.layout.intro_slide_static_00);
			colors.add(ContextCompat.getColor(this, R.color.zanavi_yellowbrown));
			id_.add(ID_CRASH);
			title_txt.add("");
			desc_txt.add(Html.fromHtml(Navit.get_text("ZANavi has recently crashed, please submit the crashlog") + "\n<br>" + "<font color=\"#fe0000\">" + Navit.get_text("Log may contain private and sensitive data!") + "</font>"));
			icon_res.add(R.drawable.crash_icon);
		}

		try
		{
			if (EasyPermissions.hasPermissions(ZANaviMainIntroActivityStatic.this, Navit.perms))
			{
			}
			else
			{
				layouts.add(R.layout.intro_slide_static_01);
				colors.add(ContextCompat.getColor(this, R.color.zanavi_yellowbrown));
				id_.add(ID_PERM);
				title_txt.add(Navit.get_text("Grant Permissions"));
				desc_txt.add(Html.fromHtml(""));
				icon_res.add(R.drawable.app_intro_permissions_v1);
			}
		}
		catch (Exception e)
		{
		}

		if (Navit.intro_flag_firststart)
		{
			layouts.add(R.layout.intro_slide_static_02);
			colors.add(ContextCompat.getColor(this, R.color.zanavi_teal));
			id_.add(ID_FIRST);
			title_txt.add("");
			desc_txt.add(Html.fromHtml(Navit.get_text("Welcome to ZANavi offline Navigation")));
			icon_res.add(R.drawable.icon_large);
		}

		if (!Navit.intro_flag_firststart)
		{
			if (Navit.intro_flag_update)
			{
				layouts.add(R.layout.intro_slide_static_03);
				colors.add(ContextCompat.getColor(this, R.color.zanavi_teal));
				id_.add(ID_UPD);
				title_txt.add("");
				desc_txt.add(Html.fromHtml(Navit.get_text("you have just updated ZANavi")));
				icon_res.add(R.drawable.icon_large);
			}
		}

		if (!Navit.intro_flag_firststart)
		{
			if (Navit.intro_flag_info)
			{

				final String ZANAVI_MSG_PLUGIN_MARKET_LINK = "https://play.google.com/store/apps/details?id=com.zoffcc.applications.zanavi_msg";
				final String ZANAVI_MSG_PLUGIN_FD_LINK = "https://static.zanavi.cc/app/zanavi_plugin_latest.apk";
				// final String ZANAVI_UDONATE_LINK = "http://more.zanavi.cc/donate/";
				final String ZANAVI_HOWTO_DEBUG_LINK = "http://static.zanavi.cc/be-a-testdriver/be-a-testdriver.html";
				final String ZANAVI_HOWTO_UDONTATE_FREE_LINK = "http://static.zanavi.cc/activate-udonate/activate-udonate.html";

				layouts.add(R.layout.intro_slide_static_04);
				colors.add(ContextCompat.getColor(this, R.color.zanavi_teal));
				title_txt.add("");

				if (Navit.FDBL)
				{
					desc_txt.add(Html.fromHtml("<small>show " + (Navit.info_popup_seen_count_max - Navit.info_popup_seen_count + 1) + " more times<br>\n<br>\n</small>Help us to improve ZANavi, be a Testdriver and send in your route debug information.<br>\n<a href=\"" + ZANAVI_HOWTO_DEBUG_LINK + "\">HowTo be a Testdriver</a><br>\n<br>\n" + "And get the uDonate Version for free.<br>\n<a href=\"" + ZANAVI_HOWTO_UDONTATE_FREE_LINK + "\">get free uDonate version</a>\n" + "\n<br>\n<br>"
							+ "Install the ZANavi Plugin and always know when updated maps are available.<br>\n<a href=\"" + ZANAVI_MSG_PLUGIN_FD_LINK + "\">download here</a><br>\n"));
				}
				else
				{
					desc_txt.add(Html.fromHtml("<small>show " + (Navit.info_popup_seen_count_max - Navit.info_popup_seen_count + 1) + " more times<br>\n<br>\n</small>Help us to improve ZANavi, be a Testdriver and send in your route debug information.<br>\n<a href=\"" + ZANAVI_HOWTO_DEBUG_LINK + "\">HowTo be a Testdriver</a><br>\n<br>\n" + "And get the uDonate Version for free.<br>\n<a href=\"" + ZANAVI_HOWTO_UDONTATE_FREE_LINK + "\">get free uDonate version</a>\n" + "\n<br>\n<br>"
							+ "Install the ZANavi Plugin and always know when updated maps are available.<br>\n<a href=\"" + ZANAVI_MSG_PLUGIN_MARKET_LINK + "\">download here</a><br>\n"));
				}

				icon_res.add(R.drawable.icon);
				id_.add(ID_INFO);
				// reset flag
				Navit.intro_flag_info = false;
			}
		}

		if (Navit.intro_flag_nomaps)
		{
			layouts.add(R.layout.intro_slide_static_05);
			colors.add(ContextCompat.getColor(this, R.color.zanavi_purple));
			title_txt.add("");
			desc_txt.add(Html.fromHtml(Navit.get_text("select your storage and download a Map for your Area")));
			icon_res.add(R.drawable.app_intro_mapdownload_v1);
			id_.add(ID_NOMAPS);
		}

		if (!Navit.intro_flag_nomaps)
		{
			if (Navit.intro_flag_indexmissing)
			{
				layouts.add(R.layout.intro_slide_static_06);
				colors.add(ContextCompat.getColor(this, R.color.zanavi_purple));
				title_txt.add("");
				desc_txt.add(Html.fromHtml(Navit.get_text("Index missing, please delete your maps and download them again")));
				icon_res.add(R.drawable.app_intro_mapdownload_v1);
				id_.add(ID_INDEX);
			}
		}

		if (layouts.size() == 1)
		{
			btnNext.setText(Navit.get_text("OK"));
		}
		else
		{
			btnNext.setText(Navit.get_text("Next"));
		}
		// layouts of all sliders ------------------------------------------

		// adding bottom dots
		addBottomDots(0);

		// making notification bar transparent
		changeStatusBarColor();

		myViewPagerAdapter = new MyViewPagerAdapter();
		viewPager.setAdapter(myViewPagerAdapter);
		viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

		btnSkip.setVisibility(View.INVISIBLE);
		btnSkip.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				System.out.println("ZANaviMainIntroActivity:" + "finish2");
				finish();
			}
		});

		btnNext.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// checking for last page
				// if last page home screen will be launched
				int current = getItem(+1);

				// System.out.println("current=" + current + " layouts.length=" + layouts.length);

				if ((current < layouts.size()) && (layouts.size() > 1))
				{
					// move to next screen
					viewPager.setCurrentItem(current);
				}
				else
				{
					System.out.println("ZANaviMainIntroActivity:" + "finish3");
					finish();
				}
			}
		});
	}

	public void onStop()
	{
		super.onStop();
		System.out.println("ZANaviMainIntroActivity:" + "onStop");
	}

	public void onDestroy()
	{
		super.onDestroy();
		System.out.println("ZANaviMainIntroActivity:" + "onDestroy");
	}

	public void onPause()
	{
		super.onPause();
		System.out.println("ZANaviMainIntroActivity:" + "onPause");
	}

	@Override
	public void onResume()
	{
		super.onResume();

		System.out.println("ZANaviMainIntroActivity:" + "onResume");
		System.out.println("ZANaviMainIntroActivity:" + "onResume" + "slide_press=" + slide_press);
		try
		{
			if (slide_press == 5)
			{
				if (NavitMapDownloader.download_active_start)
				{
					Navit.intro_flag_nomaps = false;
					// go to next slide
					btnNext.callOnClick();
				}
			}
			else if (slide_press == 6)
			{
				Navit.intro_flag_indexmissing = false;
				// go to next slide
				btnNext.callOnClick();
			}
			else if (slide_press == 0)
			{
				Navit.intro_flag_crash = false;
				// go to next slide
				btnNext.callOnClick();
			}
		}
		catch (Exception e)
		{
			System.out.println("ZANaviMainIntroActivity:" + "onResume:Ex01");
		}
		catch (java.lang.NoSuchMethodError e2)
		{
			System.out.println("ZANaviMainIntroActivity:" + "onResume:Ex02");

			try
			{
				System.out.println("ZANaviMainIntroActivity:" + "onResume" + " finish4");
				finish();
			}
			catch (Exception e4)
			{
			}
		}

		slide_press = -1;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		System.out.println("ZANaviMainIntroActivity:" + "onActivityResult");

		switch (requestCode)
		{
		case Navit.NavitDeleteSecSelectMap_id:
			try
			{
				if (resultCode == AppCompatActivity.RESULT_OK)
				{
					System.out.println("Global_Location_update_not_allowed = 1");
					Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

					// remove all sdcard maps
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putInt("Callback", 19);
					msg.setData(b);
					NavitGraphics.callback_handler.sendMessage(msg);

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
					}

					Log.d("Navit", "delete map id=" + Integer.parseInt(data.getStringExtra("selected_id")));
					String map_full_line = NavitMapDownloader.OSM_MAP_NAME_ondisk_ORIG_LIST[Integer.parseInt(data.getStringExtra("selected_id"))];
					Log.d("Navit", "delete map full line=" + map_full_line);

					String del_map_name = Navit.MAP_FILENAME_PATH + map_full_line.split(":", 2)[0];
					System.out.println("del map file :" + del_map_name);
					// remove from cat file
					NavitMapDownloader.remove_from_cat_file(map_full_line);
					// remove from disk
					File del_map_name_file = new File(del_map_name);
					del_map_name_file.delete();
					for (int jkl = 1; jkl < 51; jkl++)
					{
						File del_map_name_fileSplit = new File(del_map_name + "." + String.valueOf(jkl));
						del_map_name_fileSplit.delete();
					}
					// also remove index file
					File del_map_name_file_idx = new File(del_map_name + ".idx");
					del_map_name_file_idx.delete();
					// remove also any MD5 files for this map that may be on disk
					try
					{
						String tmp = map_full_line.split(":", 2)[1];
						if (!tmp.equals(NavitMapDownloader.MAP_URL_NAME_UNKNOWN))
						{
							tmp = tmp.replace("*", "");
							tmp = tmp.replace("/", "");
							tmp = tmp.replace("\\", "");
							tmp = tmp.replace(" ", "");
							tmp = tmp.replace(">", "");
							tmp = tmp.replace("<", "");
							System.out.println("removing md5 file:" + Navit.MAPMD5_FILENAME_PATH + tmp + ".md5");
							File md5_final_filename = new File(Navit.MAPMD5_FILENAME_PATH + tmp + ".md5");
							md5_final_filename.delete();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
					}

					// add all sdcard maps
					msg = new Message();
					b = new Bundle();
					b.putInt("Callback", 20);
					msg.setData(b);
					NavitGraphics.callback_handler.sendMessage(msg);

					System.out.println("Global_Location_update_not_allowed = 0");
					Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!

					// -----------------
					Navit.intro_flag_indexmissing = false;
					try
					{
						// go to next slide
						btnNext.callOnClick();
						// -----------------
					}
					catch (java.lang.NoSuchMethodError e2)
					{
						System.out.println("ZANaviMainIntroActivity:" + "callOnClick:Ex01");
					}
				}
			}
			catch (Exception e)
			{
				Log.d("Navit", "error on onActivityResult 3");
				e.printStackTrace();
			}
			break;
		}
	}

	private void addBottomDots(int currentPage)
	{
		dots = new TextView[layouts.size()];

		int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
		int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

		dotsLayout.removeAllViews();
		for (int i = 0; i < dots.length; i++)
		{
			dots[i] = new TextView(this);
			dots[i].setText(Html.fromHtml("&#8226;"));
			dots[i].setTextSize(35);
			dots[i].setTextColor(colorsInactive[currentPage]);
			dotsLayout.addView(dots[i]);
		}

		if (dots.length > 0) dots[currentPage].setTextColor(colorsActive[currentPage]);
	}

	private int getItem(int i)
	{
		return viewPager.getCurrentItem() + i;
	}

	//  viewpager change listener
	ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener()
	{

		@Override
		public void onPageSelected(int position)
		{
			System.out.println("position, progress: " + screen_id_to_name(id_.get(position)) + ", " + screen_id_to_name(id_.get(progress)));

			try
			{
				if (id_.get(progress) == ID_PERM)
				{
					// don't leave permission screen until granted
					if (EasyPermissions.hasPermissions(ZANaviMainIntroActivityStatic.this, Navit.perms))
					{
					}
					else
					{
						System.out.println("intro_logic:dont leave permissions screen");
						viewPager.setCurrentItem(progress, true);
						return;
					}
				}
			}
			catch (Exception e)
			{
			}

			if ((id_.get(progress) == ID_CRASH) && (position > progress) && (Navit.intro_flag_crash == true))
			{
				// don't allow to leave "crash" screen with swiping forward
				System.out.println("intro_logic:dont leave crash screen");
				viewPager.setCurrentItem(progress, true);
				return;
			}
			else if (position < progress)
			{
				// don't allow to swipe back
				System.out.println("intro_logic:dont allow swipe back");
				viewPager.setCurrentItem(progress, true);
				return;
			}
			else
			{
				progress = position;

				addBottomDots(position);

				// System.out.println("2 layouts.length=" + layouts.length + " position=" + position);

				// changing the next button text 'NEXT' / 'GOT IT'
				if (position == layouts.size() - 1)
				{
					// last page. make button text to GOT IT
					btnNext.setText(Navit.get_text("OK"));
					// btnSkip.setVisibility(View.GONE);
				}
				else
				{
					// still pages are left
					btnNext.setText(Navit.get_text("Next"));
					// btnSkip.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{
			if (Build.VERSION.SDK_INT >= 11)
			{
				if (position < (layouts.size() - 1) && position < (colors.size() - 1))
				{
					viewPager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset, colors.get(position), colors.get(position + 1)));
				}
				else
				{
					viewPager.setBackgroundColor(colors.get(colors.size() - 1));
				}
			}
			else
			{
				if (position < (layouts.size() - 1) && position < (colors.size() - 1))
				{
					viewPager.setBackgroundColor(ColorUtils.getColor(colors.get(position), colors.get(position + 1), positionOffset));
				}
				else
				{
					viewPager.setBackgroundColor(colors.get(colors.size() - 1));
				}
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0)
		{

		}
	};

	/**
	 * Making notification bar transparent
	 */
	@SuppressLint("NewApi")
	private void changeStatusBarColor()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
		}
	}

	/**
	 * View pager adapter
	 */
	public class MyViewPagerAdapter extends PagerAdapter
	{
		private LayoutInflater layoutInflater;

		public MyViewPagerAdapter()
		{
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			System.out.println("i_of: position=" + position);

			layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = layoutInflater.inflate(layouts.get(position), container, false);
			TextView header_text = null;
			TextView descr_text = null;

			int cur_id = id_.get(position);

			try
			{
				header_text = (TextView) view.findViewById(R.id.header_text_slide);
			}
			catch (Exception e)
			{
			}

			try
			{
				descr_text = (TextView) view.findViewById(R.id.desc_text_slide);
			}
			catch (Exception e)
			{
			}

			try
			{
				descr_text.setMovementMethod(LinkMovementMethod.getInstance());
			}
			catch (Exception ee3)
			{
			}

			ImageView icon_slide = null;
			try
			{
				icon_slide = (ImageView) view.findViewById(R.id.icon_slide);
			}
			catch (Exception e)
			{
			}

			try
			{
				header_text.setText(title_txt.get(position));
			}
			catch (Exception e)
			{
			}

			try
			{
				descr_text.setText(desc_txt.get(position));
			}
			catch (Exception e)
			{
			}

			try
			{
				icon_slide.setImageResource(icon_res.get(position));
			}
			catch (Exception e)
			{
				icon_slide.setVisibility(View.INVISIBLE);
			}

			container.addView(view);

			if (cur_id == ID_PERM)
			{
				System.out.println("i_of:PERMISSION_ID");

				try
				{
					Button button_slide = (Button) view.findViewById(R.id.button_slide);

					if (EasyPermissions.hasPermissions(ZANaviMainIntroActivityStatic.this, Navit.perms))
					{
						// button_slide.setVisibility(View.GONE);
					}
					else
					{
						button_slide.setText(Navit.get_text("Permissions"));
						button_slide.setVisibility(View.VISIBLE);
					}

					button_slide.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							try
							{
								// System.out.println("button 1 clicked");
								EasyPermissions.requestPermissions(ZANaviMainIntroActivityStatic.this, Navit.get_text("ZANavi needs all the Permissions granted"), Navit.RC_PERM_001, Navit.perms);
							}
							catch (Exception e)
							{
								e.printStackTrace();
								// System.out.println("button 1 clicked:" + e.getMessage());
							}
						}
					});
				}
				catch (Exception e)
				{
				}
			}

			else if (cur_id == ID_CRASH)
			{
				System.out.println("i_of:CRASH_ID");

				try
				{
					Button button_slide = (Button) view.findViewById(R.id.button_slide);
					button_slide.setText(Navit.get_text("Submit Log"));
					button_slide.setVisibility(View.VISIBLE);

					button_slide.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							try
							{
								slide_press = 0;

								progressDialog2 = ProgressDialog.show(ZANaviMainIntroActivityStatic.this, "", Navit.get_text("reading crash info ..."));

								progressDialog2.setCanceledOnTouchOutside(false);
								progressDialog2.setOnCancelListener(new DialogInterface.OnCancelListener()
								{
									@Override
									public void onCancel(DialogInterface dialog)
									{
									}
								});

								// get logcat messages ----------------
								Logging x = new Logging();
								Logging.delegate = ZANaviMainIntroActivityStatic.this;
								x.new PopulateLogcatAsyncTask(ZANaviMainIntroActivityStatic.this.getApplicationContext()).execute();
								// get logcat messages ----------------

							}
							catch (Exception e)
							{
							}
						}
					});
				}
				catch (Exception e)
				{
				}

				try
				{
					Button button_slide2 = (Button) view.findViewById(R.id.button_slide2);
					button_slide2.setText(Navit.get_text("No, thanks"));
					button_slide2.setVisibility(View.VISIBLE);

					button_slide2.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							try
							{
								try
								{
									slide_press = 0;

									// reset flag
									Navit.intro_flag_crash = false;

									try
									{
										// go to next slide
										btnNext.callOnClick();
										// -----------------
									}
									catch (java.lang.NoSuchMethodError e2)
									{
										System.out.println("ZANaviMainIntroActivity:" + "callOnClick:Ex09");
										try
										{
											System.out.println("ZANaviMainIntroActivity:" + "finish5");
											finish();
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
							catch (Exception e)
							{
							}
						}
					});
				}
				catch (Exception e)
				{
				}

			}

			else if (cur_id == ID_NOMAPS)
			{
				System.out.println("i_of:NOMAPS_ID");

				try
				{
					Button button_slide = (Button) view.findViewById(R.id.button_slide);
					button_slide.setText(Navit.get_text("Maps"));
					button_slide.setVisibility(View.VISIBLE);

					button_slide.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							try
							{
								try
								{
									slide_press = 5;

									// open map download select screen -----------
									Message msg = Navit.Navit_progress_h.obtainMessage();
									Bundle b = new Bundle();
									msg.what = 31;
									msg.setData(b);
									Navit.Navit_progress_h.sendMessage(msg);

									System.out.println("ZANaviMainIntroActivity:" + "finish1");
									finish();
								}
								catch (Exception e)
								{
								}
							}
							catch (Exception e)
							{
							}
						}
					});
				}
				catch (Exception e)
				{
				}

				try
				{
					Spinner spinner = (Spinner) findViewById(R.id.intro_spinner);
					spinner.setVisibility(View.VISIBLE);

					int count_sd_card = 1;
					String avail_space_string2 = "";
					File f = new File(Navit.NavitDataDirectory_Maps);
					int spinner_selection = 1;
					custom_path_not_needed = false;

					disk_locations.clear();
					disk_locations_long.clear();
					disk_locations_path.clear();

					// --- custom location ---
					// --- custom location ---
					// --- custom location ---
					String custom_map_path = new File(Navit.NavitDataDirectory_Maps).getAbsolutePath();
					long avail_space2 = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMB(custom_map_path);
					String avail_space_str2 = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMBformattedString(custom_map_path);
					if (avail_space2 < 0)
					{
						avail_space_string2 = "";
					}
					else if (avail_space2 > 1200)
					{
						avail_space_str2 = NavitAvailableSpaceHandler.getExternalAvailableSpaceInGBformattedString(custom_map_path);
						avail_space_string2 = " [" + avail_space_str2 + "GB free]";
					}
					else
					{
						avail_space_string2 = " [" + avail_space_str2 + "MB free]";
					}

					disk_locations.add("Custom" + avail_space_string2);
					disk_locations_long.add(disk_locations.get(disk_locations.size() - 1) + "\n" + split_every(custom_map_path, 33));
					disk_locations_path.add(custom_map_path);
					// --- custom location ---
					// --- custom location ---
					// --- custom location ---

					for (int jj2 = 0; jj2 < Navit.NavitDataStorageDirs.length; jj2++)
					{
						if (Navit.NavitDataStorageDirs[jj2] != null)
						{
							long avail_space = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMB(Navit.NavitDataStorageDirs[jj2].getAbsolutePath());
							String avail_space_str = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMBformattedString(Navit.NavitDataStorageDirs[jj2].getAbsolutePath());
							String avail_space_string = "";
							if (avail_space < 0)
							{
								avail_space_string = "";
							}
							else if (avail_space > 1200)
							{
								avail_space_str = NavitAvailableSpaceHandler.getExternalAvailableSpaceInGBformattedString(Navit.NavitDataStorageDirs[jj2].getAbsolutePath());
								avail_space_string = " [" + avail_space_str + "GB free]";
							}
							else
							{
								avail_space_string = " [" + avail_space_str + "MB free]";
							}

							if (jj2 == 0)
							{
								disk_locations.add("internal Storage" + avail_space_string);
								disk_locations_long.add(disk_locations.get(disk_locations.size() - 1) + "\n" + split_every(Navit.NavitDataStorageDirs[jj2].getAbsolutePath(), 33));
							}
							else
							{
								disk_locations.add("SD Card " + count_sd_card + avail_space_string);
								disk_locations_long.add(disk_locations.get(disk_locations.size() - 1) + "\n" + split_every(Navit.NavitDataStorageDirs[jj2].getAbsolutePath(), 33));
								count_sd_card++;
							}

							System.out.println("map:has=" + f.getAbsolutePath());
							System.out.println("map:sel=" + Navit.NavitDataStorageDirs[jj2].getAbsolutePath());

							if (custom_map_path.equals(Navit.NavitDataStorageDirs[jj2].getAbsolutePath()))
							{
								custom_path_not_needed = true;
							}

							disk_locations_path.add(Navit.NavitDataStorageDirs[jj2].getAbsolutePath());

							if (f.getAbsolutePath().equals(Navit.NavitDataStorageDirs[jj2].getAbsolutePath()))
							{
								spinner_selection = disk_locations.size() - 1;
							}
						}

					}

					if (custom_path_not_needed == true)
					{
						// remove custom path from selection
						if (spinner_selection > 0)
						{
							spinner_selection--;
							disk_locations.remove(0);
							disk_locations_long.remove(0);
							disk_locations_path.remove(0);
						}
					}

					CustomSpinnerAdapter dataAdapter = new CustomSpinnerAdapter(view.getContext(), disk_locations);
					spinner.setAdapter(dataAdapter);
					spinner.setSelection(spinner_selection);

					spinner.setOnItemSelectedListener(new OnItemSelectedListener()
					{
						@Override
						public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
						{
							if (currently_selected != position)
							{
								// we need to move the whole directory to a new location
								try
								{
									System.out.println("currently_selected=" + currently_selected + " position=" + position);

									if (!disk_locations_path.get(currently_selected).equals(disk_locations_path.get(position)))
									{
										File f1a = new File(disk_locations_path.get(currently_selected));
										File f2a = new File(disk_locations_path.get(position));

										System.out.println("currently_selected=f=" + disk_locations_path.get(currently_selected) + " to=" + disk_locations_path.get(position));

										int path_num = position;
										if (custom_path_not_needed == true)
										{
											path_num++;
										}
										AsyncTaskMapMover mover = new AsyncTaskMapMover(f1a, f2a, path_num);
										mover.execute();
									}
								}
								catch (Exception e6)
								{
									e6.printStackTrace();
								}
							}
							currently_selected = position;
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent)
						{
						}
					});
				}
				catch (Exception e)
				{
				}
			}
			else if (cur_id == ID_INFO)
			{
				System.out.println("i_of:ID_INFO");

				try
				{
					Button button_slide = (Button) view.findViewById(R.id.button_slide);
					button_slide.setText(Navit.get_text("Donate"));

					boolean already_installed = false;

					if (Navit.FDBL)
					{
						// fdroid
						PackageInfo pkgInfo;
						try
						{
							// is the u-donate version installed?
							pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_udonate", 0);
							// System.out.println("pkginfo donate=" + pkgInfo);
							if (pkgInfo.versionCode > 0)
							{
								System.out.println("## udonate version installed ##");
								already_installed = true;
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
					}
					else
					{
						// market (google play)
						PackageInfo pkgInfo;
						try
						{
							// is the largemap donate version installed?
							pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_largemap_donate", 0);
							// System.out.println("pkginfo donate=" + pkgInfo);
							if (pkgInfo.versionCode > 0)
							{
								System.out.println("## donate version installed ##");
								already_installed = true;
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

					}

					if (already_installed)
					{
						button_slide.setVisibility(View.INVISIBLE);
					}
					else
					{
						button_slide.setVisibility(View.VISIBLE);
					}

					button_slide.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							try
							{
								String url_to_donate_app = "http://info.zanavi.cc";

								try
								{
									slide_press = 4;
									if (Navit.FDBL)
									{
										// fdroid
										url_to_donate_app = "http://more.zanavi.cc/donate/";
									}
									else
									{
										// market (google play)
										url_to_donate_app = "https://play.google.com/store/apps/details?id=com.zoffcc.applications.zanavi_largemap_donate";
									}

								}
								catch (Exception e)
								{
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
					});
				}
				catch (Exception e)
				{
				}
			}
			else if (cur_id == ID_INDEX)
			{
				System.out.println("i_of:INDEX_ID");

				try
				{
					Button button_slide = (Button) view.findViewById(R.id.button_slide);
					button_slide.setText(Navit.get_text("Index"));
					button_slide.setVisibility(View.VISIBLE);

					button_slide.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View v)
						{
							try
							{
								try
								{
									slide_press = 6;

									Intent map_delete_list_activity2 = new Intent(ZANaviMainIntroActivityStatic.this.getBaseContext(), NavitDeleteSelectMapActivity.class);
									startActivityForResult(map_delete_list_activity2, Navit.NavitDeleteSecSelectMap_id);
								}
								catch (Exception e)
								{
								}
							}
							catch (Exception e)
							{
							}
						}
					});

				}
				catch (Exception e)
				{
				}
			}

			return view;
		}

		@Override
		public int getCount()
		{
			return layouts.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj)
		{
			return view == obj;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			System.out.println("position destroyItem:" + position);

			View view = (View) object;
			container.removeView(view);
		}
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			System.out.println("-request persmissions result 3-");

			super.onRequestPermissionsResult(requestCode, permissions, grantResults);

			// Forward results to EasyPermissions
			EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

			try
			{
				if (EasyPermissions.hasPermissions(ZANaviMainIntroActivityStatic.this, Navit.perms))
				{
					// if all permissions are granted, remove the slide and go to next slide
					// button_slide_perm.setVisibility(View.INVISIBLE);
					// button_slide_perm.setVisibility(View.GONE);
				}
			}
			catch (Exception e)
			{
			}

			try
			{
				if (EasyPermissions.hasPermissions(ZANaviMainIntroActivityStatic.this, Navit.perms))
				{
					// PERMISSION_ID = -1;

					try
					{
						// go to next slide
						btnNext.callOnClick();
					}
					catch (java.lang.NoSuchMethodError e2)
					{
						System.out.println("ZANaviMainIntroActivity:" + "callOnClick:Ex02");
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	public void onBackPressed()
	{
		System.out.println("ZANaviMainIntroActivity:" + "onBackPressed");

		// block back button
		return;
	}

	@Override
	public void processFinish(String output_part1)
	{
		String output = output_part1 + System.getProperty("line.separator") + System.getProperty("line.separator") + "LastStackTrace:" + System.getProperty("line.separator") + ZANaviMainApplication.last_stack_trace_as_string;
		ZANaviMainApplication.last_stack_trace_as_string = ""; // reset last stacktrace

		System.out.println("ZANaviMainIntroActivity:" + "processFinish");

		String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
		String full_file_name = Navit.NAVIT_DATA_DEBUG_DIR + "/crashlog_" + date + ".txt";
		String full_file_name_suppl = Navit.NAVIT_DATA_DEBUG_DIR + "/crashlog_single.txt";
		String feedback_text = Navit.get_text("Crashlog");

		System.out.println("crashlogfile=" + full_file_name);

		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, "ZANaviMainIntroActivityStatic:" + "crashlogfile=" + full_file_name);

		Logging.writeToFile(output, ZANaviMainIntroActivityStatic.this, full_file_name);

		String subject_d_version = "";
		if (Navit.Navit_DonateVersion_Installed)
		{
			subject_d_version = subject_d_version + "D,";
		}

		if (Navit.Navit_Largemap_DonateVersion_Installed)
		{
			subject_d_version = subject_d_version + "L,";
		}

		try
		{
			int rl = Navit.get_reglevel();

			if (rl > 0)
			{
				subject_d_version = "U" + rl + ",";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String FD_addon = "";
		if (Navit.FDBL)
		{
			FD_addon = ",FD";
		}

		try
		{
			new Handler().post(new Runnable()
			{
				@Override
				public void run()
				{
					progressDialog2.dismiss();
					System.out.println("ZANaviMainIntroActivity:" + "progressDialog2.dismiss()");
				}
			});
		}
		catch (Exception ee)
		{
		}

		Navit.Global_Navit_Object.sendEmailWithAttachment(this, "feedback@zanavi.cc", "ZANavi Crashlog (v:" + subject_d_version + FD_addon + Navit.NavitAppVersion + " a:" + android.os.Build.VERSION.SDK + ")", feedback_text, full_file_name, full_file_name_suppl);

		// reset flag
		Navit.intro_flag_crash = false;

		//		try
		//		{
		//			// go to next slide
		//			btnNext.callOnClick();
		//			// -----------------
		//		}
		//		catch (java.lang.NoSuchMethodError e2)
		//		{
		//			System.out.println("ZANaviMainIntroActivity:" + "callOnClick:Ex04");
		//		}
	}

	public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter
	{

		private final Context activity;
		private ArrayList<String> asr;

		public CustomSpinnerAdapter(Context context, ArrayList<String> asr)
		{
			this.asr = asr;
			activity = context;
		}

		public int getCount()
		{
			return asr.size();
		}

		public Object getItem(int i)
		{
			return asr.get(i);
		}

		public long getItemId(int i)
		{
			return (long) i;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			// the views you select from ---
			TextView txt = new TextView(ZANaviMainIntroActivityStatic.this);
			txt.setPadding(16, 16, 16, 16);
			txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.slide_desc) * 1.1f);
			txt.setGravity(Gravity.CENTER_VERTICAL);
			txt.setBackgroundResource(R.drawable.custom_spinner_view_background);
			txt.setText(disk_locations_long.get(position)); // .replace(" ", "\u00A0")

			txt.setSingleLine(false);

			try
			{
				// txt.setEllipsize(TextUtils.TruncateAt.END);
				// txt.setHorizontallyScrolling(false);
			}
			catch (Exception ee2)
			{
			}

			txt.setMinimumHeight(NavitGraphics.dp_to_px(50));

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			{
				txt.setBackgroundColor(Color.parseColor("#4E219E"));
				if (currently_selected == position)
				{
					txt.setBackgroundColor(Color.parseColor("#976ab7"));
				}
			}

			txt.setTextColor(Color.parseColor("#ffffff"));
			return txt;
		}

		public View getView(int i, View view, ViewGroup viewgroup)
		{
			// selection header view ---

			TextView txt = new TextView(ZANaviMainIntroActivityStatic.this);
			txt.setGravity(Gravity.CENTER);
			// txt.setPadding(16, 16, 40, 16);
			txt.setPadding(16, 4, 16, 4);
			txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.slide_desc));
			// txt.setCompoundDrawablePadding(NavitGraphics.dp_to_px(20));
			// txt.setCompoundDrawablesWithIntrinsicBounds(null, null, ZANaviMainIntroActivityStatic.scaleDrawable(this.activity.getResources().getDrawable(R.drawable.icon), 1, 1), null);
			try
			{
				txt.setCompoundDrawables(null, null, ZANaviMainIntroActivityStatic.scaleDrawable_dp(this.activity.getResources().getDrawable(R.drawable.drop_down_arrow_01), 23, 23), null);
			}
			catch (Exception e)
			{
			}

			try
			{
				txt.setText(asr.get(i));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("asr.get(" + i + ") error: asr=" + asr);
				txt.setText("*error*");
			}

			txt.setTextColor(Color.parseColor("#ffffff"));
			return txt;
		}

	}

	static public Drawable scaleDrawable(Drawable drawable, int width, int height)
	{
		int wi = drawable.getIntrinsicWidth();
		int hi = drawable.getIntrinsicHeight();
		int dimDiff = Math.abs(wi - width) - Math.abs(hi - height);
		float scale = (dimDiff > 0) ? width / (float) wi : height / (float) hi;
		Rect bounds = new Rect(0, 0, (int) (scale * wi), (int) (scale * hi));
		drawable.setBounds(bounds);
		return drawable;
	}

	static public Drawable scaleDrawable_dp(Drawable drawable, int width_dp, int height_dp)
	{
		int wi = drawable.getIntrinsicWidth();
		int hi = drawable.getIntrinsicHeight();
		int width = NavitGraphics.dp_to_px(width_dp);
		int height = NavitGraphics.dp_to_px(height_dp);
		int dimDiff = Math.abs(wi - width) - Math.abs(hi - height);
		float scale = (dimDiff > 0) ? width / (float) wi : height / (float) hi;
		Rect bounds = new Rect(0, 0, (int) (scale * wi), (int) (scale * hi));
		drawable.setBounds(bounds);
		return drawable;
	}

	static String split_every(String in, int split_pos)
	{
		String out = in;
		try
		{
			if (in.length() > split_pos)
			{
				out = "";
				List<String> parts = new ArrayList<String>();
				int len = in.length();
				for (int i = 0; i < len; i += split_pos)
				{
					parts.add(in.substring(i, Math.min(len, i + split_pos)));
				}

				for (int j = 0; j < parts.size(); j++)
				{
					if (j > 0)
					{
						out = out + "\n";
					}
					out = out + parts.get(j);
				}
			}
		}
		catch (Exception e)
		{
			out = in;
		}
		return out;
	}

	class AsyncTaskMapMover extends AsyncTask<Void, Integer, Void>
	{

		boolean running;
		private ProgressDialog progressDialog;
		private File ff1;
		private File ff2;
		private int pathnum_;

		AsyncTaskMapMover(File ff1, File ff2, int pathnum)
		{
			this.ff1 = ff1;
			this.ff2 = ff2;
			this.pathnum_ = pathnum;
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			int i = 0;

			try
			{

				File f1 = new File(this.ff1.getCanonicalPath());
				File f2 = new File(this.ff2.getCanonicalPath());
				File f1full = new File(new File(this.ff1.getAbsolutePath() + "/../../").getCanonicalPath());
				File f2full = new File(new File(this.ff2.getAbsolutePath() + "/../../").getCanonicalPath());

				try
				{
					f1.mkdirs();
				}
				catch (Exception e)
				{
				}

				try
				{
					f2.mkdirs();
				}
				catch (Exception e)
				{
				}

				System.out.println("move from:" + f1full.getCanonicalPath() + " -> " + f2full.getCanonicalPath());
				Navit.copyDirectoryOneLocationToAnotherLocation(f1full, f2full);
				Navit.deleteRecursive(f1full);

				try
				{
					f1.mkdirs();
				}
				catch (Exception e)
				{
				}

				System.out.println("move from:" + "ready");
			}
			catch (Exception e)
			{
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			super.onProgressUpdate(values);
			progressDialog.setMessage(String.valueOf(values[0]));
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			progressDialog = ProgressDialog.show(ZANaviMainIntroActivityStatic.this, Navit.get_text("Preparing Storage"), Navit.get_text("please wait ..."));

			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialog)
				{
				}
			});
		}

		@Override
		protected void onPostExecute(Void aVoid)
		{
			super.onPostExecute(aVoid);

			try
			{
				Navit.change_maps_dir(ZANaviMainIntroActivityStatic.this, this.pathnum_, this.ff2.getCanonicalPath());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			progressDialog.dismiss();
		}

	}

}
