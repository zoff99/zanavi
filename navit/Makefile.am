include $(top_srcdir)/Makefile.inc
comma=,
SUBDIRS=fib-1.1 support
if PLUGINS
  SUBDIRS += .
endif
DIST_SUBDIRS=autoload binding map maptool fib-1.1 font fonts gui graphics osd plugin speech vehicle support xpm maps
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
else
  bin_PROGRAMS = navit2
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
	navit_nls.h sunriset.c sunriset.h glib_slice.h maptool/miniz.c \
	snprintf.c snprintf.h


if SUPPORT_ANDROID
  lib@LIBNAVIT@_la_SOURCES += vehicle/android/vehicle_android.c \ 
	vehicle/demo/vehicle_demo.c \
	gui/internal/gui_internal.c \
	graphics/android/graphics_android.c \
	map/binfile/binfile.c \
	map/csv/csv.c \
	map/csv/quadtree.c \
	map/textfile/textfile.c \
	osd/core/osd_core.c \
	speech/android/speech_android.c
endif


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
# v_name = "1.0.0-"$$(LANG=C svnversion $(top_srcdir) 2> /dev/null|tr -d ":A-Z" 2> /dev/null)
v_name = $$(cat $(srcdir)/android/Android_Market_Version_int.txt|grep v_name|cut -d ":" -f 2)
b_arch = $(shell if [ "$(DO_X86_BUILD)" == "1" ]; then echo "x86"; elif [ "$(DO_ARMV7_BUILD)" == "1" ]; then echo "armeabi-v7a"; else echo "armeabi"; fi)


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

	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android/lib/; fi
	mkdir -p $(builddir)/android/lib/
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -pf $(srcdir)/android/tagsoup-1.2.1.jar $(builddir)/android/lib/; fi

	touch $(srcdir)/android/dummy.txt
	touch $(builddir)/android/dummy.txt

#	mkdir -p $(builddir)/actionbarsherlock/src/
#	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/actionbarsherlock/src/; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/actionbarsherlock/src/; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/actionbarsherlock/* $(builddir)/actionbarsherlock; fi
#	touch $(srcdir)/actionbarsherlock/dummy.txt
#	touch $(builddir)/actionbarsherlock/dummy.txt
#
#	android update project -t @ANDROID_PROJECT@ -p android -l ../actionbarsherlock
#	echo "jar.libs.dir=lib" >> $(builddir)/android/ant.properties

	mkdir -p $(builddir)/android-support-v7-appcompat/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-support-v7-appcompat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-support-v7-appcompat/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-support-v7-appcompat/* $(builddir)/android-support-v7-appcompat/; fi
	touch $(srcdir)/android-support-v7-appcompat/dummy.txt
	touch $(builddir)/android-support-v7-appcompat/dummy.txt

	mkdir -p $(builddir)/android-about-page/src/
	if [ "$(builddir)" != "$(srcdir)" ]; then chmod -R u+w $(builddir)/android-about-page/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then rm -Rf $(builddir)/android-about-page/src/; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -rpf $(srcdir)/android-about-page/* $(builddir)/android-about-page/; fi
	touch $(srcdir)/android-about-page/dummy.txt
	touch $(builddir)/android-about-page/dummy.txt

	if [ "$(builddir)" != "$(srcdir)" ]; then mkdir -p $(builddir)/android/src/com/zoffcc/applications/zanavi_msg/ ; fi
	if [ "$(builddir)" != "$(srcdir)" ]; then cp -pf $(builddir)/android/aidl/com/zoffcc/applications/zanavi_msg/*.aidl $(builddir)/android/src/com/zoffcc/applications/zanavi_msg/ ; fi

#	android update lib-project -t @ANDROID_PROJECT@ -p $(builddir)/android-support-v7-appcompat
	echo "XXXXXXXXXX YYZZ 001"
	pwd
	cd ../android-about-page
	echo "XXXXXXXXXX YYZZ 002"
	pwd
	android update project -t @ANDROID_PROJECT@ -p ./ -l ../android-support-v7-appcompat
	echo "XXXXXXXXXX YYZZ 003"
	pwd
	cd ../
	echo "XXXXXXXXXX YYZZ 004"
	pwd

	android update project -t @ANDROID_PROJECT@ -p android -l ../android-support-v7-appcompat
	android update project -t @ANDROID_PROJECT@ -p android -l ../android-about-page
	echo "jar.libs.dir=lib" >> $(builddir)/android/ant.properties


apkg-all: all android/build.xml navit.xml
	mkdir -p android/libs/$(b_arch)
	cp .libs/*.so android/libs/$(b_arch)/libnavit.so
	for i in */*/.libs/*.so ; do sed 's&lib_data_data_com\.zoffcc\.applications\.zanavi_lib_&/data/data/com.zoffcc.applications.zanavi/lib/lib&' <$$i >android/libs/$(b_arch)/$$(basename $$i); done

	mkdir -p android/res/drawable-ldpi
	mkdir -p android/res/drawable-mdpi
	mkdir -p android/res/drawable-hdpi
	mkdir -p android/res/drawable-xhdpi
	mkdir -p android/res/drawable-xxhdpi

	mkdir -p android/res2/drawable-hdpi
	mkdir -p android/res2/drawable-mdpi
	mkdir -p android/res2/drawable-ldpi

# all icons and imgs
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo *_16_16.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-ldpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_16_16xx') ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo *_32_32.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-mdpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_32_32xx') ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo *_48_48.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-hdpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_48_48xx') ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo *_48_48.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-xhdpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_48_48xx') ; done ; fi

#	nav icons (overwrite with larger versions)
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo nav_*_48_48.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-ldpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_48_48xx') ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo nav_*_48_48.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-mdpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_48_48xx') ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo nav_*_96_96.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-hdpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_96_96xx') ; done ; fi
	if [ "$(DO_PNG_BUILD)" == "1" ]; then for i in $(shell cd $(builddir)/xpm && echo nav_*_192_192.png); do cp -v $(builddir)/xpm/$$i android/res/drawable-xhdpi/$$( echo $$i | tr "[A-Z]" "[a-z]" | sed -e 'sx_192_192xx') ; done ; fi


#	only use needed pngs to make size of apk smaller
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in flag_bk_tr.png flag_bk_wh.png mini_roundabout.png gui_active_32_32.png gui_arrow_right_32_32.png gui_arrow_left_32_32.png icon_32_32.png unknown_32_32.png gui_zoom_out_64_64.png gui_zoom_in_64_64.png gui_map_32_32.png gui_home_32_32.png gui_actions_64_64.png gui_settings_64_64.png gui_tools_64_64.png gui_about_64_64.png gui_inactive_32_32.png ; do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in flag_bk_tr.png flag_bk_wh.png mini_roundabout.png gui_active_32_32.png gui_arrow_right_32_32.png gui_arrow_left_32_32.png icon_32_32.png unknown_32_32.png gui_zoom_out_64_64.png gui_zoom_in_64_64.png gui_map_32_32.png gui_home_32_32.png gui_actions_64_64.png gui_settings_64_64.png gui_tools_64_64.png gui_about_64_64.png gui_inactive_32_32.png ; do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in flag_bk_tr.png flag_bk_wh.png mini_roundabout.png gui_active_32_32.png gui_arrow_right_32_32.png gui_arrow_left_32_32.png icon_32_32.png unknown_32_32.png gui_zoom_out_64_64.png gui_zoom_in_64_64.png gui_map_32_32.png gui_home_32_32.png gui_actions_64_64.png gui_settings_64_64.png gui_tools_64_64.png gui_about_64_64.png gui_inactive_32_32.png ; do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
#	gui icons
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-hdpi && echo gui*64_64*png); do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-mdpi && echo gui*64_64*png); do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-ldpi && echo gui*64_64*png); do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
#	nav icons
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-hdpi && echo nav_*png); do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-mdpi && echo nav_*png); do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in $(shell cd android/res2/drawable-ldpi && echo nav_*png); do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
#	POIs
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in traffic_signals.png atm.png bank.png fuel.png gui_select_street_32_32.png gui_select_town_32_32.png gui_select_country_32_32.png ; do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in traffic_signals.png atm.png bank.png fuel.png gui_select_street_32_32.png gui_select_town_32_32.png gui_select_country_32_32.png ; do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in traffic_signals.png atm.png bank.png fuel.png gui_select_street_32_32.png gui_select_town_32_32.png gui_select_country_32_32.png ; do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
#	more POIs
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in airport.png attraction.png bar.png beer.png bench.png boat_ramp.png boundary_stone.png bus.png cafe.png camping.png church.png cinema.png danger.png exit.png fastfood.png firebrigade.png fountain.png golf.png heliport.png hospital.png hotel.png information.png justice.png mark.png military.png mini_roundabout.png museum.png parking.png peak.png pharmacy.png police.png post.png repair_service.png resort.png restaurant.png ruins.png school.png shop_grocery.png shopping.png skiing.png stadium.png swimming.png tec_common.png telephone.png toilets.png viewpoint.png wifi.png zoom_in.png zoom_out.png zoo.png ; do cp -v $(builddir)/android/res2/drawable-hdpi/$$i android/res/drawable-hdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in airport.png attraction.png bar.png beer.png bench.png boat_ramp.png boundary_stone.png bus.png cafe.png camping.png church.png cinema.png danger.png exit.png fastfood.png firebrigade.png fountain.png golf.png heliport.png hospital.png hotel.png information.png justice.png mark.png military.png mini_roundabout.png museum.png parking.png peak.png pharmacy.png police.png post.png repair_service.png resort.png restaurant.png ruins.png school.png shop_grocery.png shopping.png skiing.png stadium.png swimming.png tec_common.png telephone.png toilets.png viewpoint.png wifi.png zoom_in.png zoom_out.png zoo.png ; do cp -v $(builddir)/android/res2/drawable-mdpi/$$i android/res/drawable-mdpi/ ; done ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then for i in airport.png attraction.png bar.png beer.png bench.png boat_ramp.png boundary_stone.png bus.png cafe.png camping.png church.png cinema.png danger.png exit.png fastfood.png firebrigade.png fountain.png golf.png heliport.png hospital.png hotel.png information.png justice.png mark.png military.png mini_roundabout.png museum.png parking.png peak.png pharmacy.png police.png post.png repair_service.png resort.png restaurant.png ruins.png school.png shop_grocery.png shopping.png skiing.png stadium.png swimming.png tec_common.png telephone.png toilets.png viewpoint.png wifi.png zoom_in.png zoom_out.png zoo.png ; do cp -v $(builddir)/android/res2/drawable-ldpi/$$i android/res/drawable-ldpi/ ; done ; fi
# rest of nav_ icons
#	if [ "$(builddir)" != "$(srcdir)" ]; then cp -v $(builddir)/android/res2/drawable-hdpi/nav_destination_bk_59_59.png android/res/drawable-hdpi/nav_destination_bk.png ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then cp -v $(builddir)/android/res2/drawable-mdpi/nav_destination_bk_59_59.png android/res/drawable-mdpi/nav_destination_bk.png ; fi
#	if [ "$(builddir)" != "$(srcdir)" ]; then cp -v $(builddir)/android/res2/drawable-ldpi/nav_destination_bk_59_59.png android/res/drawable-ldpi/nav_destination_bk.png ; fi

# if release build -> then compress png images (this takes a long time!!!)
#
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then if [ "$(builddir)" != "$(srcdir)" ]; then find android/res/drawable-hdpi/ -maxdepth 1 -type f -exec $(srcdir)/../pngout-static -c6 -f6 -y {} \; ; fi ; fi
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then if [ "$(builddir)" != "$(srcdir)" ]; then find android/res/drawable-mdpi/ -maxdepth 1 -type f -exec $(srcdir)/../pngout-static -c6 -f6 -y {} \; ; fi ; fi
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then if [ "$(builddir)" != "$(srcdir)" ]; then find android/res/drawable-ldpi/ -maxdepth 1 -type f -exec $(srcdir)/../pngout-static -c6 -f6 -y {} \; ; fi ; fi

#	remove old libs
	ls -al android/libs/$(b_arch)/lib*.so
	rm -f android/libs/$(b_arch)/libgraphics_android.so
	rm -f android/libs/$(b_arch)/libgraphics_null.so
	rm -f android/libs/$(b_arch)/libgui_internal.so
	rm -f android/libs/$(b_arch)/libmap_binfile.so
	rm -f android/libs/$(b_arch)/libmap_csv.so
	rm -f android/libs/$(b_arch)/libmap_filter.so
	rm -f android/libs/$(b_arch)/libmap_mg.so
	rm -f android/libs/$(b_arch)/libmap_shapefile.so
	rm -f android/libs/$(b_arch)/libmap_textfile.so
	rm -f android/libs/$(b_arch)/libosd_core.so
	rm -f android/libs/$(b_arch)/libspeech_android.so
	rm -f android/libs/$(b_arch)/libvehicle_android.so
	rm -f android/libs/$(b_arch)/libvehicle_demo.so
	rm -f android/libs/$(b_arch)/libvehicle_file.so
	rm -f android/libs/$(b_arch)/libvehicle_pipe.so
	rm -f android/libs/$(b_arch)/libvehicle_serial.so
	rm -f android/libs/$(b_arch)/libvehicle_socket.so

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
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then install -p $(NDK)/prebuilt/android-arm/gdbserver/gdbserver android/libs/$(b_arch)/gdbserver ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/libs/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then mkdir -p android/obj/local/$(b_arch) ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then cp android/libs/$(b_arch)/*.so android/obj/local/$(b_arch)/ ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then echo "set solib-search-path $(builddir)/obj/local/$(b_arch)" > android/libs/$(b_arch)/gdb.setup ; fi
	if [ "$(DO_RELEASE_BUILD)" != "1" ]; then echo "directory $(NDK)/platforms/android-""$(AND_API_LEVEL_C)""/arch-arm/usr/include $(NDK)/sources/cxx-stl/system" >> android/libs/$(b_arch)/gdb.setup ; fi

#	strip libs if release build is selected
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then echo "stripping libs ..."; arm-linux-androideabi-strip --strip-unneeded $(builddir)/android/libs/armeabi/* ; echo "done" ; fi
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then echo "stripping libs ..."; arm-linux-androideabi-strip --strip-unneeded $(builddir)/android/libs/armeabi-v7a/* ; echo "done" ; fi
	if [ "$(DO_RELEASE_BUILD)" == "1" ]; then echo "stripping libs ..."; /home/navit/_navit_develop/_need/NDK/_unpack/android-ndk-r5c/toolchains/x86-4.8/prebuilt/linux-x86/bin/i686-linux-android-strip --strip-unneeded $(builddir)/android/libs/x86/* ; echo "done" ; fi


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
	/home/navit/_navit_develop/_need/SDK/_unpack/android-sdk-linux_SDK_r22.0.1/build-tools/21.1.1/zipalign -f 4 android/bin/Navit-release-unsigned.apk android/bin/Navit-aligned.apk
	cp android/bin/Navit-aligned.apk android/bin/Navit-debug.apk
else
navit_SOURCES = start.c
navit_LDADD = lib@LIBNAVIT@.la

endif


	
if SUPPORT_ANDROID

navit2_SOURCES = navit2.c
navit2_LDADD = lib@LIBNAVIT@.la
navit2_LDADD += @NAVIT_LIBS@ @WORDEXP_LIBS@ @ZLIB_LIBS@ @CRYPTO_LIBS@ @INTLLIBS@
navit2_LDFLAGS = -static -lc
navit2_LINK = $(LIBTOOL) --tag=CC --mode=link $(CC) -o navit2 -lc --sysroot=$(NDK)/platforms/android-$(AND_API_LEVEL_C)/arch-arm

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
