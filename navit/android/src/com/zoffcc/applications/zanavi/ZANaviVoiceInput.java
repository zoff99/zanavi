/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2013 Zoff <zoff@zoff.cc>
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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ZANaviVoiceInput extends Activity
{
	private static final int REQUEST_CODE = 8453702;
	// private ListView wordsList;
	private ZANaviBGTextView TextLine1;
	private ZANaviBGTextView TextLine2;
	// private ArrayList<String> matches;
	static long ZANaviVoiceInput_result_timestamp = 0L;
	String address_string = "";
	// ArrayAdapter<String> str_adapter;
	private static final int grace_delay_in_seconds = 5;
	private Button speakButton;
	private String rr_addr = "";
	private double rr_lat = 0;
	private double rr_lon = 0;
	private boolean do_location_search = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_recog);

		do_location_search = false;

		speakButton = (Button) findViewById(R.id.speakButton);
		speakButton.setText(Navit.get_text("Click to talk")); // TRANS

		TextView t1 = (TextView) findViewById(R.id.Header);
		t1.setText(Navit.get_text("Voice Search, speak your Destination")); // TRANS

		// wordsList = (ListView) findViewById(R.id.list);
		TextLine1 = (ZANaviBGTextView) findViewById(R.id.Line1);
		TextLine1.setBackgroundColor(Color.BLACK);
		TextLine2 = (ZANaviBGTextView) findViewById(R.id.Line2);
		TextLine1.set_bg_percent(0);
		TextLine1.set_bg_color(Color.BLACK);

		// Disable button if no recognition service is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0)
		{
			speakButton.setEnabled(false);
			speakButton.setText(Navit.get_text("Google Speechrecognition not found")); // TRANS
			Toast.makeText(getApplicationContext(), Navit.get_text("Google Speechrecognition not found"), Toast.LENGTH_LONG).show(); //TRANS
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		// reset the green arrow
		NavitAndroidOverlay.voice_rec_bar_visible = false;
		NavitAndroidOverlay.voice_rec_bar_visible2 = false;
		NavitGraphics.NavitAOverlay_s.postInvalidate();

		if (do_location_search)
		{
			do_location_search = false;
			start_location_search();
		}
	}

	/**
	 * button pressed
	 */
	public void speakButtonClicked(View v)
	{
		ZANaviVoiceInput_result_timestamp = 0L; // reset timestamp
		TextLine1.setBackgroundColor(Color.BLACK);
		TextLine1.setText("");
		TextLine1.set_bg_percent(0);
		TextLine1.set_bg_color(Color.BLACK);
		TextLine2.setText("");
		startVoiceRecognitionActivity();
	}

	public void Line1Clicked(View v)
	{
		ZANaviVoiceInput_result_timestamp = 0L; // reset timestamp
		TextLine1.setBackgroundColor(Color.BLACK);
		TextLine1.setText("");
		TextLine1.set_bg_percent(0);
		TextLine1.set_bg_color(Color.BLACK);
		TextLine2.setText("");
	}

	private Handler recog_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
				int j2 = msg.getData().getInt("j");
				int p2 = msg.getData().getInt("percent");
				if (j2 > 0)
				{
					TextLine1.set_bg_percent(p2);

					if (j2 > 4)
					{
						//TextLine1.setBackgroundColor(Color.RED);
						TextLine1.set_bg_color(Color.RED);
					}
					else if (j2 == 4)
					{
						//TextLine1.setBackgroundColor(Color.RED);
						TextLine1.set_bg_color(Color.RED);
					}
					else if (j2 == 3)
					{
						//TextLine1.setBackgroundColor(Color.RED);
						TextLine1.set_bg_color(Color.RED);
					}
					else if (j2 == 2)
					{
						//TextLine1.setBackgroundColor(Color.YELLOW);
						TextLine1.set_bg_color(Color.YELLOW);
					}
					else if (j2 == 1)
					{
						//TextLine1.setBackgroundColor(Color.GREEN);
						TextLine1.set_bg_color(Color.GREEN);
					}
					TextLine1.setText("drive in " + j2 + " seconds");
					TextLine2.setText(rr_addr);
				}
				else
				{
					//TextLine1.setBackgroundColor(Color.GREEN);
					TextLine1.set_bg_percent(p2);
					TextLine1.set_bg_color(Color.GREEN);

					TextLine1.setText("drive now");
					TextLine2.setText(rr_addr);
				}
				TextLine1.postInvalidate();
				TextLine2.postInvalidate();
				break;
			case 2:
				start_location_search();
				break;
			case 3:
				TextLine1.setBackgroundColor(Color.CYAN);
				TextLine1.set_bg_percent(50);
				TextLine1.set_bg_color(Color.CYAN);
				TextLine1.setText(Navit.get_text("you said:") + " " + address_string); // TRANS
				TextLine2.setText(Navit.get_text("searching for the location")); // TRANS
				TextLine1.postInvalidate();
				TextLine2.postInvalidate();
				//System.out.println("XYZ2:001a " + address_string);
				break;
			}
		}
	};

	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, Navit.get_text("Find Destination")); // TRANS
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
		{
			try
			{
				// Populate the wordsList with the String values the recognition engine thought it heard
				this.address_string = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);

				//				Message msg = new Message();
				//				Bundle b = new Bundle();
				//				msg.what = 2;
				//				msg.setData(b);
				//				recog_handler.sendMessage(msg);

				this.do_location_search = true;
			}
			catch (Exception e)
			{

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void start_location_search()
	{
		//		final Thread Thread001 = new Thread(new Runnable()
		//		{
		//			public void run()
		//			{
		//				try
		//				{
		//		Message msg = new Message();
		//		Bundle b = new Bundle();
		//		msg.what = 3;
		//		msg.setData(b);
		//		recog_handler.sendMessage(msg);
		//				}
		//				catch (Exception e2)
		//				{
		//				}
		//			}
		//		});
		//		Thread001.start();
		TextLine1.setText(Navit.get_text("you said:") + " " + address_string); // TRANS
		TextLine2.setText(Navit.get_text("searching for the location")); // TRANS
		TextLine1.postInvalidate();
		TextLine2.postInvalidate();

		List<Address> foundAdresses = null;
		rr_addr = "";
		rr_lat = 0;
		rr_lon = 0;
		try
		{
			foundAdresses = Navit.Navit_Geocoder.getFromLocationName(this.address_string, 1);

			int results_step = 0;
			String c_code = foundAdresses.get(results_step).getCountryCode();
			if (c_code != null)
			{
				rr_addr = rr_addr + foundAdresses.get(results_step).getCountryCode() + ",";
			}

			String p_code = foundAdresses.get(results_step).getPostalCode();
			if (p_code != null)
			{
				rr_addr = rr_addr + foundAdresses.get(results_step).getPostalCode() + " ";
			}

			if (foundAdresses.get(results_step).getMaxAddressLineIndex() > -1)
			{
				for (int addr_line = 0; addr_line < foundAdresses.get(results_step).getMaxAddressLineIndex(); addr_line++)
				{
					if (addr_line > 0) rr_addr = rr_addr + " ";
					rr_addr = rr_addr + foundAdresses.get(results_step).getAddressLine(addr_line);
				}
			}

			rr_lat = foundAdresses.get(results_step).getLatitude();
			rr_lon = foundAdresses.get(results_step).getLongitude();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			foundAdresses = null;
		}

		if (foundAdresses == null)
		{
			ZANaviVoiceInput_result_timestamp = 0L; // reset timestamp
			Toast.makeText(getApplicationContext(), Navit.get_text("Google did not find this location. Please search again"), Toast.LENGTH_LONG).show(); //TRANS

			TextLine1.setBackgroundColor(Color.BLACK);
			TextLine1.set_bg_percent(0);
			TextLine1.set_bg_color(Color.BLACK);
			TextLine1.setText(Navit.get_text("Google did not find this location. Please search again")); // TRANS
			TextLine2.setText(Navit.get_text("you said:") + " " + address_string); // TRANS
			TextLine1.postInvalidate();
			TextLine2.postInvalidate();
		}
		else
		{

			ZANaviVoiceInput_result_timestamp = System.currentTimeMillis();

			TextLine1.setBackgroundColor(Color.BLACK);
			TextLine1.set_bg_percent(0);
			TextLine1.set_bg_color(Color.BLACK);

			TextLine1.setText("drive in " + grace_delay_in_seconds + " seconds");
			TextLine2.setText(rr_addr);
			TextLine1.postInvalidate();
			TextLine2.postInvalidate();

			// start countdown thread
			final Thread VoiceRecCountdownThread = new Thread(new Runnable()
			{
				boolean set_dest = true;
				int j = grace_delay_in_seconds;

				public void run()
				{
					for (int i = 0; i < 101; i++)
					{
						if (ZANaviVoiceInput_result_timestamp == 0)
						{
							set_dest = false;
							return;
						}

						try
						{
							j = (int) ((grace_delay_in_seconds - ((float) i / (float) 100 * (float) grace_delay_in_seconds)) + 0.5);
							// System.out.println("xxxxxxxxxxxxxxxxxxxx i=" + i + " j=" + j);
							Message msg = new Message();
							Bundle b = new Bundle();
							msg.what = 1;
							b.putInt("j", j);
							b.putInt("percent", i);
							msg.setData(b);
							recog_handler.sendMessage(msg);
						}
						catch (Exception e2)
						{
						}

						try
						{
							//System.out.println("xxxxxxxxxxxxxxxxxxxx sleep=" + (int) ((float) grace_delay_in_seconds * 1000f / 100f));
							Thread.sleep((int) ((float) grace_delay_in_seconds * 1000f / 100f));
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						if (ZANaviVoiceInput_result_timestamp == 0)
						{
							set_dest = false;
							return;
						}
					}
					// ok set destination
					if (set_dest)
					{
						//System.out.println("XYZ:set_dest");
						executeDone(rr_lat, rr_lon, rr_addr);
					}
				}
			});
			VoiceRecCountdownThread.start();
		}

	}

	private void executeDone(double lat, double lon, String addr)
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("address_string", addr);
		resultIntent.putExtra("lat", lat);
		resultIntent.putExtra("lon", lon);
		setResult(Activity.RESULT_OK, resultIntent);
		//System.out.println("XYZ:finish");
		finish();
	}
}
