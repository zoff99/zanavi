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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;

public class ZANaviLogMessages
{
	static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	static final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT, Locale.GERMAN);
	private static HashMap<String, String> properties = new HashMap<String, String>();
	private static List<LogMessageClass> logmessages = new ArrayList<LogMessageClass>();

	public static int STATUS_INFO = 0;
	public static int STATUS_WARN = 1;
	public static int STATUS_ERROR = 2;

	public static class LogMessageClass
	{
		int status = 0; // 0 -> info "I", 1 -> warn "W", 2 -> error "E"
		String datetime = "";
		String message = "";

		LogMessageClass(int status_, String datetime_, String message_)
		{
			this.status = status_;
			this.datetime = datetime_;
			this.message = message_;
		}
	}

	public static void dump_vales_to_log()
	{
		System.out.println("=========================" + " ZANaviLogMessages DUMP " + "=========================");
		System.out.println("=   properties           " + "                        " + "                        =");
		System.out.println("=                        " + "                        " + "                        =");
		for (String k : properties.keySet())
		{
			System.out.println(k + ":" + properties.get(k));
		}
		System.out.println("=                        " + "                        " + "                        =");
		System.out.println("=========================" + " ZANaviLogMessages DUMP " + "=========================");
	}

	public static void dump_messages_to_log()
	{
		System.out.println("=========================" + " ZANaviLogMessages DUMP " + "=========================");
		System.out.println("=   messages             " + "                        " + "                        =");
		System.out.println("=                        " + "                        " + "                        =");
		for (LogMessageClass lm : logmessages)
		{
			System.out.println(lm.datetime + ":" + status_to_text(lm.status) + ":" + lm.message);
		}
		System.out.println("=                        " + "                        " + "                        =");
		System.out.println("=========================" + " ZANaviLogMessages DUMP " + "=========================");
	}

	public static String GetUTCdatetimeAsString()
	{
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());

		return utcTime;
	}

	static String status_to_text(int status_)
	{
		switch (status_)
		{
		case 0:
			return "I";
		case 1:
			return "W";
		case 2:
			return "E";
		default:
			return "x";
		}
	}

	public static void ap(String key, String value)
	{
		properties.put(key, value);
	}

	public static void am(int status_, String message_)
	{
		logmessages.add(new LogMessageClass(status_, GetUTCdatetimeAsString(), message_));
		// System.out.println("XXXXXAM:" + status_ + ":" + GetUTCdatetimeAsString() + ":" + message_);
	}
}
