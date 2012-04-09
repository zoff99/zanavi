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

static struct country country[]= {
  { 20,	"AND",	"AD", "AND", /* 020 */ "Andorra"},
  {784,	"UAE",	"AE", "ARE", /* 784 */ "United Arab Emirates"},
  {  4,	"AFG",	"AF", "AFG", /* 004 */ "Afghanistan"},
  { 28,	"AG",	"AG", "ATG", /* 028 */ "Antigua and Barbuda"},
  {660,	NULL,	"AI", "AIA", /* 660 */ "Anguilla"},
  {  8,	"AL",	"AL", "ALB", /* 008 */ "Albania"},
  { 51,	"ARM",	"AM", "ARM", /* 051 */ "Armenia"},
  {530,	"NA",	"AN", "ANT", /* 530 */ "Netherlands Antilles"},
  { 24,	"ANG",	"AO", "AGO", /* 024 */ "Angola"},
  { 10,	NULL,	"AQ", "ATA", /* 010 */ "Antarctica"},
  { 32,	"RA",	"AR", "ARG", /* 032 */ "Argentina"},
  { 16,	NULL,	"AS", "ASM", /* 016 */ "American Samoa"},
  { 40,	"A",	"AT", "AUT", /* 040 */ "Austria"},
  { 36,	"AUS",	"AU", "AUS", /* 036 */ "Australia"},
  {533,	"ARU",	"AW", "ABW", /* 533 */ "Aruba"},
  {248,	"AX",	"AX", "ALA", /* 248 */ "Aland Islands"},
  { 31,	"AZ",	"AZ", "AZE", /* 031 */ "Azerbaijan"},
  { 70,	"BiH",	"BA", "BIH", /* 070 */ "Bosnia and Herzegovina"},
  { 52,	"BDS",	"BB", "BRB", /* 052 */ "Barbados"},
  { 50,	"BD",	"BD", "BGD", /* 050 */ "Bangladesh"},
  { 56,	"B",	"BE", "BEL", /* 056 */ "Belgium"},
  {854,	"BF",	"BF", "BFA", /* 854 */ "Burkina Faso"},
  {100,	"BG",	"BG", "BGR", /* 100 */ "Bulgaria"},
  { 48,	"BRN",	"BH", "BHR", /* 048 */ "Bahrain"},
  {108,	"RU",	"BI", "BDI", /* 108 */ "Burundi"},
  {204,	"BJ",	"BJ", "BEN", /* 204 */ "Benin"},
  {652,	NULL,	"BL", "BLM", /* 652 */ "Saint Barthelemy"},
  { 60,	NULL,	"BM", "BMU", /* 060 */ "Bermuda"},
  { 96,	"BRU",	"BN", "BRN", /* 096 */ "Brunei Darussalam"},
  { 68,	"BOL",	"BO", "BOL", /* 068 */ "Bolivia"},
  { 76,	"BR",	"BR", "BRA", /* 076 */ "Brazil"},
  { 44,	"BS",	"BS", "BHS", /* 044 */ "Bahamas"},
  { 64,	"BHT",	"BT", "BTN", /* 064 */ "Bhutan"},
  { 74,	NULL,	"BV", "BVT", /* 074 */ "Bouvet Island"},
  { 72,	"RB",	"BW", "BWA", /* 072 */ "Botswana"},
  {112,	"BY",	"BY", "BLR", /* 112 */ "Belarus"},
  { 84,	"BZ",	"BZ", "BLZ", /* 084 */ "Belize"},
  {124,	"CDN",	"CA", "CAN", /* 124 */ "Canada"},
  {166,	NULL,	"CC", "CCK", /* 166 */ "Cocos (Keeling) Islands"},
  {180,	"CGO",	"CD", "COD", /* 180 */ "Congo, Democratic Republic of the"},
  {140,	"RCA",	"CF", "CAF", /* 140 */ "Central African Republic"},
  {178,	NULL,	"CG", "COG", /* 178 */ "Congo"},
  {756,	"CH",	"CH", "CHE", /* 756 */ "Switzerland"},
  {384,	"CI",	"CI", "CIV", /* 384 */ "Cote d'Ivoire"},
  {184,	NULL,	"CK", "COK", /* 184 */ "Cook Islands"},
  {152,	"RCH",	"CL", "CHL", /* 152 */ "Chile"},
  {120,	"CAM",	"CM", "CMR", /* 120 */ "Cameroon"},
  {156,	"RC",	"CN", "CHN", /* 156 */ "China"},
  {170,	"CO",	"CO", "COL", /* 170 */ "Colombia"},
  {188,	"CR",	"CR", "CRI", /* 188 */ "Costa Rica"},
  {192,	"C",	"CU", "CUB", /* 192 */ "Cuba"},
  {132,	"CV",	"CV", "CPV", /* 132 */ "Cape Verde"},
  {162,	NULL,	"CX", "CXR", /* 162 */ "Christmas Island"},
  {196,	"CY",	"CY", "CYP", /* 196 */ "Cyprus"},
  {203,	"CZ",	"CZ", "CZE", /* 203 */ "Czech Republic"},
  {276,	"D",	"DE", "DEU", /* 276 */ "Germany"},
  {262,	"DJI",	"DJ", "DJI", /* 262 */ "Djibouti"},
  {208,	"DK",	"DK", "DNK", /* 208 */ "Denmark"},
  {212,	"WD",	"DM", "DMA", /* 212 */ "Dominica"},
  {214,	"DOM",	"DO", "DOM", /* 214 */ "Dominican Republic"},
  { 12,	"DZ",	"DZ", "DZA", /* 012 */ "Algeria"},
  {218,	"EC",	"EC", "ECU", /* 218 */ "Ecuador"},
  {233,	"EST",	"EE", "EST", /* 233 */ "Estonia"},
  {818,	"ET",	"EG", "EGY", /* 818 */ "Egypt"},
  {732,	"WSA",	"EH", "ESH", /* 732 */ "Western Sahara"},
  {232,	"ER",	"ER", "ERI", /* 232 */ "Eritrea"},
  {724,	"E",	"ES", "ESP", /* 724 */ "Spain"},
  {231,	"ETH",	"ET", "ETH", /* 231 */ "Ethiopia"},
  {246,	"FIN",	"FI", "FIN", /* 246 */ "Finland"},
  {242,	"FJI",	"FJ", "FJI", /* 242 */ "Fiji"},
  {238,	NULL,	"FK", "FLK", /* 238 */ "Falkland Islands (Malvinas)"},
  {583,	"FSM",	"FM", "FSM", /* 583 */ "Micronesia, Federated States of"},
  {234,	"FO",	"FO", "FRO", /* 234 */ "Faroe Islands"},
  {250,	"F",	"FR", "FRA", /* 250 */ "France"},
  {266,	"G",	"GA", "GAB", /* 266 */ "Gabon"},
  {826,	"GB",	"GB", "GBR", /* 826 */ "United Kingdom"},
  {308,	"WG",	"GD", "GRD", /* 308 */ "Grenada"},
  {268,	"GE",	"GE", "GEO", /* 268 */ "Georgia"},
  {254,	NULL,	"GF", "GUF", /* 254 */ "French Guiana"},
  {831,	NULL,	"GG", "GGY", /* 831 */ "Guernsey"},
  {288,	"GH",	"GH", "GHA", /* 288 */ "Ghana"},
  {292,	"GBZ",	"GI", "GIB", /* 292 */ "Gibraltar"},
  {304,	"KN",	"GL", "GRL", /* 304 */ "Greenland"},
  {270,	"WAG",	"GM", "GMB", /* 270 */ "Gambia"},
  {324,	"RG",	"GN", "GIN", /* 324 */ "Guinea"},
  {312,	NULL,	"GP", "GLP", /* 312 */ "Guadeloupe"},
  {226,	"GQ",	"GQ", "GNQ", /* 226 */ "Equatorial Guinea"},
  {300,	"GR",	"GR", "GRC", /* 300 */ "Greece"},
  {239,	NULL,	"GS", "SGS", /* 239 */ "South Georgia and the South Sandwich Islands"},
  {320,	"GCA",	"GT", "GTM", /* 320 */ "Guatemala"},
  {316,	NULL,	"GU", "GUM", /* 316 */ "Guam"},
  {624,	"GUB",	"GW", "GNB", /* 624 */ "Guinea-Bissau"},
  {328,	"GUY",	"GY", "GUY", /* 328 */ "Guyana"},
  {344,	"HK",	"HK", "HKG", /* 344 */ "Hong Kong"},
  {334,	NULL,	"HM", "HMD", /* 334 */ "Heard Island and McDonald Islands"},
  {340,	"HN",	"HN", "HND", /* 340 */ "Honduras"},
  {191,	"HR",	"HR", "HRV", /* 191 */ "Croatia"},
  {332,	"RH",	"HT", "HTI", /* 332 */ "Haiti"},
  {348,	"H",	"HU", "HUN", /* 348 */ "Hungary"},
  {360,	"RI",	"ID", "IDN", /* 360 */ "Indonesia"},
  {372,	"IRL",	"IE", "IRL", /* 372 */ "Ireland"},
  {376,	"IL",	"IL", "ISR", /* 376 */ "Israel"},
  {833,	NULL,	"IM", "IMN", /* 833 */ "Isle of Man"},
  {356,	"IND",	"IN", "IND", /* 356 */ "India"},
  { 86,	NULL,	"IO", "IOT", /* 086 */ "British Indian Ocean Territory"},
  {368,	"IRQ",	"IQ", "IRQ", /* 368 */ "Iraq"},
  {364,	"IR",	"IR", "IRN", /* 364 */ "Iran, Islamic Republic of"},
  {352,	"IS",	"IS", "ISL", /* 352 */ "Iceland"},
  {380,	"I",	"IT", "ITA", /* 380 */ "Italy"},
  {832,	NULL,	"JE", "JEY", /* 832 */ "Jersey"},
  {388,	"JA",	"JM", "JAM", /* 388 */ "Jamaica"},
  {400,	"JOR",	"JO", "JOR", /* 400 */ "Jordan"},
  {392,	"J",	"JP", "JPN", /* 392 */ "Japan"},
  {404,	"EAK",	"KE", "KEN", /* 404 */ "Kenya"},
  {417,	"KS",	"KG", "KGZ", /* 417 */ "Kyrgyzstan"},
  {116,	"K",	"KH", "KHM", /* 116 */ "Cambodia"},
  {296,	"KIR",	"KI", "KIR", /* 296 */ "Kiribati"},
  {174,	"COM",	"KM", "COM", /* 174 */ "Comoros"},
  {659,	"KAN",	"KN", "KNA", /* 659 */ "Saint Kitts and Nevis"},
  {408,	"KP",	"KP", "PRK", /* 408 */ "Korea, Democratic People's Republic of"},
  {410,	"ROK",	"KR", "KOR", /* 410 */ "Korea, Republic of"},
  {414,	"KWT",	"KW", "KWT", /* 414 */ "Kuwait"},
  {136,	NULL,	"KY", "CYM", /* 136 */ "Cayman Islands"},
  {398,	"KZ",	"KZ", "KAZ", /* 398 */ "Kazakhstan"},
  {418,	"LAO",	"LA", "LAO", /* 418 */ "Lao People's Democratic Republic"},
  {422,	"RL",	"LB", "LBN", /* 422 */ "Lebanon"},
  {662,	"WL",	"LC", "LCA", /* 662 */ "Saint Lucia"},
  {438,	"FL",	"LI", "LIE", /* 438 */ "Liechtenstein"},
  {144,	"CL",	"LK", "LKA", /* 144 */ "Sri Lanka"},
  {430,	"LB",	"LR", "LBR", /* 430 */ "Liberia"},
  {426,	"LS",	"LS", "LSO", /* 426 */ "Lesotho"},
  {440,	"LT",	"LT", "LTU", /* 440 */ "Lithuania"},
  {442,	"L",	"LU", "LUX", /* 442 */ "Luxembourg"},
  {428,	"LV",	"LV", "LVA", /* 428 */ "Latvia"},
  {434,	"LAR",	"LY", "LBY", /* 434 */ "Libyan Arab Jamahiriya"},
  {504,	"MA",	"MA", "MAR", /* 504 */ "Morocco"},
  {492,	"MC",	"MC", "MCO", /* 492 */ "Monaco"},
  {498,	"MD",	"MD", "MDA", /* 498 */ "Moldova, Republic of"},
  {499,	"MNE",	"ME", "MNE", /* 499 */ "Montenegro"},
  {663,	NULL,	"MF", "MAF", /* 663 */ "Saint Martin (French part)"},
  {450,	"RM",	"MG", "MDG", /* 450 */ "Madagascar"},
  {584,	"MH",	"MH", "MHL", /* 584 */ "Marshall Islands"},
  {807,	"MK",	"MK", "MKD", /* 807 */ "Macedonia, the former Yugoslav Republic of"},
  {466,	"RMM",	"ML", "MLI", /* 466 */ "Mali"},
  {104,	"MYA",	"MM", "MMR", /* 104 */ "Myanmar"},
  {496,	"MGL",	"MN", "MNG", /* 496 */ "Mongolia"},
  {446,	NULL,	"MO", "MAC", /* 446 */ "Macao"},
  {580,	NULL,	"MP", "MNP", /* 580 */ "Northern Mariana Islands"},
  {474,	NULL,	"MQ", "MTQ", /* 474 */ "Martinique"},
  {478,	"RIM",	"MR", "MRT", /* 478 */ "Mauritania"},
  {500,	NULL,	"MS", "MSR", /* 500 */ "Montserrat"},
  {470,	"M",	"MT", "MLT", /* 470 */ "Malta"},
  {480,	"MS",	"MU", "MUS", /* 480 */ "Mauritius"},
  {462,	"MV",	"MV", "MDV", /* 462 */ "Maldives"},
  {454,	"MW",	"MW", "MWI", /* 454 */ "Malawi"},
  {484,	"MEX",	"MX", "MEX", /* 484 */ "Mexico"},
  {458,	"MAL",	"MY", "MYS", /* 458 */ "Malaysia"},
  {508,	"MOC",	"MZ", "MOZ", /* 508 */ "Mozambique"},
  {516,	"NAM",	"NA", "NAM", /* 516 */ "Namibia"},
  {540,	"NCL",	"NC", "NCL", /* 540 */ "New Caledonia"},
  {562,	"RN",	"NE", "NER", /* 562 */ "Niger"},
  {574,	NULL,	"NF", "NFK", /* 574 */ "Norfolk Island"},
  {566,	"NGR",	"NG", "NGA", /* 566 */ "Nigeria"},
  {558,	"NIC",	"NI", "NIC", /* 558 */ "Nicaragua"},
  {528,	"NL",	"NL", "NLD", /* 528 */ "Netherlands"},
  {578,	"N",	"NO", "NOR", /* 578 */ "Norway"},
  {524,	"NEP",	"NP", "NPL", /* 524 */ "Nepal"},
  {520,	"NAU",	"NR", "NRU", /* 520 */ "Nauru"},
  {570,	NULL,	"NU", "NIU", /* 570 */ "Niue"},
  {554,	"NZ",	"NZ", "NZL", /* 554 */ "New Zealand"},
  {512,	"OM",	"OM", "OMN", /* 512 */ "Oman"},
  {591,	"PA",	"PA", "PAN", /* 591 */ "Panama"},
  {604,	"PE",	"PE", "PER", /* 604 */ "Peru"},
  {258,	NULL,	"PF", "PYF", /* 258 */ "French Polynesia"},
  {598,	"PNG",	"PG", "PNG", /* 598 */ "Papua New Guinea"},
  {608,	"RP",	"PH", "PHL", /* 608 */ "Philippines"},
  {586,	"PK",	"PK", "PAK", /* 586 */ "Pakistan"},
  {616,	"PL",	"PL", "POL", /* 616 */ "Poland"},
  {666,	NULL,	"PM", "SPM", /* 666 */ "Saint Pierre and Miquelon"},
  {612,	NULL,	"PN", "PCN", /* 612 */ "Pitcairn"},
  {630,	"PRI",	"PR", "PRI", /* 630 */ "Puerto Rico"},
  {275,	"AUT",	"PS", "PSE", /* 275 */ "Palestinian Territory, Occupied"},
  {620,	"P",	"PT", "PRT", /* 620 */ "Portugal"},
  {585,	"PAL",	"PW", "PLW", /* 585 */ "Palau"},
  {600,	"PY",	"PY", "PRY", /* 600 */ "Paraguay"},
  {634,	"Q",	"QA", "QAT", /* 634 */ "Qatar"},
  {638,	NULL,	"RE", "REU", /* 638 */ "Reunion"},
  {642,	"RO",	"RO", "ROU", /* 642 */ "Romania"},
  {688,	"SRB",	"RS", "SRB", /* 688 */ "Serbia"},
  {643,	"RUS",	"RU", "RUS", /* 643 */ "Russian Federation"},
  {646,	"RWA",	"RW", "RWA", /* 646 */ "Rwanda"},
  {682,	"KSA",	"SA", "SAU", /* 682 */ "Saudi Arabia"},
  { 90,	"SOL",	"SB", "SLB", /* 090 */ "Solomon Islands"},
  {690,	"SY",	"SC", "SYC", /* 690 */ "Seychelles"},
  {736,	"SUD",	"SD", "SDN", /* 736 */ "Sudan"},
  {752,	"S",	"SE", "SWE", /* 752 */ "Sweden"},
  {702,	"SGP",	"SG", "SGP", /* 702 */ "Singapore"},
  {654,	NULL,	"SH", "SHN", /* 654 */ "Saint Helena"},
  {705,	"SLO",	"SI", "SVN", /* 705 */ "Slovenia"},
  {744,	NULL,	"SJ", "SJM", /* 744 */ "Svalbard and Jan Mayen"},
  {703,	"SK",	"SK", "SVK", /* 703 */ "Slovakia"},
  {694,	"WAL",	"SL", "SLE", /* 694 */ "Sierra Leone"},
  {674,	"RSM",	"SM", "SMR", /* 674 */ "San Marino"},
  {686,	"SN",	"SN", "SEN", /* 686 */ "Senegal"},
  {706,	"SO",	"SO", "SOM", /* 706 */ "Somalia"},
  {740,	"SME",	"SR", "SUR", /* 740 */ "Suriname"},
  {678,	"STP",	"ST", "STP", /* 678 */ "Sao Tome and Principe"},
  {222,	"ES",	"SV", "SLV", /* 222 */ "El Salvador"},
  {760,	"SYR",	"SY", "SYR", /* 760 */ "Syrian Arab Republic"},
  {748,	"SD",	"SZ", "SWZ", /* 748 */ "Swaziland"},
  {796,	NULL,	"TC", "TCA", /* 796 */ "Turks and Caicos Islands"},
  {148,	"TD",	"TD", "TCD", /* 148 */ "Chad"},
  {260,	"ARK",	"TF", "ATF", /* 260 */ "French Southern Territories"},
  {768,	"RT",	"TG", "TGO", /* 768 */ "Togo"},
  {764,	"T",	"TH", "THA", /* 764 */ "Thailand"},
  {762,	"TJ",	"TJ", "TJK", /* 762 */ "Tajikistan"},
  {772,	NULL,	"TK", "TKL", /* 772 */ "Tokelau"},
  {626,	"TL",	"TL", "TLS", /* 626 */ "Timor-Leste"},
  {795,	"TM",	"TM", "TKM", /* 795 */ "Turkmenistan"},
  {788,	"TN",	"TN", "TUN", /* 788 */ "Tunisia"},
  {776,	"TON",	"TO", "TON", /* 776 */ "Tonga"},
  {792,	"TR",	"TR", "TUR", /* 792 */ "Turkey"},
  {780,	"TT",	"TT", "TTO", /* 780 */ "Trinidad and Tobago"},
  {798,	"TUV",	"TV", "TUV", /* 798 */ "Tuvalu"},
  {158,	NULL,	"TW", "TWN", /* 158 */ "Taiwan, Province of China"},
  {834,	"EAT",	"TZ", "TZA", /* 834 */ "Tanzania, United Republic of"},
  {804,	"UA",	"UA", "UKR", /* 804 */ "Ukraine"},
  {800,	"EAU",	"UG", "UGA", /* 800 */ "Uganda"},
  {581,	NULL,	"UM", "UMI", /* 581 */ "United States Minor Outlying Islands"},
  {840,	"USA",	"US", "USA", /* 840 */ "United States"},
  {858,	"ROU",	"UY", "URY", /* 858 */ "Uruguay"},
  {860,	"UZ",	"UZ", "UZB", /* 860 */ "Uzbekistan"},
  {336,	"SCV",	"VA", "VAT", /* 336 */ "Holy See (Vatican City State)"},
  {670,	"WV",	"VC", "VCT", /* 670 */ "Saint Vincent and the Grenadines"},
  {862,	"YV",	"VE", "VEN", /* 862 */ "Venezuela"},
  { 92,	NULL,	"VG", "VGB", /* 092 */ "Virgin Islands, British"},
  {850,	NULL,	"VI", "VIR", /* 850 */ "Virgin Islands, U.S."},
  {704,	"VN",	"VN", "VNM", /* 704 */ "Viet Nam"},
  {548,	"VAN",	"VU", "VUT", /* 548 */ "Vanuatu"},
  {876,	NULL,	"WF", "WLF", /* 876 */ "Wallis and Futuna"},
  {882,	"WS",	"WS", "WSM", /* 882 */ "Samoa"},
  {887,	"YAR",	"YE", "YEM", /* 887 */ "Yemen"},
  {175,	NULL,	"YT", "MYT", /* 175 */ "Mayotte"},
  {710,	"ZA",	"ZA", "ZAF", /* 710 */ "South Africa"},
  {894,	"Z",	"ZM", "ZMB", /* 894 */ "Zambia"},
  {716,	"ZW",	"ZW", "ZWE", /* 716 */ "Zimbabwe"},
  {999, "*",    "*",  "*",             "Unknown"},
};


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


static int
match(struct country_search *this_, enum attr_type type, const char *name)
{
	int ret;
	char *s1, *s2;
	if (!name)
		return 0;
	if (this_->search.type != type && this_->search.type != attr_country_all)
		return 0;
	s1=linguistics_casefold(this_->search.u.str);
	s2=linguistics_casefold(name);
	ret=linguistics_compare(s2,s1,this_->partial)==0;
	g_free(s1);
	g_free(s2);
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
