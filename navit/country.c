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

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <glib.h>
#include "debug.h"
#include "item.h"
#include "country.h"
#include "search.h"
#include "navit_nls.h"



struct country {
	int id;
	char *car;
	char *iso2;
	char *iso3;
	char *name;
};

#include "country_countrytable.h"




struct country_search {
	struct attr search;
	int len;
	int partial;
	struct item item;
	int count;
	struct country *country;
	enum attr_type attr_next;
};

static int
country_attr_get(void *priv_data, enum attr_type attr_type, struct attr *attr)
{
        struct country_search *this_=priv_data;
	struct country *country=this_->country;

        attr->type=attr_type;
        switch (attr_type) {
        case attr_any:
                while (this_->attr_next != attr_none) {
                        if (country_attr_get(this_, this_->attr_next, attr))
                                return 1;
                }
                return 0;
        case attr_label:
		attr->u.str=gettext(country->name);
		this_->attr_next=attr_country_id;
		return 1;
	case attr_country_id:
		attr->u.num=country->id;
		this_->attr_next=country->car ? attr_country_car : attr_country_iso2;
		return 1;
        case attr_country_car:
		attr->u.str=country->car;
		this_->attr_next=attr_country_iso2;
		return attr->u.str ? 1 : 0;
        case attr_country_iso2:
		attr->u.str=country->iso2;
		this_->attr_next=attr_country_iso3;
		return 1;
        case attr_country_iso3:
		attr->u.str=country->iso3;
		this_->attr_next=attr_country_name;
		return 1;
        case attr_country_name:
		attr->u.str=gettext(country->name);
		this_->attr_next=attr_none;
		return 1;
 	default:
                return 0;
        }
}



struct item_methods country_meth = {
	NULL, 			/* coord_rewind */
	NULL, 			/* coord_get */
	NULL, 			/* attr_rewind */
	country_attr_get, 	/* attr_get */
};

struct country_search *
country_search_new(struct attr *search, int partial)
{
	struct country_search *ret=g_new(struct country_search, 1);
	ret->search=*search;
	if (search->type != attr_country_id)
		ret->len=strlen(search->u.str);
	else
		ret->len=0;
	ret->partial=partial;
	ret->count=0;

	ret->item.type=type_country_label;
	ret->item.id_hi=0;		
	ret->item.map=NULL;
	ret->item.meth=&country_meth;
	ret->item.priv_data=ret;

	return ret;
}

char* str_tolower(char *name)
{
	int i;
	char *str;
	str = g_strdup(name);
	for(i = 0; str[i]; i++)
	{
		str[i] = tolower(str[i]);
	}
	return str;
}

static int
match(struct country_search *this_, enum attr_type type, const char *name)
{
	int ret;
	char *s1 = NULL;
	char *s2 = NULL; 

	if (!name)
		return 0;

	if (!this_->search.u.str)
	{
		return 0;
	}

	if (this_->search.type != type && this_->search.type != attr_country_all)
		return 0;


	//fprintf(stderr, "1 name=%s str=%s\n", name, this_->search.u.str);

	//s1=linguistics_casefold(this_->search.u.str);
	s1=str_tolower(this_->search.u.str);

	//fprintf(stderr, "2 name=%s str=%s\n", name, this_->search.u.str);
	//fprintf(stderr, "3 s1=%s\n", s1);

	//s2=linguistics_casefold(name);
	s2=str_tolower(name);

	//fprintf(stderr, "4 name=%s str=%s\n", name, this_->search.u.str);
	//fprintf(stderr, "5 s2=%s\n", s2);

	//if ((s1 == NULL)||(s2 == NULL))
	//{
	//	// ok i give up, i cant find the damn bug. so here is a stupid!! stupid!! workaround
	//	fprintf(stderr, "2 name=%s str=%s\n", name, this_->search.u.str);
	//	fprintf(stderr, "3 s1=%s\n", s1);
	//	fprintf(stderr, "5 s2=%s\n", s2);
	//	return 0;
	//}
	//else
	//{
		ret=linguistics_compare(s2,s1,this_->partial)==0;
	//}
	//fprintf(stderr, "6 s1=%s\n", s1);
	//fprintf(stderr, "7 s2=%s\n", s2);

	if (s1)
	{
		g_free(s1);
	}

	if (s2)
	{
		g_free(s2);
	}

	return ret;
}


struct item *
country_search_get_item(struct country_search *this_)
{
	for (;;) {
		if (this_->count >= sizeof(country)/sizeof(struct country))
			return NULL;
		this_->country=&country[this_->count++];
		if ((this_->search.type == attr_country_id && this_->search.u.num == this_->country->id) ||
                    match(this_, attr_country_iso3, this_->country->iso3) ||
		    match(this_, attr_country_iso2, this_->country->iso2) ||
		    match(this_, attr_country_car, this_->country->car) ||
		    match(this_, attr_country_name, gettext(this_->country->name))) {
			this_->item.id_lo=this_->country->id;
			return &this_->item;
		}
	}
}

static struct attr country_default_attr;
static char iso2[3];

struct attr *
country_default(void)
{
	char *lang;
	if (country_default_attr.u.str)
		return &country_default_attr;
	lang=getenv("LANG");
	if (!lang || strlen(lang) < 5)
		return NULL;
	strncpy(iso2, lang+3, 2);
	country_default_attr.type=attr_country_iso2;
	country_default_attr.u.str=iso2;
	return &country_default_attr;
}

void
country_search_destroy(struct country_search *this_)
{
	g_free(this_);
}
