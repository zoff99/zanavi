#include <string.h>
#include <stdio.h>
#include <glib.h>
#include "debug.h"
#include "linguistics.h"

static const char *special[][3] =
{
/* Capital Diacritics */
/* ¨ Diaresis */
{ "Ä", "A", "AE" },
{ "Ë", "E" },
{ "Ï", "I" },
{ "Ö", "O", "OE" },
{ "Ü", "U", "UE" },
{ "Ÿ", "Y" },
/* ˝ Double Acute Accent */
{ "Ő", "O", "Ö" },
{ "Ű", "U", "Ü" },
/* ´ Acute Accent */
{ "Á", "A" },
{ "Ć", "C" },
{ "É", "E" },
{ "Í", "I" },
{ "Ĺ", "L" },
{ "Ń", "N" },
{ "Ó", "O" },
{ "Ŕ", "R" },
{ "Ś", "S" },
{ "Ú", "U" },
{ "Ý", "Y" },
{ "Ź", "Z" },
/* ˛ Ogonek (nosinė) */
{ "Ą", "A" },
{ "Ę", "E" },
{ "Į", "I" },
{ "Ų", "U" },
/* ˙ Dot */
{ "Ċ", "C" },
{ "Ė", "E" },
{ "Ġ", "G" },
{ "İ", "I" },
{ "Ŀ", "L" },
{ "Ż", "Z" },
/* – Stroke */
{ "Đ", "D", "DJ" }, /* Croatian Dj, not to be confused with the similar-looking Icelandic Eth */
{ "Ħ", "H" },
{ "Ł", "L" },
{ "Ŧ", "T" },
/* ˚ Ring */
{ "Å", "A", "AA" },
{ "Ů", "U" },
/* ˇ Caron (haček, paukščiukas) */
{ "Č", "C" },
{ "Ď", "D" },
{ "Ě", "E" },
{ "Ľ", "L" },
{ "Ň", "N" },
{ "Ř", "R" },
{ "Š", "S" },
{ "Ť", "T" },
{ "Ž", "Z" },
/* / Slash */
{ "Ø", "O", "OE" },
/* ¯ Macron */
{ "Ā", "A", "AA" },
{ "Ē", "E", "EE" },
{ "Ī", "I", "II" },
{ "Ō", "O", "OO" },
{ "Ū", "U", "UU" },
/* ˘ Brevis */
{ "Ă", "A" },
{ "Ĕ", "E" },
{ "Ğ", "G" },
{ "Ĭ", "I" },
{ "Ŏ", "O" },
{ "Ŭ", "U" },
/* ^ Circumflex */
{ "Â", "A" },
{ "Ĉ", "C" },
{ "Ê", "E" },
{ "Ĝ", "G" },
{ "Ĥ", "H" },
{ "Î", "I" },
{ "Ĵ", "J" },
{ "Ô", "O" },
{ "Ŝ", "S" },
{ "Û", "U" },
{ "Ŵ", "W" },
{ "Ŷ", "Y" },
/* ¸ Cedilla */
{ "Ç", "C" },
{ "Ģ", "G", "GJ" },
{ "Ķ", "K", "KJ" },
{ "Ļ", "L", "LJ" },
{ "Ņ", "N", "NJ" },
{ "Ŗ", "R" },
{ "Ş", "S" },
{ "Ţ", "T" },
/* ~ Tilde */
{ "Ã", "A" },
{ "Ĩ", "I" },
{ "Ñ", "N" },
{ "Õ", "O" },
{ "Ũ", "U" },
/* ` Grave */
{ "À", "A" },
{ "È", "E" },
{ "Ì", "I" },
{ "Ò", "O" },
{ "Ù", "U" },
/* ligatures */
{ "Æ", "A", "AE" },
//{ "Ĳ", "IJ" },
{ "Œ", "O", "OE" },
/* special letters */
{ "Ð", "D", "DH" }, /* Icelandic Eth, not to be confused with the similar-looking Croatian Dj */
{ "Ŋ", "N", "NG" },
{ "Þ", "T", "TH" },
/* Small Diacritics */
/* ¨ Diaresis */
{ "ä", "a", "ae" },
{ "ë", "e" },
{ "ï", "i" },
{ "ö", "o", "oe" },
{ "ü", "u", "ue" },
{ "ÿ", "y" },
/* ˝ Double Acute Accent */
{ "ő", "o", "ö" },
{ "ű", "u", "ü" },
/* ´ Acute Accent */
{ "á", "a" },
{ "ć", "c" },
{ "é", "e" },
{ "í", "i" },
{ "ĺ", "l" },
{ "ń", "n" },
{ "ó", "o" },
{ "ŕ", "r" },
{ "ś", "s" },
{ "ú", "u" },
{ "ý", "y" },
{ "ź", "z" },
/* ˛ Ogonek (nosinė) */
{ "ą", "a" },
{ "ę", "e" },
{ "į", "i" },
{ "ų", "u" },
/* ˙ Dot (and dotless i) */
{ "ċ", "c" },
{ "ė", "e" },
{ "ġ", "g" },
{ "ı", "i" },
{ "ŀ", "l" },
{ "ż", "z" },
/* – Stroke */
{ "đ", "d", "dj" },
{ "ħ", "h" },
{ "ł", "l" },
{ "ŧ", "t" },
/* ˚ Ring */
{ "å", "a", "aa" },
{ "ů", "u" },
/* ˇ Caron (haček, paukščiukas) */
{ "č", "c" },
{ "ď", "d" },
{ "ě", "e" },
{ "ľ", "l" },
{ "ň", "n" },
{ "ř", "r" },
{ "š", "s" },
{ "ť", "t" },
{ "ž", "z" },
/* / Slash */
{ "ø", "o", "oe" },
/* Macron */
{ "ā", "a", "aa" },
{ "ē", "e", "ee" },
{ "ī", "i", "ii" },
{ "ō", "o", "oo" },
{ "ū", "u", "uu" },
/* ˘ Brevis */
{ "ă", "a" },
{ "ĕ", "e" },
{ "ğ", "g" },
{ "ĭ", "i" },
{ "ŏ", "o" },
{ "ŭ", "u" },
/* ^ Circumflex */
{ "â", "a" },
{ "ĉ", "c" },
{ "ê", "e" },
{ "ĝ", "g" },
{ "ĥ", "h" },
{ "î", "i" },
{ "ĵ", "j" },
{ "ô", "o" },
{ "ŝ", "s" },
{ "û", "u" },
{ "ŵ", "w" },
{ "ŷ", "y" },
/* ¸ Cedilla */
{ "ç", "c" },
{ "ģ", "g", "gj" },
{ "ķ", "k", "kj" },
{ "ļ", "l", "lj" },
{ "ņ", "n", "nj" },
{ "ŗ", "r" },
{ "ş", "s" },
{ "ţ", "t" },
/* ~ Tilde */
{ "ã", "a" },
{ "ĩ", "i" },
{ "õ", "o" },
{ "ñ", "n" },
{ "ũ", "u" },
/* ` Grave */
{ "à", "a" },
{ "è", "e" },
{ "ì", "i" },
{ "ò", "o" },
{ "ù", "u" },
/* ligatures */
{ "æ", "a", "ae" },
//{ "ĳ", "ij" },
{ "œ", "o", "oe" },
{ "ß", "s", "ss" },
/* special letters */
{ "ð", "d", "dh" },
{ "ŋ", "n", "ng" },
{ "þ", "t", "th" },

/* Cyrillic capital */

{ "Ё", "Е" },
{ "Й", "И" },
{ "І", "I" },
{ "Ї", "I" },
{ "Ў", "У" },
{ "Є", "Е", "Э" },
{ "Ґ", "Г" },
{ "Ѓ", "Г" },
{ "Ђ", "Д" },
{ "Ќ", "К" },
//{"Љ","Л","ЛЬ"},
		//{"Њ","Н","НЬ"},
		{ "Џ", "Ц" },

		/* Cyrillic small */

		{ "ё", "е" },
		{ "й", "и" },
		{ "і", "i" },
		{ "ї", "i" },
		{ "ў", "у" },
		//{"є","е","э"},
		{ "ґ", "г" },
		{ "ѓ", "г" },
		{ "ђ", "д" },
		{ "ќ", "к" },
		//{"љ","л","ль"},
		//{"њ","н","нь"},
		{ "џ", "ц" },

};


static GHashTable *special_hash;

/* Array of strings for case conversion
 * Even elements of array are strings of upper-case letters
 * Odd elements of array are strings of lower-case letters, in the order corresponding to directly preceeding even element.
 * Last element of array should be NULL.
 */
static const char
		*upperlower[] =
				{
						/*Latin diacritics*/
						"ÄËÏÖÜŸŐŰÁĆÉÍĹŃÓŔŚÚÝŹĄĘĮŲĊĖĠİĿŻĐĦŁŦÅŮČĎĚĽŇŘŠŤŽØĀĒĪŌŪĂĔĞĬŎŬÂĈÊĜĤÎĴÔŜÛŴŶÇĢĶĻŅŖŞŢÃĨÑÕŨÀÈÌÒÙÆĲŒÐŊÞ",
						"äëïöüÿőűáćéíĺńóŕśúýźąęįųċėġıŀżđħłŧåůčďěľňřšťžøāēīōūăĕğĭŏŭâĉêĝĥîĵôŝûŵŷçģķļņŗşţãĩõñũàèìòùæĳœðŋþ",
						/*Cyrillic*/
						"АБВГҐЃДЂЕЄЁЖЗИЙКЌЛЉМНЊОПРСТУФХЦЏЧШЩЪЫЬЭЮЯІЇЎ",
						"абвгґѓдђеєёжзийкќлљмнњопрстуфхцџчшщъыьэюяіїў",

						NULL };

static GHashTable *casefold_hash;

struct special_pos
{
	char **variants;
	int n;
	char *s1, *s2;
};

static char**
linguistics_get_special(char *str, char *end)
{
	char buf[11];
	int len;
	if (!end)
	{
		end = g_utf8_find_next_char(str, NULL);
	}
	len = end - str + 1;
	g_strlcpy(buf, str, len > 10 ? 10 : len);
	return g_hash_table_lookup(special_hash, buf);
}

/*
 * @brief Prepare an utf-8 string for case insensitive comparison.
 * @param in String to prepeare.
 * @return String prepared for case insensitive search. Result shoud be g_free()d after use.
 */
char*
linguistics_casefold(char *in)
{
	int len = strlen(in);
	char *src = in;
	//char *ret=g_new(char,len+1);
	char *ret=g_new(char,len+20); // try to fix strange BUG
	char *dest = ret;
	char buf[10];

	// string end
	ret[19] = '\0';
	// fprintf(stderr, "xxxsssssssssssss\n");

	while (*src && ((dest - ret) < len))
	{
		if (*src >= 'A' && *src <= 'Z')
		{
			*dest++ = *src++ - 'A' + 'a';
		}
		else if (!(*src & 128))
		{
			*dest++ = *src++;
		}
		else
		{
			int charlen;
			char *tmp, *folded;
			tmp = g_utf8_find_next_char(src, NULL);
			charlen = tmp - src + 1;
			g_strlcpy(buf, src, charlen > 10 ? 10 : charlen);
			folded = g_hash_table_lookup(casefold_hash, buf);

			if (folded)
			{
				while (*folded && dest - ret < len)
				{
					*dest++ = *folded++;
				}
				src = tmp;
			}
			else
			{
				while (src < tmp && dest - ret < len)
				{
					*dest++ = *src++;
				}
			}
		}
	}

	*dest = 0;
	if (*src)
	{
		dbg(
				0,
				"Casefolded string for '%s' needs extra space, result is trucated to '%s'.\n",
				in, ret);
	}

	return ret;
}

char* linguistics_fold_and_prepare_complete(char *in, int free_input)
{
	char *tmp1;
	char *tmp2;

	if (in == NULL)
	{
		return NULL;
	}

	tmp1 = linguistics_casefold(in);
	if (tmp1)
	{
		tmp2 = linguistics_remove_all_specials(tmp1);
		if (tmp2)
		{
			g_free(tmp1);
			tmp1 = tmp2;
		}
		tmp2 = linguistics_expand_special(tmp1, 1);
		if (tmp2)
		{
			g_free(tmp1);
			tmp1 = tmp2;
		}
	}

	if (free_input)
	{
		if (in)
		{
			g_free(in);
			in = NULL;
		}
	}

	return tmp1;
}




/*
* Verify that "in" points to valid "modified UTF-8" data.
* returns: string of useable utf-8 bytes (dont need to free, original input is just truncated)
*/
char* linguistics_check_utf8_string(char* in)
{
    char* bytes = in;

    if (bytes == NULL)
	{
		return NULL;
    }

    while (*bytes != '\0')
	{
        guint32 utf8 = *(bytes++);
        // Switch on the high four bits.
        switch (utf8 >> 4)
		{
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
			{
                // Bit pattern 0xxx. No need for any extra bytes.
                break;
            }
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0f:
			{
                /*
                 * Bit pattern 10xx or 1111, which are illegal start bytes.
                 * Note: 1111 is valid for normal UTF-8, but not the
                 * modified UTF-8 used here.
                 */
                // LOGW("JNI WARNING: illegal start byte 0x%x\n", utf8);
				*(bytes--) = '\0';
                return in;
            }
            case 0x0e:
			{
                // Bit pattern 1110, so there are two additional bytes.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80)
				{
                    // LOGW("JNI WARNING: illegal continuation byte 0x%x\n", utf8);
					*(bytes-2) = '\0';
					return in;
                }
                // Fall through to take care of the final byte.
            }
            case 0x0c:
            case 0x0d:
			{
                // Bit pattern 110x, so there is one additional byte.
                utf8 = *(bytes++);
                if ((utf8 & 0xc0) != 0x80)
				{
                    // LOGW("JNI WARNING: illegal continuation byte 0x%x\n", utf8);
					*(bytes-2) = '\0';
					return in;
                }
                break;
            }
        }
    }

	return in;
}



/*
 *
 * @brief find match anywhere in str (only complete match, not partial!)
 * both strings should have beend folded (and specials removed) before calling this function
 *
 * @return =0 on match =1 on not matched
 *
 */

int linguistics_compare_anywhere(char *str, char *match)
{
	char *match_1;
	char *match_next;
	char *next;
	char *next_char;
	int found = 1;
	gunichar match_1_unichar;

	if ((str == NULL)||(match == NULL))
	{
		return found;
	}

	match_1 = g_strdup(match);
	next = g_utf8_find_next_char(match_1, NULL);
	if (next == NULL)
	{
		g_free(match_1);
		return found;
	}

	*next = '\0'; // cut off after first utf-8 char

	//dbg(0, "match=%s match_1=%s", match, match_1);

	match_1_unichar = g_utf8_get_char(match_1);

	match_next = g_utf8_strchr(str, -1, match_1_unichar);
	while (match_next)
	{

		//dbg(0, "cmp1: match=%s match_next=%s", match, match_next);

		// see if the utf-8 chars match
		if (!strncmp(match, match_next, strlen(match)))
		{
			found = 0;
			break;
		}
		match_next = g_utf8_strchr(g_utf8_find_next_char(match_next, NULL), -1, match_1_unichar);

		//dbg(0, "cmp2: match=%s match_next=%s", match, match_next);
	}

	g_free(match_1);

	return found;
}


/**
 * @brief Compare two strings using special characters expansion.
 *
 * @param str first string to compare, special characters are expanded.
 * @param match second string to compare, special characters are not expanded.
 * @param partial if = 1 then str string may be shorter than match string, in which case the rest from str isn't analysed.
 * @return  =0 strings matched, =1 not matched. Note this function return value is not fully compatible with strcmp().
 */

int linguistics_compare(char *str, char *match, int partial)
{

	if ((str == NULL)||(match == NULL))
	{
		return 1;
	}

	char *s1 = str, *s2 = match;
	char **sp;
	int ret = 0;
	int got_match;
	GList *l = NULL;
	while (*s1 && *s2)
	{
		int j;
		struct special_pos *spp;
		char *utf_boundary, *tmp;
		/* Skip all matching chars */
		for (j = 0; s1[j] && s1[j] == s2[j]; j++)
		{
			;
		}

		if (!s2[j] && (partial || !s1[j]))
		{
			/* MATCH! */
			ret = 0;
			break;
		}

		/* Find beginning of first mismatching utf-8 encoded char */
		utf_boundary = s1;
		while (*(tmp = g_utf8_find_next_char(utf_boundary, NULL)))
		{
			if (tmp > s1 + j)
			{
				break;
			}
			utf_boundary = tmp;
		}

		/* Push first mismatching char to the list if it's a special char */
		sp = linguistics_get_special(utf_boundary, tmp);

		if (sp)
		{
			spp=g_new(struct special_pos,1);
			spp->variants = sp;
			spp->n = 1;
			spp->s1 = utf_boundary;
			spp->s2 = s2 + (utf_boundary - s1);
			l = g_list_prepend(l, spp);
		}

		/* Try to find a match using special char variants from the list */
		got_match = 0;
		while (l && !got_match)
		{
			spp = l->data;
			s1 = spp->s1;
			s2 = spp->s2;
			while (spp->n < 3 && !got_match)
			{
				char *s = spp->variants[(spp->n)++];
				int len;
				if (!s)
					break;
				len = strlen(s);
				if (!strncmp(s, s2, len))
				{
					s2 += len;
					s1 += strlen(spp->variants[0]);
					got_match = 1;
					break;
				}
			}
			if (spp->n >= 3 || !spp->variants[spp->n])
			{
				/* No matches for current top list element, go to the closest special char towards beginning of the string */
				g_free(spp);
				l = g_list_delete_link(l, l);
			}
		}

		if (!got_match)
		{
			/* NO MATCH
			 * FIXME: If we're going to use this function to sort a string list alphabetically we should use 
			 * utf-aware comparison here.
			 */
			ret = 1;
			break;
		}
	}

	while (l)
	{
		g_free(l->data);
		l = g_list_delete_link(l, l);
	}
	return ret;
}

char *
linguistics_expand_special(char *str, int mode)
{
	char *in = str;
	char *out, *ret;
	int found = 0;

	if (!str)
	{
		return NULL;
	}

	ret = g_strdup(str);
	out = ret;

	if (!mode)
	{
		return ret;
	}

	while (*in)
	{
		char *next = g_utf8_find_next_char(in, NULL);
		int i, len = next - in;
		int match = 0;
		if (len > 1)
		{
			for (i = 0; i < sizeof(special) / sizeof(special[0]); i++)
			{
				const char *search = special[i][0];
				if (!strncmp(in, search, len))
				{
					const char *replace = special[i][mode];
					if (replace)
					{
						int replace_len = strlen(replace);

						if (replace_len > len)
						{
							fprintf(
									stderr,
									"* ERROR !! ERROR !! found %s %s %d %s %d\n",
									in, search, len, replace, replace_len);
						}
						dbg_assert(replace_len <= len);
						if (replace_len > len)
						{
							out += len;
							match = 0;
							break;
						}
						else
						{
							// fprintf(stderr,"  GOOD  !!  GOOD !! found %s %s %d %s %d\n",in,search,len,replace,replace_len);
							strcpy(out, replace);
							out += replace_len;
							match = 1;
							break;
						}
					}
				}
			}
		}

		if (match)
		{
			found = 1;
			in = next;
		}
		else
		{
			while (len-- > 0)
			{
				*out++ = *in++;
			}
		}
	}
	*out++ = '\0';
	if (!found)
	{
		if (ret)
		{
			g_free(ret);
		}
		ret = NULL;
	}
	return ret;
}

char *linguistics_remove_all_spaces(char *str)
{
	char *p;
	char *next = NULL;
	int len = 0;
	char *ret;
	char *out;

	ret = g_strdup(str);
	out = ret;
	p = str;
	while (*p)
	{
		next = g_utf8_find_next_char(p, NULL);
		len = next - p;
		if ((len > 1)||(p[0] != ' '))
		{
			strncpy(out, p, len);
			out = out + len;
		}
		p = next;
	}
	*out = '\0';

	return ret;
}

// special characters
static const char *remove_those = " _-.—,;:*#?=%&$§!@~()[]{}'`´^°|<>\\/\n\r\t\"\'";

char *linguistics_remove_all_specials(char *str)
{
	char *p;
	char *next = NULL;
	int len = 0;
	char *ret;
	char *out;
	int i;
	int found_special;
	int so_rtz = sizeof(remove_those[0]); // should be 1, but lets calculate it anyway
	int so_rt = strlen(remove_those) * so_rtz;

	ret = g_strdup(str);
	out = ret;
	p = str;
	while (*p)
	{
		next = g_utf8_find_next_char(p, NULL);
		len = next - p;
		if (len > 1)
		{
			strncpy(out, p, len);
			out = out + len;
		}
		else
		{
			found_special = 0;
			for (i = 0; i < (so_rt / so_rtz); i++)
			{
				if (p[0] == remove_those[i])
				{
					// special found -> skip it
					found_special = 1;
					break;
				}
			}

			if (found_special == 0)
			{
				strncpy(out, p, len);
				out = out + len;
			}
		}
		p = next;
	}
	*out = '\0';

	return ret;
}


char *
linguistics_next_word(char *str)
{
	char* ret = strtok(str, " -/()\"\',.;_[]{}\\");
	return ret;

	//	int len=strcspn(str, " -/()");
	//	if (!str[len] || !str[len+1])
	//		return NULL;
	//	return str+len+1;

}

int linguistics_search(char *str)
{
	if (!g_strcasecmp(str, "str"))
		return 0;
	if (!g_strcasecmp(str, "str."))
		return 0;
	if (!g_strcasecmp(str, "strasse"))
		return 0;
	if (!g_strcasecmp(str, "weg"))
		return 0;
	return 1;
}

/**
 * @brief Copy one utf8 encoded char to newly allocated buffer.
 *
 * @param s pointer to the beginning of the char.
 * @return  newly allocated nul-terminated string containing one utf8 encoded character.
 */
static char *linguistics_dup_utf8_char(const char *s)
{
	char *ret, *next;
	next = g_utf8_find_next_char(s, NULL);
	ret=g_new(char, next-s+1);
	g_strlcpy(ret, s, next - s + 1);
	return ret;
}

void linguistics_init(void)
{
	int i;
	special_hash = g_hash_table_new(g_str_hash, g_str_equal);
	casefold_hash = g_hash_table_new(g_str_hash, g_str_equal);

	for (i = 0; i < sizeof(special) / sizeof(special[0]); i++)
	{
		g_hash_table_insert(special_hash, (gpointer) special[i][0], special[i]);
	}

	for (i = 0; upperlower[i]; i += 2)
	{
		int j, k;
		for (j = 0, k = 0; upperlower[i][j] && upperlower[i + 1][k];)
		{
			char *s1 = linguistics_dup_utf8_char(upperlower[i] + j);
			char *s2 = linguistics_dup_utf8_char(upperlower[i + 1] + k);
			g_hash_table_insert(casefold_hash, s1, s2);
			j += strlen(s1);
			k += strlen(s2);
		}
	}
}

