package com.zoffcc.applications.zanavi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class ZANaviBGTextView extends TextView
{

	private Paint p = null;
	private Rect r = null;
	private int percent = 0;

	public ZANaviBGTextView(Context context)
	{
		super(context);
		this.p = new Paint();
		this.p.setColor(Color.BLACK);
		this.r = new Rect(0, 0, 1, 1);
	}

	public ZANaviBGTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.p = new Paint();
		this.p.setColor(Color.BLACK);
		this.r = new Rect(0, 0, 1, 1);
	}

	public void set_bg_percent(int per)
	{
		if ((per >= 0) && (per <= 100))
		{
			try
			{
				this.percent = per;
				r.set(0, 0, (int) ((float) this.percent * (float) this.getWidth() / 100f), this.getHeight());
			}
			catch (Exception e)
			{
				//e.printStackTrace();
			}
		}
	}

	public void set_bg_color(int color)
	{
		this.p.setColor(color);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		try
		{
			if (r == null)
			{
				r.set(0, 0, (int) ((float) this.percent * (float) this.getWidth() / 100f), this.getHeight());
			}
			else
			{
				r = new Rect(0, 0, (int) ((float) this.percent * (float) this.getWidth() / 100f), this.getHeight());
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
	}

	protected void onDraw(Canvas canvas)
	{
		try
		{
			canvas.drawRect(r, p);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}

		super.onDraw(canvas);
	}
}
