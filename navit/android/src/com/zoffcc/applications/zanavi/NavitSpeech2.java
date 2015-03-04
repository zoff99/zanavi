/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2012 Zoff <zoff@zoff.cc>
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

import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.View;

// new TTS, this is used now!
public class NavitSpeech2 implements TextToSpeech.OnInitListener, NavitActivityResult
{
	private Navit navit;
	int MY_DATA_CHECK_CODE = 1; // this needs to be "1" for the C-code !!
	private Locale want_locale = null;
	private int request_focus_result = 0;
	private int need_audio_focus = 0;
	float debug_lat = 0;
	float debug_lon = 0;
	HashMap<String, String> tts_params = new HashMap<String, String>();

	@SuppressWarnings("deprecation")
	public void onInit(int status)
	{
		tts_params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ZANaviUtterID");

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
			try
			{
				result = navit.mTts.setLanguage(locale2);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				Log.e("NavitSpeech2", "3.1 want locale=" + locale2.getLanguage());
				//Log.e("NavitSpeech2", "3 E=" + navit.mTts.getDefaultEngine());
				//Log.e("NavitSpeech2", "3 def.enf.=" + navit.mTts.areDefaultsEnforced());
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
				Log.e("NavitSpeech2", "3 lang. Country=" + navit.mTts.getLanguage().getDisplayCountry());
				Log.e("NavitSpeech2", "3 lang. Country=" + navit.mTts.getLanguage().getDisplayLanguage());
				Log.e("NavitSpeech2", "3 lang. Country=" + navit.mTts.getLanguage().getDisplayName());
				Log.e("NavitSpeech2", "3 lang. Country=" + navit.mTts.getLanguage().getDisplayVariant());
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
			String will_use_lang_code = navit.mTts.getLanguage().getISO3Language();
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

		try
		{
			navit.mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
			{

				@Override
				public void onUtteranceCompleted(String utteranceId)
				{
					try
					{
						if (need_audio_focus == 0)
						{
							Navit.NavitAudioManager.abandonAudioFocus(Navit.focusChangeListener);
						}
					}
					catch (Exception e44)
					{
					}
					//					runOnUiThread(new Runnable()
					//					{
					//
					//						@Override
					//						public void run()
					//						{
					//							//UI changes
					//						}
					//					});
				}
			});
		}
		catch (Exception e77)
		{
			e77.printStackTrace();
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
				msg.what = 2;
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
				msg.what = 2;
				b.putString("text", Navit.get_text("Using Voice for:") + "\n" + navit.mTts.getLanguage().getDisplayName()); //TRANS
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

		// disable for now ------------
		// disable for now ------------

		//		try
		//		{
		//			if (requestCode == MY_DATA_CHECK_CODE)
		//			{
		//				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
		//				{
		//					// success, create the TTS instance
		//					if (navit.mTts != null)
		//					{
		//						stop_me();
		//					}
		//					Log.e("NavitSpeech2", "init_me");
		//					mTts = new TextToSpeech(navit, this);
		//
		//					try
		//					{
		//						// just for info -------
		//						Log.e("NavitSpeech2", Arrays.toString(Locale.getAvailableLocales()));
		//						// just for info -------
		//					}
		//					catch (Exception e)
		//					{
		//						e.printStackTrace();
		//					}
		//				}
		//				else
		//				{
		//					// missing data, install it
		//					Intent installIntent = new Intent();
		//					installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		//					navit.startActivity(installIntent);
		//				}
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}

		// disable for now ------------
		// disable for now ------------

	}

	NavitSpeech2(Navit navit)
	{
		Log.e("NavitSpeech2", "constructor");
		Log.e("NavitSpeech2", "locale=" + java.util.Locale.getDefault().getDisplayName());
		want_locale = java.util.Locale.getDefault();
		try
		{
			this.navit = navit;
			//navit.setActivityResult(MY_DATA_CHECK_CODE, this);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		//		try
		//		{
		//			Intent checkIntent = new Intent();
		//			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		//			navit.startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		//		}
		//		catch (Exception e2)
		//		{
		//			e2.printStackTrace();
		//		}
	}

	@SuppressLint("NewApi")
	public void say(String what, int lat, int lon)
	{

		if (Navit.PREF_enable_debug_write_gpx)
		{
			// ------- SPEECH DEBUG -------------------------------
			// ------- SPEECH DEBUG -------------------------------
			// ------- SPEECH DEBUG -------------------------------
			if ((lat != 0) || (lon != 0))
			{
				debug_lat = (float) (lat) / 100000.0f;
				debug_lon = (float) (lon) / 100000.0f;
			}
			else
			{
				debug_lat = 0;
				debug_lon = 0;
			}

			if (!NavitVehicle.speech_recording_started)
			{
				NavitVehicle.speech_recording_start();
			}
			// System.out.println("SPEECH:J:lat=" + debug_lat + " lon=" + debug_lon);
			NavitVehicle.speech_recording_add(debug_lat, debug_lon, what, System.currentTimeMillis());
			// ------- SPEECH DEBUG -------------------------------
			// ------- SPEECH DEBUG -------------------------------
			// ------- SPEECH DEBUG -------------------------------
		}

		try
		{
			if (navit.mTts != null)
			{

				try
				{
					need_audio_focus = 1;
					// Request audio focus for playback
					request_focus_result = Navit.NavitAudioManager.requestAudioFocus(Navit.focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
					// AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK // --> other players just lower their audio volume 

					//if (request_focus_result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
					//{
					//	// other app had stopped playing song now , so u can do u stuff now .
					//}
				}
				catch (Exception e33)
				{
				}

				if (Navit.PREF_speak_filter_special_chars)
				{
					what = filter_out_special_chars(what);
				}

				what = filter_out_special_chars_google(what);

				//try
				//{
				//if (Integer.valueOf(android.os.Build.VERSION.SDK) < 21)
				//{
				navit.mTts.speak(what, TextToSpeech.QUEUE_FLUSH, tts_params);
				//}
				//else
				//{
				//	navit.mTts.speak(what, TextToSpeech.QUEUE_FLUSH, null, "ZANavi.NAVSpeech");
				//}
				//}
				//catch (Exception e)
				//{
				//	navit.mTts.speak(what, TextToSpeech.QUEUE_FLUSH, tts_params);
				//}

				need_audio_focus = 0;

				if (NavitGraphics.NavitMsgTv2_.getVisibility() == View.VISIBLE)
				{
					Navit.set_debug_messages_say_wrapper("SAY:" + what + "\n");
				}

				if (Navit.PREF_show_debug_messages)
				{
					Navit.set_debug_messages3_wrapper(what);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String filter_out_special_chars_google(String in)
	{
		String out = in;
		// google TTS seems to say "Staatsstrasse" instead of "St." , try to correct it here
		// nice when you want to go to "St. Pölten" :-)
		out = out.replace("St.", "St ");
		return out;
	}

	public static String filter_out_special_chars(String in)
	{
		String out = in;
		out = out.replace("-", " ");
		out = out.replace("\"", "");
		out = out.replace("'", "");
		out = out.replace("\\n", "");
		out = out.replace("\\r", "");
		out = out.replace("\\\\", "");
		return out;
	}

	public void resume_me()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		Log.e("NavitSpeech2", "resume_me");

		if (navit.mTts != null)
		{
			//			try
			//			{
			//				Log.e("NavitSpeech2", "resume_me 00a mTts=" + navit.mTts);
			//				navit.mTts.shutdown();
			//				Log.e("NavitSpeech2", "resume_me 00b");
			//			}
			//			catch (Exception e)
			//			{
			//				e.printStackTrace();
			//			}
			//
			//			Log.e("NavitSpeech2", "resume_me 001");
			//
			//			try
			//			{
			//				navit.mTts.shutdown();
			//			}
			//			catch (Exception e)
			//			{
			//				e.printStackTrace();
			//			}
			//
			//			Log.e("NavitSpeech2", "resume_me 002");
			//
			//			try
			//			{
			//				navit.mTts.shutdown();
			//			}
			//			catch (Exception e)
			//			{
			//				e.printStackTrace();
			//			}

		}
		else
		{
			Log.e("NavitSpeech2", "resume_me 003");

			try
			{
				// navit.mTts = new TextToSpeech(navit, this);
				new startup_in_other_thread().execute(this);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		Log.e("NavitSpeech2", "resume_me finished");

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private class startup_in_other_thread extends AsyncTask<Object, Void, String>
	{

		@Override
		protected String doInBackground(Object... params)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);
			//						try
			//						{
			//							Thread.sleep(6000);
			//						}
			//						catch (InterruptedException e)
			//						{
			//						}
			navit.mTts = new TextToSpeech(navit, (OnInitListener) params[0]);

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result)
		{
		}

		@Override
		protected void onPreExecute()
		{
		}

		@Override
		protected void onProgressUpdate(Void... values)
		{
		}
	}

	public void stop_me()
	{
		Log.e("NavitSpeech2", "stop_me");
		try
		{
			navit.mTts.shutdown();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			navit.mTts.shutdown();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			navit.mTts.shutdown();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// mTts = null;
	}
}
