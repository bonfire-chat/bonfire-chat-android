#!/bin/bash

for file in ../belege/checkstyle/*.xml
do
  dst=$file".html"
  saxon -s:$file -xsl:checkstyle-noframes-sorted.xsl -o:$dst
done
