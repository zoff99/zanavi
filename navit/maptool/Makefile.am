include $(top_srcdir)/Makefile.inc

if !SUPPORT_ANDROID
  P2TC_publicdir = $(top_srcdir)/navit/maptool
  export P2TC_publicdir
  P2TC_public_HEADERS =
#  AM_LDFLAGS = -version-info 1.0.0
#  SUBDIRS = libcfu-0.03-zanavi p2t refine
endif

SUBDIRS = libcfu-0.03-zanavi p2t refine

if !SUPPORT_ANDROID
  bin_PROGRAMS = maptool
  noinst_LTLIBRARIES        = libmaptool.la
endif

AM_CPPFLAGS = @NAVIT_CFLAGS@ -I$(top_srcdir)/navit @ZLIB_CFLAGS@ @POSTGRESQL_CFLAGS@ -DMODULE=maptool
BUILT_SOURCES = version_maptool.h
#
#libmaptool_la_SOURCES =  poly2tri-c/002/poly2tri-c/p2t/common/shapes.c \
#	poly2tri-c/002/poly2tri-c/p2t/common/utils.c \
#	poly2tri-c/002/poly2tri-c/p2t/sweep/advancing_front.c \
#	poly2tri-c/002/poly2tri-c/p2t/sweep/cdt.c \
#	poly2tri-c/002/poly2tri-c/p2t/sweep/sweep.c \
#	poly2tri-c/002/poly2tri-c/p2t/sweep/sweep_context.c \
#
libmaptool_la_SOURCES = boundaries.c buffer.c ch.c coastline.c geom.c itembin.c itembin_buffer.c misc.c osm.c osm_psql.c osm_protobuf.c osm_protobufdb.c osm_relations.c osm_xml.c sourcesink.c tempfile.c tile.c zip.c maptool.h generated-code/fileformat.pb-c.c generated-code/fileformat.pb-c.h generated-code/osmformat.pb-c.c generated-code/osmformat.pb-c.h google/protobuf-c/protobuf-c.c google/protobuf-c/protobuf-c.h google/protobuf-c/protobuf-c-private.h miniz.c sqlite3.c sqlite3async.c

maptool_SOURCES = maptool.c
maptool_LDADD = libmaptool.la ../libnavit.la @NAVIT_LIBS@ @WORDEXP_LIBS@ @ZLIB_LIBS@ @POSTGRESQL_LIBS@ @CRYPTO_LIBS@ @INTLLIBS@ @LIBC_LIBS@ p2t/libp2tc.la refine/libp2tc-refine.la libcfu-0.03-zanavi/src/libcfu.a

#libcfu_dummy.h:
#	echo "" >libcfu_dummy.h
#	cp -v $(top_srcdir)/navit/maptool/libcfu.a .

#libcfu.a:
#	cp -v $(top_srcdir)/navit/maptool/libcfu-0.03-zanavi/src/libcfu.a .

version_maptool.h.tmp:
	echo "" >version_maptool.h.tmp
	echo "#define SVN_VERSION \"$$(LANG=C svnversion $(top_srcdir)/navit/maptool 2>/dev/null)\"" >>version_maptool.h.tmp
	if ! diff version_maptool.h.tmp version_maptool.h >/dev/null 2>/dev/null; \
	then \
		mv version_maptool.h.tmp version_maptool.h; \
	fi

version_maptool.h: version_maptool.h.tmp
