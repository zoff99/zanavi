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

#include <glib.h>
#include <string.h>
#include "item.h"
#include "debug.h"
#include "callback.h"
#include "navit.h"

struct callback_list *
callback_list_new(char *name)
{
	struct callback_list *ret=g_new0(struct callback_list, 1);

	if (name != NULL)
	{
		snprintf(ret->cb_name, 398, "%s", name);
		ret->cb_name[397] = '\0';
		ret->cb_name[398] = '\0';
		ret->cb_name[399] = '\0';
	}
	else
	{
		snprintf(ret->cb_name, 398, "*unknown*");
		// ret->cb_name[397] = '\0';
		// ret->cb_name[398] = '\0';
		// ret->cb_name[399] = '\0';
	}

	//dbg(0, "cbl:new:cbl=%p, name=%s, glob=%p\n", ret, name, global_all_cbs);
	global_all_cbs = g_list_append(global_all_cbs, ret);

	return ret;
}

void callback_dump_callbacks()
{

#ifndef NAVIT_DEBUG_BAREMETAL

	struct callback_list *cl1;
	struct callback *cb;
	GList *cbi;
	GList *all_cbl;

	if (!global_all_cbs)
	{
		return;
	}

	all_cbl = global_all_cbs;

	dbg(0,"cbl:=================================================\n");
	dbg(0,"cbl:=================================================\n");

	while (all_cbl)
	{
		if ((all_cbl) && (all_cbl->data))
		{
			cl1 = all_cbl->data;
			dbg(0,"\n");
			dbg(0,"cbl:%s(%p)\n", cl1->cb_name, cl1);

			if ((cl1) && (cl1->list))
			{
				cbi = cl1->list;
				while (cbi)
				{
					if (cbi == NULL)
					{
						break;
					}

					cb = cbi->data;
					if (cb)
					{
						dbg(0,"   cb:%s:%s(%p) [f:%p type=%s]\n", cb->setup_func_name, cb->func_name, cb, cb->func, attr_to_name(cb->type));
					}

					cbi = g_list_next(cbi);
				}
			}
		}

		all_cbl = g_list_next(all_cbl);
	}

	dbg(0,"cbl:=================================================\n");
	dbg(0,"cbl:=================================================\n");

#endif

}

struct callback *
callback_new_attr(void (*func)(void), enum attr_type type, int pcount, void **p)
{
	struct callback *ret;
	int i;

	ret=g_malloc0(sizeof(struct callback)+pcount*sizeof(void *));
	ret->func=func;
	ret->pcount=pcount;
	ret->type=type;
	for (i = 0 ; i < pcount ; i++)
	{
		ret->p[i]=p[i];
	}

	ret->func_name[0] = 'e';
	ret->func_name[1] = 'm';
	ret->func_name[2] = 'p';
	ret->func_name[3] = 't';
	ret->func_name[4] = 'y';
	ret->func_name[5] = '\0';

	ret->setup_func_name[0] = 'e';
	ret->setup_func_name[1] = 'm';
	ret->setup_func_name[2] = 'p';
	ret->setup_func_name[3] = 't';
	ret->setup_func_name[4] = 'y';
	ret->setup_func_name[5] = '\0';

	return ret;
}

static void callback_print_names(struct callback *cb, const char *cb_func)
{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	if (cb == NULL)
	{
		dbg(0,"%p CB_f=%s:CB=NULL!\n", cb, cb_func);
	}
	else
	{
		dbg(0,"%p CB_f=%s, parent=%s, func=%s\n", cb, cb_func, cb->setup_func_name, cb->func_name);
	}
#endif
}

static void callback_print_names2(struct callback *cb, const char *cb_func, const char *module, const int mlen,const char *function)
{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	if (cb == NULL)
	{
		dbg(0,"%p CB_f=%s:CB=NULL! file=%s line=%d func=%s\n", cb, cb_func, module, mlen, function);
	}
	else
	{
		dbg(0,"%p CB_f=%s, parent=%s, func=%s -- file=%s line=%d func=%s\n", cb, cb_func, cb->setup_func_name, cb->func_name, module, mlen, function);
	}
#endif
}

void callback_add_names(struct callback *cb, const char *parent_name, const char *func_name)
{
	if (cb == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_add_names:CB=NULL!\n");
#endif
		return;
	}

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	dbg(0,"%p CB_I=%s, %s\n", cb, parent_name, func_name);
#endif

	snprintf(cb->func_name, 398, "%s", func_name);
	cb->func_name[397] = '\0';
	cb->func_name[398] = '\0';
	cb->func_name[399] = '\0';

	snprintf(cb->setup_func_name, 398, "%s", parent_name);
	cb->setup_func_name[397] = '\0';
	cb->setup_func_name[398] = '\0';
	cb->setup_func_name[399] = '\0';

}

struct callback *
callback_new_attr_args(const char *module, const int mlen,const char *function, void (*func)(void), enum attr_type type, int count, ...)
{
	int i;
	void **p=g_alloca(sizeof(void*)*count);
	va_list ap;
	va_start(ap, count);
	for (i = 0 ; i < count ; i++)
	{
		p[i]=va_arg(ap, void *);
	}

	va_end(ap);

	struct callback *ret = callback_new_attr(func, type, count, p);
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	dbg(0,"%p callback_new_attr_%d -- file=%s line=%d func=%s type=%s\n", ret, count, module, mlen, function, attr_to_name(type));
#endif
	return ret;
}

struct callback *
callback_new(void (*func)(void), int pcount, void **p)
{
	return callback_new_attr(func, attr_none, pcount, p);
}

struct callback *
callback_new_args(const char *module, const int mlen,const char *function, void (*func)(void), int count, ...)
{
	int i;
	void **p=g_alloca(sizeof(void*)*count);
	va_list ap;
	va_start(ap, count);
	for (i = 0 ; i < count ; i++)
	{
		p[i]=va_arg(ap, void *);
	}
	va_end(ap);

	struct callback *ret = callback_new(func, count, p);

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	dbg(0,"%p callback_new_%d -- file=%s line=%d func=%s\n", ret, count, module, mlen, function);
#endif
	return ret;
}

void
callback_destroy_real(const char *module, const int mlen,const char *function, struct callback *cb)
{
	if (cb == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"%p callback_destroy_real:CB=NULL!! -- file=%s line=%d func=%s\n", cb, module, mlen, function);
#endif
		return;
	}

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	callback_print_names2(cb, "callback_destroy", module, mlen, function);
#endif

	g_free(cb);
}

void
callback_set_arg(struct callback *cb, int arg, void *p)
{
	if (arg < 0 || arg > cb->pcount)
	{
		return;
	}
	cb->p[arg]=p;
}

void callback_list_add_2(const char *module, const int mlen,const char *function, struct callback_list *l, struct callback *cb)
{
	if (cb == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_list_add_2:CB=NULL!!\n");
#endif
		return;
	}

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	dbg(0,"callback_list_add_2:cbl=%p cb=%p file=%s line=%d func=%s\n", l, cb, module, mlen, function);
#endif

	l->list=g_list_prepend(l->list, cb);
}


void
callback_list_add_internal(struct callback_list *l, struct callback *cb)
{
	if (cb == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_list_add:CB=NULL!!\n");
#endif
		return;
	}

	l->list=g_list_prepend(l->list, cb);
}



struct callback *
callback_list_add_new(struct callback_list *l, void (*func)(void), int pcount, void **p)
{
	struct callback *ret;

	ret=callback_new(func, pcount, p);	
	callback_list_add_internal(l, ret);
	return ret;
}

void callback_list_remove_2(const char *module, const int mlen,const char *function, struct callback_list *l, struct callback *cb)
{
	//dbg(0,"callback_list_remove_2:******************************************\n");

	if (l == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_list_remove_2:CBL=NULL!!\n");
#endif
		return;
	}

	if (cb == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_list_remove_2:CB=NULL!!\n");
#endif
		return;
	}

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	dbg(0,"callback_list_remove_2:cbl=%p cb=%p file=%s line=%d func=%s\n", l, cb, module, mlen, function);
#endif

	l->list = g_list_remove(l->list, cb);

	//dbg(0,"callback_list_remove_2:******************************************RRRRRRRRRR\n");
}


void
callback_list_remove_internal(struct callback_list *l, struct callback *cb)
{
	l->list = g_list_remove(l->list, cb);
}


void
callback_list_remove_destroy(struct callback_list *l, struct callback *cb)
{
	if (l == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_list_remove_destroy:CBL=NULL!!\n");
#endif
		return;
	}

	if (cb == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_list_remove_destroy:CB=NULL!!\n");
#endif
		return;
	}

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	callback_print_names(cb, "callback_list_remove_destroy");
#endif

	callback_list_remove_internal(l, cb);
	g_free(cb);
}

void
callback_call(struct callback *cb, int pcount, void **p)
{
	int i;
	void *pf[8];

#ifndef NAVIT_DEBUG_BAREMETAL

	if (! cb)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_call:CB=NULL!!\n");
#endif
		return;
	}

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	callback_print_names(cb, "callback_call");
#endif

	if (cb->pcount + pcount <= 8)
	{
		//dbg(1,"cb->pcount=%d\n", cb->pcount);
		if (cb->pcount && cb->p)
		{
			//dbg(1,"cb->p[0]=%p\n", cb->p[0]);
		}
		//dbg(1,"pcount=%d\n", pcount);
		if (pcount)
		{
	       	dbg_assert(p!=NULL);
			//dbg(1,"p[0]=%p\n", p[0]);
		}

		for (i = 0 ; i < cb->pcount ; i++) 
			pf[i]=cb->p[i];

		for (i = 0 ; i < pcount ; i++)
			pf[i+cb->pcount]=p[i];

		switch (cb->pcount+pcount)
		{
		case 8:
			cb->func(pf[0],pf[1],pf[2],pf[3],pf[4],pf[5],pf[6],pf[7]);
			break;
		case 7:
			cb->func(pf[0],pf[1],pf[2],pf[3],pf[4],pf[5],pf[6]);
			break;
		case 6:
			cb->func(pf[0],pf[1],pf[2],pf[3],pf[4],pf[5]);
			break;
		case 5:
			cb->func(pf[0],pf[1],pf[2],pf[3],pf[4]);
			break;
		case 4:
			cb->func(pf[0],pf[1],pf[2],pf[3]);
			break;
		case 3:
			cb->func(pf[0],pf[1],pf[2]);
			break;
		case 2:
			cb->func(pf[0],pf[1]);
			break;
		case 1:
			cb->func(pf[0]);
			break;
		case 0:
			cb->func();
			break;
		}
	}
	else
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"too many parameters for callback (%d+%d)\n", cb->pcount, pcount);
#endif
	}

#endif
}

void
callback_call_args_real(const char *module, const int mlen,const char *function, struct callback *cb, int count, ...)
{

#ifndef NAVIT_DEBUG_BAREMETAL

	if (cb == NULL)
	{
		// callback struct is NULL
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"callback_call:CB=NULL! file=%s line=%d func=%s\n", module, mlen, function);
#endif
		return;
	}

#ifdef NAVIT_CALLBACK_DEBUG_PRINT
	callback_print_names2(cb, "callback_call", module, mlen, function);
#endif

	int i;
	void **p=g_alloca(sizeof(void*)*count);
	va_list ap;
	va_start(ap, count);

	for (i = 0 ; i < count ; i++)
	{
		p[i]=va_arg(ap, void *);
	}

	va_end(ap);
	callback_call(cb, count, p);

#endif
}


void
callback_list_call_attr(struct callback_list *l, enum attr_type type, int pcount, void **p)
{

#ifndef NAVIT_DEBUG_BAREMETAL

	GList *cbi;
	struct callback *cb;

	if (!l)
	{
		return;
	}

	cbi=l->list;
	while (cbi)
	{
		cb=cbi->data;
		if (type == attr_any || cb->type == attr_any || cb->type == type)
		{
			callback_call(cb, pcount, p);
		}
		cbi=g_list_next(cbi);
	}

#endif	
}

void
callback_list_call_attr_args(const char *module, const int mlen,const char *function, struct callback_list *cbl, enum attr_type type, int count, ...)
{

#ifndef NAVIT_DEBUG_BAREMETAL

	int i;
	void **p=g_alloca(sizeof(void*)*count);
	va_list ap;
	va_start(ap, count);

	for (i = 0 ; i < count ; i++)
		p[i]=va_arg(ap, void *);

	va_end(ap);

	if (cbl == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"%p callback_list_call_attr_args:CBL=NULL! file=%s line=%d func=%s type=%s\n", cbl, module, mlen, function, attr_to_name(type));
#endif
		return;
	}
	else
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"%p callback_list_call_attr_args:file=%s line=%d func=%s type=%s\n", cbl, module, mlen, function, attr_to_name(type));
#endif
	}

	callback_list_call_attr(cbl, type, count, p);

#endif
}

void
callback_list_call(struct callback_list *l, int pcount, void **p)
{
#ifndef NAVIT_DEBUG_BAREMETAL
	callback_list_call_attr(l, attr_any, pcount, p);
#endif
}

void
callback_list_call_args(const char *module, const int mlen,const char *function, struct callback_list *cbl, int count, ...)
{

#ifndef NAVIT_DEBUG_BAREMETAL

	if (cbl == NULL)
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"%p callback_list_call_args:CBL=NULL! file=%s line=%d func=%s\n", cbl, module, mlen, function);
#endif
		return;
	}
	else
	{
#ifdef NAVIT_CALLBACK_DEBUG_PRINT
		dbg(0,"%p callback_list_call_args:file=%s line=%d func=%s\n", cbl, module, mlen, function);
#endif
	}

	int i;
	void **p=g_alloca(sizeof(void*)*count);
	va_list ap;
	va_start(ap, count);

	for (i = 0 ; i < count ; i++)
	{
		p[i]=va_arg(ap, void *);
	}

	va_end(ap);

	callback_list_call(cbl, count, p);

#endif
}

void 
callback_list_destroy(struct callback_list *l)
{
	GList *cbi;

	if (!l)
	{
		return;
	}

	//dbg(0, "cbl:destroy:cbl=%p, name=%s, glob=%p\n", l, l->cb_name, global_all_cbs);
	global_all_cbs = g_list_remove(global_all_cbs, l);

	cbi=l->list;

	if (!cbi)
	{
		g_free(l);
		return;
	}

	while (cbi)
	{
		if (cbi == NULL)
		{
			break;
		}

		if (cbi->data)
		{
			g_free(cbi->data);
		}
		cbi=g_list_next(cbi);
	}

	g_list_free(l->list);

	g_free(l);

}


