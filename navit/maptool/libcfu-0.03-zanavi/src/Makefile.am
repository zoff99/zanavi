lib_LIBRARIES = libcfu.a
libcfu_a_SOURCES = cfuhash.c cfutime.c cfustring.c cfulist.c cfuconf.c cfuthread_queue.c cfu.c cfuopt.c
# bin_PROGRAMS = libcfu-config

# CFU_CONFIG_DEFINES = -DCFU_LIBDIR="\"@libdir@\"" -DCFU_LIBS="\"@LIBS@\"" -DCFU_INCLUDEDIR="\"@includedir@\"" -DCFU_PREFIX="\"@prefix@\""  -DCFU_EXEC_PREFIX="\"@exec_prefix@\""
CFU_CONFIG_DEFINES = 

#if DEBUG
#AM_CFLAGS = -Wall -W -DCFU_DEBUG=1 $(CFU_CONFIG_DEFINES)
#else
AM_CFLAGS = -Wall -W $(CFU_CONFIG_DEFINES)
#endif

include_HEADERS = $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfu.h $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfuhash.h \
    $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfutime.h $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfustring.h \
    $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfulist.h $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfuconf.h \
    $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfuthread_queue.h $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfutypes.h \
    $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include/cfuopt.h

# libcfu_a_LDFLAGS = -lpthread
INCLUDES = -I$(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/include

default: libcfu.a

# libcfu-config

#libcfu-config.o: libcfu-config.c

#libcfu-config: libcfu.a libcfu-config.o
#	$(CC) $(CFLAGS) -o libcfu-config libcfu-config.o libcfu.a -pthread

