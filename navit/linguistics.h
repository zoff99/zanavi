#ifdef __cplusplus
extern "C" {
#endif
char *linguistics_expand_special(char *str, int mode);
char *linguistics_next_word(char *str);
void linguistics_init(void);
char *linguistics_casefold(char *in);
int linguistics_compare(char *str, char *match, int partial);
#ifdef __cplusplus
}
#endif

/* Prototypes */
int linguistics_search(char *str);

