#!/bin/bash

saxon -s:../../BonfireChat/app/build/reports/checkstyle/checkstyle.xml -xsl:checkstyle-noframes-sorted.xsl -o:../belege/checkstyle/checkstyle.html
