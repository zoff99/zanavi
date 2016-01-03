#!/bin/bash

xml="$1"

tests_res="$2" # 0 -> ok, 1 -> fail, (2 -> skipped)

tests_name="$3"
tests_header="$4"
tests_message="$5"

tests_name=`echo "$tests_name" | tr '<"> \\&' '_'`
tests_header=`echo "$tests_header" | tr '<"> \\&' '_'`
tests_message=`echo "$tests_message" | tr '<"> \\&' '_'`


if [ "$tests_res""x" == "0x" ]; then

        echo '      <testcase classname="JUnitXmlReporter.constructor" name="'"$tests_name"'" time="0" />' >> "$xml"

else

        echo '      <testcase classname="JUnitXmlReporter.constructor" name="'"$tests_name"'" time="0">
         <failure message="'"$tests_header"'">'"$tests_message"'</failure>
      </testcase>' >> "$xml"

fi

