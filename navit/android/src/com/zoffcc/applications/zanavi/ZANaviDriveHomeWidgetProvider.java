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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class ZANaviDriveHomeWidgetProvider extends AppWidgetProvider
{
	// SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		final int N = appWidgetIds.length;

		// loop for each app widget
		for (int i = 0; i < N; i++)
		{
			int appWidgetId = appWidgetIds[i];

			// create an intent to launch ZANavi
			Intent intent = new Intent(context, com.zoffcc.applications.zanavi.Navit.class);
			// but some data into the intent
			Bundle sendBundle = new Bundle();
			sendBundle.putLong("com.zoffcc.applications.zanavi.ZANAVI_INTENT_type", Navit.NAVIT_START_INTENT_DRIVE_HOME);
			intent.putExtras(sendBundle);
			System.out.println("DH:W 001 B=" + sendBundle.describeContents());
			System.out.println("DH:W 001 B=" + sendBundle.toString());
			System.out.println("DH:W 002 I=" + intent.toString());

			// if ZANavi main activity already running, then only bring to front, otherwise start it //  Intent.FLAG_ACTIVITY_NEW_TASK
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			System.out.println("DH:W 003 I=" + pendingIntent.toString());

			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_home);
			views.setOnClickPendingIntent(R.id.icon_widget_home, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
