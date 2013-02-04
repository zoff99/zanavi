struct cache_entry
{
	int usage;
	int size;
	struct cache_entry_list *where;
	struct cache_entry *next;
	struct cache_entry *prev;
	int id[0];
};

struct cache_entry_list
{
	struct cache_entry *first, *last;
	int size;
};

struct cache
{
	struct cache_entry_list t1, b1, t2, b2, *insert;
	int size, id_size, entry_size;
	int t1_target;
	long misses;
	long hits;
	GHashTable *hash;
//	long long real_size_bytes;
};

/* prototypes */
struct cache *cache_new(int id_size, int size);
void *cache_entry_new(struct cache *cache, void *id, int size);
void cache_entry_destroy(struct cache *cache, void *data);
void *cache_lookup(struct cache *cache, void *id);
void cache_insert(struct cache *cache, void *data);
void *cache_insert_new(struct cache *cache, void *id, int size);
void cache_flush(struct cache *cache, void *id);
void cache_stats(struct cache *cache);
void cache_dump(struct cache *cache);
/* end of prototypes */
