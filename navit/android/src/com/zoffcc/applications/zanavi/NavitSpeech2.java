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

import java.util.Arrays;
import java.util.Locale;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

// new TTS, this is used now!
public class NavitSpeech2 implements TextToSpeech.OnInitListener, NavitActivityResult
{
	private static TextToSpeech mTts;
	private Navit navit;
	int MY_DATA_CHECK_CODE = 1; // this needs to be "1" for the C-code !!
	private Locale want_locale = null;

	public void onInit(int status)
	{
		Log.e("NavitSpeech2", "onInit: Status " + status);
		int result = -1;
		try
		{
			// set the new locale here -----------------------------------
			Locale locale2 = want_locale;
			Locale.setDefault(locale2);
			Configuration config2 = new Configuration();
			config2.locale = locale2;
			// set the new locale here -----------------------------------
			result = mTts.setLanguage(locale2);

			try
			{
				Log.e("NavitSpeech2", "3.1 want locale=" + locale2.getLanguage());
				Log.e("NavitSpeech2", "3 E=" + mTts.getDefaultEngine());
				Log.e("NavitSpeech2", "3 def.enf.=" + mTts.areDefaultsEnforced());
			}
			catch (NoSuchMethodError e2)
			{
				e2.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				Log.e("NavitSpeech2", "3 lang. Country=" + mTts.getLanguage().getDisplayCountry());
				Log.e("NavitSpeech2", "3 lang. Country=" + mTts.getLanguage().getDisplayLanguage());
				Log.e("NavitSpeech2", "3 lang. Country=" + mTts.getLanguage().getDisplayName());
				Log.e("NavitSpeech2", "3 lang. Country=" + mTts.getLanguage().getDisplayVariant());
			}
			catch (NoSuchMethodError e2)
			{
				e2.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			String want_lang_code = locale2.getISO3Language();
			Log.e("NavitSpeech2", "want:" + want_lang_code);
			String will_use_lang_code = mTts.getLanguage().getISO3Language();
			Log.e("NavitSpeech2", "will use:" + will_use_lang_code);
			if (want_lang_code.compareToIgnoreCase(will_use_lang_code) != 0)
			{
				result = TextToSpeech.LANG_NOT_SUPPORTED;
			}

			Log.e("NavitSpeech2", "3 ok result=" + result);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			result = TextToSpeech.LANG_NOT_SUPPORTED;
		}

		if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
		{
			// Lanuage data is missing or the language is not supported.
			Log.e("NavitSpeech2", "3 Language is not available.");

			try
			{
				// lang for TTS not found, show toast
				Message msg = Navit.Navit_progress_h.obtainMessage();
				Bundle b = new Bundle();
				msg.what = 3;
				b.putString("text", Navit.get_text("Language is not available for TTS! Using your phone's default settings")); //TRANS
				msg.setData(b);
				Navit.Navit_progress_h.sendMessage(msg);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
		else
		{
			try
			{
				// lang for TTS not found, show toast
				Message msg = Navit.Navit_progress_h.obtainMessage();
				Bundle b = new Bundle();
				msg.what = 3;
				b.putString("text", Navit.get_text("Using Voice for:") + "\n" + mTts.getLanguage().getDisplayName()); //TRANS
				msg.setData(b);
				Navit.Navit_progress_h.sendMessage(msg);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.e("NavitSpeech2", "onActivityResult " + requestCode + " " + resultCode);
		try
		{
			if (requestCode == MY_DATA_CHECK_CODE)
			{
				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
				{
					// success, create the TTS instance
					mTts = new TextToSpeech(navit, this);

					try
					{
						// just for info -------
						Log.e("NavitSpeech2", Arrays.toString(Locale.getAvailableLocales()));
						// just for info -------
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					// missing data, install it
					Intent installIntent = new Intent();
					installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
					navit.startActivity(installIntent);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	NavitSpeech2(Navit navit)
	{
		Log.e("NavitSpeech2", "constructor");
		Log.e("NavitSpeech2", "locale=" + java.util.Locale.getDefault().getDisplayName());
		want_locale = java.util.Locale.getDefault();
		try
		{
			this.navit = navit;
			navit.setActivityResult(MY_DATA_CHECK_CODE, this);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		try
		{
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			navit.startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		}
		catch (Exception e2)
		{
			e2.printStackTrace();
		}
	}

	public void say(String what)
	{
		try
		{
			if (mTts != null)
			{
				mTts.speak(what, TextToSpeech.QUEUE_FLUSH, null);
				if (NavitGraphics.NavitMsgTv2_.getVisibility() == View.VISIBLE)
				{
					Navit.N_NavitGraphics.NavitMsgTv2_.append("SAY:" + what + "\n");
				}
				if (Navit.PREF_show_debug_messages)
				{
					Navit.set_debug_messages3(what);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void stop_me()
	{
		// Log.e("NavitSpeech2", "shutdown");
		try
		{
			mTts.shutdown();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
