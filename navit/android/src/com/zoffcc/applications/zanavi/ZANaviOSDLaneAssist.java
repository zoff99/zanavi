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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ZANaviOSDLaneAssist extends View
{
	int w = 10;
	int h = 10;
	//	RectF bounds_speedwarning = new RectF(120, 800, 120 + 200, 800 + 200);
	//	Paint paint_speedwarning = new Paint(0);
	Paint paint = new Paint(0);
	Paint paint2 = new Paint(0);
	float textHeight = 10;
	float textOffset = 10;
	Matrix lanes_scaleMatrix = new Matrix();
	Matrix lanes_transMatrix = new Matrix();
	RectF lanes_rectF = new RectF();
	boolean no_draw = false;
	Path pathForTurn = new Path();

	public ZANaviOSDLaneAssist(Context context)
	{
		super(context);
	}

	public ZANaviOSDLaneAssist(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public void onSizeChanged(int w1, int h1, int oldw, int oldh)
	{
		super.onSizeChanged(w1, h1, oldw, oldh);
		this.w = w1;
		this.h = h1;
		lanes_transMatrix.reset();
		lanes_transMatrix.setTranslate(0.0f, NavitGraphics.dp_to_px(40)); // works: 0.0f, 120.0f
	}

	int get_lanes_kind_count(int parsed_num_lanes, String[] lanes_split)
	{
		final int num_of_kinds = 6;
		int[] lanes_kind = new int[num_of_kinds];
		int lanes_kind_count = 0;

		int k = 0;
		for (k = 0; k < num_of_kinds; k++)
		{
			lanes_kind[k] = 0; // reset all
		}

		int j = 0;
		for (j = 0; j < parsed_num_lanes; j++)
		{
			String lanes_split_sub[] = lanes_split[j].split(";");
			int parsed_num_lanes_sub = lanes_split_sub.length;

			k = 0;
			String single_arrow = "";
			for (k = 0; k < parsed_num_lanes_sub; k++)
			{
				single_arrow = lanes_split_sub[k].replaceAll("\\s", "");

				// none					3
				// through				3
				// left					1
				// right				5
				// slight_left			2
				// slight_right			4
				// sharp_left			1
				// sharp_right			5
				// mergeto_left			-
				// mergeto_right		-
				// merge_to_left		-
				// merge_to_right		-

				if (single_arrow.equalsIgnoreCase("sharp_left"))
				{
					lanes_kind[1] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("sharp_right"))
				{
					lanes_kind[5] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("none"))
				{
					lanes_kind[3] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("through"))
				{
					lanes_kind[3] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("left"))
				{
					lanes_kind[1] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("right"))
				{
					lanes_kind[5] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("slight_left"))
				{
					lanes_kind[2] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("slight_right"))
				{
					lanes_kind[4] = 1;
				}

			}
		}

		lanes_kind_count = 0;
		for (k = 0; k < num_of_kinds; k++)
		{
			if (lanes_kind[k] == 1)
			{
				lanes_kind_count++;
			}
		}

		return lanes_kind_count;
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);

		if (Navit.p.PREF_lane_assist)
		{
			if ((Navit.lanes_text == null) || (Navit.lanes_text.equals("")))
			{
				if (Navit.seg_len < 200)
				{
					// if we dont have lane information now, use the lane information of the next segment
					// but only if the current segment is not too long (in meters) !!
					Navit.lanes_num = Navit.lanes_num1;
					Navit.lanes_num_forward = Navit.lanes_num_forward1;
					Navit.lanes_text = Navit.lanes_text1;
				}
			}

			if (Navit.lanes_text != null)
			{
				if (!Navit.lanes_text.equals(""))
				{
					//				paint.setAntiAlias(true);
					//				paint.setColor(Color.BLUE);
					//				paint.setStyle(Style.FILL);
					//				c.drawRect(100, 340, 1000, 440, paint);
					//				paint.setColor(Color.WHITE);
					//				paint.setStyle(Style.FILL_AND_STROKE);
					//				paint.setStrokeWidth(2);
					//				paint.setTextSize(50);
					//				c.drawText(Navit.lanes_num + ":" + Navit.lanes_num_forward + ":" + Navit.lanes_text, 120, 400, paint);

					String lanes_split[] = Navit.lanes_text.split("\\|");
					int parsed_num_lanes = lanes_split.length;

					int lanes_choices_count = 0;
					int lanes_choices_route = -1;
					int[] lanes_choices_split_int = null;
					String[] lanes_choices_split = null;
					int highlight_kind = -1;

					int[] lanes_kind = new int[10];
					int lanes_kind_count = 0;

					// only 1 choice ---------------------
					if (Navit.lane_choices != null)
					{
						if (!Navit.lane_choices.equals(""))
						{
							lanes_choices_split = Navit.lane_choices.split("\\|");
							lanes_choices_count = lanes_choices_split.length;
							int tmp_lanes_kind_count = get_lanes_kind_count(parsed_num_lanes, lanes_split);

							if ((lanes_choices_count == 1) && (tmp_lanes_kind_count > 1))
							{
								// we only have 1 choice to drive to (means: no turns here)
								// so use info from next segment
								Navit.lane_choices = Navit.lane_choices1;
							}
						}
					}
					// only 1 choice ---------------------

					// only 1 choice (again) ---------------------
					if (Navit.lane_choices != null)
					{
						if (!Navit.lane_choices.equals(""))
						{
							lanes_choices_split = Navit.lane_choices.split("\\|");
							lanes_choices_count = lanes_choices_split.length;
							int tmp_lanes_kind_count = get_lanes_kind_count(parsed_num_lanes, lanes_split);

							if ((lanes_choices_count == 1) && (tmp_lanes_kind_count > 1))
							{
								// we only have 1 choice to drive to (means: no turns here)
								// so use info from next next segment
								Navit.lane_choices = Navit.lane_choices2;
							}
						}
					}
					// only 1 choice (again) ---------------------

					// sort and check lane choice -------------------------------------
					if (Navit.lane_choices != null)
					{
						if (!Navit.lane_choices.equals(""))
						{
							lanes_choices_split = Navit.lane_choices.split("\\|");
							lanes_choices_count = lanes_choices_split.length;
							lanes_choices_route = -1;
							lanes_choices_split_int = new int[lanes_choices_split.length];

							//System.out.println("SORTED:---orig---=" + Navit.lane_choices);

							if (lanes_choices_count > 1)
							{
								// find route lane
								int kk = 0;
								for (kk = 0; kk < lanes_choices_count; kk++)
								{
									// System.out.println("SORTED:kk=" + kk + " lcs length=" + lanes_choices_split.length);

									if (lanes_choices_split[kk].startsWith("x"))
									{
										lanes_choices_route = kk;
										//System.out.println("SORTED:route lane=" + lanes_choices_route);
										lanes_choices_split_int[kk] = Integer.parseInt(lanes_choices_split[kk].substring(1));
										//System.out.println("SORTED:res1=" + lanes_choices_split_int[kk] + " " + lanes_choices_split[kk].substring(1) + " " + lanes_choices_split[kk]);
									}
									else
									{
										lanes_choices_split_int[kk] = Integer.parseInt(lanes_choices_split[kk]);
										//System.out.println("SORTED:res2=" + lanes_choices_split_int[kk]);
									}
								}

								// sort entries (remember to also move the found "route" lane!!)
								kk = 0;
								int ll = 0;
								int temp;
								int max;
								for (kk = 1; kk < lanes_choices_count; kk++)
								{
									//System.out.println("SORTED:loop1=" + kk);

									temp = lanes_choices_split_int[kk - 1];
									max = lanes_choices_split_int[kk - 1];
									for (ll = kk; ll < lanes_choices_count; ll++)
									{
										//System.out.println("SORTED:loop2=" + ll + " temp=" + temp + " max=" + max);

										if (lanes_choices_split_int[ll] > max)
										{
											if (lanes_choices_route == ll)
											{
												// move the found "route" lane
												lanes_choices_route = kk - 1;
												//System.out.println("SORTED:move route lane1=" + ll + " -> " + (kk - 1));
											}
											else if (lanes_choices_route == kk - 1)
											{
												// move the found "route" lane
												lanes_choices_route = ll;
												//System.out.println("SORTED:move route lane2=" + (kk - 1) + " -> " + ll);
											}

											temp = lanes_choices_split_int[ll];
											lanes_choices_split_int[ll] = max;
											lanes_choices_split_int[kk - 1] = temp;
										}
									}
								}

								// sorted:
								//for (kk = 0; kk < lanes_choices_count; kk++)
								//{
								//	System.out.println("SORTED:k=" + kk + " v=" + lanes_choices_split_int[kk]);
								//}
								//System.out.println("SORTED:Route=" + lanes_choices_route);

							}
							else if (lanes_choices_count == 1)
							{
								if (lanes_choices_split[0].startsWith("x"))
								{
									lanes_choices_route = 0;
									lanes_choices_split_int[0] = Integer.parseInt(lanes_choices_split[0].substring(1));
								}
								else
								{
									lanes_choices_split_int[0] = Integer.parseInt(lanes_choices_split[0]);
								}
							}

							//						paint2.setAntiAlias(true);
							//						paint2.setColor(Color.BLUE);
							//						paint2.setStyle(Style.FILL);
							//						c.drawRect(100, 340, 1000, 440, paint2);
							//						paint2.setColor(Color.WHITE);
							//						paint2.setStyle(Style.FILL_AND_STROKE);
							//						paint2.setStrokeWidth(2);
							//						paint2.setTextSize(50);
							//						c.drawText(Navit.lane_choices, 120, 400, paint2);

							final int num_of_kinds = 6;
							lanes_kind = new int[num_of_kinds];
							lanes_kind_count = 0;

							int k = 0;
							for (k = 0; k < num_of_kinds; k++)
							{
								lanes_kind[k] = 0; // reset all
							}

							int j = 0;
							for (j = 0; j < parsed_num_lanes; j++)
							{
								String lanes_split_sub[] = lanes_split[j].split(";");
								int parsed_num_lanes_sub = lanes_split_sub.length;

								k = 0;
								String single_arrow = "";
								for (k = 0; k < parsed_num_lanes_sub; k++)
								{
									single_arrow = lanes_split_sub[k].replaceAll("\\s", "");

									// none					3
									// through				3
									// left					1
									// right				5
									// slight_left			2
									// slight_right			4
									// sharp_left			1
									// sharp_right			5
									// mergeto_left			-
									// mergeto_right		-
									// merge_to_left		-
									// merge_to_right		-

									if (single_arrow.equalsIgnoreCase("sharp_left"))
									{
										lanes_kind[1] = 1;
									}
									else if (single_arrow.equalsIgnoreCase("sharp_right"))
									{
										lanes_kind[5] = 1;
									}
									else if (single_arrow.equalsIgnoreCase("none"))
									{
										lanes_kind[3] = 1;
									}
									else if (single_arrow.equalsIgnoreCase("through"))
									{
										lanes_kind[3] = 1;
									}
									else if (single_arrow.equalsIgnoreCase("left"))
									{
										lanes_kind[1] = 1;
									}
									else if (single_arrow.equalsIgnoreCase("right"))
									{
										lanes_kind[5] = 1;
									}
									else if (single_arrow.equalsIgnoreCase("slight_left"))
									{
										lanes_kind[2] = 1;
									}
									else if (single_arrow.equalsIgnoreCase("slight_right"))
									{
										lanes_kind[4] = 1;
									}

								}
							}

							lanes_kind_count = 0;
							for (k = 0; k < num_of_kinds; k++)
							{
								if (lanes_kind[k] == 1)
								{
									//System.out.println("SORTED:lanes_kind_count=" + lanes_kind_count + " lanes_choices_route=" + lanes_choices_route);
									if (lanes_kind_count == lanes_choices_route)
									{
										highlight_kind = k;
										//System.out.println("SORTED:highlight_kind=" + highlight_kind);
									}
									lanes_kind_count++;
									//System.out.println("SORTED:lanes_kind:" + k + "=" + lanes_kind[k]);
								}
							}
							//System.out.println("SORTED:lanes_kind_count=" + lanes_kind_count);

						}

					}
					// sort and check lane choice -------------------------------------

					// -------- START POINT ----------
					// -------- START POINT ----------
					float scale_x = ((float) NavitGraphics.dp_to_px(100)) / 100.0f * 0.5f / 3.0f; // works: 0.5f
					float scale_y = ((float) NavitGraphics.dp_to_px(100)) / 100.0f * 0.5f / 3.0f; // works: 0.5f
					int lane_symbol_width = 152; // works: 140
					int xx1 = 0; // works: 0
					int yy1 = NavitGraphics.dp_to_px(10); // works: 30
					int xx1_start = xx1;
					int yy1_start = yy1;

					// fill with grey ----------------------
					c.drawColor(0xbbcccccc); // grey
					// fill with grey ----------------------

					// float fx_factor = this.w;
					// float fy_factor = this.h;
					// calc resize factor:
					// float fx_ = fx_factor / 1;
					// float fy_ = fy_factor / 1;
					// float final_factor = 1.0f / fx_;
					//					if (fy_ > fx_)
					//					{
					//						final_factor = 1.0f / fy_;
					//					}
					// float final_translator = (float) yy1_start * final_factor;
					// float final_translator_x = 170.0f * ((float) NavitGraphics.mCanvasWidth / fx_factor);

					//System.out.println("SORTED:tran: " + fx_factor + " " + NavitGraphics.mCanvasWidth + " " + final_translator_x);

					//					if (NavitGraphics.mCanvasWidth > fx_factor)
					//					{
					//						lanes_transMatrix.setTranslate((final_translator_x) - 170.0f, final_translator - (float) yy1_start);
					//					}
					//					else
					//					{
					//						lanes_transMatrix.setTranslate(0.0f, final_translator - (float) yy1_start);
					//					}

					//System.out.println("SORTED:tran: x=" + NavitGraphics.mCanvasWidth + " y=" + NavitGraphics.mCanvasHeight + " " + (final_translator - (float) yy1_start) + " f=" + final_factor);
					// -------- START POINT ----------
					// -------- START POINT ----------

					boolean highlight_lane = false;
					if (lanes_kind_count == lanes_choices_count)
					{
						highlight_lane = true;
						//System.out.println("SORTED:highlight_lane=" + highlight_lane);
					}

					if (parsed_num_lanes == 3)
					{
						xx1 = xx1 + (lane_symbol_width) * 1;
					}
					else if (parsed_num_lanes == 2)
					{
						xx1 = xx1 + (lane_symbol_width) * 2;
					}
					else if (parsed_num_lanes == 1)
					{
						xx1 = xx1 + (lane_symbol_width) * 3;
					}

					int j = 0;
					for (j = 0; j < parsed_num_lanes; j++)
					{

						// move to next lane (move to right)
						xx1 = xx1 + lane_symbol_width;

						String lanes_split_sub[] = lanes_split[j].split(";");
						int parsed_num_lanes_sub = lanes_split_sub.length;

						int k = 0;
						String single_arrow = "";
						for (k = 0; k < parsed_num_lanes_sub; k++)
						{

							single_arrow = lanes_split_sub[k].replaceAll("\\s", "");

							// dirty correction hack !! ------------------
							// dirty correction hack !! ------------------
							//						if (single_arrow.equalsIgnoreCase("sharp_right"))
							//						{
							//							single_arrow = "right";
							//						}
							//						else if (single_arrow.equalsIgnoreCase("sharp_left"))
							//						{
							//							single_arrow = "left";
							//						}
							if (single_arrow.equalsIgnoreCase("merge_to_left"))
							{
								single_arrow = "mergeto_left";
							}
							else if (single_arrow.equalsIgnoreCase("merge_to_right"))
							{
								single_arrow = "mergeto_right";
							}
							// dirty correction hack !! ------------------
							// dirty correction hack !! ------------------

							// ---------===================---------------
							// ---------===================---------------
							// ---------===================---------------

							pathForTurn.reset();
							int ha = 72;
							int wa = 72;

							int th = 12 * 3; // (12) thickness
							// pathForTurn.moveTo(wa / 2, ha - 1);

							pathForTurn.moveTo(xx1, yy1);

							float sarrowL = 22 * 4; // (22) side of arrow ?
							float harrowL = (float) (Math.sqrt(2) * sarrowL); // (float) (Math.sqrt(2) * sarrowL)
							float spartArrowL = (float) ((sarrowL - th / Math.sqrt(2)) / 2); // (float) ((sarrowL - th / Math.sqrt(2)) / 2)
							float hpartArrowL = ((float) (harrowL - th) / 2); // ((float) (harrowL - th) / 2)

							// none
							// through
							// left
							// right
							// slight_left
							// slight_right
							// sharp_left
							// sharp_right
							// mergeto_left
							// mergeto_right
							// merge_to_left
							// merge_to_right

							no_draw = false;

							paint.setColor(Color.LTGRAY);

							if ((single_arrow.equalsIgnoreCase("slight_left")) || (single_arrow.equalsIgnoreCase("slight_right")))
							{
								// -----------------------------------------------------
								// turn slighty left or right
								// -----------------------------------------------------
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("slight_left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);
									if ((highlight_lane) && (highlight_kind == 2))
									{
										//System.out.println("SORTED: XXXXXXXXXXXXXXXX");
										paint.setColor(Color.GREEN);
									}

								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == 4))
									{
										paint.setColor(Color.GREEN);
									}
								}

								int h = (int) (24.0f * 3.0f); // (24)
								int quadShiftY = 22 * 4; // (22)
								float quadShiftX = ((float) (quadShiftY / (1 + Math.sqrt(2)))) * 1.5f; // (float) (quadShiftY / (1 + Math.sqrt(2)))
								float nQuadShiftX = ((sarrowL - 2 * spartArrowL) - quadShiftX - th); // (sarrowL - 2 * spartArrowL) - quadShiftX - th
								float nQuadShifty = quadShiftY + (sarrowL - 2 * spartArrowL); // quadShiftY + (sarrowL - 2 * spartArrowL)
								pathForTurn.rMoveTo(-b * 4, 0);
								pathForTurn.rLineTo(0, -h /* + partArrowL */);
								pathForTurn.rQuadTo(0, -quadShiftY + quadShiftX /*- partArrowL*/, b * quadShiftX, -quadShiftY /*- partArrowL*/);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rLineTo(0, -sarrowL); // center
								pathForTurn.rLineTo(-b * sarrowL, 0);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rQuadTo(b * nQuadShiftX, -nQuadShiftX, b * nQuadShiftX, nQuadShifty);
								pathForTurn.rLineTo(0, h);
								// -----------------------------------------------------
								// -----------------------------------------------------
							}
							else if ((single_arrow.equalsIgnoreCase("left")) || (single_arrow.equalsIgnoreCase("right")))
							{
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);

									if ((highlight_lane) && (highlight_kind == 1))
									{
										paint.setColor(Color.GREEN);
									}
								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == 5))
									{
										paint.setColor(Color.GREEN);
									}
								}

								float quadShiftX = 18;
								float quadShiftY = 18;
								int wl = 10; // width
								int h = (int) (ha - quadShiftY - harrowL + hpartArrowL - 5);

								// --
								h = -2 * h;
								// --

								int sl = wl + th / 2;

								// --
								sl = 2;
								// --

								pathForTurn.rMoveTo(-b * sl, 0);
								pathForTurn.rLineTo(0, -h);
								pathForTurn.rQuadTo(0, -quadShiftY, b * quadShiftX, -quadShiftY);
								pathForTurn.rLineTo(b * wl, 0);

								pathForTurn.rLineTo(0, hpartArrowL);
								pathForTurn.rLineTo(b * harrowL / 2, -harrowL / 2); // center
								pathForTurn.rLineTo(-b * harrowL / 2, -harrowL / 2);
								pathForTurn.rLineTo(0, hpartArrowL);

								pathForTurn.rLineTo(-b * wl, 0);
								pathForTurn.rQuadTo(-b * (quadShiftX + th), 0, -b * (quadShiftX + th), quadShiftY + th);
								pathForTurn.rLineTo(0, h);
							}
							else if ((single_arrow.equalsIgnoreCase("sharp_left")) || (single_arrow.equalsIgnoreCase("sharp_right")))
							{
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("sharp_left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);

									if ((highlight_lane) && (highlight_kind == 1))
									{
										paint.setColor(Color.GREEN);
									}
								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == 5))
									{
										paint.setColor(Color.GREEN);
									}
								}

								float quadShiftX = 18;
								float quadShiftY = 18;
								int wl = 10; // width
								int h = (int) (ha - quadShiftY - harrowL + hpartArrowL - 5);

								// --
								h = -2 * h;
								// --

								int sl = wl + th / 2;

								// --
								sl = 2;
								// --

								pathForTurn.rMoveTo(-b * sl, 0);
								pathForTurn.rLineTo(0, -h);
								pathForTurn.rQuadTo(0, -quadShiftY, b * quadShiftX, -quadShiftY);
								pathForTurn.rLineTo(b * wl, 0);

								pathForTurn.rLineTo(0, hpartArrowL);
								pathForTurn.rLineTo(b * harrowL / 2, -harrowL / 2); // center
								pathForTurn.rLineTo(-b * harrowL / 2, -harrowL / 2);
								pathForTurn.rLineTo(0, hpartArrowL);

								pathForTurn.rLineTo(-b * wl, 0);
								pathForTurn.rQuadTo(-b * (quadShiftX + th), 0, -b * (quadShiftX + th), quadShiftY + th);
								pathForTurn.rLineTo(0, h);
							}
							else if ((single_arrow.equalsIgnoreCase("mergeto_left")) || (single_arrow.equalsIgnoreCase("mergeto_right")))
							{
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("mergeto_left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);

									if ((highlight_lane) && (highlight_kind == -99))
									{
										paint.setColor(Color.GREEN);
									}
								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == -99))
									{
										paint.setColor(Color.GREEN);
									}
								}

								int h = (int) (24.0f * 3.0f); // (24)

								// --
								h = (int) ((float) h * 0.9f);
								// --

								int quadShiftY = 22 * 4; // (22)
								float quadShiftX = ((float) (quadShiftY / (1 + Math.sqrt(2)))) * 1.2f; // (float) (quadShiftY / (1 + Math.sqrt(2)))
								float nQuadShiftX = ((sarrowL - 2 * spartArrowL) - quadShiftX - th); // (sarrowL - 2 * spartArrowL) - quadShiftX - th
								float nQuadShifty = quadShiftY + (sarrowL - 2 * spartArrowL); // quadShiftY + (sarrowL - 2 * spartArrowL)
								pathForTurn.rMoveTo(-b * 4, 0);
								pathForTurn.rLineTo(0, -h /* + partArrowL */);
								pathForTurn.rQuadTo(0, -quadShiftY + quadShiftX /*- partArrowL*/, b * quadShiftX, -quadShiftY /*- partArrowL*/);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rLineTo(0, -sarrowL); // center
								pathForTurn.rLineTo(-b * sarrowL, 0);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rQuadTo(b * nQuadShiftX, -nQuadShiftX, b * nQuadShiftX, nQuadShifty);
								pathForTurn.rLineTo(0, h);
							}
							else if ((single_arrow.equalsIgnoreCase("none")) || (single_arrow.equalsIgnoreCase("through")))
							{
								int h = ((int) (ha - hpartArrowL - 16)); // (int) (ha - hpartArrowL - 16)

								if ((highlight_lane) && (highlight_kind == 3))
								{
									paint.setColor(Color.GREEN);
									//System.out.println("SORTED:highlight straight");
								}

								h = 18 * h;

								pathForTurn.rMoveTo(th, 0);
								pathForTurn.rLineTo(0, -h);
								pathForTurn.rLineTo(hpartArrowL, 0);
								pathForTurn.rLineTo(-harrowL / 2, -harrowL / 2); // center
								pathForTurn.rLineTo(-harrowL / 2, harrowL / 2);
								pathForTurn.rLineTo(hpartArrowL, 0);
								pathForTurn.rLineTo(0, h);
							}
							else
							{
								no_draw = true;
							}

							if (!no_draw)
							{
								pathForTurn.close();

								// now scale path to correct size ---------------
								// now scale path to correct size ---------------
								// now scale path to correct size ---------------
								lanes_scaleMatrix.reset();
								// pathForTurn.computeBounds(lanes_rectF, true);
								// lanes_scaleMatrix.setScale(0.25f, 0.25f, lanes_rectF.centerX(), lanes_rectF.centerY());
								lanes_scaleMatrix.setScale(scale_x, scale_y, xx1_start, yy1_start);
								pathForTurn.transform(lanes_scaleMatrix);
								pathForTurn.transform(lanes_transMatrix);
								// now scale path to correct size ---------------
								// now scale path to correct size ---------------
								// now scale path to correct size ---------------

								paint.setAntiAlias(true);
								paint.setDither(true);

								paint.setStyle(Style.FILL);
								paint.setStrokeWidth(0);

								c.drawPath(pathForTurn, paint);

								paint.setColor(Color.BLACK);
								paint.setStyle(Style.STROKE);
								paint.setStrokeWidth((float) NavitGraphics.dp_to_px(100) * (float) 4.0f / (float) 3.0f / (float) 100.0f); // works: 4

								c.drawPath(pathForTurn, paint);
							}
						}

					}
					// ---------===================---------------
					// ---------===================---------------
				}
				else
				{
					c.drawColor(Color.TRANSPARENT);
					// c.drawColor(Color.RED);
				}
			}
			else
			{
				c.drawColor(Color.TRANSPARENT);
				// c.drawColor(Color.GREEN);
			}
		}
		else
		{
			c.drawColor(Color.TRANSPARENT);
			// c.drawColor(Color.BLUE);
		}

		// System.out.println("onDraw:OSDLaneAssist");
	}
}
