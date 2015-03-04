/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2014 Zoff <zoff@zoff.cc>
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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ZANaviDonateActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.PREF_current_theme);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donate);
		
		android.support.v7.widget.Toolbar bar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar2bd);
		bar.setTitle(Navit.get_text("Donate with Bitcoin"));
		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		((TextView) findViewById(R.id.t1_donate_with_bitcoin)).setText(Navit.get_text("Donate with Bitcoin"));

		((Button) findViewById(R.id.b1_bitcoin_to_clip)).setText(Navit.get_text("copy Bitcoinaddress to Clipboard"));
		((Button) findViewById(R.id.b1_bitcoin_to_email)).setText(Navit.get_text("send Bitcoinaddress and QR Code as Email"));
		((Button) findViewById(R.id.b1_bitcoin_to_wallet)).setText(Navit.get_text("Donate with Bitcoin Wallet App"));
	}

	public void onBitcoinWallet(View arg0)
	{
		try
		{
			de.schildbach.wallet.integration.android.BitcoinIntegration.request(this, Navit.BITCOIN_DONATE_ADDR);
		}
		catch (Exception e)
		{
		}
	}

	public void onBitcoinEmail(View arg0)
	{
		try
		{
			final String subject = Navit.get_text("ZANavi Donation with Bitcoin");
			final String body = Navit.get_text("Bitcoin address") + ":\n" + Navit.BITCOIN_DONATE_ADDR + "\n\n" + Navit.get_text("generate QR code") + ":\n" + "https://chart.googleapis.com/chart?cht=qr&chl=bitcoin%3A" + Navit.BITCOIN_DONATE_ADDR + "&choe=UTF-8&chs=300x300";

			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			if (subject != null) emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			if (body != null) emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

			startActivity(Intent.createChooser(emailIntent, Navit.get_text("Send Bitcoin address to email ...")));
		}
		catch (Exception e)
		{
		}
	}

	public void onBitcoinClipboard(View arg0)
	{
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		clipboard.setText(Navit.BITCOIN_DONATE_ADDR);
		Toast.makeText(this, Navit.get_text("Bitcoinaddress copied to Clipboard"), Toast.LENGTH_LONG).show();
	}
}
