/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2014 Zoff <zoff@zoff.cc>
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;

import com.zoffcc.applications.zanavi.NavitMapDownloader.ProgressThread;

public class ZANaviMapDownloaderService extends Service
{
	private static NotificationManager nm;
	private static Notification notification;
	public static int NOTIFICATION_ID__DUMMY = 1;
	public static int NOTIFICATION_ID__DUMMY2 = 2;
	public static String Notification_header = "";
	public static String Notification_text = "";
	private static Context con = null;
	private static PendingIntent p_activity = null;
	private static Intent notificationIntent = null;

	private static NotificationCompat.Builder builder_ = null;

	private static ProgressThread progressThread_pri = null;
	public static boolean service_running = false;
	private static ZANaviMapDownloaderService my_object = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		System.out.println("ZANaviMapDownloaderService: ************************ start ************************");

		Notification_header = "ZANavi";
		Notification_text = Navit.get_text("downloading, please wait ...");

		con = this;

		Notification notification = null;

		try
		{
			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationIntent = new Intent(con, ZANaviDownloadMapCancelActivity.class);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			p_activity = PendingIntent.getActivity(con, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder_ = new NotificationCompat.Builder(con);
			builder_.setAutoCancel(false);
			builder_.setOngoing(true);
			builder_.setSmallIcon(R.drawable.icon);
			//			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			//			{
			//				builder_.setColor(Color.TRANSPARENT);
			//			}
			builder_.setContentTitle("ZANavi");
			builder_.setProgress(100, 0, true);
			builder_.setContentText(Notification_text);
			builder_.setContentTitle(Notification_header);
			builder_.setContentIntent(p_activity);
			Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
			builder_.setLargeIcon(bm);
			notification = builder_.build();
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			System.out.println("Notifi:006:ok");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Notifi:006:Ex=" + e.getMessage());
		}

		try
		{
			nm.notify(NOTIFICATION_ID__DUMMY, notification);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Notifi:003:Ex=" + e.getMessage() + "nm=" + nm + " notification=" + notification);

			try
			{
				p_activity = PendingIntent.getActivity(con, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				// notification.setLatestEventInfo(con, Notification_header, Notification_text, p_activity);
				nm.notify(NOTIFICATION_ID__DUMMY, notification);
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
				System.out.println("Notifi:004:Ex=" + e2.getMessage());
			}
		}

		service_running = true;

		start_map_download();
		System.out.println("ZANaviMapDownloaderService: ************************ start(finished) ************************");

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	public static void start_map_download()
	{
		try
		{
			System.out.println("ZANaviMapDownloaderService: xxxxxxxx download start xxxxxxxx");
			Navit.mapdownloader_pri = new NavitMapDownloader(Navit.Global_Navit_Object);
			progressThread_pri = Navit.mapdownloader_pri.new ProgressThread(Navit.Navit_progress_h, NavitMapDownloader.z_OSM_MAPS[Navit.download_map_id], Navit.MAP_NUM_PRIMARY);
			progressThread_pri.start();
		}
		catch (Exception e)
		{
		}
	}

	public static void set_noti_text(String text, int percent)
	{
		try
		{
			// System.out.println("ZANaviMapDownloaderService: !!!!!!!!! NOTIFY !!!!!!!!!" + " text=" + text + " con=" + con + " p_activity=" + p_activity);
			// System.out.println("Notifi:002:" + "nm=" + nm + " text=" + text + " con=" + con + " p_activity=" + p_activity);
			Notification_text = text;
			builder_.setContentTitle("");
			if (percent > 0)
			{
				builder_.setProgress(100, percent, false);
			}
			else
			{
				builder_.setProgress(100, 0, true);
			}
			builder_.setContentText(Notification_text);
			builder_.setOngoing(true);

			Notification notification = builder_.build();
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			nm.notify(NOTIFICATION_ID__DUMMY, notification);
		}
		catch (Exception e)
		{
			System.out.println("Notifi:001:Ex=" + e.getMessage());
		}
	}

	public static void set_large_text(String text)
	{
		try
		{
			Message msg2 = new Message();
			Bundle b2 = new Bundle();
			b2.putString("text", text);
			msg2.what = 0;
			msg2.setData(b2);
			ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg2);
		}
		catch (Exception e)
		{
		}
	}

	public static void stop_downloading()
	{
		System.out.println("ZANaviMapDownloaderService: xxxxxxxx download stop 1 xxxxxxxx");
		try
		{
			progressThread_pri.stop_thread();
		}
		catch (Exception e)
		{
		}

		try
		{
			my_object.stopSelf();
		}
		catch (Exception e)
		{
		}

		try
		{
			nm.cancel(NOTIFICATION_ID__DUMMY);
		}
		catch (Exception e)
		{
		}

		System.out.println("ZANaviMapDownloaderService: xxxxxxxx download stop 2 xxxxxxxx");
	}

	public void onCreate()
	{
		my_object = this;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		try
		{
			nm.cancel(NOTIFICATION_ID__DUMMY);
		}
		catch (Exception e)
		{
		}

		try
		{
			progressThread_pri.stop_thread();
		}
		catch (Exception e)
		{
		}

		System.out.println("ZANaviMapDownloaderService: ####################### STOPPED #######################");

		service_running = false;
	}

}
