/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2015 Zoff <zoff@zoff.cc>
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ZANaviOSDDebug01 extends ImageView
{

	private Paint paint = new Paint();
	private Paint paint_s = new Paint();
	int w = 0;
	int h = 0;
	int dx = 0;
	int dy = 20;

	int[] th = new int[6];
	static String[] ts = new String[6];
	int ddy = 0;

	public ZANaviOSDDebug01(Context context)
	{
		super(context);

		ts[0] = "";
		ts[1] = "";
		ts[2] = "";
		ts[3] = "";
		ts[4] = "";
		ts[5] = "";

		th[0] = (int) (Navit.metrics.density * 35.0f + 0.5f);
		th[1] = (int) (Navit.metrics.density * 32.0f + 0.5f);
		th[2] = (int) (Navit.metrics.density * 25.0f + 0.5f);
		th[3] = (int) (Navit.metrics.density * 20.0f + 0.5f);
		th[4] = (int) (Navit.metrics.density * 15.0f + 0.5f);
		th[5] = (int) (Navit.metrics.density * 10.0f + 0.5f);

		ddy = (int) (Navit.metrics.density * 2.0f + 0.5f);

		paint_s.setStyle(Style.FILL_AND_STROKE);
		paint_s.setStrokeWidth((int) (Navit.metrics.density * 6.0f + 0.5f));
		paint_s.setAntiAlias(true);
		paint_s.setColor(Color.WHITE);
		paint_s.setAlpha(255);

		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(0);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		paint.setAlpha(255);
	}

	public ZANaviOSDDebug01(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		ts[0] = "";
		ts[1] = "";
		ts[2] = "";
		ts[3] = "";
		ts[4] = "";
		ts[5] = "";

		th[0] = (int) (Navit.metrics.density * 35.0f + 0.5f);
		th[1] = (int) (Navit.metrics.density * 32.0f + 0.5f);
		th[2] = (int) (Navit.metrics.density * 25.0f + 0.5f);
		th[3] = (int) (Navit.metrics.density * 20.0f + 0.5f);
		th[4] = (int) (Navit.metrics.density * 15.0f + 0.5f);
		th[5] = (int) (Navit.metrics.density * 10.0f + 0.5f);

		ddy = (int) (Navit.metrics.density * 2.0f + 0.5f);

		paint_s.setStyle(Style.FILL_AND_STROKE);
		paint_s.setStrokeWidth((int) (Navit.metrics.density * 6.0f + 0.5f));
		paint_s.setAntiAlias(true);
		paint_s.setColor(Color.WHITE);
		paint_s.setAlpha(255);

		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(0);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		paint.setAlpha(255);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		this.w = w;
		this.h = h;

		// float dp = 1f;
		// float fpixels = Navit.metrics.density * dp;
		// int pixels = (int) (Navit.metrics.density * dp + 0.5f);

		dx = -(int) (this.w * 0.25f * 0.7f);

	}

	static void add_text(String text)
	{
		ts[5] = ts[4];
		ts[4] = ts[3];
		ts[3] = ts[2];
		ts[2] = ts[1];
		ts[1] = ts[0];
		ts[0] = text;

		try
		{
			if (Navit.NAVIT_DEBUG_TEXT_VIEW) NavitGraphics.debug_text_view.postInvalidate();
		}
		catch (Exception e)
		{
		}
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);

		// System.out.println("OVOV:001");

		if (!NavitGraphics.MAP_DISPLAY_OFF)
		{
			paint_s.setTextSize(th[0]);
			c.drawText(ts[0], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0], paint_s);
			paint.setTextSize(th[0]);
			c.drawText(ts[0], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0], paint);

			paint_s.setTextSize(th[1]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + 2 * ddy, paint_s);
			paint.setTextSize(th[1]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + 2 * ddy, paint);

			paint_s.setTextSize(th[2]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + 3 * ddy, paint_s);
			paint.setTextSize(th[2]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + 3 * ddy, paint);

			paint_s.setTextSize(th[3]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + th[3] + 4 * ddy, paint_s);
			paint.setTextSize(th[3]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + th[3] + 4 * ddy, paint);

			paint_s.setTextSize(th[4]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + th[3] + th[4] + 5 * ddy, paint_s);
			paint.setTextSize(th[4]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + th[3] + th[4] + 5 * ddy, paint);

			paint_s.setTextSize(th[5]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + th[3] + th[4] + th[5] + 6 * ddy, paint_s);
			paint.setTextSize(th[5]);
			c.drawText(ts[1], (int) (this.w * 0.25) + dx, (int) (this.h * 0.35) + th[0] + th[1] + th[2] + th[3] + th[4] + th[5] + 6 * ddy, paint);
		}
	}
}
