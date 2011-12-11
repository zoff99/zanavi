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
		x = Navit.get_text("__PREF__title__use_fast_provider");
		x = Navit.get_text("__PREF__summ__use_fast_provider");
		x = Navit.get_text("__PREF__title__follow_gps");
		x = Navit.get_text("__PREF__summ__follow_gps");
		x = Navit.get_text("__PREF__title__show_vehicle_in_center");
		x = Navit.get_text("__PREF__summ__show_vehicle_in_center");
		x = Navit.get_text("__PREF__title__use_compass_heading_base");
		x = Navit.get_text("__PREF__summ__use_compass_heading_base");
		x = Navit.get_text("__PREF__title__use_compass_heading_always");
		x = Navit.get_text("__PREF__summ__use_compass_heading_always");
		x = Navit.get_text("__PREF__title__use_compass_heading_fast");
		x = Navit.get_text("__PREF__summ__use_compass_heading_fast");
		x = Navit.get_text("__PREF__title__use_imperial");
		x = Navit.get_text("__PREF__summ__use_imperial");
		x = Navit.get_text("__PREF__title__show_3d_map");
		x = Navit.get_text("__PREF__summ__show_3d_map");
		x = Navit.get_text("__PREF__title__use_anti_aliasing");
		x = Navit.get_text("__PREF__summ__use_anti_aliasing");
		x = Navit.get_text("__PREF__title__gui_oneway_arrows");
		x = Navit.get_text("__PREF__summ__gui_oneway_arrows");
		x = Navit.get_text("__PREF__title__show_debug_messages");
		x = Navit.get_text("__PREF__summ__show_debug_messages");
		x = Navit.get_text("__PREF__title__navit_lang");
		x = Navit.get_text("__PREF__summ__navit_lang");
		x = Navit.get_text("__PREF__title__use_lock_on_roads");
		x = Navit.get_text("__PREF__summ__use_lock_on_roads");
		x = Navit.get_text("__PREF__title__use_route_highways");
		x = Navit.get_text("__PREF__summ__use_route_highways");
		x = Navit.get_text("__PREF__title__save_zoomlevel");
		x = Navit.get_text("__PREF__summ__save_zoomlevel");
		x = Navit.get_text("__PREF__title__show_sat_status");
		x = Navit.get_text("__PREF__summ__show_sat_status");
		x = Navit.get_text("__PREF__title__use_agps");
		x = Navit.get_text("__PREF__summ__use_agps");
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

		String[] PrefTransTexts = new String[] { "use_fast_provider", "follow_gps", "show_vehicle_in_center", "use_compass_heading_base", "use_compass_heading_always", "use_compass_heading_fast", "use_imperial", "show_3d_map", "use_anti_aliasing", "gui_oneway_arrows", "show_debug_messages", "navit_lang", "use_lock_on_roads", "use_route_highways", "save_zoomlevel", "show_sat_status", "use_agps" };

		int i = 0;
		for (i = 0; i < PrefTransTexts.length; i++)
		{
			try
			{
				Preference pref = findPreference(PrefTransTexts[i]);
				pref.setTitle(Navit.get_text("__PREF__title__" + PrefTransTexts[i])); //TRANS
				pref.setSummary(Navit.get_text("__PREF__summ__" + PrefTransTexts[i])); //TRANS
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
