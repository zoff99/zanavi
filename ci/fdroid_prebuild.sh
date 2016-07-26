#! /bin/bash

echo '<project name="ZANavi"><target name="clean"/></project>' > build.xml

mkdir -p po/lp/navit-orig-import
rm navit/maptool/poly2tri-c/001/seidel-1.0/triangulate
rm -Rf navit/support/espeak/espeak-data/*

rm pngout-static
echo '#! /bin/bash' > pngout-static && echo 'echo $*' >> pngout-static && chmod u+rx pngout-static
