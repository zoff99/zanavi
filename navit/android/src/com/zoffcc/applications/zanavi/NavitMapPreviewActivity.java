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

package com.zoffcc.applications.zanavi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NavitMapPreviewActivity extends ActionBarActivity
{
	int selected_id;
	private ImageView view = null;
	private LinearLayout ll;

	public static Canvas view_canvas = null;
	public static Bitmap view_bitmap = null;
	public static Canvas view_canvas_upper = null;
	public static Bitmap view_bitmap_upper = null;
	public int my_height = 0;
	public int my_width = 0;
	private float my_lat = 0f;
	private float my_lon = 0f;
	private static int mp_overview = 0;
	private static int MaxConfigs = 6;
	private static Paint paint = new Paint();
	private static int MaxStrokeConfigs = 3;

	public static class MapPreviewConfig
	{
		int my_zoom;//= 14; // 14 -> detail || 7 -> overview || 5 -> o2       // show items like on this "order"-level
		int my_scale;// = 16; // 16 -> detail  ||  1024 -> overview; || 8192 -> o2 // real zoom level
		int my_font_size;// = 200; // 200 -> detail || 100 -> overview
		int my_selection_range;// = 400; // 400 -> detail || 40.000 -> overview || 1.400.000 -> o2 // square around destination in which to show items
		int[] my_strokewidth;
		String[] my_color;
		String my_bgcolor_1;
	}

	public static MapPreviewConfig[] mapconf = new MapPreviewConfig[MaxConfigs];

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.p.PREF_current_theme);

		this.selected_id = -1;

		super.onCreate(savedInstanceState);

		int j;

		j = 0;
		mapconf[j] = new MapPreviewConfig();
		mapconf[j].my_zoom = 15;
		mapconf[j].my_scale = 16;
		mapconf[j].my_font_size = 200;
		mapconf[j].my_selection_range = 400;
		mapconf[j].my_strokewidth = new int[MaxStrokeConfigs];
		mapconf[j].my_strokewidth[0] = 16;
		mapconf[j].my_strokewidth[2] = 16;
		mapconf[j].my_color = new String[MaxStrokeConfigs];
		mapconf[j].my_color[0] = "#FEFC8C";
		mapconf[j].my_color[2] = "#9B1199";
		mapconf[j].my_bgcolor_1 = "#FEFEFE";
		j++;
		mapconf[j] = new MapPreviewConfig();
		mapconf[j].my_zoom = 15;
		mapconf[j].my_scale = 16 * 2;
		mapconf[j].my_font_size = 200;
		mapconf[j].my_selection_range = 400;
		mapconf[j].my_strokewidth = new int[MaxStrokeConfigs];
		mapconf[j].my_strokewidth[0] = 16;
		mapconf[j].my_strokewidth[2] = 16;
		mapconf[j].my_color = new String[MaxStrokeConfigs];
		mapconf[j].my_color[0] = "#FEFC8C";
		mapconf[j].my_color[2] = "#9B1199";
		mapconf[j].my_bgcolor_1 = "#FEFEFE";
		j++;
		mapconf[j] = new MapPreviewConfig();
		mapconf[j].my_zoom = 7;
		mapconf[j].my_scale = 1024;
		mapconf[j].my_font_size = 100;
		mapconf[j].my_selection_range = 50000;
		mapconf[j].my_strokewidth = new int[MaxStrokeConfigs];
		mapconf[j].my_strokewidth[0] = 6;
		mapconf[j].my_strokewidth[2] = 6;
		mapconf[j].my_color = new String[MaxStrokeConfigs];
		mapconf[j].my_color[0] = "#CCCCCC";
		mapconf[j].my_color[2] = "#9B1199";
		mapconf[j].my_bgcolor_1 = "#FEF9EE";
		j++;
		mapconf[j] = new MapPreviewConfig();
		mapconf[j].my_zoom = 5;
		mapconf[j].my_scale = 8192;
		mapconf[j].my_font_size = 65;
		mapconf[j].my_selection_range = 100000;
		mapconf[j].my_strokewidth = new int[MaxStrokeConfigs];
		mapconf[j].my_strokewidth[0] = 3;
		mapconf[j].my_strokewidth[2] = 3;
		mapconf[j].my_color = new String[MaxStrokeConfigs];
		mapconf[j].my_color[0] = "#CCCCCC";
		mapconf[j].my_color[2] = "#9B1199";
		mapconf[j].my_bgcolor_1 = "#FEF9EE";
		j++;
		mapconf[j] = new MapPreviewConfig();
		mapconf[j].my_zoom = 3;
		mapconf[j].my_scale = 8192 * 2 * 2 * 2;
		mapconf[j].my_font_size = 65;
		mapconf[j].my_selection_range = 710000;
		mapconf[j].my_strokewidth = new int[MaxStrokeConfigs];
		mapconf[j].my_strokewidth[0] = 3;
		mapconf[j].my_strokewidth[2] = 3;
		mapconf[j].my_color = new String[MaxStrokeConfigs];
		mapconf[j].my_color[0] = "#CCCCCC";
		mapconf[j].my_color[2] = "#9B1199";
		mapconf[j].my_bgcolor_1 = "#FEF9EE";
		j++;
		mapconf[j] = new MapPreviewConfig();
		mapconf[j].my_zoom = 3;
		mapconf[j].my_scale = 8192 * 2 * 2 * 2 * 2;
		mapconf[j].my_font_size = 65;
		mapconf[j].my_selection_range = 1210000;
		mapconf[j].my_strokewidth = new int[MaxStrokeConfigs];
		mapconf[j].my_strokewidth[0] = 3;
		mapconf[j].my_strokewidth[2] = 3;
		mapconf[j].my_color = new String[MaxStrokeConfigs];
		mapconf[j].my_color[0] = "#CCCCCC";
		mapconf[j].my_color[2] = "#9B1199";
		mapconf[j].my_bgcolor_1 = "#FEF9EE";

		// set startup value
		mp_overview = 0;

		Display display = getWindowManager().getDefaultDisplay();
		my_width = display.getWidth();
		my_height = display.getHeight();

		try
		{
			my_lat = getIntent().getExtras().getFloat("lat");
		}
		catch (Exception e)
		{
		}

		try
		{
			my_lon = getIntent().getExtras().getFloat("lon");
		}
		catch (Exception e)
		{
		}

		//System.out.println("my_lat=" + my_lat);
		//System.out.println("my_lat=" + my_lon);

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
		// root_view.addView(bar, 0); // insert at top

		// scrollview
		ScrollView sv = new ScrollView(this);
		sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// panel linearlayout
		LinearLayout relativelayout = new LinearLayout(this);
		relativelayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		relativelayout.setOrientation(LinearLayout.VERTICAL);
		relativelayout.setPadding(5, 5, 5, 5);

		Button btn_set_dest = new Button(this);
		btn_set_dest.setText(Navit.get_text("Use as destination")); //TRANS
		btn_set_dest.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				//System.out.println("1111111");
				selected_id = 1;
				executeDone();
			}
		});
		btn_set_dest.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		btn_set_dest.setGravity(Gravity.CENTER);

		Button btn_cancel_dest = new Button(this);
		btn_cancel_dest.setText(Navit.get_text("back")); //TRANS
		btn_cancel_dest.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				//System.out.println("2222222");
				selected_id = 2;
				executeDone();
			}
		});
		btn_cancel_dest.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		btn_cancel_dest.setGravity(Gravity.CENTER);

		Button btn_view_dest = new Button(this);
		btn_view_dest.setText(Navit.get_text("show destination on map")); //TRANS
		btn_view_dest.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				//System.out.println("2222222");
				selected_id = 3;
				executeDone();
			}
		});
		btn_view_dest.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		btn_view_dest.setGravity(Gravity.CENTER);

		TextView header = new TextView(this);
		try
		{
			String h = getIntent().getExtras().getString("q");
			h = h + "\n" + Navit.get_text("touch map to zoom"); //TRANS
			header.setText(h);
		}
		catch (Exception e)
		{
			header.setText("destination"); //TRANS
		}
		header.setTextSize(16);
		if (this.my_height > this.my_width)
		{
			header.setLines(2);
		}
		else
		{
			header.setLines(1);
		}
		header.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		header.setGravity(Gravity.TOP);

		view = new ImageView(this)
		{
			@Override
			protected void onDraw(Canvas canvas)
			{
				if (view_bitmap != null)
				{
					// clear canvas with backgorund color
					view_canvas.drawColor(Color.parseColor(mapconf[mp_overview].my_bgcolor_1));
					view_canvas_upper.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
					DrawMapPreview(my_lat + "#" + my_lon + "#" + mapconf[mp_overview].my_zoom, this.getWidth(), this.getHeight(), mapconf[mp_overview].my_font_size, mapconf[mp_overview].my_scale, mapconf[mp_overview].my_selection_range);
					canvas.drawBitmap(view_bitmap, 0, 0, null);
					canvas.drawBitmap(view_bitmap_upper, 0, 0, null);
				}
			}
		};

		view.setOnTouchListener(new View.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				// System.out.println("xxxxxxxx222222");
				mp_overview++;

				// sanity check
				if (mp_overview < 0)
				{
					mp_overview = 0;
				}
				// sanity check

				if (mp_overview > MaxConfigs - 1)
				{
					mp_overview = 0;
				}

				view.postInvalidate();

				return false;
			}
		});
		view.setLayoutParams(new LayoutParams((int) (this.my_width * 0.8), (int) (this.my_height * 0.4)));

		TextView dummy1 = new TextView(this);
		dummy1.setText("");
		// dummy1.setTextColor(color);
		dummy1.setHeight(this.my_height * 15 / 800);

		if (view_bitmap != null)
		{
			try
			{
				view_bitmap.recycle();
				System.gc();
				view_bitmap = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		view_bitmap = Bitmap.createBitmap((int) (this.my_width * 0.8), (int) (this.my_height * 0.4), Bitmap.Config.ARGB_8888);
		view_canvas = new Canvas(view_bitmap);

		if (view_bitmap_upper != null)
		{
			try
			{
				view_bitmap_upper.recycle();
				System.gc();
				view_bitmap_upper = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		view_bitmap_upper = Bitmap.createBitmap((int) (this.my_width * 0.8), (int) (this.my_height * 0.4), Bitmap.Config.ARGB_8888);
		view_canvas_upper = new Canvas(view_bitmap_upper);

		LinearLayout relativelayout2 = new LinearLayout(this);
		relativelayout2.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		relativelayout2.setOrientation(LinearLayout.HORIZONTAL);
		relativelayout2.setPadding(this.my_height * 20 / 800, this.my_height * 20 / 800, this.my_height * 20 / 800, this.my_height * 20 / 800);

		relativelayout2.addView(view);
		relativelayout2.setGravity(Gravity.CENTER_HORIZONTAL);

		relativelayout.addView(header);
		relativelayout.addView(relativelayout2);
		// relativelayout.addView(btn_cancel_dest);
		relativelayout.addView(btn_view_dest);
		relativelayout.addView(dummy1);
		relativelayout.addView(btn_set_dest);

		sv.addView(relativelayout);

		ll.addView(bar);
		ll.addView(sv);

		// set the main view
		setContentView(ll);
	}

	@Override
	public void onBackPressed()
	{
		selected_id = -1;
		executeDone();
		// super.onBackPressed();
	}

	private void executeDone()
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
		setResult(ActionBarActivity.RESULT_OK, resultIntent);

		if (view_bitmap != null)
		{
			try
			{
				view_bitmap.recycle();
				System.gc();
				view_bitmap = null;
				view_canvas = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (view_bitmap_upper != null)
		{
			try
			{
				view_bitmap_upper.recycle();
				System.gc();
				view_bitmap_upper = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		finish();
	}

	/**
	 * draw map preview
	 */
	public static native void DrawMapPreview(String latlonzoom, int width, int height, int font_size, int scale, int sel_range);

	public static void DrawMapPreview_text(int x, int y, String text, int size, int dx, int dy)
	{
		//System.out.println("DrawMapPreview_text: x=" + x + " y=" + y + " text=" + text);
		//System.out.println("DrawMapPreview_text: dx=" + dx + " dy=" + dy);

		if ((view_bitmap_upper != null) && (text != null))
		{
			// Paint paint = new Paint();
			paint.setStrokeWidth(0);
			paint.setTextSize(size / 15);
			paint.setColor(Color.parseColor("#0A0A0A"));
			paint.setStyle(Paint.Style.FILL);
			paint.setAntiAlias(Navit.p.PREF_use_anti_aliasing);
			if (dx == 0x10000 && dy == 0)
			{
				view_canvas_upper.drawText(text, x, y, paint);
			}
			else
			{
				Path path = new Path();
				path.moveTo(x, y);
				path.rLineTo(dx, dy);
				paint.setTextAlign(Paint.Align.LEFT);
				view_canvas_upper.drawTextOnPath(text, path, 0, 0, paint);
			}
		}
	}

	public static void DrawMapPreview_target(int x, int y)
	{

		if (view_bitmap != null)
		{
			//System.out.println("draw target x=" + x + " y=" + y);
			// Paint paint = new Paint();
			paint.setStrokeWidth(0);
			paint.setAntiAlias(Navit.p.PREF_use_anti_aliasing);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.parseColor("#FF0303"));
			view_canvas.drawCircle(x, y, 10, paint);
			paint.setStyle(Paint.Style.STROKE);
			// paint.setColor(Color.parseColor("#FF0303"));
			view_canvas.drawCircle(x, y, 16, paint);
		}
	}

	public static void DrawMapPreview_polyline(int type, int c[])
	{
		if (view_bitmap != null)
		{
			// Paint paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(Navit.p.PREF_use_anti_aliasing);
			paint.setStrokeWidth(mapconf[mp_overview].my_strokewidth[type]);
			paint.setColor(Color.parseColor(mapconf[mp_overview].my_color[type]));
			Path path = new Path();
			path.moveTo(c[0], c[1]);
			for (int i = 2; i < c.length; i += 2)
			{
				path.lineTo(c[i], c[i + 1]);
			}
			//global_path.close();
			view_canvas.drawPath(path, paint);
		}
	}
}
