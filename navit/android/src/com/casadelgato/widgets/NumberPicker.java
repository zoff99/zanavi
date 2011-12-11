/**
 * Copyright 2008 by John Lussmyer
 *
 * http://casadelgato.com/content/numberpicker
 * 
 */

package com.casadelgato.widgets;

import android.content.Context;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * This is a widget that provides a standard Spin button control for numbers.
 * XML Attributes that may be set
 * <ul>
 * <li>plusminus_width - width of + and - buttons, defaults to WRAP_CONTENT</li>
 * <li>plusminus_height - height of + and - buttons, defaults to WRAP_CONTENT</li>
 * <li>textarea_width - width of text number field, defaults to WRAP_CONTENT</li>
 * <li>textarea_height - height of text number field, defaults to WRAP_CONTENT</li>
 * <li>textSize - size of text to use, integer, defaults to 25</li>
 * <li>vertical - specifies vertical arrangement, defaults to false</li>
 * <li>minValue - Minimum value allowed (defaults to 0)</li>
 * <li>maxValue - Maximum value allowed (defaults to 100)</li>
 * <li>defaultValue - default value (defaults to minValue)</li>
 * <li>repeatInterval - initial milliseconds between repeats (defaults to 200)</li>
 * <li>repeatAcceleration - Num milliseconds to decrement the repeat interval on each repeat (defaults to 0)</li>
 * </ul>
 * 
 * @author John Lussmyer
 */
public class NumberPicker extends RelativeLayout
{

	/** If this is true, debug statements will be included */
	private static final boolean debug = false;

	private static final int DEF_MINVAL = 0;
	private static final int DEF_MAXVAL = 100;
	private static final int DEF_TXTSIZE = 25;
	private static final int DEF_REPINT = 200;
	/** Minimum Repeat interval */
	private static final int MIN_REPINT = 40;

	private int mIdDec = getNextID();
	private int mIdInc = getNextID();
	private int mIdTxt = getNextID();
	private int mRepeatValue = 0;
	private Button mBtnDec;
	private Button mBtnInc;
	private int mCurVal = DEF_MINVAL;
	private int mMaxValue = DEF_MAXVAL;
	private int mMinValue = DEF_MINVAL;
	private EditText mTxtNum;
	private int mTxtSize = DEF_TXTSIZE;
	private int mRepeatDefInt = DEF_REPINT;
	private int mRepeatInterval = DEF_REPINT;
	private Handler mRepeatHandler = new Handler();
	private int mRepeatAccel = 0;
	private ValueChangeListener mVCListener;
	private ButtonRepeater mRepeater = new ButtonRepeater();

	private static int mNextID = 7734;

	private static int getNextID()
	{
		return mNextID++;
	}

	/**
	 * Constructor used when building via XML entry
	 * 
	 * @param context
	 *            Context to use for controls
	 * @param attrs
	 *            XML values
	 */
	public NumberPicker(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// Attributes that may be set via XML tags
		// TODO: font
		boolean vertical = attrs.getAttributeBooleanValue(null, "vertical", false);
		int btnW = attrs.getAttributeIntValue(null, "plusminus_width", android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		int btnH = attrs.getAttributeIntValue(null, "plusminus_height", android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		int txtW = attrs.getAttributeIntValue(null, "textarea_width", android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		int txtH = attrs.getAttributeIntValue(null, "textarea_height", android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mTxtSize = attrs.getAttributeIntValue(null, "textSize", mTxtSize);

		mMinValue = attrs.getAttributeIntValue(null, "minValue", mMinValue);
		mMaxValue = attrs.getAttributeIntValue(null, "maxValue", mMaxValue);
		mCurVal = attrs.getAttributeIntValue(null, "defaultValue", mMinValue);

		mRepeatDefInt = attrs.getAttributeIntValue(null, "repeatInterval", mRepeatDefInt);
		mRepeatAccel = attrs.getAttributeIntValue(null, "repeatAcceleration", mRepeatAccel);

		buildContent(context, attrs, vertical, btnW, btnH, txtW, txtH);

		return;
	}

	/**
	 * Constructor when building widget programmatically
	 * 
	 * @param context
	 *            Context for widget
	 * @param vertical
	 *            true if vertical arrangement, false for horizontal
	 * @param btnW
	 *            Width of +/- buttons
	 * @param btnH
	 *            Height of +/- buttons
	 * @param txtW
	 *            Width of number text field
	 * @param txtH
	 *            Height of number text field
	 */
	public NumberPicker(Context context, boolean vertical, int btnW, int btnH, int txtW, int txtH)
	{
		super(context);
		buildContent(context, null, vertical, btnW, btnH, txtW, txtH);
		return;
	}

	/**
	 * Create all the sub widgets and arrange them appropriately.
	 * 
	 * @param context
	 *            Context for widget
	 * @param vertical
	 *            true if vertical arrangement, false for horizontal
	 * @param btnW
	 *            Width of +/- buttons
	 * @param btnH
	 *            Height of +/- buttons
	 * @param txtW
	 *            Width of number text field
	 * @param txtH
	 *            Height of number text field
	 */
	private void buildContent(Context context, AttributeSet attrs, boolean vertical, int btnW, int btnH, int txtW, int txtH)
	{
		// Build the member control/widgets
		mBtnInc = buildIncButton(context, attrs);
		mBtnDec = buildDecButton(context, attrs);
		mTxtNum = buildValueText(context, attrs);

		RelativeLayout.LayoutParams lp;

		lp = new RelativeLayout.LayoutParams(btnW, btnH);
		if (vertical)
		{
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
			lp.addRule(RelativeLayout.ALIGN_LEFT, mIdInc);
			lp.addRule(RelativeLayout.ALIGN_RIGHT, mIdInc);
		}
		else
		{
			// lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		addView(mBtnDec, lp);

		lp = new RelativeLayout.LayoutParams(txtW, txtH);
		if (vertical)
		{
			lp.addRule(RelativeLayout.ABOVE, mIdDec);
			lp.addRule(RelativeLayout.BELOW, mIdInc);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		}
		else
		{
			lp.addRule(RelativeLayout.RIGHT_OF, mIdDec);
			// lp.addRule(RelativeLayout.LEFT_OF, ID_INC);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		addView(mTxtNum, lp);

		lp = new RelativeLayout.LayoutParams(btnW, btnH);
		if (vertical)
		{
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		}
		else
		{
			lp.addRule(RelativeLayout.RIGHT_OF, mIdTxt);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		addView(mBtnInc, lp);
	}

	/**
	 * Get the Maximum value this widget will allow
	 * 
	 * @return Maximum spin value
	 */
	public int getMaxValue()
	{
		return mMaxValue;
	}

	/**
	 * Get the Minimum value this widget will allow
	 * 
	 * @return Minimum spin value
	 */
	public int getMinValue()
	{
		return mMinValue;
	}

	/**
	 * Get the current value.
	 * 
	 * @return Current value.
	 */
	public int getValue()
	{
		return mCurVal;
	}

	/**
	 * Increment the value by 1.
	 */
	public void increment()
	{
		int old = mCurVal;
		mCurVal = Math.min(mMaxValue, mCurVal + 1);

		if (mCurVal != old)
		{
			mTxtNum.setText(String.valueOf(mCurVal));

			if (mVCListener != null)
			{
				mVCListener.onNumberPickerValueChange(this, mCurVal);
			}
		}

		return;
	}

	/**
	 * Decrement the value by 1
	 */
	public void decrement()
	{
		int old = mCurVal;
		mCurVal = Math.max(mMinValue, mCurVal - 1);

		if (mCurVal != old)
		{
			mTxtNum.setText(String.valueOf(mCurVal));

			if (mVCListener != null)
			{
				mVCListener.onNumberPickerValueChange(this, mCurVal);
			}
		}

		return;
	}

	/**
	 * Build the Decrement button
	 * 
	 * @param context
	 * @return Decrement Button
	 */
	private Button buildDecButton(Context context, AttributeSet attrs)
	{
		Button btn = new Button(context, attrs);
		btn.setTextSize(mTxtSize);
		btn.setText("-");
		btn.setId(mIdDec);

		// Decrement once for a click
		btn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				decrement();
			}
		});

		// Auto Decrement for a long click
		btn.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View arg0)
			{
				startRepeating(-1);
				return false;
			}
		});

		// When the button is released, if we're auto decrementing, stop
		btn.setOnTouchListener(new View.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_UP)
				{
					startRepeating(0);
				}
				return false;
			}
		});

		return btn;
	}

	/**
	 * Build the Increment button
	 * 
	 * @param context
	 * @return Increment button
	 */
	private Button buildIncButton(Context context, AttributeSet attrs)
	{
		Button btn = new Button(context, attrs);
		btn.setTextSize(mTxtSize);
		btn.setText("+");
		btn.setId(mIdInc);

		// Increment once for a click
		btn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				increment();
			}
		});

		// Auto increment for a long click
		btn.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View arg0)
			{
				startRepeating(1);
				return false;
			}
		});

		// When the button is released, if we're auto incrementing, stop
		btn.setOnTouchListener(new View.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_UP)
				{
					startRepeating(0); // stops it
				}
				return false;
			}
		});

		return btn;
	}

	/**
	 * Build the text field
	 * 
	 * @param context
	 * @return Text field for value display
	 */
	private EditText buildValueText(Context context, AttributeSet attrs)
	{
		EditText txt = new EditText(context, attrs);

		txt.setTextSize(mTxtSize);
		txt.setId(mIdTxt);
		txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		txt.setText(String.valueOf(mCurVal));
		txt.setInputType(InputType.TYPE_CLASS_NUMBER);

		// Highlight the number when we get focus
		txt.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					((EditText) v).selectAll();
				}
				else
				{
					int oldValue = mCurVal;
					try
					{
						mCurVal = Integer.parseInt(((EditText) v).getText().toString());

						if (mCurVal > mMaxValue)
						{
							mCurVal = mMaxValue;
						}
						else if (mCurVal < mMinValue)
						{
							mCurVal = mMinValue;
						}
						((EditText) v).setText(String.valueOf(mCurVal));
						if (debug)
						{
							Log.d("NumberPicker", "NumberPicker.buildValueText: new value " + mCurVal);
						}
						if (mVCListener != null)
						{
							mVCListener.onNumberPickerValueChange(NumberPicker.this, mCurVal);
						}
					}
					catch (NumberFormatException nfe)
					{
						mCurVal = oldValue;
						if (debug)
						{
							Log.d("NumberPicker", "NumberPicker.buildValueText: bad value");
						}
					}
				}
			}
		});

		return txt;
	}

	/**
	 * Set the maximum value the control will display.
	 * 
	 * @param maxValue
	 */
	public void setMaxValue(int maxValue)
	{
		mMaxValue = maxValue;
		setValue(getValue()); // Make sure we are within the new limits
		return;
	}

	/**
	 * Set the minimum value the control will display.
	 * 
	 * @param minValue
	 */
	public void setMinValue(int minValue)
	{
		mMinValue = minValue;
		setValue(getValue()); // Make sure we are within the new limits
		return;
	}

	/**
	 * Set the current number for the spin control
	 * 
	 * @param newVal
	 *            New number to use
	 * @return Actual number used. (due to Min/Max)
	 */
	public int setValue(int newVal)
	{
		if (newVal > mMaxValue)
		{
			newVal = mMaxValue;
		}
		if (newVal < mMinValue)
		{
			newVal = mMinValue;
		}
		mCurVal = newVal;
		mTxtNum.setText(String.valueOf(mCurVal));
		mTxtNum.invalidate();

		if (mVCListener != null)
		{
			mVCListener.onNumberPickerValueChange(this, mCurVal);
		}
		if (debug)
		{
			Log.d("Metronome", "NumberPicker.setValue: val=" + newVal + ", min=" + mMinValue + ", max=" + mMaxValue);
		}
		return mCurVal;
	}

	/**
	 * This is used to force display of some arbitrary text in the text field.
	 * It does NOT change the current value.
	 * 
	 * @param txt
	 *            Text to be displayed.
	 */
	public void setText(String txt)
	{
		mTxtNum.setText(txt);
		return;
	}

	/**
	 * Interface for objects to be nofified of changes to the picker value.
	 * 
	 * @author Cougar
	 */
	public interface ValueChangeListener
	{
		public void onNumberPickerValueChange(NumberPicker picker, int value);
	}

	/**
	 * Set the listener to be notified of changes to the picker value.
	 * 
	 * @param listener
	 *            listener to be called.
	 */
	public void setOnValueChangeListener(ValueChangeListener listener)
	{
		mVCListener = listener;
		return;
	}

	/**
	 * Start auto repeating an increment/or decrement. Will stop when this is
	 * called with an inc of 0.
	 * 
	 * @param inc
	 *            Amount to change the value by each interval. 0 to stop.
	 */
	private void startRepeating(int inc)
	{
		mRepeatValue = inc;
		if (inc != 0)
		{
			mRepeatHandler.postDelayed(mRepeater, mRepeatInterval);
		}
		return;
	}

	/**
	 * Handle doing continuous increments or decrements if the button is held
	 * down. It stops when mRepeatValue is set to 0.
	 * 
	 * @author Cougar
	 */
	private class ButtonRepeater implements Runnable
	{
		public void run()
		{
			if (mRepeatValue > 0)
			{
				increment();
			}
			else if (mRepeatValue < 0)
			{
				decrement();
			}
			if (mRepeatValue != 0)
			{
				mRepeatHandler.postDelayed(mRepeater, mRepeatInterval);
				mRepeatInterval -= mRepeatAccel;
				if (mRepeatInterval < MIN_REPINT)
				{
					mRepeatInterval = MIN_REPINT;
				}
			}
			else
			{ // Restore to default repeat rate
				mRepeatInterval = mRepeatDefInt;
			}
			return;
		}
	}

}
