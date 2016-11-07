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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class ZANaviBootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			Navit.app_status_string = "down";
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Navit.PREF_KEY_CRASH, "down").apply();
			System.out.println("app_status_string set:[ZANaviBootReceiver:onReceive]" + Navit.app_status_string);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
