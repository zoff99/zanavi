#ifdef __cplusplus
extern "C"
{
#endif
char *linguistics_expand_special(char *str, int mode);
char *linguistics_next_word(char *str);
void linguistics_init(void);
char *linguistics_casefold(char *in);
int linguistics_compare(char *str, char *match, int partial);
char *linguistics_remove_all_spaces(char *str);
char *linguistics_remove_all_specials(char *str);
int linguistics_compare_anywhere(char *str, char *match);
char* linguistics_fold_and_prepare_complete(char *in, int free_input);
char* linguistics_check_utf8_string(char* in);
#ifdef __cplusplus
}
#endif

/* Prototypes */
int linguistics_search(char *str);

