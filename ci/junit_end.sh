#!/bin/bash

xml="$1"

echo '   </testsuite>
</testsuites>' >> "$xml"

