/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2012 Zoff <zoff@zoff.cc>
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
 * Copyright (C) 2011 Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
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

package de.oberoner.gpx2navit_txt;

import itcrowd.gps.util.DistanceUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Created by IntelliJ IDEA.
 * User: Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
 * Date: 06.11.11
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class GpxDOM
{
	protected Document gpxDoc;

	public class GPX_Track
	{
		ArrayList<NavitTrkptHeader> h;
		ArrayList<NavitTrkptList> i;
	}

	public GpxDOM(String GpxFilePath)
	{
		gpxDoc = null;

		try
		{
			File inFile = new File(GpxFilePath);

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			gpxDoc = docBuilder.parse(inFile);

			// Check if file is really a GPX-File
			Element gpxElement = gpxDoc.getDocumentElement();
			if (!"gpx".equals(gpxElement.getNodeName()))
			{
				gpxDoc = null;
				throw new SAXException("This is not a GPX file: " + gpxElement.getNodeName());
			}
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public GPX_Track getTrackLabel()
	{
		NodeList nameList = gpxDoc.getElementsByTagName("trk");
		ArrayList<NavitTrkptHeader> header = new ArrayList<NavitTrkptHeader>();
		ArrayList<NavitTrkptList> navitTrkptRecList = new ArrayList<NavitTrkptList>();
		GPX_Track obj = new GPX_Track();

		if (nameList.getLength() > 0)
		{
			for (int ii = 0; ii < nameList.getLength(); ii++)
			{
				org.w3c.dom.Node nn = nameList.item(ii);
				if (nn.getNodeType() == Node.ELEMENT_NODE)
				{
					try
					{
						Element firstElement = (Element) nn;
						NodeList nameElementList = firstElement.getElementsByTagName("name");
						Element nameElement = (Element) nameElementList.item(0);
						NodeList name = nameElement.getChildNodes();
						// System.out.println("ii:" + ii + ":" + name.item(0).getNodeValue());
						NavitTrkptHeader h = new NavitTrkptHeader();
						h.setLabel("\"" + MainFrame.remove_special_chars(name.item(0).getNodeValue()) + "\"");
						header.add(h);
					}
					catch (Exception e)
					{

					}
				}

				//else
				{
					try
					{
						Element firstElement = (Element) nn;
						NodeList nodeList = firstElement.getElementsByTagName("trkpt");
						if (nodeList.getLength() > 0)
						{
							NavitTrkptList tpl = new NavitTrkptList();

							for (int jj = 0; jj < nodeList.getLength(); jj++)
							{
								NavitTrkptRec navitTrkptRec = new NavitTrkptRec();
								Node node = nodeList.item(jj);
								if (node.getNodeType() == Node.ELEMENT_NODE)
								{
									try
									{
										Element element = (Element) nodeList.item(jj);
										//System.out.println("jj:" + jj + ":" + element.getAttribute("lat") + "," + element.getAttribute("lon"));
										navitTrkptRec.setLat(element.getAttribute("lat"));
										navitTrkptRec.setLon(element.getAttribute("lon"));
									}
									catch (Exception e)
									{

									}
								}
								tpl.add(navitTrkptRec);
							}
							navitTrkptRecList.add(tpl);
						}
					}
					catch (Exception e)
					{

					}
				}
			}
		}

		obj.h = header;
		obj.i = navitTrkptRecList;

		return obj;
	}

	public NavitWptList getWptList()
	{

		/*
		 * <wpt lon="12.384331925" lat="50.310372273">
		 * <ele>560.329834</ele>
		 * <name>001</name>
		 * <desc>13-MRZ-11 12:47:52</desc>
		 * </wpt>
		 */

		Node node;
		NavitWptList navitWptList = null;

		// Liste der Wegpunkte lesen
		NodeList wptList = gpxDoc.getElementsByTagName("wpt");

		// Gibt es Wegpunkte?
		if (wptList.getLength() > 0)
		{

			navitWptList = new NavitWptList();

			for (int i = 0; i < wptList.getLength(); i++)
			{

				NavitWptRec navitWptRec = new NavitWptRec();

				node = wptList.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE)
				{

					Element element = (Element) node;

					// Attribute des wpt-Tags lesen
					navitWptRec.setLat(element.getAttribute("lat"));
					navitWptRec.setLon(element.getAttribute("lon"));

					// Name des Waypoints ermitteln - wenn vorhanden
					String name_ = "";
					NodeList nameElementList = element.getElementsByTagName("name");
					Element nameElement = (Element) nameElementList.item(0);
					if (nameElement != null)
					{
						NodeList name = nameElement.getChildNodes();
						navitWptRec.setLabel(name.item(0).getNodeValue());
						name_ = name.item(0).getNodeValue();
					}
					else
					{
						navitWptRec.setLabel("");
					}

					// Beschreibung des Waypoints holen - wenn vorhanden
					NodeList descElementList = element.getElementsByTagName("desc");
					Element descElement = (Element) descElementList.item(0);
					if (descElement != null)
					{
						NodeList desc = descElement.getChildNodes();
						navitWptRec.setDescription(desc.item(0).getNodeValue());

						if (name_.equals(""))
						{
							// we dont have a name, so set "desc" as name
							navitWptRec.setLabel(desc.item(0).getNodeValue());
						}
					}
					else
					{
						navitWptRec.setDescription("");
					}
				}

				// Den Datensatz des neuen Wegpunkts in die Wegpunktliste schreiben
				navitWptList.add(navitWptRec);
			}
		}
		return navitWptList;
	}

	public GPX_Track getRouteLabel()
	{
		NodeList nameList = gpxDoc.getElementsByTagName("rte");
		ArrayList<NavitTrkptHeader> header = new ArrayList<NavitTrkptHeader>();
		ArrayList<NavitTrkptList> navitTrkptRecList = new ArrayList<NavitTrkptList>();
		GPX_Track obj = new GPX_Track();

		if (nameList.getLength() > 0)
		{
			for (int ii = 0; ii < nameList.getLength(); ii++)
			{
				org.w3c.dom.Node nn = nameList.item(ii);
				if (nn.getNodeType() == Node.ELEMENT_NODE)
				{
					try
					{
						Element firstElement = (Element) nn;
						NodeList nameElementList = firstElement.getElementsByTagName("name");
						Element nameElement = (Element) nameElementList.item(0);
						NodeList name = nameElement.getChildNodes();
						// System.out.println("ii:" + ii + ":" + name.item(0).getNodeValue());
						NavitTrkptHeader h = new NavitTrkptHeader();
						h.setLabel("\"" + MainFrame.remove_special_chars(name.item(0).getNodeValue()) + "\"");
						header.add(h);
					}
					catch (Exception e)
					{

					}
				}

				//else
				{
					try
					{
						Element firstElement = (Element) nn;
						NodeList nodeList = firstElement.getElementsByTagName("rtept");
						if (nodeList.getLength() > 0)
						{
							NavitTrkptList tpl = new NavitTrkptList();

							for (int jj = 0; jj < nodeList.getLength(); jj++)
							{
								NavitTrkptRec navitTrkptRec = new NavitTrkptRec();
								Node node = nodeList.item(jj);
								if (node.getNodeType() == Node.ELEMENT_NODE)
								{
									Element element = (Element) nodeList.item(jj);
									// System.out.println("jj:" + jj + ":" + element.getAttribute("lat") + "," + element.getAttribute("lon"));
									navitTrkptRec.setLat(element.getAttribute("lat"));
									navitTrkptRec.setLon(element.getAttribute("lon"));
								}
								tpl.add(navitTrkptRec);
							}
							navitTrkptRecList.add(tpl);
						}
					}
					catch (Exception e)
					{

					}
				}
			}
		}

		obj.h = header;
		obj.i = navitTrkptRecList;

		return obj;
	}

	/**************************************************************************
	 * calculateDistanc Berechnet die Gesamtlänge des Tracks auf der
	 * Grundlage der Kreisbogenmethode.
	 * Siehe: http://www.kompf.de/gps/distcalc.html
	 * 
	 * @param trkptList
	 *            Die Liste der Trackpunkte
	 * @return Gesamtlänge in Meter
	 */
	public double calculateDistance(NavitTrkptList trkptList)
	{

		double dist = 0;
		double lat1;
		double lat2 = 0;
		double lon1;
		double lon2 = 0;

		for (int i = 0; i < trkptList.size() - 1; i++)
		{

			if (i == 0)
			{

				lat1 = Double.valueOf(trkptList.get(i).getLat());
				lon1 = Double.valueOf(trkptList.get(i).getLon());
			}
			else
			{
				lat1 = lat2;
				lon1 = lon2;
			}

			lat2 = Double.valueOf(trkptList.get(i + 1).getLat());
			lon2 = Double.valueOf(trkptList.get(i + 1).getLon());

			if (i == 930)
			{
				i = 930;
			}
			dist = dist + DistanceUtil.distance(lat1, lon1, lat2, lon2);
		}

		return dist * 1000; //Entfernung von km in m umrechnen
	}

	public void writeWptList(NavitWptList wptList, BufferedWriter outFile)
	{

		String s;

		try
		{

			DecimalFormat trkptFormat = new DecimalFormat("#.000000");
			for (int i = 0; i < wptList.size(); i++)
			{
				outFile.write("type=" + wptList.get(i).getType() + " " + "label=" + "\"" + MainFrame.remove_special_chars(wptList.get(i).getLabel()) + "\" " + "description=" + "\"" + MainFrame.remove_special_chars(wptList.get(i).getDescription()) + "\" " + "gc_type=" + "\"" + MainFrame.remove_special_chars(wptList.get(i).getGc_type()) + "\"\n");

				s = trkptFormat.format(new Double(wptList.get(i).getLon())) + " ";
				s = s.replace(",", ".");
				outFile.write(s);
				s = trkptFormat.format(new Double(wptList.get(i).getLat())) + " " + "\n";
				s = s.replace(",", ".");
				outFile.write(s);
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void writeTrkptList(NavitTrkptHeader trkptHeader, NavitTrkptList trkptList, BufferedWriter outFile)
	{

		String s;

		try
		{
			String name_ = MainFrame.remove_special_chars(trkptHeader.getLabel());
			if (name_.equals(""))
			{
				name_ = MainFrame.remove_special_chars(trkptHeader.getDesc());
			}

			outFile.write("type=" + trkptHeader.getNavitType() + " " + "label=" + name_ + " " + "desc=" + MainFrame.remove_special_chars(trkptHeader.getDesc()) + " " + "type=" + MainFrame.remove_special_chars(trkptHeader.getType()) + " " + "length=" + trkptHeader.getLength() + " " + "count=" + trkptHeader.getCount() + "\n");

			DecimalFormat trkptFormat = new DecimalFormat("#.000000");
			for (int i = 0; i < trkptList.size(); i++)
			{
				s = trkptFormat.format(new Double(trkptList.get(i).getLon())) + " ";
				s = s.replace(",", ".");
				outFile.write(s);
				s = trkptFormat.format(new Double(trkptList.get(i).getLat())) + "\n";
				s = s.replace(",", ".");
				outFile.write(s);
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
