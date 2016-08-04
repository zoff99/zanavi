/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2015 Zoff <zoff@zoff.cc>
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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.v7.internal.widget.TintCheckBox;
import android.support.v7.internal.widget.TintCheckedTextView;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.internal.widget.TintRadioButton;
import android.support.v7.internal.widget.TintSpinner;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

public class NavitPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	@SuppressWarnings("unused")
	private void dummy_xgettext()
	{
		// dummy for xgettext
		String x = null;
		//
		// cat /tmp/aa |grep 'x '|sed -e 's#.*get_text("__PREF__##g'|sed -e 's#^title__##'|sed -e 's#^summ__##'|sed -e 's#");.*$##g'|uniq|xargs -L 99999999999 echo | sed -e 's# #","#g'
		// ---> to use in "PrefTransTexts =" further down in this file!
		//
		//
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_fast_provider");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_fast_provider");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_agps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_agps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__follow_gps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__follow_gps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_lock_on_roads");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_lock_on_roads");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_vehicle_in_center");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_vehicle_in_center");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_sat_status");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_sat_status");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_compass_heading_base");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_compass_heading_base");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_compass_heading_always");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_compass_heading_always");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_compass_heading_fast");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_compass_heading_fast");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_imperial");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_imperial");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_route_highways");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_route_highways");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_index_search");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_index_search");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__trafficlights_delay");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__trafficlights_delay");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__speak_street_names");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__speak_street_names");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__speak_filter_special_chars");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__speak_filter_special_chars");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__route_style");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__route_style");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_3d_map");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_3d_map");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_2d3d_toggle");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_2d3d_toggle");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__save_zoomlevel");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__save_zoomlevel");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__autozoom_flag");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__autozoom_flag");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_anti_aliasing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_anti_aliasing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_map_filtering");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_map_filtering");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_custom_font");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_custom_font");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_smooth_drawing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_smooth_drawing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_more_smooth_drawing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_more_smooth_drawing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_multipolygons");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_multipolygons");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_vehicle_3d");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_vehicle_3d");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__map_font_size");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__map_font_size");
		//
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__drawatorder"); // not used anymore
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__drawatorder"); // not used anymore
		//
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__more_map_detail");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__more_map_detail");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__mapcache");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__mapcache");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__streetsearch_r");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__streetsearch_r");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__gui_oneway_arrows");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__shrink_on_high_dpi");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__shrink_on_high_dpi");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__streets_only");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__streets_only");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__gui_oneway_arrows");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_debug_messages");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_debug_messages");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__enable_debug_functions");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__enable_debug_functions");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__navit_lang");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__navit_lang");
		// __map_directory // dont use this one here!!
		// dummy for xgettext
	}

	// thanks to: http://stackoverflow.com/questions/17849193/how-to-add-action-bar-from-support-library-into-preferenceactivity
	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs)
	{
		// Allow super to try and create a view first
		final View result = super.onCreateView(name, context, attrs);
		if (result != null)
		{
			return result;
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		{
			// If we're running pre-L, we need to 'inject' our tint aware Views in place of the
			// standard framework versions
			if (name.compareTo("EditText") == 0)
			{
				return new TintEditText(this, attrs);
			}
			else if (name.compareTo("Spinner") == 0)
			{
				return new TintSpinner(this, attrs);
			}
			else if (name.compareTo("CheckBox") == 0)
			{
				return new TintCheckBox(this, attrs);
			}
			else if (name.compareTo("RadioButton") == 0)
			{
				return new TintRadioButton(this, attrs);
			}
			else if (name.compareTo("CheckedTextView") == 0)
			{
				return new TintCheckedTextView(this, attrs);
			}
		}

		return null;
	}

	// thanks to: http://stackoverflow.com/questions/26509180/no-actionbar-in-preferenceactivity-after-upgrade-to-support-library-v21
	@SuppressLint("NewApi")
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		Toolbar bar;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
			bar.setTitle(Navit.get_text("Settings"));
			root.addView(bar, 0); // insert at top
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
			bar.setTitle(Navit.get_text("Settings"));
			root_view.addView(ll);

			ll.addView(bar);
			ll.addView(content);

		}

		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//System.out.println("ZZZZZZZZZZZZZ1");
				finish();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.p.PREF_current_theme);

		super.onCreate(savedInstanceState);

		// Override how this activity is animated into view
		// The new activity is pulled in from the left and the current activity is kept still
		// This has to be called before onCreate
		overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);

		addPreferencesFromResource(R.xml.preferences);

		//		try
		//		{
		//			this.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}

		try
		{
			EditTextPreference about_text_pref = ((EditTextPreference) findPreference("about_edit_text"));

			if (Navit.FDBL)
			{
				about_text_pref.setSummary("v" + Navit.NavitAppVersion_string + "-" + Navit.VERSION_TEXT_LONG_INC_REV + ":FD");
			}
			else
			{
				about_text_pref.setSummary("v" + Navit.NavitAppVersion_string + "-" + Navit.VERSION_TEXT_LONG_INC_REV);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference routing_prof = findPreference("routing_profile");
			if (Navit.Navit_Largemap_DonateVersion_Installed == false)
			{
				routing_prof.setEnabled(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference road_prof_001 = findPreference("road_priority_001");
			if (Navit.Navit_Largemap_DonateVersion_Installed == false)
			{
				road_prof_001.setEnabled(false);
			}
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("road_priority_001", (68 - 10)) + 10;
			road_prof_001.setSummary(road_prof_001.getSummary() + " [" + read_value + "]");

			if (!Navit.p.PREF_enable_debug_functions)
			{
				PreferenceCategory cat = (PreferenceCategory) findPreference("category_tracking");
				cat.removePreference(road_prof_001);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference road_prof_002 = findPreference("road_priority_002");
			if (Navit.Navit_Largemap_DonateVersion_Installed == false)
			{
				road_prof_002.setEnabled(false);
			}
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("road_priority_002", (329 - 10)) + 10;
			road_prof_002.setSummary(road_prof_002.getSummary() + " [" + read_value + "]");

			if (!Navit.p.PREF_enable_debug_functions)
			{
				PreferenceCategory cat = (PreferenceCategory) findPreference("category_tracking");
				cat.removePreference(road_prof_002);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference road_prof_003 = findPreference("road_priority_003");
			if (Navit.Navit_Largemap_DonateVersion_Installed == false)
			{
				road_prof_003.setEnabled(false);
			}
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("road_priority_003", (5000 - 10)) + 10;
			road_prof_003.setSummary(road_prof_003.getSummary() + " [" + read_value + "]");

			if (!Navit.p.PREF_enable_debug_functions)
			{
				PreferenceCategory cat = (PreferenceCategory) findPreference("category_tracking");
				cat.removePreference(road_prof_003);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference road_prof_004 = findPreference("road_priority_004");
			if (Navit.Navit_Largemap_DonateVersion_Installed == false)
			{
				road_prof_004.setEnabled(false);
			}
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("road_priority_004", (5 - 0)) + 0;
			road_prof_004.setSummary(road_prof_004.getSummary() + " [" + read_value + "]");

			if (!Navit.p.PREF_enable_debug_functions)
			{
				PreferenceCategory cat = (PreferenceCategory) findPreference("category_tracking");
				cat.removePreference(road_prof_004);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference a = findPreference("tracking_connected_pref");
			if (Navit.Navit_Largemap_DonateVersion_Installed == false)
			{
				a.setEnabled(false);
			}
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("tracking_connected_pref", (250 - 0)) + 0;
			a.setSummary(a.getSummary() + " [" + read_value + "]");

			if (!Navit.p.PREF_enable_debug_functions)
			{
				PreferenceCategory cat = (PreferenceCategory) findPreference("category_tracking");
				cat.removePreference(a);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference a = findPreference("tracking_angle_pref");
			if (Navit.Navit_Largemap_DonateVersion_Installed == false)
			{
				a.setEnabled(false);
			}
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("tracking_angle_pref", (40 - 0)) + 0;
			a.setSummary(a.getSummary() + " [" + read_value + "]");

			if (!Navit.p.PREF_enable_debug_functions)
			{
				PreferenceCategory cat = (PreferenceCategory) findPreference("category_tracking");
				cat.removePreference(a);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference a = findPreference("traffic_speed_factor");
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("traffic_speed_factor", (83 - 20)) + 20;
			a.setSummary(a.getSummary() + " [" + read_value + "]");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference a = findPreference("night_mode_lux");
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("night_mode_lux", (10 - 1)) + 1;
			a.setSummary(a.getSummary() + " [" + read_value + "]");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Preference a = findPreference("night_mode_buffer");
			int read_value = PreferenceManager.getDefaultSharedPreferences(this).getInt("night_mode_buffer", (20 - 1)) + 1;
			a.setSummary(a.getSummary() + " [" + read_value + "]");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		disable_pref("roadspeed_warning", false, false);
		disable_pref("lane_assist", false, false);

		try
		{
			if (Navit.NavitDataStorageDirs != null)
			{
				if (Navit.NavitDataStorageDirs.length > 0)
				{
					//Preference a = findPreference("map_directory");
					Preference b = findPreference("storage_directory");

					int new_count = 0;
					for (int ij = 0; ij < Navit.NavitDataStorageDirs.length; ij++)
					{
						if (Navit.NavitDataStorageDirs[ij] != null)
						{
							new_count++;
						}
					}

					CharSequence[] entries = new CharSequence[new_count + 1];
					CharSequence[] entryValues = new CharSequence[new_count + 1];
					entries[0] = "Custom Path";
					entryValues[0] = "0";
					long avail_space = 0L;
					String avail_space_string = "";
					new_count = 0;
					for (int ij = 0; ij < Navit.NavitDataStorageDirs.length; ij++)
					{
						System.out.println("DataStorageDir prefs list=" + Navit.NavitDataStorageDirs[ij]);

						if (Navit.NavitDataStorageDirs[ij] != null)
						{
							avail_space = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMB(Navit.NavitDataStorageDirs[ij].getAbsolutePath());
							String avail_space_str = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMBformattedString(Navit.NavitDataStorageDirs[ij].getAbsolutePath());
							if (avail_space < 0)
							{
								avail_space_string = "";
							}
							else if (avail_space > 1200)
							{
								avail_space_str = NavitAvailableSpaceHandler.getExternalAvailableSpaceInGBformattedString(Navit.NavitDataStorageDirs[ij].getAbsolutePath());
								avail_space_string = " \n[" + avail_space_str + "GB free]";
							}
							else
							{
								avail_space_string = " \n[" + avail_space_str + "MB free]";
							}

							System.out.println("DataStorageDir avail space=" + avail_space);

							entries[new_count + 1] = "SD Card:" + Navit.NavitDataStorageDirs[ij].getAbsolutePath() + avail_space_string;
							entryValues[new_count + 1] = "" + (ij + 1);

							new_count++;
						}
						else
						{
							// entries[ij + 1] = "--";
							// entryValues[ij + 1] = "-1";
						}
					}
					((ListPreference) b).setEntries(entries);
					((ListPreference) b).setEntryValues(entryValues);
					System.out.println("DataStorageDir 009");
				}
				else
				{
					//Preference a = findPreference("map_directory");
					Preference b = findPreference("storage_directory");

					CharSequence[] entries = new CharSequence[1];
					CharSequence[] entryValues = new CharSequence[1];
					entries[0] = "Custom Path";
					entryValues[0] = "0";
					((ListPreference) b).setEntries(entries);
					((ListPreference) b).setEntryValues(entryValues);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("DataStorageDir Ex01");
			e.printStackTrace();
		}

		try
		{
			EditTextPreference mapdata_pref = ((EditTextPreference) findPreference("map_directory"));
			//. TRANSLATORS: see en_US for english text to translate!!
			mapdata_pref.setTitle(Navit.get_text("__PREF__title__map_directory"));
			//. TRANSLATORS: see en_US for english text to translate!!
			mapdata_pref.setSummary(Navit.get_text("__PREF__summ__map_directory"));
			//. TRANSLATORS: see en_US for english text to translate!!
			mapdata_pref.setDialogTitle(Navit.get_text("__PREF__dialogtitle__map_directory"));
			//. TRANSLATORS: see en_US for english text to translate!!
			mapdata_pref.setNegativeButtonText(Navit.get_text("__PREF__dialogcancel__map_directory"));
			//. TRANSLATORS: see en_US for english text to translate!!
			mapdata_pref.setPositiveButtonText(Navit.get_text("__PREF__dialogok__map_directory"));
			//. TRANSLATORS: see en_US for english text to translate!!
			mapdata_pref.setDialogMessage(Navit.get_text("__PREF__dialogmsg__map_directory"));
			//
			// **dont save this!! ** mapdata_pref.setText(Navit.NavitDataDirectory_Maps);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		final String[] PrefTransTexts = new String[] { "use_fast_provider", "use_agps", "follow_gps", "use_lock_on_roads", "show_vehicle_in_center", "show_sat_status", "use_compass_heading_base", "use_compass_heading_always", "use_compass_heading_fast", "use_imperial", "use_route_highways", "use_index_search", "trafficlights_delay", "speak_street_names", "speak_filter_special_chars", "route_style", "show_3d_map", "show_2d3d_toggle", "save_zoomlevel", "autozoom_flag", "use_anti_aliasing",
				"use_map_filtering", "use_custom_font", "use_smooth_drawing", "use_more_smooth_drawing", "show_multipolygons", "show_vehicle_3d", "map_font_size", "drawatorder", "more_map_detail", "mapcache", "streetsearch_r", "gui_oneway_arrows", "show_debug_messages", "enable_debug_functions", "navit_lang", "map_directory", "shrink_on_high_dpi", "streets_only" };

		int i = 0;
		for (i = 0; i < PrefTransTexts.length; i++)
		{
			try
			{
				Preference pref = findPreference(PrefTransTexts[i]);
				pref.setTitle(Navit.get_text("__PREF__title__" + PrefTransTexts[i])); //TRANS exclude
				pref.setSummary(Navit.get_text("__PREF__summ__" + PrefTransTexts[i])); //TRANS exclude
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// Get the custom preference
		/*
		 * Preference customPref = (Preference) findPreference("customPref");
		 */
		/*
		 * customPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		 * {
		 * 
		 * public boolean onPreferenceClick(Preference preference)
		 * {
		 * Toast.makeText(getBaseContext(), "The custom preference has been clicked", Toast.LENGTH_LONG).show();
		 * SharedPreferences customSharedPreference = getSharedPreferences("myCustomSharedPrefs", Activity.MODE_PRIVATE);
		 * SharedPreferences.Editor editor = customSharedPreference.edit();
		 * editor.putString("myCustomPref", "The preference has been clicked");
		 * editor.commit();
		 * return true;
		 * }
		 * 
		 * });
		 */
	}

	@Override
	protected void onPause()
	{
		// Whenever this activity is paused (i.e. looses focus because another activity is started etc)
		// Override how this activity is animated out of view
		// The new activity is kept still and this activity is pushed out to the left
		overridePendingTransition(R.anim.hold, R.anim.push_out_to_left);
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		// System.out.println("onSharedPreferenceChanged key=" + key);

		try
		{
			if (key.equals("road_priority_001"))
			{
				Preference road_prof_001 = findPreference("road_priority_001");
				int read_value = sharedPreferences.getInt("road_priority_001", (68 - 10)) + 10;
				int pos_start = road_prof_001.getSummary().toString().lastIndexOf("[");
				road_prof_001.setSummary(road_prof_001.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("road_priority_002"))
			{
				Preference road_prof_002 = findPreference("road_priority_002");
				int read_value = sharedPreferences.getInt("road_priority_002", (329 - 10)) + 10;
				int pos_start = road_prof_002.getSummary().toString().lastIndexOf("[");
				road_prof_002.setSummary(road_prof_002.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("road_priority_003"))
			{
				Preference road_prof_003 = findPreference("road_priority_003");
				int read_value = sharedPreferences.getInt("road_priority_003", (5000 - 10)) + 10;
				int pos_start = road_prof_003.getSummary().toString().lastIndexOf("[");
				road_prof_003.setSummary(road_prof_003.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("road_priority_004"))
			{
				Preference road_prof_004 = findPreference("road_priority_004");
				int read_value = sharedPreferences.getInt("road_priority_004", (5 - 0)) + 0;
				int pos_start = road_prof_004.getSummary().toString().lastIndexOf("[");
				road_prof_004.setSummary(road_prof_004.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("tracking_connected_pref"))
			{
				Preference tracking_connected_pref = findPreference("tracking_connected_pref");
				int read_value = sharedPreferences.getInt("tracking_connected_pref", (250 - 0)) + 0;
				int pos_start = tracking_connected_pref.getSummary().toString().lastIndexOf("[");
				tracking_connected_pref.setSummary(tracking_connected_pref.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("tracking_angle_pref"))
			{
				Preference a = findPreference("tracking_angle_pref");
				int read_value = sharedPreferences.getInt("tracking_angle_pref", (40 - 0)) + 0;
				int pos_start = a.getSummary().toString().lastIndexOf("[");
				a.setSummary(a.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("traffic_speed_factor"))
			{
				Preference a = findPreference("traffic_speed_factor");
				int read_value = sharedPreferences.getInt("traffic_speed_factor", (83 - 20)) + 20;
				int pos_start = a.getSummary().toString().lastIndexOf("[");
				a.setSummary(a.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("night_mode_lux"))
			{
				Preference a = findPreference("night_mode_lux");
				int read_value = sharedPreferences.getInt("night_mode_lux", (10 - 1)) + 1;
				int pos_start = a.getSummary().toString().lastIndexOf("[");
				a.setSummary(a.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (key.equals("night_mode_buffer"))
			{
				Preference a = findPreference("night_mode_buffer");
				int read_value = sharedPreferences.getInt("night_mode_buffer", (20 - 1)) + 1;
				int pos_start = a.getSummary().toString().lastIndexOf("[");
				a.setSummary(a.getSummary().subSequence(0, pos_start - 1) + " [" + read_value + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	void disable_pref(String pref_name, boolean large_donate_only, boolean debug_function)
	{
		try
		{
			Preference a = findPreference(pref_name);
			if ((large_donate_only) && (Navit.Navit_Largemap_DonateVersion_Installed == false))
			{
				a.setEnabled(false);
			}
			else if ((Navit.Navit_DonateVersion_Installed == false) && (Navit.Navit_Largemap_DonateVersion_Installed == false))
			{
				a.setEnabled(false);
			}

			if ((debug_function) && (!Navit.p.PREF_enable_debug_functions))
			{
				a.setEnabled(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
