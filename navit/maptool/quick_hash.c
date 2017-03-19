/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2012-2013 Zoff <zoff@zoff.cc>
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

#include "maptool.h"

struct quickhash_table {
	unsigned int num_buckets;
	unsigned long long num_entries; /* Total number of entries in the table */
	struct quickhash_bucket *bucket_list; // pointer to list of all buckets
};

struct quickhash_entry {
	long long key;
	// void* value_ptr;
	int value_ptr;
};

struct quickhash_bucket {
	struct quickhash_entry *entry;
	unsigned char* mem; // mem that holds the entries
	unsigned int mem_size; // size of mem in number of entries
	unsigned int num_entries; /* Total number of entries in this bucket */
};


#define QHASH_ENTRY_INC 10

#define GOOD_PRIME 373587743
#define HASH_SEED 373587743

static const unsigned int mmm__ = 0xc6a4a793;
static const int rrr__ = 16;

unsigned int MurmurHash1Aligned(const void *key, int len, unsigned int seed)
{

  const unsigned char * data = (const unsigned char *)key;

  unsigned int h = seed ^ (len * mmm__);

  int align = (uint64_t)data & 3;

  if(align && (len >= 4))
  {
    // Pre-load the temp registers

    unsigned int t = 0, d = 0;

    switch(align)
    {
      case 1: t |= data[2] << 16;
      case 2: t |= data[1] << 8;
      case 3: t |= data[0];
    }

    t <<= (8 * align);

    data += 4-align;
    len -= 4-align;

    int sl = 8 * (4-align);
    int sr = 8 * align;

    // Mix

    while(len >= 4)
    {
      d = *(unsigned int *)data;
      t = (t >> sr) | (d << sl);
      h += t;
      h *= mmm__;
      h ^= h >> rrr__;
      t = d;

      data += 4;
      len -= 4;
    }

    // Handle leftover data in temp registers

    int pack = len < align ? len : align;

    d = 0;

    switch(pack)
    {
    case 3: d |= data[2] << 16;
    case 2: d |= data[1] << 8;
    case 1: d |= data[0];
    case 0: h += (t >> sr) | (d << sl);
        h *= mmm__;
        h ^= h >> rrr__;
    }

    data += pack;
    len -= pack;
  }
  else
  {
    while(len >= 4)
    {
      h += *(unsigned int *)data;
      h *= mmm__;
      h ^= h >> rrr__;

      data += 4;
      len -= 4;
    }
  }

  //----------
  // Handle tail bytes

  switch(len)
  {
  case 3: h += data[2] << 16;
  case 2: h += data[1] << 8;
  case 1: h += data[0];
      h *= mmm__;
      h ^= h >> rrr__;
  };

  h *= mmm__;
  h ^= h >> 10;
  h *= mmm__;
  h ^= h >> 17;

  return h;
}





static uint32_t h1 = (uint32_t)(1) ^ (sizeof (long long));
static uint32_t h2 = (uint32_t)(1 >> 32);

uint64_t MurmurHash64B ( const void * key, int len, uint64_t seed )
{
  const uint32_t m = 0x5bd1e995;
  const int r = 24;

  // uint32_t h1 = (uint32_t)(seed) ^ len;
  // uint32_t h2 = (uint32_t)(seed >> 32);

  const uint32_t * data = (const uint32_t *)key;

  while(len >= 8)
  {
    uint32_t k1 = *data++;
    k1 *= m; k1 ^= k1 >> r; k1 *= m;
    h1 *= m; h1 ^= k1;
    len -= 4;

    uint32_t k2 = *data++;
    k2 *= m; k2 ^= k2 >> r; k2 *= m;
    h2 *= m; h2 ^= k2;
    len -= 4;
  }

  if(len >= 4)
  {
    uint32_t k1 = *data++;
    k1 *= m; k1 ^= k1 >> r; k1 *= m;
    h1 *= m; h1 ^= k1;
    len -= 4;
  }

  switch(len)
  {
  case 3: h2 ^= ((unsigned char*)data)[2] << 16;
  case 2: h2 ^= ((unsigned char*)data)[1] << 8;
  case 1: h2 ^= ((unsigned char*)data)[0];
      h2 *= m;
  };

  h1 ^= h2 >> 18; h1 *= m;
  h2 ^= h1 >> 22; h2 *= m;
  h1 ^= h2 >> 17; h1 *= m;
  h2 ^= h1 >> 19; h2 *= m;

  uint64_t h = h1;

  h = (h << 32) | h2;

  return h;
}


// input:	a 64bit key
// returns:	bucket number (0 - max_buckets)
int quick_hash_hash(struct quickhash_table* table, long long key)
{
	int out;
	out = MurmurHash64B(&key, sizeof(key), 1);
	return (out % table->num_buckets);
}


void quick_hash_add_entry(struct quickhash_table* table, long long key, int value_ptr)
{
	int bucket;
	struct quickhash_bucket *cur_bucket;
	struct quickhash_entry* e;
	void *tmp_ptr;
	unsigned int mem_size_old;

#ifdef HASH_SIMPLE
	key ^= key >> 33;
	key *= 0xff51afd7ed558ccd;
	key ^= key >> 33;
	//key *= 0xc4ceb9fe1a85ec53;
	//key ^= key >> 33;
	bucket = key % table->num_buckets;
#else
	bucket = MurmurHash1Aligned(&key, sizeof(key), HASH_SEED);
	// bucket = MurmurHash64B(&key, sizeof(key), 1);
	bucket =  bucket % table->num_buckets;
#endif

	cur_bucket = table->bucket_list + bucket; // move pointer to wanted bucket list struct
	//fprintf(stderr, "quick_hash:add_entry:k=%lld bl=%p b=%p bnum=%d\n", key, table->bucket_list, cur_bucket, bucket);

	// init the bucket and its memory
	if (cur_bucket->mem == NULL)
	{
		// cur_bucket->mem = malloc((size_t)(QHASH_ENTRY_INC * sizeof(struct quickhash_entry)));
		// cur_bucket->mem = calloc(QHASH_ENTRY_INC, sizeof(struct quickhash_entry));
		cur_bucket->mem = g_new0(struct quickhash_entry, QHASH_ENTRY_INC);
		cur_bucket->entry = cur_bucket->mem;
		cur_bucket->mem_size = QHASH_ENTRY_INC;
	}

	// add more mem for entries
	if (cur_bucket->mem_size <= cur_bucket->num_entries)
	{
		tmp_ptr = cur_bucket->mem;
		mem_size_old = cur_bucket->mem_size;
		cur_bucket->mem_size = cur_bucket->mem_size + QHASH_ENTRY_INC;
		// cur_bucket->mem = realloc((void *)tmp_ptr, (size_t)(cur_bucket->mem_size * sizeof(struct quickhash_entry)));
		// cur_bucket->mem = malloc((size_t)(cur_bucket->mem_size * sizeof(struct quickhash_entry)));
		// cur_bucket->mem = (struct quickhash_entry *)g_new0(struct quickhash_entry, cur_bucket->mem_size);
		// cur_bucket->mem = g_realloc(tmp_ptr, cur_bucket->mem_size * sizeof(struct quickhash_entry));
		cur_bucket->mem = g_renew(struct quickhash_entry, tmp_ptr, cur_bucket->mem_size);
		if (!cur_bucket->mem)
		{
			cur_bucket->mem = tmp_ptr;
			cur_bucket->mem_size = mem_size_old;
			fprintf(stderr, "quick_hash:ERROR-002\n");
			return;
		}

		if (tmp_ptr != cur_bucket->mem)
		{
			// copy over the memory
			// *** // memcpy(cur_bucket->mem, tmp_ptr, (size_t)(mem_size_old * QHASH_ENTRY_INC));

			//fprintf(stderr, "quick_hash:add_entry:diff=%d entry=%p mem=%p num entries=%d\n", ((int)cur_bucket->entry - (int)tmp_ptr), cur_bucket->entry, tmp_ptr, cur_bucket->num_entries);
			// move entry pointer to new mem
			cur_bucket->entry = cur_bucket->mem;
			cur_bucket->entry = cur_bucket->entry + cur_bucket->num_entries;
			//fprintf(stderr, "quick_hash:add_entry:diff=%d entry=%p mem=%p num entries=%d\n", ((int)cur_bucket->entry - (int)cur_bucket->mem), cur_bucket->entry, cur_bucket->mem, cur_bucket->num_entries);

			// free the old mem
			// *** // g_free(tmp_ptr);
		}
	}

	e = (struct quickhash_entry*)cur_bucket->entry;
	e->key = key;
	e->value_ptr = value_ptr;

	cur_bucket->num_entries++;
	cur_bucket->entry++; // move pointer to next free entry

	table->num_entries++;
}

void* quick_hash_print_stats(struct quickhash_table* table)
{
	long long size_all;
	long long min_entries;
	long long max_entries;
	long long empty_buckets;
	int i;
	struct quickhash_bucket *cur_bucket = table->bucket_list;

	size_all = (long long)(table->num_buckets * sizeof(struct quickhash_bucket));
	// size_all = size_all + (table->num_entries * sizeof(struct quickhash_entry));

	min_entries = 1000000;
	max_entries = 0;
	empty_buckets = 0;
	for (i=0;i<table->num_buckets;i++)
	{
		size_all = size_all + (cur_bucket->mem_size * sizeof(struct quickhash_entry));

		if (cur_bucket->mem == NULL)
		{
			min_entries = 0;
			empty_buckets++;
		}
		else
		{
			if (cur_bucket->num_entries == 0)
			{
				empty_buckets++;
			}

			if (cur_bucket->num_entries < min_entries)
			{
				min_entries = cur_bucket->num_entries;
			}

			if (cur_bucket->num_entries > max_entries)
			{
				max_entries = cur_bucket->num_entries;
			}
		}
		cur_bucket++;
	}

	fprintf_(stderr, "quick_hash:size    all=%lld MB\n", (long long)(size_all / 1024.0 / 1024.0));
	fprintf_(stderr, "quick_hash:size  entry=%d\n", (sizeof(struct quickhash_bucket)));
	fprintf_(stderr, "quick_hash:size bucket=%d\n", (sizeof(struct quickhash_entry)));
	fprintf_(stderr, "quick_hash:item  count=%lld\n", table->num_entries);
	fprintf_(stderr, "quick_hash:minbentries=%d\n", min_entries);
	fprintf_(stderr, "quick_hash:maxbentries=%d\n", max_entries);
	fprintf_(stderr, "quick_hash:empty bckts=%lld\n", empty_buckets);
}

int quick_hash_lookup(struct quickhash_table* table, long long key)
{
	int bucket;
	int i;
	struct quickhash_bucket *cur_bucket;

#ifdef HASH_SIMPLE
	key ^= key >> 33;
	key *= 0xff51afd7ed558ccd;
	key ^= key >> 33;
	//key *= 0xc4ceb9fe1a85ec53;
	//key ^= key >> 33;
	bucket = key % table->num_buckets;
	bucket = bucket % table->num_buckets;
#else
	bucket = MurmurHash1Aligned(&key, sizeof(key), HASH_SEED);
	// bucket = MurmurHash64B(&key, sizeof(key), 1);
	bucket =  bucket % table->num_buckets;
#endif

	cur_bucket = table->bucket_list + bucket; // move pointer to wanted bucket list struct
	//fprintf(stderr, "quick_hash:lookup:k=%lld bl=%p b=%p bnum=%d\n", key, table->bucket_list, cur_bucket, bucket);

	if (cur_bucket->mem == NULL)
	{
		//fprintf(stderr, "quick_hash:lookup:NULL 001\n");
		return -1;
	}

	if (cur_bucket->num_entries == 0)
	{
		//fprintf(stderr, "quick_hash:lookup:NULL 002\n");
		return -1;
	}

	struct quickhash_entry* e = (struct quickhash_entry *)cur_bucket->mem;
	for(i=0;i<cur_bucket->num_entries;i++)
	{
		//fprintf(stderr, "quick_hash:lookup:k=%lld d=%p\n", e->key, e->value_ptr);
		if (e->key == key)
		{
			return e->value_ptr; // found key
		}
		e++; // move to next entry
	}

	return -1;
}

void quick_hash_destroy(struct quickhash_table* table)
{
	int i;
	struct quickhash_bucket *cur_bucket = table->bucket_list;
	// struct quickhash_entry* e = cur_bucket->entry;

	for (i=0;i<table->num_buckets;i++)
	{
		if (cur_bucket->mem != NULL)
		{
			g_free(cur_bucket->mem);
		}
		cur_bucket++;
	}

	g_free(table->bucket_list);

}

struct quickhash_table* quick_hash_init(int num_buckets)
{
	int i;

	struct quickhash_table *table=g_new0(struct quickhash_table, 1);
	table->num_buckets = num_buckets;
	table->num_entries = 0;

	struct quickhash_bucket *bucket_list=g_new0(struct quickhash_bucket, num_buckets);
	table->bucket_list = bucket_list;

	struct quickhash_bucket *cur_bucket;
	cur_bucket = table->bucket_list;
	for (i=0;i<num_buckets;i++)
	{
		cur_bucket->mem = NULL;
		cur_bucket->num_entries = 0;
		cur_bucket->entry = NULL;
		cur_bucket->mem_size = 0;
	}

	fprintf_(stderr, "quick_hash:size all=%lld\n", (long long)(table->num_buckets * sizeof(struct quickhash_bucket)));
	fprintf_(stderr, "quick_hash:size entry=%d\n", (sizeof(struct quickhash_bucket)));
	fprintf_(stderr, "quick_hash:size bucket=%d\n", (sizeof(struct quickhash_entry)));

	return table;
}


