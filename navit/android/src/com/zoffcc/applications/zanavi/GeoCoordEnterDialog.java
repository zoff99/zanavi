/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2013 Zoff <zoff@zoff.cc>
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

import net.technologichron.manacalc.NumberPicker;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class GeoCoordEnterDialog extends ActionBarActivity
{
	float lat;
	float lon;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.PREF_current_theme);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.geocoordenter);
		
		android.support.v7.widget.Toolbar bar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar2vi);
		bar.setTitle(Navit.get_text("Coord Dialog"));
		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		final Button ok_button = (Button) findViewById(R.id.ok01);
		final Button destination_button = (Button) findViewById(R.id.dst01);
		final Button cancel_button = (Button) findViewById(R.id.cancel01);
		ok_button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				executeDone("view");
			}
		});

		destination_button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				executeDone("dest");
			}
		});

		cancel_button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent resultIntent = new Intent();
				setResult(ActionBarActivity.RESULT_CANCELED, resultIntent);
				finish();
			}
		});
	}

	private void executeDone(String what)
	{
		Intent resultIntent = new Intent();
		final NumberPicker value_lat1 = (NumberPicker) findViewById(R.id.lat1);
		final NumberPicker value_lat2 = (NumberPicker) findViewById(R.id.lat2);
		final NumberPicker value_lat3 = (NumberPicker) findViewById(R.id.lat3);
		lat = value_lat1.getValue() + ((float) value_lat2.getValue() / 60) + ((float) value_lat3.getValue() / 3600);

		final NumberPicker value_lon1 = (NumberPicker) findViewById(R.id.lon1);
		final NumberPicker value_lon2 = (NumberPicker) findViewById(R.id.lon2);
		final NumberPicker value_lon3 = (NumberPicker) findViewById(R.id.lon3);
		lon = value_lon1.getValue() + ((float) value_lon2.getValue() / 60) + ((float) value_lon3.getValue() / 3600);

		resultIntent.putExtra("lat", String.valueOf(this.lat));
		resultIntent.putExtra("lon", String.valueOf(this.lon));
		final ToggleButton toggle_NS = (ToggleButton) findViewById(R.id.toggleButtonNS);
		if (!toggle_NS.isSelected())
		{
			this.lat = -this.lat;
		}
		final ToggleButton toggle_WE = (ToggleButton) findViewById(R.id.toggleButtonWE);
		if (toggle_WE.isSelected())
		{
			this.lon = -this.lon;
		}
		setResult(ActionBarActivity.RESULT_OK, resultIntent);
		if (what.equals("view"))
		{
			resultIntent.putExtra("what", "view");
		}
		else
		{
			resultIntent.putExtra("what", "-");
		}
		finish();
	}
}
