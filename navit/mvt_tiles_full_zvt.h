/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2016 Zoff <zoff@zoff.cc>
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



// **********=================== hack to display ZVT tiles ===================**********
// **********=================== hack to display ZVT tiles ===================**********
// **********=================== hack to display ZVT tiles ===================**********
// **********=================== hack to display ZVT tiles ===================**********
// **********=================== hack to display ZVT tiles ===================**********





#define _MVT_LITTLEENDIAN_

/*
====================================================================
*/

struct __attribute__((__packed__)) zvt_header
{
	uint8_t signature[3];
	unsigned char zoom;
};


struct __attribute__((__packed__)) wkb_header
{
	// uint8_t is_little_endian; // 0 or 1
	uint8_t type;
};

struct wkb_point
{
	int x;
	int y;
};

struct mapnik_tile
{
	int x;
	int y;
};


/*
====================================================================
*/

#ifdef _MVT_LITTLEENDIAN_
uint32_t swap_endian32(uint32_t num)
{
	return ((num>>24)&0xff) | // move byte 3 to byte 0
	((num<<8)&0xff0000) | // move byte 1 to byte 2
	((num>>8)&0xff00) | // move byte 2 to byte 1
	((num<<24)&0xff000000); // byte 0 to byte 3
}


uint64_t swap_endian64(uint64_t x)
{
	return (uint64_t)((((x) & 0xff00000000000000ull) >> 56)
	 | (((x) & 0x00ff000000000000ull) >> 40)
	 | (((x) & 0x0000ff0000000000ull) >> 24)
	 | (((x) & 0x000000ff00000000ull) >> 8)
	 | (((x) & 0x00000000ff000000ull) << 8)
	 | (((x) & 0x0000000000ff0000ull) << 24)
	 | (((x) & 0x000000000000ff00ull) << 40)
	 | (((x) & 0x00000000000000ffull) << 56));
}

#else
#define swap_endian32(x) x
#define swap_endian64(x) x
#endif

// for zlib ------------
#define Bytef unsigned char
#define uLong unsigned long
#define uLongf unsigned long
// for zlib ------------

const char *wkb_feature_type[] =
{
"Geometry", // 0
"Point",
"LineString",
"Polygon", // 3
"MultiPoint",
"MultiLineString",
"MultiPolygon", // 6
"GeometryCollection",
"CircularString",
"CompoundCurve",
"CurvePolygon", // 10
"MultiCurve", // 11
"MultiSurface",
"Curve",
"Surface",
"PolyhedralSurface",
"TIN",
"Triangle", // 17
};



// Max extent edge for spherical mercator
#define MVT_MAX_EXTENT 20037508.342789244
#define MVT_M_PI 3.14159265358979323846
#define MVT_M_PI_2 1.57079632679489661923
#define MVT_DEG_TO_RAD 0.01745329251
#define MVT_RAD_TO_DEG 57.2957795131

/*
void mvt_merc2lonlat(struct wkb_point *in)
{
    // "Convert coordinate pair from epsg:3857 to epsg:4326"
    in->x = (in->x / MVT_MAX_EXTENT) * 180.0;
    in->y = (in->y / MVT_MAX_EXTENT) * 180.0;
    in->y = MVT_RAD_TO_DEG * (2.0 * atan(exp(in->y * MVT_DEG_TO_RAD)) - MVT_M_PI_2);

	// fprintf(stderr, "lat, lon: %f %f\n", x1, y1);
}
*/

void get_mapnik_tilenumber(struct mapnik_tile *mt, double lat, double lon, int mapnik_zoom)
{
    mt->x = (int)( ( lon + 180) / 360 * pow(2, mapnik_zoom) );
    mt->y = (int)( (1 - log(tan(MVT_DEG_TO_RAD * lat) + (1/(cos(MVT_DEG_TO_RAD * lat))) )/MVT_M_PI)/2 * pow(2, mapnik_zoom) );

	// dbg(0, "%d %d %f %f %d\n", mt->x, mt->y, lat, lon, mapnik_zoom);
}

void get_mapniktile_2_geo(int tile_x, int tile_y, int zoom, struct coord_geo *g)
{
	float n;
	double lat;
	double lon;
	n = pow(2, zoom);
	g->lng = tile_x / n * 360.0 - 180.0;
	g->lat = MVT_RAD_TO_DEG * ( atan( sinh( MVT_M_PI * ( 1 - 2 * tile_y / n ))));
}

void draw_water_tile_new(int mapnik_zoom, int tile_x, int tile_y, struct displaylist *display_list)
{
	struct color custom_color;
	struct graphics *gra = display_list->dc.gra;
	struct point *p_ring = malloc(sizeof(struct point) * 4);
	struct coord *c_ring = malloc(sizeof(struct coord) * 4);
	struct coord *c_temp = c_ring;
	struct coord_geo g_temp;
	int count;

	if (!gra->gc[0])
	{
		gra->gc[0] = graphics_gc_new(gra);
	}

	// water color:	#82c8ea
	//				130, 200, 234
	custom_color.r = 130 << 8;
	custom_color.g = 200 << 8;
	custom_color.b = 234 << 8;

	custom_color.a = 0xffff;

	gra->gc[0]->meth.gc_set_foreground(gra->gc[0]->priv, &custom_color);

	count = 4;

	get_mapniktile_2_geo(tile_x, tile_y, mapnik_zoom, &g_temp);
	transform_from_geo(projection_mg, &g_temp, c_temp);
	c_temp++;

	get_mapniktile_2_geo(tile_x + 1, tile_y, mapnik_zoom, &g_temp);
	transform_from_geo(projection_mg, &g_temp, c_temp);
	c_temp++;

	get_mapniktile_2_geo(tile_x + 1, tile_y + 1, mapnik_zoom, &g_temp);
	transform_from_geo(projection_mg, &g_temp, c_temp);
	c_temp++;

	get_mapniktile_2_geo(tile_x, tile_y + 1, mapnik_zoom, &g_temp);
	transform_from_geo(projection_mg, &g_temp, c_temp);

	count = transform(global_navit->trans, projection_mg, c_ring, p_ring, count, 0, 0, NULL);
	gra->meth.draw_polygon(gra->priv, gra->gc[0]->priv, p_ring, count);


}


void decode_mvt_tile(const char* basedir, FILE *mapfile, uint32_t compr_tilesize, int mapnik_zoom, int tile_x, int tile_y, struct displaylist *display_list)
{

	// dbg(0,"decode_mvt_tile:--ENTER--\n");

	struct wkb_header *buffer_wkb_header = NULL;
	void *buffer_compressed = NULL;
	Bytef *buffer_uncpr = NULL;
	uLongf buffer_uncpr_len = -1;
	unsigned long buffer_uncpr_end = -1;
	int res = -1;
	uint32_t *feature_count = NULL;
	// uint32_t *feature_len = NULL;
	uint32_t numrings = -1;
	uint32_t numpolys = -1;
	Bytef *buffer_uncpr_pos = NULL;
	Bytef *buffer_uncpr_pos_save = NULL;
	int i = -1;
	int j = -1;
	int k = -1;
	int l = -1;
	int num_coords = 0;
	uint32_t *n = NULL;
	double lat = 0;
	uint64_t *lat_p = NULL;
	double lon = 0;
	uint64_t *lon_p = NULL;
	struct wkb_point *point1 = NULL;
	struct coord point2;


	{
		{

			// -------------- GFX -----------------
			// -------------- GFX -----------------
			// -------------- GFX -----------------
			struct point *p_ring = malloc(sizeof(struct point) * 20000); // 20000 points in 1 polygon max ?!
			struct coord *c_ring = malloc(sizeof(struct coord) * 20000); // 20000 coords in 1 polygon max ?!
			struct coord *c_temp = c_ring;
			struct coord_geo g_temp;
			struct graphics *gra = display_list->dc.gra;
			// struct graphics_gc *gc = display_list->dc.gc;
			struct color custom_color;
			int count = 0;

			if (!gra->gc[0])
			{
				gra->gc[0] = graphics_gc_new(gra);
			}

			// -------------- GFX -----------------
			// -------------- GFX -----------------
			// -------------- GFX -----------------

			// dbg(0, "compressed len:%ld\n", (long)compr_tilesize);
			// fprintf(stderr, "0x%08x\n", compr_tilesize);

			buffer_compressed = malloc((size_t)compr_tilesize);
			fread(buffer_compressed, compr_tilesize, 1, mapfile);

			buffer_uncpr_len = compr_tilesize * 20;
			buffer_uncpr_end = buffer_uncpr + buffer_uncpr_len;
			buffer_uncpr = malloc((size_t)buffer_uncpr_len);

			res = uncompress(buffer_uncpr, &buffer_uncpr_len, buffer_compressed, compr_tilesize);

			if (res == Z_BUF_ERROR)
			{
				dbg(0, "res=Z_BUF_ERROR\n");
			}
			else if (res == Z_STREAM_ERROR)
			{
				dbg(0, "res=Z_STREAM_ERROR\n");
			}
			else if (res == Z_MEM_ERROR)
			{
				dbg(0, "res=Z_MEM_ERROR\n");
			}
			else if (res == Z_DATA_ERROR)
			{
				dbg(0, "res=Z_DATA_ERROR\n");
			}
			else
			{
				// dbg(0, "res=%d\n", res);

				// dbg(0, "un-compressed len:%ld\n", (long)buffer_uncpr_len);

				// dbg(0, "001\n");
				buffer_uncpr_pos = buffer_uncpr;
				buffer_uncpr_pos = buffer_uncpr_pos + 1; // skip to count
				// dbg(0, "002\n");
				feature_count = (void *)buffer_uncpr_pos;
				// dbg(0, "003\n");
				// ZZZ // *feature_count = swap_endian32(*feature_count);
				// dbg(0, "buffer_uncpr_pos=%p\n", buffer_uncpr_pos);
				// dbg(0, "feature_count=%p\n", feature_count);
				// dbg(0, "0x%08x\n", *feature_count);
				buffer_uncpr_pos = buffer_uncpr_pos + sizeof(uint32_t);
				// dbg(0, "004\n");
				// dbg(0, "geometry_count:%d\n", (int)*feature_count);
				// dbg(0, "005\n");
				// fprintf(stderr, "sizeof feature_count:%d\n", sizeof(uint32_t));

				int ff_count = (int)*feature_count;

				// loop through geometries
				for(i=0;i<ff_count;i++)
				{
					// dbg(0, "feature#:%d\n", i);

/*
					feature_len = (void *)buffer_uncpr_pos;
					// ZZZ // *feature_len = swap_endian32(*feature_len);
					fprintf(stderr, "0x%08x\n", *feature_len);
					buffer_uncpr_pos = buffer_uncpr_pos + sizeof(uint32_t);
					// dbg(0, "  feature_len:%d\n", (int)*feature_len);
*/


					// =======================
					buffer_uncpr_pos_save = buffer_uncpr_pos; // save position
					// =======================


					// dbg(0, "006\n");
					buffer_wkb_header = (void *)buffer_uncpr_pos;
					// dbg(0, "  is_little_endian:%d\n", (int)buffer_wkb_header->is_little_endian);
					// fprintf(stderr, "  0x%08x\n", (Bytef)buffer_wkb_header->is_little_endian);

					buffer_uncpr_pos = buffer_uncpr_pos + sizeof(struct wkb_header);

						// dbg(0, "  feature type:%d\n", (int)buffer_wkb_header->type);
						// dbg(0, "  feature type:%s\n", wkb_feature_type[(int)buffer_wkb_header->type]);


						if (buffer_wkb_header->type == 3)
						{
							// Polygon --------------------------------------------
							// Polygon --------------------------------------------
							// Polygon --------------------------------------------
							n = (void *)buffer_uncpr_pos;
							numrings = *n;
							// dbg(0, "  numRings:%d\n", (int)*n);
							buffer_uncpr_pos = buffer_uncpr_pos + sizeof(uint32_t);




							// -------------- GFX -----------------
							// -------------- GFX -----------------
							// -------------- GFX -----------------
							count = 0;

							// water color:	#82c8ea
							//				130, 200, 234
							custom_color.r = 130 << 8;
							custom_color.g = 200 << 8;
							custom_color.b = 234 << 8;

							custom_color.a = 0xffff;

							// graphics_gc_set_foreground(gra->gc[0], &custom_color);
							gra->gc[0]->meth.gc_set_foreground(gra->gc[0]->priv, &custom_color);
							// -------------- GFX -----------------
							// -------------- GFX -----------------
							// -------------- GFX -----------------



							for(k=0;k<numrings;k++)
							{

								n = (void *)buffer_uncpr_pos;
								num_coords = (int)*n;
								buffer_uncpr_pos = buffer_uncpr_pos + sizeof(uint32_t);


								// -------------- GFX -----------------
								// -------------- GFX -----------------
								// -------------- GFX -----------------
								count = 0;
								c_temp = c_ring;
								// -------------- GFX -----------------
								// -------------- GFX -----------------
								// -------------- GFX -----------------


								// dbg(0, "  num of coords:%d\n", num_coords);

								for(j=0;j<num_coords;j++)
								{
									// fprintf(stderr, "  size of double:%d\n", sizeof(lat));

									point1 = (void *)buffer_uncpr_pos;
									c_temp->x = point1->x;
									c_temp->y = point1->y;
									// ** // mvt_merc2lonlat(&point2);
									buffer_uncpr_pos = buffer_uncpr_pos + sizeof(struct wkb_point);

									// dbg(0, "  lat,lon [%d] %lf %lf\n", j, point2.x, point2.y);
									//fprintf(stderr, "%016llx\n", (long long unsigned int)point1->x);
									//mvt_print_binary_64((uint32_t)point1->x);
									//fprintf(stderr, "%016llx\n", (long long unsigned int)point1->y);
									//mvt_print_binary_64((uint32_t)point1->y);

									// -------------- GFX -----------------
									// -------------- GFX -----------------
									// -------------- GFX -----------------
									// ** // g_temp.lat = point2.y;
									// ** // g_temp.lng = point2.x;
									// ** // transform_from_geo(projection_mg, &g_temp, c_temp);
									count++;
									c_temp++;
									// -------------- GFX -----------------
									// -------------- GFX -----------------
									// -------------- GFX -----------------


								}


								// -------------- GFX -----------------
								// -------------- GFX -----------------
								// -------------- GFX -----------------
								if (count > 0)
								{
									count = transform(global_navit->trans, projection_mg, c_ring, p_ring, count, 0, 0, NULL);

									//Z//graphics_draw_polygon_clipped(gra, display_list->dc.gc, p_ring, count);
									gra->meth.draw_polygon(gra->priv, gra->gc[0]->priv, p_ring, count);
									//Z//gra->meth.draw_polygon(gra, display_list->dc.gc, p_ring, count);
								}
								// -------------- GFX -----------------
								// -------------- GFX -----------------
								// -------------- GFX -----------------



							}

							// Polygon --------------------------------------------
							// Polygon --------------------------------------------
							// Polygon --------------------------------------------
						}
						else if (buffer_wkb_header->type == 6)
						{
							// MultiPolygon
							n = (void *)buffer_uncpr_pos;
							numpolys = *n;
							// dbg(0, "  numpolys:%d\n", (int)*n);
							buffer_uncpr_pos = buffer_uncpr_pos + sizeof(uint32_t);


							// -------------- GFX -----------------
							// -------------- GFX -----------------
							// -------------- GFX -----------------
							count = 0;

							// water color:	#82c8ea
							//				130, 200, 234
							custom_color.r = 130 << 8;
							custom_color.g = 200 << 8;
							custom_color.b = 234 << 8;

							custom_color.a = 0xffff;

							//graphics_gc_set_foreground(gra->gc[0], &custom_color);
							gra->gc[0]->meth.gc_set_foreground(gra->gc[0]->priv, &custom_color);

							// -------------- GFX -----------------
							// -------------- GFX -----------------
							// -------------- GFX -----------------

							for(l=0;l<numpolys;l++)
							{

								// dbg(0, "  poly#:%d\n", l);

								buffer_wkb_header = (void *)buffer_uncpr_pos;
								// dbg(0, "  is_little_endian:%d\n", (int)buffer_wkb_header->is_little_endian);
								// fprintf(stderr, "  0x%08x\n", (Bytef)buffer_wkb_header->is_little_endian);

								buffer_uncpr_pos = buffer_uncpr_pos + sizeof(struct wkb_header);

								// Polygon --------------------------------------------
								// Polygon --------------------------------------------
								// Polygon --------------------------------------------
								n = (void *)buffer_uncpr_pos;
								numrings = *n;
								// dbg(0, "  numRings:%d\n", (int)*n);
								buffer_uncpr_pos = buffer_uncpr_pos + sizeof(uint32_t);

								for(k=0;k<numrings;k++)
								{

									// -------------- GFX -----------------
									// -------------- GFX -----------------
									// -------------- GFX -----------------
									count = 0;
									c_temp = c_ring;

									if (k == 0)
									{
										// water color:	#82c8ea
										//				130, 200, 234
										custom_color.r = 130 << 8;
										custom_color.g = 200 << 8;
										custom_color.b = 234 << 8;

										// graphics_gc_set_foreground(gra->gc[0], &custom_color);
										gra->gc[0]->meth.gc_set_foreground(gra->gc[0]->priv, &custom_color);
									}
									else
									{
										// bg color:	#fef9ee
										//				254, 249, 238
										custom_color.r = 254 << 8;
										custom_color.g = 249 << 8;
										custom_color.b = 238 << 8;

										// graphics_gc_set_foreground(gra->gc[0], &custom_color);
										gra->gc[0]->meth.gc_set_foreground(gra->gc[0]->priv, &custom_color);

									}
									// -------------- GFX -----------------
									// -------------- GFX -----------------
									// -------------- GFX -----------------

									n = (void *)buffer_uncpr_pos;
									num_coords = (int)*n;
									buffer_uncpr_pos = buffer_uncpr_pos + sizeof(uint32_t);

									// dbg(0, "  num of coords:%d\n", num_coords);

									for(j=0;j<num_coords;j++)
									{
										// fprintf(stderr, "  size of double:%d\n", sizeof(lat));

										point1 = (void *)buffer_uncpr_pos;
										c_temp->x = point1->x;
										c_temp->y = point1->y;
										// ** // mvt_merc2lonlat(&point2);
										buffer_uncpr_pos = buffer_uncpr_pos + sizeof(struct wkb_point);

										// dbg(0, "  lat,lon [%d] %d %d\n", j, (int)point1->x, (int)point1->y);

										//fprintf(stderr, "0x%08x\n", (uint32_t)point1->x);
										//mvt_print_binary_64((uint32_t)point1->x);
										//fprintf(stderr, "0x%08x\n", (uint32_t)point1->y);
										//mvt_print_binary_64((uint32_t)point1->y);

										// -------------- GFX -----------------
										// -------------- GFX -----------------
										// -------------- GFX -----------------
										//g_temp.lat = point2.y;
										//g_temp.lng = point2.x;
										//transform_from_geo(projection_mg, &g_temp, c_temp);
										count++;
										c_temp++;
										// -------------- GFX -----------------
										// -------------- GFX -----------------
										// -------------- GFX -----------------

									}

									// dbg(0, "  XX1\n");

									// -------------- GFX -----------------
									// -------------- GFX -----------------
									// -------------- GFX -----------------
									if (count > 0)
									{
										count = transform(global_navit->trans, projection_mg, c_ring, p_ring, count, 0, 0, NULL);
										//Z//graphics_draw_polygon_clipped(gra, display_list->dc.gc, p_ring, count);
										gra->meth.draw_polygon(gra->priv, gra->gc[0]->priv, p_ring, count);
										//Z//gra->meth.draw_polygon(gra, display_list->dc.gc, p_ring, count);
									}
									// -------------- GFX -----------------
									// -------------- GFX -----------------
									// -------------- GFX -----------------

								}
								// Polygon --------------------------------------------
								// Polygon --------------------------------------------
								// Polygon --------------------------------------------


									// dbg(0, "  XX6\n");
							}

									// dbg(0, "  XX7\n");
						}
									// dbg(0, "  XX8\n");



					// dbg(0, "  XX8a\n");

					// =======================
					// buffer_uncpr_pos = buffer_uncpr_pos_save + (long)*feature_len; // use save position as start here
					// =======================

					// dbg(0, "  XX8b\n");

				}

									// dbg(0, "  XX9\n");

			}

			// dbg(0, "  XX10\n");

			free(buffer_compressed);
			buffer_compressed = NULL;

			// dbg(0, "  XX11\n");

			free(buffer_uncpr);
			buffer_uncpr = NULL;

			// dbg(0, "  XX12\n");

			// -------------- GFX -----------------
			// -------------- GFX -----------------
			// -------------- GFX -----------------
			free(p_ring);
			// dbg(0, "  XX13\n");
			free(c_ring);
			// dbg(0, "  XX14\n");
			// -------------- GFX -----------------
			// -------------- GFX -----------------
			// -------------- GFX -----------------

			
		}

			// dbg(0, "  XX15\n");

	}

			// dbg(0, "  XX16\n");


	// dbg(0,"decode_mvt_tile:--LEAVE--\n");

}


void loop_mapnik_tiles(double lat_lt, double lon_lt, double lat_cn, double lon_cn, double lat_rb, double lon_rb, int mapnik_zoom, const char* basedir, struct displaylist *display_list)
{
	// loop thru all mapniktiles in this bbox and call something

	struct mapnik_tile mnt_lt;
	struct mapnik_tile mnt_cn;
	struct mapnik_tile mnt_rb;
	int i;
	int j;
	int d_lat;
	int d_lon;
	float d_lat_f;
	float d_lon_f;
	int d_max;
	int base_x;
	int base_y;
	int d2_lat;
	int d2_lon;
	float d2_lat_f;
	float d2_lon_f;
	int d2_max;
	int color;
	int tile_x;
	int tile_y;
	int overlap = 1; // tiles more than needed around the bbox
	int is_ok = 0;

	FILE *mapfile_zvt = NULL;
	char *filename = NULL;
	struct zvt_header *buffer = NULL;
	// char *buffer2 = NULL;
	long long header_start = 4; // hard coded
	uint64_t tile_x_header_start = 0;
	uint32_t tile_y_offset = 0;
	uint32_t tile_y_offset_end = 0;
	uint32_t compr_size = 0;


	filename = malloc(5000);
	if (filename)
	{
		sprintf(filename, "%s/coastline.bin", basedir);
		// dbg(0, "filename=%s\n", filename);

		mapfile_zvt = fopen(filename, "rb");

		is_ok = 1;

		if ((mapfile_zvt) && (is_ok))
		{

			buffer = malloc(sizeof(struct zvt_header));
			fread(buffer, sizeof(struct zvt_header), 1, mapfile_zvt);

			// buffer2 = buffer;
			// buffer2[sizeof(struct zvt_header)] = '\0';
			// dbg(0, "header=%s\n", buffer2);

			free(buffer);


			get_mapnik_tilenumber(&mnt_lt, lat_lt, lon_lt, mapnik_zoom);
			get_mapnik_tilenumber(&mnt_cn, lat_cn, lon_cn, mapnik_zoom);
			get_mapnik_tilenumber(&mnt_rb, lat_rb, lon_rb, mapnik_zoom);

		/*
			struct point_rect *r;
			if (global_navit->trans->screen_sel)
			{
				r = &global_navit->trans->screen_sel->u.p_rect;
				screen_width = r->rl.x - r->lu.x;
				screen_height = r->rl.y - r->lu.y;
				dbg(0, "transform_get_size:w=%d h=%d\n", screen_width, screen_height);
				dbg(0, "transform_get_size:%d %d %d %d\n", r->rl.x, r->rl.y, r->lu.x, r->lu.y);
			}
		*/







			d_lat = (mnt_lt.y) - (mnt_cn.y);
			d_lon = (mnt_lt.x) - (mnt_cn.x);
			d_max = abs(d_lon);
			if (abs(d_lat) > abs(d_lon))
			{
				d_max = abs(d_lat);
			}

			if (d_max > 0)
			{
				d_lat_f = (float)d_lat / (float)d_max;
				d_lon_f = (float)d_lon / (float)d_max;
			}
			else
			{
				d_lat_f = 0.0;
				d_lon_f = 0.0;
			}
			// dbg(0, "%f %f %f %f %f\n", (float)d_lat_f, (float)d_lon_f, (float)d_max, (float)d_lat, (float)d_lon);


			d2_lat = (mnt_rb.y) - (mnt_cn.y);
			d2_lon = (mnt_rb.x) - (mnt_cn.x);
			d2_max = abs(d2_lon);
			if (abs(d2_lat) > abs(d2_lon))
			{
				d2_max = abs(d2_lat);
			}

			if (d2_max > 0)
			{
				d2_lat_f = (float)d2_lat / (float)d2_max;
				d2_lon_f = (float)d2_lon / (float)d2_max;
			}
			else
			{
				d2_lat_f = 0.0;
				d2_lon_f = 0.0;
			}
			// dbg(0, "%f %f %f %f %f\n", (float)d2_lat_f, (float)d2_lon_f, (float)d2_max, (float)d2_lat, (float)d2_lon);

			// lon == x
			// lat == y
			color = 2;
			for(i=-overlap;i<(d2_max + 1 + overlap);i++)
			{
				base_y = (int)((float)i * (d2_lat_f));
				base_x = (int)((float)i * (d2_lon_f));
				// dbg(0, "%d:%d %d\n", i, base_x, base_y);

				color = 3 - color; // color = toggle between "1" and "2"
				for(j=-overlap;j<(d_max + 1 + overlap);j++)
				{
					tile_y = mnt_cn.y + base_y + (int)((float)j * (d_lat_f));
					tile_x = mnt_cn.x + base_x + (int)((float)j * (d_lon_f));

					// int old_y = tile_y;
					// int old_x = tile_x;
					// tile_x = 0;
					// tile_y = 0;

					// dbg(0, "tile:%d/%d/%d\n", mapnik_zoom, tile_x, tile_y);

					if ((tile_x < 0) || (tile_x > 4095) || (tile_y < 0) || (tile_y > 4095))
					{
						// ERROR
					}
					else
					{

						fseeko(mapfile_zvt, (off_t)(header_start + (tile_x * 8)), SEEK_SET);
						// dbg(0, "1s=%lld\n", (long long)(header_start + (tile_x * 8)));
						fread(&tile_x_header_start, 8, 1, mapfile_zvt);
						// dbg(0, "1res=%lld\n", (long long)(tile_x_header_start));
						fseeko(mapfile_zvt, (off_t)(tile_x_header_start + (tile_y * 4)), SEEK_SET);
						// dbg(0, "2s=%lld\n", (long long)(tile_x_header_start + (tile_y * 4)));
						fread(&tile_y_offset, 4, 1, mapfile_zvt);
						// dbg(0, "2res-a=%lld\n", (long long)(tile_y_offset));
						fread(&tile_y_offset_end, 4, 1, mapfile_zvt);
						// dbg(0, "2res-b=%lld\n", (long long)(tile_y_offset_end));
						fseeko(mapfile_zvt, (off_t)(tile_x_header_start + tile_y_offset), SEEK_SET);
						// dbg(0, "3s=%lld\n", (long long)(tile_x_header_start + tile_y_offset));

						compr_size = (tile_y_offset_end - tile_y_offset);
						// dbg(0, "zvt:tile x:%d y:%d compr. size=%ld\n", tile_x, tile_y, (long)(compr_size));


						// tile_y = old_y;
						// tile_x = old_x;

						if (compr_size == 0)
						{
							// EMPTY
						}
						else if (compr_size == 1)
						{
							// water square
							// dbg(0, "zvt:water square x:%d y:%d\n", tile_x, tile_y);
							draw_water_tile_new(mapnik_zoom, tile_x, tile_y, display_list);
						}
						else if (compr_size < 0)
						{
							// ERROR
						}
						else if (compr_size > 500000)
						{
							// ERROR
						}
						else
						{
							decode_mvt_tile(basedir, mapfile_zvt, compr_size, mapnik_zoom, tile_x, tile_y, display_list);
						}
					}
				}
			}

			fclose(mapfile_zvt);

		}

		free(filename);
	}
}

void mvt_print_binary_64(uint64_t n)
{
	int c;

	for(c=0;c<64;c++)
	{
		// fprintf(stderr, "c=%d\n", c);
		if (n & 1)
		{
			fprintf(stderr, "1");
		}
		else
		{
			fprintf(stderr, "0");
		}
		n >>= 1;
	}
	fprintf(stderr, "\n");
}


/*
====================================================================
*/




// **********=================== hack to display MVT tiles ===================**********
// **********=================== hack to display MVT tiles ===================**********
// **********=================== hack to display MVT tiles ===================**********
// **********=================== hack to display MVT tiles ===================**********
// **********=================== hack to display MVT tiles ===================**********


