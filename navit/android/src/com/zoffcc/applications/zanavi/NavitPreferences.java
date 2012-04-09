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

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class NavitPreferences extends PreferenceActivity
{
	private void dummy_xgettext()
	{
		// dummy for xgettext
		String x = null;
		//
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_fast_provider");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_fast_provider");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__follow_gps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__follow_gps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_vehicle_in_center");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_vehicle_in_center");
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
		x = Navit.get_text("__PREF__title__show_3d_map");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_3d_map");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_anti_aliasing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_anti_aliasing");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__gui_oneway_arrows");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__gui_oneway_arrows");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_debug_messages");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_debug_messages");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__navit_lang");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__navit_lang");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_lock_on_roads");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_lock_on_roads");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_route_highways");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_route_highways");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__save_zoomlevel");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__save_zoomlevel");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__show_sat_status");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__show_sat_status");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_agps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_agps");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__enable_debug_functions");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__enable_debug_functions");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__speak_street_names");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__speak_street_names");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__map_font_size");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__map_font_size");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__use_custom_font");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__use_custom_font");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__cancel_map_drawing_timeout");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__cancel_map_drawing_timeout");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__mapcache");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__mapcache");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__drawatorder");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__drawatorder");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__title__streetsearch_r");
		//. TRANSLATORS: see en_US for english text to translate!!
		x = Navit.get_text("__PREF__summ__streetsearch_r");
		//
		// dummy for xgettext
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

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

		String[] PrefTransTexts = new String[] { "use_fast_provider", "follow_gps", "show_vehicle_in_center", "use_compass_heading_base", "use_compass_heading_always", "use_compass_heading_fast", "use_imperial", "show_3d_map", "use_anti_aliasing", "gui_oneway_arrows", "show_debug_messages", "navit_lang", "use_lock_on_roads", "use_route_highways", "save_zoomlevel", "show_sat_status", "use_agps", "enable_debug_functions", "speak_street_names", "use_custom_font", "map_font_size", "drawatorder",
				"streetsearch_r", "cancel_map_drawing_timeout", "mapcache", "draw_polyline_circles" };

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
