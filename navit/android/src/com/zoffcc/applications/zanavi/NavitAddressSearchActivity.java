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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NavitAddressSearchActivity extends ActionBarActivity
{
	private AutoCompleteTextView address_string;
	private EditText hn_string;
	private CheckBox pm_checkbox;
	private CheckBox hdup_checkbox;
	private CheckBox ff_checkbox;
	private String search_type;
	private int search_country_id = 0;
	private Button search_country_select;
	private ScrollView sv;
	private LinearLayout ll;

	//	@Override
	//	protected void onPostCreate(Bundle savedInstanceState)
	//	{
	//		super.onPostCreate(savedInstanceState);
	//		Toolbar bar;
	//
	//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	//		{
	//			ViewGroup root_view = (ViewGroup) ll;
	//			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
	//			bar.setTitle(Navit.get_text("address search"));
	//			root_view.addView(bar, 0); // insert at top
	//		}
	//		else
	//		{
	//			
	//			System.out.println("ZZXX22:r1=" + ll);
	//			System.out.println("ZZXX22:r2=" + (ViewGroup) ll.getChildAt(0));
	//			System.out.println("ZZXX22:r1=" + findViewById(android.R.id.content));
	//			
	//			ViewGroup root_view = (ViewGroup) ll;
	//			View content = (View) root_view.getChildAt(0);
	//			
	//			root_view.removeAllViews();
	//
	//			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
	//			bar.setTitle(Navit.get_text("address search"));
	//			root_view.addView(bar);
	//
	//			int height;
	//			TypedValue tv = new TypedValue();
	//			if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
	//			{
	//				height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
	//			}
	//			else
	//			{
	//				height = bar.getHeight();
	//			}
	//
	//			content.setPadding(0, height, 0, 0);
	//
	//			root_view.addView(content);
	//		}
	//
	//		bar.setNavigationOnClickListener(new View.OnClickListener()
	//		{
	//			@Override
	//			public void onClick(View v)
	//			{
	//				finish();
	//			}
	//		});
	//	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.PREF_current_theme);

		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		ll = new LinearLayout(this);
		ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		ll.setOrientation(LinearLayout.VERTICAL);

		ViewGroup root_view = (ViewGroup) ll;
		Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
		bar.setTitle(Navit.get_text("Map Preview"));

		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		// scrollview
		sv = new ScrollView(this);
		sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// panel linearlayout
		LinearLayout panel = new LinearLayout(this);
		panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		panel.setOrientation(LinearLayout.VERTICAL);

		// address: label
		TextView addr_view = new TextView(this);
		addr_view.setText(Navit.get_text("Enter Destination")); //TRANS
		addr_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		addr_view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addr_view.setPadding(4, 4, 4, 4);

		// housenumber: label
		TextView addrhn_view = new TextView(this);
		addrhn_view.setText(Navit.get_text("Housenumber")); //TRANS
		addrhn_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		addrhn_view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addrhn_view.setPadding(4, 4, 4, 4);

		// partial match checkbox
		pm_checkbox = new CheckBox(this);
		pm_checkbox.setText(Navit.get_text("partial match")); //TRANS
		pm_checkbox.setChecked(true);
		pm_checkbox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		pm_checkbox.setGravity(Gravity.CENTER);

		// hide duplicates checkbox
		hdup_checkbox = new CheckBox(this);
		hdup_checkbox.setText(Navit.get_text("hide duplicates")); //TRANS
		hdup_checkbox.setChecked(true);
		hdup_checkbox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		hdup_checkbox.setGravity(Gravity.CENTER);

		// full file checkbox
		ff_checkbox = new CheckBox(this);
		if (!Navit.use_index_search)
		{
			ff_checkbox.setText(Navit.get_text("search full mapfile [BETA]")); //TRANS
			ff_checkbox.setChecked(false);
			ff_checkbox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			ff_checkbox.setGravity(Gravity.CENTER);

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
		final Button btnSearch = new Button(this);
		btnSearch.setText(Navit.get_text("Search")); //TRANS
		btnSearch.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		btnSearch.setGravity(Gravity.CENTER);
		btnSearch.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				executeDone();
			}
		});

		this.search_type = getIntent().getExtras().getString("type");
		if (this.search_type.endsWith("offline"))
		{
			this.search_country_id = getIntent().getExtras().getInt("search_country_id");
		}

		// select country button
		search_country_select = new Button(this);
		if (!Navit.use_index_search)
		{

			search_country_select.setText(NavitAddressSearchCountrySelectActivity.CountryList_Human[search_country_id][2]);
			search_country_select.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			search_country_select.setGravity(Gravity.CENTER);
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

		if (Navit.use_index_search)
		{

			// house number string
			try
			{
				hn_string = new EditText(this);
				hn_string.setSingleLine();
				hn_string.setHint(Navit.get_text("Housenumber")); // TRANS
				// hn_string.setInputType(InputType.TYPE_CLASS_NUMBER);
				hn_string.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
				hn_string.setText(getIntent().getExtras().getString("hn_string"));

				hn_string.setOnEditorActionListener(new TextView.OnEditorActionListener()
				{
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
					{
						if (actionId == EditorInfo.IME_ACTION_SEARCH)
						{
							executeDone();
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
			// old -> only normal edittext without autocomplete
			//address_string = new EditText(this);
			//address_string.setText(getIntent().getExtras().getString("address_string"));

			// address: text field -> with autocomplete dropdown
			address_string = new AutoCompleteTextView(this);
			ArrayAdapter addr_view_autocomplete_adapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Navit.PREF_StreetSearchStrings);
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

			address_string.setOnEditorActionListener(new TextView.OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if (actionId == EditorInfo.IME_ACTION_SEARCH)
					{
						executeDone();
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

		// actually adding the views (that have layout set on them) to the panel
		panel.addView(addr_view);
		panel.addView(address_string);
		if (this.search_type.equals("offline"))
		{
			if (Navit.use_index_search)
			{
				panel.addView(addrhn_view);
				panel.addView(hn_string);
			}
			if (!Navit.use_index_search)
			{
				panel.addView(search_country_select);
			}
			panel.addView(pm_checkbox);
			panel.addView(hdup_checkbox);
			if (!Navit.use_index_search)
			{
				panel.addView(ff_checkbox);
			}
		}
		panel.addView(btnSearch);

		sv.addView(panel);
		ll.addView(bar);
		ll.addView(sv);

		// set the main view
		setContentView(ll);
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
		}
	}

	private void executeDone()
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("address_string", NavitAddressSearchActivity.this.address_string.getText().toString());

		if (this.search_type.endsWith("offline"))
		{
			if (Navit.use_index_search)
			{
				resultIntent.putExtra("hn_string", NavitAddressSearchActivity.this.hn_string.getText().toString());
			}

			if (NavitAddressSearchActivity.this.pm_checkbox.isChecked())
			{
				resultIntent.putExtra("partial_match", "1");
			}
			else
			{
				resultIntent.putExtra("partial_match", "0");
			}

			if (NavitAddressSearchActivity.this.hdup_checkbox.isChecked())
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

		setResult(ActionBarActivity.RESULT_OK, resultIntent);
		finish();
	}
}
