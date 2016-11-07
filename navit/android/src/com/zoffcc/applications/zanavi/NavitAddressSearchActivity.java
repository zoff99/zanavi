/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2015 Zoff <zoff@zoff.cc>
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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zoffcc.applications.zanavi.Navit.Navit_Address_Result_Struct;
import com.zoffcc.applications.zanavi.NavitSearchResultListArrayAdapter.search_result_entry;

public class NavitAddressSearchActivity extends AppCompatActivity
{
	static ZANaviAutoCompleteTextViewSearchLocation address_string;
	private EditText hn_string;
	private TextView addrhn_view;
	private static CheckBox pm_checkbox;
	private static CheckBox hdup_checkbox;
	static TextView result_count_number;
	static String result_count_text = "0";
	private CheckBox ff_checkbox;
	private String search_type;
	private int search_country_id = 0;
	private Button search_country_select;
	private ScrollView sv;
	private LinearLayout ll;
	static ListView listview;
	static SearchResultListNewArrayAdapter adapter;
	private ImageView index_light;
	private RelativeLayout index_container;
	private TextView index_light_noindex_text;
	static SearchResultsThreadNew searchresultsThreadNew_offline = null;
	private static int res_counter_ = 0;
	private int selected_id = -1;
	private int selected_id_passthru = -1;
	static Activity NavitAddressSearchActivity_s = null;

	public class SearchResultListNewArrayAdapter extends ArrayAdapter<search_result_entry>
	{
		private final Context context;
		ArrayList<search_result_entry> l = null;

		public SearchResultListNewArrayAdapter(Context context, ArrayList<search_result_entry> values)
		{
			super(context, -1, values);
			this.l = values;
			this.context = context;
		}

		@Override
		public void add(search_result_entry e)
		{
			this.l.add(e);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			search_result_entry entry = l.get(position);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (entry.gettype() == 1)
			{
				convertView = inflater.inflate(R.layout.search_result_item_street, null);

				ImageView icon_view = (ImageView) convertView.findViewById(R.id.icon);
				if (Navit.p.PREF_current_theme == Navit.DEFAULT_THEME_OLD_LIGHT)
				{
					// leave it as it is
				}
				else
				{
					// thanks to: http://stackoverflow.com/questions/17841787/invert-colors-of-drawable-android
					Drawable drawable_homeicon = icon_view.getResources().getDrawable(R.drawable.roadicon);
					float[] colorMatrix_Negative = { -1.0f, 0, 0, 0, 255, //red
							0, -1.0f, 0, 0, 255, //green
							0, 0, -1.0f, 0, 255, //blue
							0, 0, 0, 1.0f, 0 //alpha  
					};

					ColorFilter colorFilter_Negative = new ColorMatrixColorFilter(colorMatrix_Negative);
					drawable_homeicon.setColorFilter(colorFilter_Negative);
					icon_view.setImageDrawable(drawable_homeicon);
				}
			}
			else if (entry.gettype() == 3)
			{
				convertView = inflater.inflate(R.layout.search_result_item_poi, null);

				ImageView icon_view = (ImageView) convertView.findViewById(R.id.icon);
				if (Navit.p.PREF_current_theme == Navit.DEFAULT_THEME_OLD_LIGHT)
				{
					// leave it as it is
				}
				else
				{
					Drawable drawable_homeicon = icon_view.getResources().getDrawable(R.drawable.poiicon);
					float[] colorMatrix_Negative = { -1.0f, 0, 0, 0, 255, //red
							0, -1.0f, 0, 0, 255, //green
							0, 0, -1.0f, 0, 255, //blue
							0, 0, 0, 1.0f, 0 //alpha  
					};

					ColorFilter colorFilter_Negative = new ColorMatrixColorFilter(colorMatrix_Negative);
					drawable_homeicon.setColorFilter(colorFilter_Negative);
					icon_view.setImageDrawable(drawable_homeicon);
				}
			}
			else
			{
				convertView = inflater.inflate(R.layout.search_result_item_town, null);

				ImageView icon_view = (ImageView) convertView.findViewById(R.id.icon);
				if (Navit.p.PREF_current_theme == Navit.DEFAULT_THEME_OLD_LIGHT)
				{
					// leave it as it is
				}
				else
				{
					Drawable drawable_homeicon = icon_view.getResources().getDrawable(R.drawable.townicon);
					float[] colorMatrix_Negative = { -1.0f, 0, 0, 0, 255, //red
							0, -1.0f, 0, 0, 255, //green
							0, 0, -1.0f, 0, 255, //blue
							0, 0, 0, 1.0f, 0 //alpha  
					};

					ColorFilter colorFilter_Negative = new ColorMatrixColorFilter(colorMatrix_Negative);
					drawable_homeicon.setColorFilter(colorFilter_Negative);
					icon_view.setImageDrawable(drawable_homeicon);
				}
			}

			// R.layout.search_result_item_poi --> for POI result
			//}

			TextView text = (TextView) convertView.findViewById(R.id.toptext);
			text.setText(entry.getName());

			return convertView;
		}
	}

	public static void fillStringArrayNew(String s)
	{
		if (s.equals("D:D"))
		{
			// ok its a dummy
		}
		else
		{
			try
			{
				// we hope its a real result value
				Navit.Navit_Address_Result_Struct tmp_addr = new Navit_Address_Result_Struct();
				String[] tmp_s = s.split(":");
				tmp_addr.result_type = tmp_s[0];
				tmp_addr.item_id = tmp_s[1];

				tmp_addr.lat = Integer.parseInt(tmp_s[2]);
				tmp_addr.lon = Integer.parseInt(tmp_s[3]);
				// the rest ist address
				tmp_addr.addr = s.substring(4 + tmp_s[0].length() + tmp_s[1].length() + tmp_s[2].length() + tmp_s[3].length(), s.length());

				// String hash_id = tmp_addr.result_type + ":" + tmp_addr.lat + ":" + tmp_addr.lon + ":" + tmp_addr.addr;
				String hash_id = tmp_addr.result_type + ":" + tmp_addr.addr;
				//System.out.println("hash_id=" + hash_id);
				if ((!Navit.search_hide_duplicates) || (!Navit.Navit_Address_Result_double_index.contains(hash_id)))
				{
					Navit.NavitAddressResultList_foundItems.add(tmp_addr);
					Navit.Navit_Address_Result_double_index.add(hash_id);
					//System.out.println("*add*=" + hash_id);

					// System.out.println("search_result:I:" + Navit.NavitAddressResultList_foundItems.size() + ":" + tmp_addr.result_type + ":" + tmp_addr.lat + ":" + tmp_addr.lon + ":" + tmp_addr.addr);

					if (tmp_addr.result_type.equals("TWN"))
					{
						Navit.search_results_towns++;
					}
					else if (tmp_addr.result_type.equals("STR"))
					{
						Navit.search_results_streets++;
					}
					else if (tmp_addr.result_type.equals("SHN"))
					{
						Navit.search_results_streets_hn++;
					}
					else if (tmp_addr.result_type.equals("POI"))
					{
						Navit.search_results_poi++;
					}

					result_count_text = "" + Navit.NavitAddressResultList_foundItems.size();

					//
					//
					// System.out.println("RES=" + s);
					//
					//

					try
					{
						if (tmp_addr.result_type.equals("TWN"))
						{
							final search_result_entry res_entry = new search_result_entry(2, tmp_addr.addr, res_counter_);

							Navit.runOnUI(new Runnable()
							{
								@Override
								public void run()
								{
									adapter.add(res_entry);
									result_count_number.setText(result_count_text);
								}
							});

						}
						else if (tmp_addr.result_type.equals("POI"))
						{
							final search_result_entry res_entry = new search_result_entry(3, tmp_addr.addr, res_counter_);
							Navit.runOnUI(new Runnable()
							{
								@Override
								public void run()
								{
									adapter.add(res_entry);
									result_count_number.setText(result_count_text);
								}
							});
						}
						else
						{
							final search_result_entry res_entry = new search_result_entry(1, tmp_addr.addr, res_counter_);
							Navit.runOnUI(new Runnable()
							{
								@Override
								public void run()
								{
									adapter.add(res_entry);
									result_count_number.setText(result_count_text);
								}
							});
						}
						res_counter_++;
						// listview.postInvalidate();
					}
					catch (Exception e4)
					{
						System.out.println("AAEE:077:" + e4.getMessage());
					}

					try
					{
						Message msg = Navit.Navit_progress_h.obtainMessage();
						Bundle b = new Bundle();
						msg.what = 39;
						msg.setData(b);
						Navit.Navit_progress_h.sendMessage(msg);
					}
					catch (Exception e)
					{
						System.out.println("AAEE:011:" + e.getMessage());
						e.printStackTrace();
					}

					// Navit.NavitSearchresultBar_title = Navit.get_text("loading search results");
					// Navit.NavitSearchresultBar_text = Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn + " " + Navit.get_text("POI") + ":" + Navit.search_results_poi;
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	protected void onPause()
	{
		super.onPause();

		try
		{
			if (searchresultsThreadNew_offline != null)
			{
				searchresultsThreadNew_offline.stop_me();
			}
		}
		catch (Exception e)
		{
		}
	}

	public class SearchResultsThreadNew extends Thread
	{
		private Boolean running;
		private Boolean changed = false;
		private Boolean is_searching = false;

		SearchResultsThreadNew()
		{
			this.running = true;
			Log.e("Navit", "SearchResultsThreadNew created");

			Navit.NavitAddressResultList_foundItems.clear();
			Navit.Navit_Address_Result_double_index.clear();

			Navit.runOnUI(new Runnable()
			{
				@Override
				public void run()
				{
					result_count_text = "" + Navit.NavitAddressResultList_foundItems.size();
					result_count_number.setText(result_count_text);
				}
			});

			try
			{
				Message msg = Navit.Navit_progress_h.obtainMessage();
				Bundle b = new Bundle();
				msg.what = 38;
				msg.setData(b);
				Navit.Navit_progress_h.sendMessage(msg);
			}
			catch (Exception e)
			{
				System.out.println("AAEE:003");
				e.printStackTrace();
			}
			res_counter_ = 0;
		}

		public void set_search_strings(String addr, String hn)
		{
			Navit.Navit_last_address_search_string = addr;
			Navit.Navit_last_address_hn_string = hn;
		}

		public void change_search()
		{
			if (is_searching)
			{
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 46);
				msg.setData(b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg);
				}
				catch (Exception e)
				{
				}
				Log.e("Navit", "SearchResultsThreadNew -> Cancel");

				changed = true;
				this.interrupt();
			}
			else
			{
				changed = true;
				this.interrupt();
			}
		}

		public void cancel_search()
		{
		}

		public void stop_me()
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 46);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
			Log.e("Navit", "SearchResultsThreadNew -> stop_me");

			this.running = false;
			this.interrupt();
		}

		public void run()
		{
			Log.e("Navit", "SearchResultsThreadNew started");

			Navit.index_search_realtime = true;
			Navit.search_ready = false;

			System.out.println("Global_Location_update_not_allowed = 1");
			Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

			if (Navit.use_index_search)
			{
				while (running) // loop until we leave
				{
					// start the search, this could take a long time!!

					Navit.NavitAddressResultList_foundItems.clear();
					Navit.Navit_Address_Result_double_index.clear();

					Navit.runOnUI(new Runnable()
					{
						@Override
						public void run()
						{
							result_count_text = "" + Navit.NavitAddressResultList_foundItems.size();
							result_count_number.setText(result_count_text);
						}
					});

					try
					{
						Message msg = Navit.Navit_progress_h.obtainMessage();
						Bundle b = new Bundle();
						msg.what = 38;
						msg.setData(b);
						Navit.Navit_progress_h.sendMessage(msg);

						Thread.sleep(20);
					}
					catch (Exception e)
					{
						System.out.println("AAEE:002");
						e.printStackTrace();
					}
					res_counter_ = 0;

					if (NavitAddressSearchActivity.pm_checkbox.isChecked())
					{
						Navit.Navit_last_address_partial_match = true;
					}
					else
					{
						Navit.Navit_last_address_partial_match = false;
					}

					if (NavitAddressSearchActivity.hdup_checkbox.isChecked())
					{
						Navit.search_hide_duplicates = true;
					}
					else
					{
						Navit.search_hide_duplicates = false;
					}

					int partial_match_i = 0;
					if (Navit.Navit_last_address_partial_match)
					{
						partial_match_i = 1;
					}

					// Log.e("Navit", "SearchResultsThread run1");
					// need lowercase to find stuff !!
					Navit.Navit_last_address_search_string = Navit.filter_bad_chars(Navit.Navit_last_address_search_string).toLowerCase();
					if ((Navit.Navit_last_address_hn_string != null) && (!Navit.Navit_last_address_hn_string.equals("")))
					{
						Navit.Navit_last_address_hn_string = Navit.filter_bad_chars(Navit.Navit_last_address_hn_string).toLowerCase();
					}

					// new method with index search
					// -----------------
					//Navit_last_address_search_string
					String street_ = "";
					String town_ = "";
					String hn_ = Navit.Navit_last_address_hn_string;

					int last_space = Navit.Navit_last_address_search_string.lastIndexOf(" ");
					if (last_space != -1)
					{
						street_ = Navit.Navit_last_address_search_string.substring(0, last_space);
						town_ = Navit.Navit_last_address_search_string.substring(last_space + 1);
						// System.out.println("XX" + street_ + "YY" + town_ + "ZZ");
					}
					else
					{
						street_ = Navit.Navit_last_address_search_string;
						town_ = "";
					}

					is_searching = true;

					Log.e("Navit", "SearchResultsThread args:pm=" + partial_match_i + " str=" + street_ + " town=" + town_ + " hn=" + hn_ + " cfl=" + Navit.Navit_last_address_search_country_flags + " iso=" + Navit.Navit_last_address_search_country_iso2_string);
					Navit.N_NavitGraphics.SearchResultList(2, partial_match_i, street_, town_, hn_, Navit.Navit_last_address_search_country_flags, Navit.Navit_last_address_search_country_iso2_string, "0#0", 0);

					is_searching = false;
					Navit.search_ready = true;

					while ((!changed) && (running))
					{
						try
						{
							Log.e("Navit", "SearchResultsThread SLEEP");
							Thread.sleep(10000);
						}
						catch (Exception e2)
						{
						}
					}

					changed = false;

					// sort result list
					// Collections.sort(Navit.NavitAddressResultList_foundItems);
				}
			}

			Log.e("Navit", "SearchResultsThreadNew run2");

			Navit.NavitAddressSearchSpinnerActive = false;

			// reset the startup-search flag
			Navit.NavitStartupAlreadySearching = false;

			System.out.println("Global_Location_update_not_allowed = 0");
			Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!

			Navit.index_search_realtime = false;
			searchresultsThreadNew_offline = null;

			Log.e("Navit", "SearchResultsThreadNew ended");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.p.PREF_current_theme);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_search_form);

		android.support.v7.widget.Toolbar bar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar2nd);
		bar.setTitle(Navit.get_text("address search"));
		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		NavitAddressSearchActivity_s = this;

		// address: label
		TextView addr_view = (TextView) findViewById(R.id.enter_dest);
		addr_view.setText(Navit.get_text("Enter Destination")); //TRANS

		// housenumber: label
		addrhn_view = (TextView) findViewById(R.id.house_number);
		addrhn_view.setText(Navit.get_text("Housenumber")); //TRANS

		// partial match checkbox
		pm_checkbox = (CheckBox) findViewById(R.id.cb_partial_match);
		pm_checkbox.setText(Navit.get_text("partial match")); //TRANS
		pm_checkbox.setChecked(true);

		// hide duplicates checkbox
		hdup_checkbox = (CheckBox) findViewById(R.id.cb_hide_dup);
		hdup_checkbox.setText(Navit.get_text("hide duplicates")); //TRANS
		hdup_checkbox.setChecked(true);

		result_count_number = (TextView) findViewById(R.id.result_count_number);

		index_light = (ImageView) findViewById(R.id.index_light_img);
		index_light_noindex_text = (TextView) findViewById(R.id.index_light_noindex_text);
		index_light_noindex_text.setText(Navit.get_text("click to activate Index Search"));
		final Intent donate_intent = new Intent(this, ZANaviNormalDonateActivity.class);
		index_light_noindex_text.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent m)
			{
				startActivityForResult(donate_intent, Navit.NavitDonateFromSearch_id);
				return false;
			}
		});

		if (Navit.use_index_search)
		{
			index_light.setImageResource(R.drawable.round_light_green);
			index_light_noindex_text.setVisibility(View.GONE);
		}
		else
		{
			index_light.setImageResource(R.drawable.round_light_red);
		}

		index_container = (RelativeLayout) findViewById(R.id.index_light_container);

		// full file checkbox
		ff_checkbox = (CheckBox) findViewById(R.id.cb_full_file);

		if (!Navit.use_index_search)
		{
			ff_checkbox.setText(Navit.get_text("search full mapfile [BETA]")); //TRANS
			ff_checkbox.setChecked(false);
			ff_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					if (isChecked)
					{
						try
						{
							search_country_select.setVisibility(View.INVISIBLE);
							hdup_checkbox.setVisibility(View.INVISIBLE);
						}
						catch (Exception e)
						{

						}
					}
					else
					{
						try
						{
							search_country_select.setVisibility(View.VISIBLE);
							hdup_checkbox.setVisibility(View.VISIBLE);
						}
						catch (Exception e)
						{

						}
					}
				}
			});
		}

		// search button
		final Button btnSearch = (Button) findViewById(R.id.bt_search);
		btnSearch.setText(Navit.get_text("Search")); //TRANS
		btnSearch.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				executeDone("x");
			}
		});

		this.search_type = getIntent().getExtras().getString("type");
		if (this.search_type.endsWith("offline"))
		{
			this.search_country_id = getIntent().getExtras().getInt("search_country_id");
		}

		// select country button
		search_country_select = (Button) findViewById(R.id.bt_country_select);
		if (!Navit.use_index_search)
		{

			search_country_select.setText(NavitAddressSearchCountrySelectActivity.CountryList_Human[search_country_id][2]);
			search_country_select.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					start_country_select_form();
				}
			});
		}

		// title
		try
		{
			String s = getIntent().getExtras().getString("title");
			if (s.length() > 0)
			{
				this.setTitle(s);
			}
		}
		catch (Exception e)
		{
		}

		// partial match
		try
		{
			String s = getIntent().getExtras().getString("partial_match");
			if (s.length() > 0)
			{
				if (s.equals("1"))
				{
					pm_checkbox.setChecked(true);
				}
				else
				{
					pm_checkbox.setChecked(false);
				}
			}
		}
		catch (Exception e)
		{
		}

		hn_string = (EditText) findViewById(R.id.et_house_number_string);

		if (Navit.use_index_search)
		{

			// hide duplicates
			hdup_checkbox.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if (((CheckBox) v).isChecked())
					{
						Navit.search_hide_duplicates = true;
					}
					else
					{
						Navit.search_hide_duplicates = false;
					}

					// checkbox has changed state
					try
					{
						if (searchresultsThreadNew_offline == null)
						{
							Log.e("Navit", "SearchResults NEW:001:HN");

							searchresultsThreadNew_offline = new SearchResultsThreadNew();
							searchresultsThreadNew_offline.start();
						}
						else
						{
							Log.e("Navit", "SearchResults NEW:002:HN");

							searchresultsThreadNew_offline.change_search();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			});

			// house number string
			try
			{
				hn_string.setSingleLine();
				hn_string.setHint(Navit.get_text("Housenumber")); // TRANS
				// hn_string.setInputType(InputType.TYPE_CLASS_NUMBER);
				hn_string.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
				hn_string.setText(getIntent().getExtras().getString("hn_string"));

				hn_string.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count)
					{
					}

					@Override
					public void afterTextChanged(Editable arg0)
					{
						// address text has changed
						try
						{
							if (searchresultsThreadNew_offline == null)
							{
								Log.e("Navit", "SearchResults NEW:001:HN");

								searchresultsThreadNew_offline = new SearchResultsThreadNew();
								Navit.Navit_last_address_hn_string = arg0.toString();
								searchresultsThreadNew_offline.start();
							}
							else
							{
								Log.e("Navit", "SearchResults NEW:002:HN");

								searchresultsThreadNew_offline.set_search_strings(Navit.Navit_last_address_search_string, arg0.toString());
								searchresultsThreadNew_offline.change_search();
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{
					}
				});

				hn_string.setOnEditorActionListener(new TextView.OnEditorActionListener()
				{
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
					{
						if (actionId == EditorInfo.IME_ACTION_SEARCH)
						{
							executeDone("x");
							return true;
						}
						return false;
					}
				});
			}
			catch (Exception e)
			{
			}
		}

		// address string
		try
		{

			// address: text field -> with autocomplete dropdown
			address_string = (ZANaviAutoCompleteTextViewSearchLocation) findViewById(R.id.et_address_string);
			ArrayAdapter addr_view_autocomplete_adapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Navit.p.PREF_StreetSearchStrings);
			address_string.setCompletionHint(Navit.get_text("last searches")); // TRANS
			if (this.search_type.equals("offline"))
			{
				if (Navit.use_index_search)
				{
					address_string.setHint(Navit.get_text("Streetname") + " " + Navit.get_text("Town")); // TRANS
				}
				else
				{
					address_string.setHint(Navit.get_text("Town") + " " + Navit.get_text("Streetname")); // TRANS
				}
			}
			else
			{
				address_string.setHint(Navit.get_text("Address or POI-Name")); // TRANS				
			}

			address_string.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
				}

				@Override
				public void afterTextChanged(Editable arg0)
				{
					try
					{
						if (arg0.length() > 2)
						{
							// address text has changed
							if (searchresultsThreadNew_offline == null)
							{
								Log.e("Navit", "SearchResults NEW:001");

								searchresultsThreadNew_offline = new SearchResultsThreadNew();
								Navit.Navit_last_address_search_string = arg0.toString();
								searchresultsThreadNew_offline.start();
							}
							else
							{
								Log.e("Navit", "SearchResults NEW:002");

								searchresultsThreadNew_offline.set_search_strings(arg0.toString(), Navit.Navit_last_address_hn_string);
								searchresultsThreadNew_offline.change_search();
							}
						}
						else
						{
							if (searchresultsThreadNew_offline != null)
							{
								Log.e("Navit", "SearchResults NEW:003");
								searchresultsThreadNew_offline.cancel_search();
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				}
			});

			address_string.setOnEditorActionListener(new TextView.OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if (actionId == EditorInfo.IME_ACTION_SEARCH)
					{
						executeDone("x");
						return true;
					}
					return false;
				}
			});

			address_string.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
			address_string.setSingleLine();
			address_string.setThreshold(1);
			address_string.setAdapter(addr_view_autocomplete_adapter);
			address_string.setText(getIntent().getExtras().getString("address_string"));
		}
		catch (Exception e)
		{
		}

		if (this.search_type.equals("offline"))
		{
			if (Navit.use_index_search)
			{
				ff_checkbox.setVisibility(View.GONE);
				search_country_select.setVisibility(View.GONE);

				// deactivate some stuff for realtime-searchresults
				Button b = (Button) findViewById(R.id.bt_search);
				b.setVisibility(View.GONE);

				pm_checkbox.setPadding(0, 0, 0, 0);
				pm_checkbox.setTextSize(NavitGraphics.dp_to_px(5));
				pm_checkbox.setVisibility(View.GONE);

				pm_checkbox.setChecked(true);

				// hdup_checkbox.setPadding(0, 0, 0, 0);
				hdup_checkbox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

				TextView v1 = (TextView) findViewById(R.id.enter_dest);
				v1.setVisibility(View.GONE);
				TextView v2 = (TextView) findViewById(R.id.house_number);
				v2.setVisibility(View.GONE);

				result_count_text = "" + Navit.NavitAddressResultList_foundItems.size();
				result_count_number.setText(result_count_text);

				// index_container.setVisibility(View.GONE);

				listview = (ListView) findViewById(R.id.search_realtime_result_container);
				ArrayList<search_result_entry> l2 = new ArrayList<search_result_entry>();
				// search_result_entry a = new search_result_entry(1, "eoprk woroewk r3k0", 0);
				// l2.add(a);
				adapter = new SearchResultListNewArrayAdapter(this, l2);

				final Context c = this.getBaseContext();

				listview.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView parent, View v, int position, long id)
					{
						selected_id_passthru = position;

						Intent search_intent = new Intent(c, NavitMapPreviewActivity.class);
						search_intent.putExtra("lat", (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(position).lat));
						search_intent.putExtra("lon", (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(position).lon));

						search_intent.putExtra("q", Navit.NavitAddressResultList_foundItems.get(position).addr);
						startActivityForResult(search_intent, Navit.NavitMapPreview_id);
					}
				}

				);

				listview.setAdapter(adapter);
			}
			else
			// no index search
			{
				result_count_number.setVisibility(View.GONE);
			}
		}
		else
		{
			ff_checkbox.setVisibility(View.GONE);
			search_country_select.setVisibility(View.GONE);
			pm_checkbox.setVisibility(View.GONE);
			hdup_checkbox.setVisibility(View.GONE);
			index_container.setVisibility(View.GONE);
			result_count_number.setVisibility(View.GONE);
			hn_string.setVisibility(View.GONE);
			addrhn_view.setVisibility(View.GONE);
		}
	}

	public void start_country_select_form()
	{
		Intent search_intent = new Intent(this, NavitAddressSearchCountrySelectActivity.class);
		this.startActivityForResult(search_intent, Navit.NavitAddressSearchCountry_id);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case Navit.NavitAddressSearchCountry_id:
			try
			{
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					search_country_id = Integer.parseInt(data.getStringExtra("selected_id"));
					// System.out.println("search_country_id=" + search_country_id);
					search_country_select.setText(NavitAddressSearchCountrySelectActivity.CountryList_Human[search_country_id][2]);
				}
			}
			catch (Exception e)
			{

			}
			break;

		case Navit.NavitMapPreview_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					Log.e("Navit", "*activity ready*");
					int sel_id = Integer.parseInt(data.getStringExtra("selected_id"));
					Log.e("Navit", "*activity ready* sel_id=" + sel_id);

					if (sel_id == 1)
					{
						// user wants to set as destination
						this.selected_id = this.selected_id_passthru;
						// close this activity
						executeDone("set");
					}
					else if (sel_id == 2)
					{
						// "back"
					}
					else if (sel_id == 3)
					{
						// show destination on map
						this.selected_id = this.selected_id_passthru;
						executeDone("view");
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		}

	}

	static void force_done()
	{
		try
		{
			if (searchresultsThreadNew_offline != null)
			{
				searchresultsThreadNew_offline.stop_me();
			}
		}
		catch (Exception e)
		{
		}

		Intent resultIntent = new Intent();

		NavitAddressSearchActivity_s.setResult(ActionBarActivity.RESULT_OK, resultIntent);
		resultIntent.putExtra("address_string", NavitAddressSearchActivity.address_string.getText().toString());
		resultIntent.putExtra("what", "-");

		NavitAddressSearchActivity_s.finish();
	}

	private void executeDone(String what)
	{
		try
		{
			if (searchresultsThreadNew_offline != null)
			{
				searchresultsThreadNew_offline.stop_me();
			}
		}
		catch (Exception e)
		{
		}

		Intent resultIntent = new Intent();
		resultIntent.putExtra("address_string", NavitAddressSearchActivity.address_string.getText().toString());

		if (this.search_type.endsWith("offline"))
		{
			if (Navit.use_index_search)
			{
				resultIntent.putExtra("hn_string", NavitAddressSearchActivity.this.hn_string.getText().toString());
			}

			if (NavitAddressSearchActivity.pm_checkbox.isChecked())
			{
				resultIntent.putExtra("partial_match", "1");
			}
			else
			{
				resultIntent.putExtra("partial_match", "0");
			}

			if (NavitAddressSearchActivity.hdup_checkbox.isChecked())
			{
				resultIntent.putExtra("hide_dup", "1");
			}
			else
			{
				resultIntent.putExtra("hide_dup", "0");
			}

			try
			{
				if (NavitAddressSearchActivity.this.ff_checkbox.isChecked())
				{
					resultIntent.putExtra("full_file_search", "1");
				}
				else
				{
					resultIntent.putExtra("full_file_search", "0");
				}
			}
			catch (Exception e)
			{
				// on error assume its turned off
				resultIntent.putExtra("full_file_search", "0");
			}

			try
			{
				resultIntent.putExtra("address_country_iso2", NavitAddressSearchCountrySelectActivity.CountryList_Human[search_country_id][0]);
				resultIntent.putExtra("search_country_id", search_country_id);
				if (NavitAddressSearchCountrySelectActivity.CountryList_Human[search_country_id][0].equals("*D"))
				{
					resultIntent.putExtra("address_country_flags", 1);
				}
				else if (NavitAddressSearchCountrySelectActivity.CountryList_Human[search_country_id][0].equals("*A"))
				{
					resultIntent.putExtra("address_country_flags", 3);
				}
				else
				{
					resultIntent.putExtra("address_country_flags", 2);
				}
			}
			catch (Exception e)
			{
				// index search
				resultIntent.putExtra("address_country_iso2", "X");
				resultIntent.putExtra("search_country_id", 0);
				resultIntent.putExtra("address_country_flags", 2);
			}
		}

		try
		{
			// now hide the keyboard before we switch back to the mapscreen (bitmaps wont be recreated because now the size stays the same!)
			InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		catch (Exception e)
		{

		}

		resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
		if (what.equals("view"))
		{
			resultIntent.putExtra("what", "view");
		}
		else if (what.equals("set"))
		{
			resultIntent.putExtra("what", "set");
		}
		else
		{
			resultIntent.putExtra("what", "-");
		}

		setResult(ActionBarActivity.RESULT_OK, resultIntent);
		finish();
	}
}
