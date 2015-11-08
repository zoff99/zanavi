package com.zoffcc.applications.zanavi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ZANaviXMLHandler extends DefaultHandler
{

	public static ZANaviXMLList xMLList;
	Boolean currentElement = false;
	String currentValue = null;

	StringBuilder buff = null;

	public ZANaviXMLHandler()
	{
		xMLList = new ZANaviXMLList();

		System.out.println("XML:init");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	{

		System.out.println("XML:START:u=" + uri + " localname=" + localName + " qname=" + qName);

		//		if (xMLList == null)
		//		{
		//			xMLList = new ZANaviXMLList();
		//		}

		//		if (localName.equals("item"))
		//		{
		//			inItem = true;
		//		}
		//		if (inItem)
		//		{
		//			Log.d("START " + localName, "");
		//			if (localName.equals("title"))
		//			{
		//				inTitle = true;
		//				buff = new StringBuilder();
		//			}
		//			if (localName.equals("description"))
		//			{
		//				inDescription = true;
		//				buff = new StringBuilder();
		//			}
		//			if (localName.equals("link"))
		//			{
		//				inLink = true;
		//				buff = new StringBuilder();
		//			}
		//			if (localName.equals("pubDate"))
		//			{
		//				inDate = true;
		//				buff = new StringBuilder();
		//			}
		//			if (localName.equals("category"))
		//			{
		//				inCategory = true;
		//				buff = new StringBuilder();
		//			}
		//		}
	}

	/*
	 * Called when an xml tag ends
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		System.out.println("XML:END:u=" + uri + " localname=" + localName + " qname=" + qName);

		//		if (inItem && !inTitle && !inDescription && !inLink && !inDate && !inCategory)
		//		{
		//			Log.d("END ITEM", "");
		//			inItem = false;
		//		}
		//		else if (inTitle)
		//		{
		//			String check = buff.toString().trim();
		//			Log.d("TITLE:", check);
		//			Log.d("END " + localName, "");
		//			xMLList.setTitle(check);
		//			inTitle = false;
		//			buff = null;
		//		}
		//		else if (inDescription)
		//		{
		//			String check = buff.toString().trim();
		//			Log.d("DESC:", check);
		//			Log.d("END " + localName, "");
		//			xMLList.setDescription(check);
		//			inDescription = false;
		//			buff = null;
		//		}
		//		else if (inLink)
		//		{
		//			String check = buff.toString().trim();
		//			Log.d("LINK:", check);
		//			Log.d("END " + localName, "");
		//			xMLList.setLink(check);
		//			inLink = false;
		//			buff = null;
		//		}
		//		else if (inDate)
		//		{
		//			String check = buff.toString().trim();
		//			Log.d("DATE:", check);
		//			Log.d("END " + localName, "");
		//			check = check.substring(0, 16);
		//			xMLList.setDate(check);
		//			inDate = false;
		//			buff = null;
		//		}
		//		else if (inCategory)
		//		{
		//			String check = buff.toString().trim();
		//			Log.d("CATEGORY:", check);
		//			Log.d("END " + localName, "");
		//			xMLList.setCategory(check);
		//			inCategory = false;
		//			buff = null;
		//		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (buff != null)
		{
			for (int i = start; i < start + length; i++)
			{
				buff.append(ch[i]);
			}
		}

	}

}
