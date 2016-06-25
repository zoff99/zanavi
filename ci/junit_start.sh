#!/bin/bash

xml="$1"
tests_count="$2"
tests_fail="$3"
tests_skipped="0"

echo '<?xml version="1.0" encoding="UTF-8"?>
<testsuites>
   <testsuite name="JUnitXmlReporter" errors="0" tests="0" failures="0" time="0" />
   <testsuite name="JUnitXmlReporter.constructor" errors="0" skipped="0" tests="'"$tests_count"'" failures="'"$tests_fail"'" time="0" >
      <properties>
         <property name="java.vendor" value="Sun Microsystems Inc." />
         <property name="app" value="ZANavi for Android" />
         <property name="developer.name" value="Zoff" />
         <property name="developer.email" value="zoff@zanavi.cc" />
      </properties>' > "$xml"

