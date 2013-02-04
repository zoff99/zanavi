include $(top_srcdir)/Makefile.inc
comma=,
SUBDIRS=fib-1.1 support
if PLUGINS
  SUBDIRS += .
endif
DIST_SUBDIRS=autoload binding map maptool fib-1.1 font fonts gui graphics osd plugin speech support vehicle xpm maps
SUBDIRS+=autoload binding map font gui graphics osd plugin speech vehicle xpm

MODULES = $(wildcard $(top_builddir)/navit/binding/*/*.la $(top_builddir)/navit/font/*/*.la $(top_builddir)/navit/graphics/*/*.la $(top_builddir)/navit/gui/*/*.la $(top_builddir)/navit/map/*/*.la $(top_builddir)/navit/osd/*/*.la $(top_builddir)/navit/speech/*/*.la $(top_builddir)/navit/vehicle/*/*.la)

if FONTS
  SUBDIRS += fonts
endif
if !PLUGINS
  SUBDIRS += .
endif

if MAPTOOL
  SUBDIRS += maptool
endif

if BUILD_SAMPLEMAP
  SUBDIRS += maps
endif


AM_CPPFLAGS = -I$(top_srcdir)/navit/fib-1.1 @NAVIT_CFLAGS@ @ZLIB_CFLAGS@ -DPREFIX=\"@prefix@\" -DLIBDIR=\"@libdir@\" -DMODULE=navit
BUILT_SOURCES = version.h navit_config.h

if BIN_NAVIT
  bin_PROGRAMS = navit
endif

if SHARED_LIBNAVIT
  lib_LTLIBRARIES        = lib@LIBNAVIT@.la
  lib@LIBNAVIT@_la_LDFLAGS = -avoid-version @MODULE_LDFLAGS@ @NAVIT_SOCKET_LDFLAGS@ -no-undefined -Wl,--no-undefined
else
  noinst_LTLIBRARIES        = lib@LIBNAVIT@.la
endif
lib@LIBNAVIT@_la_LIBADD = @NAVIT_LIBS@ @WORDEXP_LIBS@ @ZLIB_LIBS@ @INTLLIBS@ -Lfib-1.1 -lfib @MODULE_LIBADD@ @CRYPTO_LIBS@

if SUPPORT_WIN32CE
  lib@LIBNAVIT@_la_LIBADD += -Lsupport/libc -lsupport_libc
endif

pkgdata_DATA = navit.xml

EXTRA_DIST = navit_shipped.xml navit.dtd

lib@LIBNAVIT@_la_SOURCES = announcement.c atom.c attr.c cache.c callback.c command.c compass.c config_.c coord.c country.c data_window.c debug.c \
	event.c event_glib.h file.c graphics.c gui.c item.c layout.c log.c main.c map.c \
	linguistics.c mapset.c maptype.c menu.c messages.c bookmarks.c bookmarks.h navit.c navigation.c osd.c param.c phrase.c plugin.c popup.c \
	profile.c projection.c roadprofile.c route.c routech.c search.c speech.c start_real.c transform.c track.c \
	util.c vehicle.c vehicleprofile.c xmlconfig.c announcement.h atom.h attr.h attr_def.h cache.h callback.h color.h command.h compass.h config_.h coord.h country.h \
	android.h data.h data_window.h data_window_int.h debug.h destination.h draw_info.h endianess.h event.h \
	file.h graphics.h gtkext.h gui.h item.h item_def.h keys.h log.h layer.h layout.h linguistics.h main.h map-share.h map.h\
	map_data.h mapset.h maptype.h menu.h messages.h navigation.h navit.h osd.h \
	param.h phrase.h plugin.h point.h plugin_def.h projection.h popup.h route.h profile.h roadprofile.h search.h speech.h start_real.h \
	transform.h track.h types.h util.h vehicle.h vehicleprofile.h window.h xmlconfig.h zipfile.h \
	navit_nls.h sunriset.c sunriset.h glib_slice.h maptool/miniz.c

XSLTS=@XSLTS@
navit.xml: navit_shipped.xml  $(foreach xslt, $(subst $(comma), ,$(XSLTS)), $(addsuffix .xslt,$(addprefix xslt/,$(xslt)))) navit_android_xml
	cp $< navit.xml.new
	if [ ! -f navit.dtd ]; then cp -f $(top_srcdir)/navit/navit.dtd .; fi
	for i in $^; do if [ "$${i%.xslt}" != "$$i" ]; then echo "Applying $$i" ; @SAXON@ -snone navit.xml.new $$i >navit.xml.tmp || exit ; mv navit.xml.tmp navit.xml.new || exit ; fi ; done
	mv navit.xml.new navit.xml
	rm -f navit.xml.tmp

navit_android_xml: navit_shipped.xml
if SUPPORT_ANDROID
#	new android xml files for (mdpi,ldpi,hdpi)
	cp $(srcdir)/navit_shipped.xml navit_android.xml.new
	if [ ! -f navit.dtd ]; then cp -f $(top_srcdir)/navit/navit.dtd .; fi

#	# now apply any other xslt files (this would allow for pedestrian plugin)
#	for i in $(foreach xslt, $(subst $(comma), ,$(XSLTS)), $(addsuffix .xslt,$(addprefix xslt/,$(xslt)))); do if [ "$${i%.xslt}" != "$$i" ]; then j=`basename $$i` ; if [ "$$j" != "android.xslt" ] ; then echo "Android: Applying $$i" ; @SAXON@ -snone navit_android.xml.new $(srcdir)/"$$i" >navit_android.xml.new.tmp || exit ; mv navit_android.xml.new.tmp navit_android.xml.new || exit ; fi ; fi ; done
#	# for now disable pedestrian plugin (zoff99)

	@SAXON@ -snone navit_android.xml.new $(srcdir)/xslt/android_mdpi.xslt >navit_android_mdpi.xml.tmp
	@SAXON@ -snone navit_android.xml.new $(srcdir)/xslt/android_ldpi.xslt >navit_android_ldpi.xml.tmp
	@SAXON@ -snone navit_android.xml.new $(srcdir)/xslt/android_hdpi.xslt >navit_android_hdpi.xml.tmp
	rm -f navit_android.xml.new
	rm -f navit_android.xml.new.tmp
	mv navit_android_mdpi.xml.tmp navit_android_mdpi.xml
	mv navit_android_ldpi.xml.tmp navit_android_ldpi.xml
	mv navit_android_hdpi.xml.tmp navit_android_hdpi.xml
	rm -f navit_android_mdpi.xml.tmp
	rm -f navit_android_ldpi.xml.tmp
	rm -f navit_android_hdpi.xml.tmp
else
	echo "no Android support"
endif

if SUPPORT_ANDROID
  lib@LIBNAVIT@_la_SOURCES += android.c
  navit_SOURCES =
  navit_LDADD =

v_int = $$(cat $(srcdir)/android/Android_Market_Version_int.txt|grep v_int|cut -d ":" -f 2)
v_name = "1.0.0-"$$(LANG=C svnversion $(top_srcdir) 2> /dev/null|tr -d ":A-Z" 2> /dev/null)
b_arch = $(shell if [ "$(DO_X86_BUILD)" == "1" ]; then echo "x86";else echo "armeabi"; fi)


android/AndroidManifest.xml: android/AndroidManifest.xml.in
	mkdir -p $(builddir)/android
	mkdir -p $(builddir)/android/res2/drawable-hdpi
	mkdir -p $(builddir)/android/res2/drawable-mdpi
	mkdir -p $(builddir)/android/res2/drawable-ldpi

	sed '0,/<uses-sdk/!d' <$< >$@
	for i in $(subst $(comma), ,@ANDROID_PERMISSIONS@); do echo "    <uses-permission android:name=\"android.permission.$$i\" />" >>$@; done
	sed '0,/<uses-sdk/d' <$< >>$@
	sed -e "s/@ANDROID_VERSION_INT@/$(v_int)/" <$@ >android/AndroidManifest.xml.tmp
	mv android/AndroidManifest.xml.tmp $@
	sed -e "s/@ANDROID_VERSION_NAME@/$(v_name)/" <$@ >android/AndroidManifest.xml.tmp
	mv android/AndroidManifest.xml.tmp $@

android/build.xml: android/AndroidManifest.xml android/dummy.txt
#	this is needed, otherwise somehow the files cant be deleted
	mkdir -p $(builddir)/android/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android/* $(builddir)/android; fi
	touch $(srcdir)/android/dummy.txt
	touch $(builddir)/android/dummy.txt
	android update project -t @ANDROID_PROJECT@ -p android

apkg-all: all android/build.xml navit.xml

	mkdir -p android/libs/$(b_arch)
	cp .libs/*.so android/libs/$(b_arch)/libnavit.so
	for i in */*/.libs/*.so ; do sed 's&lib_data_data_com\.zoffcc\.applications\.zanavi_lib_&/data/data/com.zoffcc.applications.zanavi/lib/lib&' <$$i >android/libs/$(b_arch)/$$(basename $$i); done

	mkdir -p android/res/drawable-ldpi
	mkdir -p android/res/drawable-mdpi
	mkdir -p android/res/drawable-hdpi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(srcdir)/xpm && echo *.xpm); do convert $(srcdir)/xpm/$$i android/res/drawable-mdpi/$${i%.xpm}.png; done  ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(srcdir)/xpm && echo *.xpm); do convert -resize 75% $(srcdir)/xpm/$$i android/res/drawable-ldpi/$${i%.xpm}.png; done  ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(srcdir)/xpm && echo *.xpm); do convert -resize 150% $(srcdir)/xpm/$$i android/res/drawable-hdpi/$${i%.xpm}.png; done  ; fi

	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(srcdir)/xpm && echo *.png); do convert $(srcdir)/xpm/$$i android/res/drawable-mdpi/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(srcdir)/xpm && echo *.png); do convert -resize 75% $(srcdir)/xpm/$$i android/res/drawable-ldpi/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(srcdir)/xpm && echo *.png); do convert -resize 150% $(srcdir)/xpm/$$i android/res/drawable-hdpi/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done ; fi

	mkdir -p android/res2/drawable-hdpi
	mkdir -p android/res2/drawable-mdpi
	mkdir -p android/res2/drawable-ldpi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd $(builddir)/xpm && echo *.png); do convert $(builddir)/xpm/$$i android/res2/drawable-mdpi/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done ; fi ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd $(builddir)/xpm && echo *.png); do convert -resize 75% $(builddir)/xpm/$$i android/res2/drawable-ldpi/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done ; fi ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd $(builddir)/xpm && echo *.png); do convert -resize 150% $(builddir)/xpm/$$i android/res2/drawable-hdpi/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done ; fi ; fi

#	only use needed pngs to make size of apk smaller
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in flag_bk_tr.png flag_bk_wh.png mini_roundabout.png gui_active_32_32.png gui_arrow_right_32_32.png gui_arrow_left_32_32.png icon_32_32.png unknown_32_32.png gui_zoom_out_64_64.png gui_zoom_in_64_64.png gui_map_32_32.png gui_home_32_32.png gui_actions_64_64.png gui_settings_64_64.png gui_tools_64_64.png gui_about_64_64.png gui_inactive_32_32.png ; do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in flag_bk_tr.png flag_bk_wh.png mini_roundabout.png gui_active_32_32.png gui_arrow_right_32_32.png gui_arrow_left_32_32.png icon_32_32.png unknown_32_32.png gui_zoom_out_64_64.png gui_zoom_in_64_64.png gui_map_32_32.png gui_home_32_32.png gui_actions_64_64.png gui_settings_64_64.png gui_tools_64_64.png gui_about_64_64.png gui_inactive_32_32.png ; do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in flag_bk_tr.png flag_bk_wh.png mini_roundabout.png gui_active_32_32.png gui_arrow_right_32_32.png gui_arrow_left_32_32.png icon_32_32.png unknown_32_32.png gui_zoom_out_64_64.png gui_zoom_in_64_64.png gui_map_32_32.png gui_home_32_32.png gui_actions_64_64.png gui_settings_64_64.png gui_tools_64_64.png gui_about_64_64.png gui_inactive_32_32.png ; do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
#	gui icons
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-hdpi && echo gui*64_64*png); do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-mdpi && echo gui*64_64*png); do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-ldpi && echo gui*64_64*png); do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
#	nav icons
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-hdpi && echo nav_*png); do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-mdpi && echo nav_*png); do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-ldpi && echo nav_*png); do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
#	POIs
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in traffic_signals.png bank.png fuel.png gui_select_street_32_32.png gui_select_town_32_32.png gui_select_country_32_32.png ; do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in traffic_signals.png bank.png fuel.png gui_select_street_32_32.png gui_select_town_32_32.png gui_select_country_32_32.png ; do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then for i in traffic_signals.png bank.png fuel.png gui_select_street_32_32.png gui_select_town_32_32.png gui_select_country_32_32.png ; do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
# rest of nav_ icons
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -v $(builddir)/android/res2/drawable-hdpi/nav_destination_bk_59_59.png android/res/drawable-hdpi/nav_destination_bk.png ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -v $(builddir)/android/res2/drawable-mdpi/nav_destination_bk_59_59.png android/res/drawable-mdpi/nav_destination_bk.png ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -v $(builddir)/android/res2/drawable-ldpi/nav_destination_bk_59_59.png android/res/drawable-ldpi/nav_destination_bk.png ; fi

#	clean up
#	rm android/res2/drawable-hdpi/*
#	rm android/res2/drawable-mdpi/*
#	rm android/res2/drawable-ldpi/*


	mkdir -p android/res/raw
	for i in $(shell cd ../po && echo *.mo); do cp ../po/$$i android/res/raw/$$( echo $$i | tr "[A-Z]" "[a-z]") ; done
#	cp navit.xml android/res/raw
	cp navit_android_mdpi.xml android/res/raw/navitmdpi.xml
	cp navit_android_ldpi.xml android/res/raw/navitldpi.xml
	cp navit_android_hdpi.xml android/res/raw/navithdpi.xml

#	cleanup svn dirs, they should never be in build dir
	if [ "$(builddir)" != "$(srcdir)" ]; then find $(builddir) -type d -name '\.svn' -exec rm -Rf {} \; ; fi; echo "ignore the find errors!!"

	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/libs/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then install -p $(NDK)/toolchains/arm-linux-androideabi-4.4.3/prebuilt/gdbserver android/libs/$(b_arch)/gdbserver ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/libs/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/obj/local/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then cp android/libs/$(b_arch)/*.so android/obj/local/$(b_arch)/ ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then echo "set solib-search-path $(builddir)/obj/local/$(b_arch)" > android/libs/$(b_arch)/gdb.setup ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then echo "directory $(NDK)/platforms/android-""$(AND_API_LEVEL_C)""/arch-arm/usr/include $(NDK)/sources/cxx-stl/system" >> android/libs/$(b_arch)/gdb.setup ; fi

#	strip libs if release build is selected
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then echo "stripping libs ..."; arm-linux-androideabi-strip --strip-unneeded $(builddir)/android/libs/armeabi/* ; echo "done" ; fi
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then echo "stripping libs ..."; i686-android-linux-strip --strip-unneeded $(builddir)/android/libs/x86/* ; echo "done" ; fi


apkg: apkg-all
	touch $(srcdir)/android/dummy.txt
	touch $(builddir)/android/dummy.txt
	cd android && ant debug
#	zipalign is already run automatically

apkg-release: apkg-all
	touch $(srcdir)/android/dummy.txt
	touch $(builddir)/android/dummy.txt
	cd android && ant release
#	jarsigner $$(cat ~/.keys_args) android/bin/Navit-unsigned.apk mykey
#	zipalign -f 4 android/bin/Navit-unsigned.apk android/bin/Navit-aligned.apk
	zipalign -f 4 android/bin/Navit-release-unsigned.apk android/bin/Navit-aligned.apk
	cp android/bin/Navit-aligned.apk android/bin/Navit-debug.apk
else
navit_SOURCES = start.c
navit_LDADD = lib@LIBNAVIT@.la

endif

if EVENT_GLIB
  lib@LIBNAVIT@_la_SOURCES += event_glib.c
endif

if !PLUGINS
  lib@LIBNAVIT@_la_SOURCES += builtin.c
  lib@LIBNAVIT@_la_LIBADD += $(MODULES)
endif

navit_LDFLAGS = -export-dynamic

navit_LDADD += @NAVIT_LIBS@ @WORDEXP_LIBS@ @ZLIB_LIBS@ @CRYPTO_LIBS@ @INTLLIBS@

if SUPPORT_WIN32
  navit_LDADD += resource.rsc
if SUPPORT_WIN32CE
  navit_LDADD += -lcommctrl -Lsupport/libc
else
  navit_LDADD += -lgdi32 -lcomctl32 -lwinmm
endif


resource.rsc: $(top_srcdir)/navit/gui/win32/resources/resource.rc
	$(WINDRES) -I $(top_srcdir)/navit/gui/win32/resources $(top_srcdir)/navit/gui/win32/resources/resource.rc resource.rsc
endif

.PHONY: version.h.tmp

version.h.tmp:
	echo "#include \"config.h\"" >version.h.tmp
if SOURCE_MODE_SVN
	echo "#define SVN_VERSION \"$$(LANG=C svnversion $(top_srcdir) 2>/dev/null)\"" >>version.h.tmp
else
	echo "#define SVN_VERSION \"@SOURCE_MODE@\"" >>version.h.tmp
endif
	echo "#define NAVIT_VARIANT \"@NAVIT_VARIANT@\"" >>version.h.tmp
	if ! diff version.h.tmp version.h >/dev/null 2>/dev/null; \
	then \
		mv version.h.tmp version.h; \
	fi

version.h: version.h.tmp

navit_config.h: ../config.h
	cp ../config.h navit_config.h.tmp
	if ! diff navit_config.h.tmp navit_config.h >/dev/null 2>/dev/null; \
	then \
		mv navit_config.h.tmp navit_config.h; \
	fi


distclean-local:
	rm -f version.h version.h.tmp navit_config.h navit_config.h.tmp builtin.c support-builtin.c navit.xml
	if [ "$(top_builddir)" != "$(top_srcdir)" ]; then rm -f navit.dtd; fi

builtin.c: $(top_builddir)/config.h
	ls $(top_builddir)/navit/*/*/*.la | sed -e "s/\.la/_init(void);/" -e "s/.*lib/extern void module_/" >builtin.c
	echo "extern void builtin_init(void);" >>builtin.c
	echo "void builtin_init(void) {" >>builtin.c
	ls $(top_builddir)/navit/*/*/*.la | sed -e "s/\.la/_init();/" -e "s/.*\\/lib/	module_/" >>builtin.c
	echo "}" >>builtin.c
support-builtin.c: $(top_builddir)/config.h
	ls $(top_builddir)/navit/support/*/*.la | sed -e "s/\.la/_init(void);/" -e "s/.*lib/extern void module_/" >support-builtin.c
	echo "extern void builtin_init(void);" >>support-builtin.c
	echo "void builtin_init(void) {" >>support-builtin.c
	ls $(top_builddir)/navit/support/*/*.la | sed -e "s/\.la/_init();/" -e "s/.*\\/lib/	module_/" >>support-builtin.c
	echo "}" >>support-builtin.c
