<img src="https://cloud.githubusercontent.com/assets/16841860/23113427/4eb1e016-f738-11e6-9b71-7503210245a4.png" width="530" />

**Travis:** [![Build Status](https://travis-ci.org/zoff99/zanavi.png?branch=master)](https://travis-ci.org/zoff99/zanavi/branches)
**CircleCI:** [![CircleCI](https://circleci.com/gh/zoff99/zanavi/tree/master.png?style=badge)](https://circleci.com/gh/zoff99/zanavi/tree/master)

ZANavi is a fork of NavIT. It is for the Android platfrom only!
for more details look at our website

http://zanavi.cc

### Compiling (command line)
```
git clone https://github.com/zoff99/zanavi
cd zanavi
git checkout master
./download-androidstudio-files.sh
cd navit
./gradlew assembleRelease --stacktrace
find . -name '*.apk' -exec ls -al {} \;
```

### Compiling Android Studio
first do this **outside** of Android Studio!
```
git clone https://github.com/zoff99/zanavi
cd zanavi
git checkout master
./download-androidstudio-files.sh
```
now start Android Studio and select "import Project" then select the **navit** subdirectory<BR>
in Android Studio just press "play"

### Development Snapshot Version
the latest Development Snapshot can be downloaded from CircleCI, [here](https://circleci.com/api/v1/project/zoff99/zanavi/latest/artifacts/0/$CIRCLE_ARTIFACTS/zanavi.apk?filter=successful&branch=master)

### Coding Style
https://github.com/zoff99/Code-Style-Guidelines/blob/master/Android/Java.md

### tagsoup-1.2.1.jar:
http://home.ccil.org/~cowan/tagsoup/

downloaded from: http://home.ccil.org/~cowan/tagsoup/tagsoup-1.2.1.jar



