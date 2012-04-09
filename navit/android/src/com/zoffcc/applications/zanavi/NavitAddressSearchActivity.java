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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class NavitAddressSearchActivity extends Activity
{
	private EditText address_string;
	private CheckBox pm_checkbox;
	private CheckBox hdup_checkbox;
	private CheckBox ff_checkbox;
	private String search_type;
	private int search_country_id = 0;
	private Button search_country_select;

	// public RelativeLayout NavitAddressSearchActivity_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		// scrollview
		ScrollView sv = new ScrollView(this);
		sv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		// panel linearlayout
		LinearLayout panel = new LinearLayout(this);
		panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		panel.setOrientation(LinearLayout.VERTICAL);

		// address: label and text field
		TextView addr_view = new TextView(this);
		addr_view.setText(Navit.get_text("Enter Destination")); //TRANS
		addr_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		addr_view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addr_view.setPadding(4, 4, 4, 4);

		// partial match checkbox
		pm_checkbox = new CheckBox(this);
		pm_checkbox.setText(Navit.get_text("partial match")); //TRANS
		pm_checkbox.setChecked(false);
		pm_checkbox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		pm_checkbox.setGravity(Gravity.CENTER);

		// partial match checkbox
		hdup_checkbox = new CheckBox(this);
		hdup_checkbox.setText(Navit.get_text("hide duplicates")); //TRANS
		hdup_checkbox.setChecked(false);
		hdup_checkbox.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		hdup_checkbox.setGravity(Gravity.CENTER);

		// full file checkbox
		ff_checkbox = new CheckBox(this);
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

		// address string
		try
		{
			address_string = new EditText(this);
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
			panel.addView(search_country_select);
			panel.addView(pm_checkbox);
			panel.addView(hdup_checkbox);
			panel.addView(ff_checkbox);
		}
		panel.addView(btnSearch);

		sv.addView(panel);

		// set the main view
		setContentView(sv);
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
				if (resultCode == Activity.RESULT_OK)
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

		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
