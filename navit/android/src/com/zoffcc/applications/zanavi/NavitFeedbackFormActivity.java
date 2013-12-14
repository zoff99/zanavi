/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2013 Zoff <zoff@zoff.cc>
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
import android.widget.EditText;
import android.widget.TextView;

public class NavitFeedbackFormActivity extends Activity
{
	private EditText feedback_text;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		setContentView(R.layout.feedback_dialog);
		// scrollview
		//ScrollView sv = new ScrollView(this);
		//sv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		// panel linearlayout
		//LinearLayout panel = new LinearLayout(this);
		//panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		//panel.setOrientation(LinearLayout.VERTICAL);

		// label
		TextView addr_view = (TextView) findViewById(R.id.fb_label);
		addr_view.setText(Navit.get_text("Enter your Feedback text")); //TRANS
		addr_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		//addr_view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addr_view.setPadding(4, 4, 4, 4);

		// feedback string exidbox
		feedback_text = (EditText) findViewById(R.id.fb_text);
		feedback_text.setText("");
		feedback_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
		//feedback_text.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 300));
		feedback_text.setMinLines(6);
		feedback_text.setGravity(Gravity.TOP);

		// send button
		Button btnSearch = (Button) findViewById(R.id.fb_sendbutton);
		btnSearch.setText(Navit.get_text("send")); //TRANS
		//btnSearch.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		btnSearch.setGravity(Gravity.CENTER);
		btnSearch.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				executeDone();
			}
		});

		// actually adding the views (that have layout set on them) to the panel
		//panel.addView(addr_view);
		//panel.addView(feedback_text);
		//panel.addView(btnSearch);

		//sv.addView(panel);

		// set the main view
		//setContentView(sv);
	}

	private void executeDone()
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("feedback_text", NavitFeedbackFormActivity.this.feedback_text.getText().toString());

		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

}
