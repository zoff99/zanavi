/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2008 Navit Team
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

#include <stdlib.h>
#include <glib.h>
#include "config.h"
#include "item.h"
#include "debug.h"
#include "plugin.h"
#include "android.h"
#include "speech.h"

struct speech_priv {
	jclass NavitSpeechClass;
	jobject NavitSpeech;
	jmethodID NavitSpeech_say;
	int flags;
};


jclass NavitClass4 = NULL;
jmethodID Navit_get_speech;

static int find_static_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	//DBG dbg(0,"EEnter\n");
	*ret = (*jnienv2)->GetStaticMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		//DBG dbg(0, "Failed to get static Method %s with signature %s\n", name, args);
		return 0;
	}
	return 1;
}

static int
speech_android_say(struct speech_priv *this, const char *text)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	char *str=g_strdup(text);
	jstring string;
	int i;

/*
	if (this->flags & 2)
	{
		for (i = 0 ; i < strlen(str) ; i++)
		{
			if (str[i] == 0xc3 && str[i+1] == 0x84) {
				str[i]='A';
				str[i+1]='e';
			}
			if (str[i] == 0xc3 && str[i+1] == 0x96) {
				str[i]='O';
				str[i+1]='e';
			}
			if (str[i] == 0xc3 && str[i+1] == 0x9c) {
				str[i]='U';
				str[i+1]='e';
			}
			if (str[i] == 0xc3 && str[i+1] == 0xa4) {
				str[i]='a';
				str[i+1]='e';
			}
			if (str[i] == 0xc3 && str[i+1] == 0xb6) {
				str[i]='o';
				str[i+1]='e';
			}
			if (str[i] == 0xc3 && str[i+1] == 0xbc) {
				str[i]='u';
				str[i+1]='e';
			}
			if (str[i] == 0xc3 && str[i+1] == 0x9f) {
				str[i]='s';
				str[i+1]='s';
			}
		}
	}
*/

	string = (*jnienv2)->NewStringUTF(jnienv2, str);
	// dbg(0,"enter %s\n",str);
    (*jnienv2)->CallVoidMethod(jnienv2, this->NavitSpeech, this->NavitSpeech_say, string);
    (*jnienv2)->DeleteLocalRef(jnienv2, string);

	g_free(str);

	return 1;
}

static void 
speech_android_destroy(struct speech_priv *this)
{
	g_free(this);
}

static struct speech_methods speech_android_meth =
{
	speech_android_destroy,
	speech_android_say,
};

static int
speech_android_init(struct speech_priv *ret)
{
	dbg(0,"EEnter\n");

	int thread_id = gettid();
	dbg(0, "THREAD ID=%d\n", thread_id);

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jmethodID cid;
	char *class="com/zoffcc/applications/zanavi/NavitSpeech2";

	// obsolete ----------
	//if (ret->flags & 1)
	//{
	//	class="com/zoffcc/applications/zanavi/NavitSpeech";
	//}
	// obsolete ----------

	if (!android_find_class_global(class, &ret->NavitSpeechClass))
	{
		dbg(0,"No class found\n");
		return 0;
	}

	if (!android_find_method(ret->NavitSpeechClass, "say", "(Ljava/lang/String;)V", &ret->NavitSpeech_say))
	{
		return 0;
	}





	// --------------- Init the new Speech Object here -----------------
	// --------------- Init the new Speech Object here -----------------
	// --------------- Init the new Speech Object here -----------------
	dbg(0,"Init the new Speech Object here\n");

	if (NavitClass4 == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/Navit", &NavitClass4))
		{
			NavitClass4 = NULL;
			return 0;
		}
	}

	if (!find_static_method(NavitClass4, "get_speech_object", "()Lcom/zoffcc/applications/zanavi/NavitSpeech2;", &Navit_get_speech))
		return 0;


	/// --old-- ret->NavitSpeech=(*jnienv2)->NewObject(jnienv2, ret->NavitSpeechClass, cid, android_activity);

	/// --new--
	ret->NavitSpeech = (*jnienv2)->CallStaticObjectMethod(jnienv2, NavitClass4, Navit_get_speech);
	/// --new--

	// --------------- Init the new Speech Object here -----------------
	// --------------- Init the new Speech Object here -----------------
	// --------------- Init the new Speech Object here -----------------



	if (!ret->NavitSpeech)
	{
		return 0;
	}
	if (ret->NavitSpeech)
	{
		ret->NavitSpeech = (*jnienv2)->NewGlobalRef(jnienv2, ret->NavitSpeech);
	}
	return 1;
}

static struct speech_priv *
speech_android_new(struct speech_methods *meth, struct attr **attrs, struct attr *parent)
{
	dbg(0,"EEnter\n");
	struct speech_priv *this;
	struct attr *flags;
	*meth=speech_android_meth;
	this=g_new0(struct speech_priv,1);

	if (!speech_android_init(this))
	{
		g_free(this);
		this=NULL;
	}

	if (android_version < 4)
	{
		this->flags=3;
	}

	if ((flags = attr_search(attrs, NULL, attr_flags)))
	{
		this->flags=flags->u.num;
	}
	
	return this;
}


void
plugin_init(void)
{
	plugin_register_speech_type("android", speech_android_new);
}

