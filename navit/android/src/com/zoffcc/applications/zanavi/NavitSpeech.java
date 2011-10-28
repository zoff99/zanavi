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

/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2008 Navit Team
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

import android.util.Log;

import com.google.tts.TTS;

// old crappy TTS, no longer used!!
public class NavitSpeech implements Runnable
{
	private static TTS tts;
	private TTS.InitListener ttsInitListener;
	private String what;
	private Thread thread;

	NavitSpeech(Navit navit)
	{
		try
		{
			ttsInitListener = new TTS.InitListener()
			{
				public void onInit(int version)
				{
				}
			};
			tts = new TTS(navit, ttsInitListener, true);
		}
		catch (Exception e)
		{
			tts = null;
		}
		try
		{
			Log.e("NavitSpeech", "Create with locale=" + java.util.Locale.getDefault().getLanguage());
			tts.setLanguage(java.util.Locale.getDefault().getLanguage());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		try
		{
			Log.e("NavitSpeech", "In " + what);
			tts.speak(what, 0, null);
		}
		catch (Exception e)
		{
		}
	}

	public void say(String what)
	{
		try
		{
			this.what = what;
			thread = new Thread(this, "speech thread");
			thread.start();
		}
		catch (Exception e)
		{
		}
	}

	public static void stop_me()
	{
		Log.e("NavitSpeech", "shutdown");
		try
		{
			tts.shutdown();
		}
		catch (Exception e)
		{
			// old class, so this will always fire!
			// e.printStackTrace();
		}
	}
}
