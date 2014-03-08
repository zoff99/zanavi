/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2012 Zoff <zoff@zoff.cc>
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

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class NavitPreferences extends PreferenceActivity
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
			about_text_pref.setSummary("v" + Navit.NavitAppVersion_string + "-" + Navit.VERSION_TEXT_LONG_INC_REV);
		}
		catch (Exception e)
		{
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
			mapdata_pref.setText(Navit.NavitDataDirectory_Maps);
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
}
