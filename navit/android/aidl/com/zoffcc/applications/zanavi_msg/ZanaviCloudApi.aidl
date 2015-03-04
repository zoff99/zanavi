package com.zoffcc.applications.zanavi_msg;

import com.zoffcc.applications.zanavi_msg.ZListener;

interface ZanaviCloudApi
{
	String getResult( in int id, in int cat, in String data);
	void addListener(ZListener listener);
	void removeListener(ZListener listener);
}
