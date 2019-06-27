#!/bin/bash
find -iname *.sql |\
 while read line ; do file --mime-encoding --separator=# "$line" ; done |\
 grep 'utf-16le'|\
 awk --field-separator=# '{print "iconv --from-code=UTF-16LE --to-code=UTF-8 "$1" --output="$1}' |\
 sh

