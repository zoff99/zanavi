#! /bin/bash

echo '<project name="ZANavi"><target name="clean"/></project>' > build.xml
mkdir -p po/lp/navit-orig-import
rm navit/maptool/poly2tri-c/001/seidel-1.0/triangulate
rm pngout-static
rm -Rf navit/material-intro
rm -Rf navit/appintro
rm -Rf navit/support/espeak/espeak-data/*
