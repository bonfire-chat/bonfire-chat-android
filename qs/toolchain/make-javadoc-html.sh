#!/bin/bash

DEST=javadoc.html

cat javadoc-partials-start.html > $DEST

for file in ../belege/junit/javadoc/de/tudarmstadt/informatik/bp/bonfirechat/{models,data,helper}/test/*Test.html
do
  cat $file | sed -n '/<div class="subTitle">/,/<\/div>/p' >> $DEST
  cat $file | sed -n '/<!-- ============ METHOD DETAIL ========== -->/,/<!-- ========= END OF CLASS DATA ========= -->/p' >> $DEST
done

cat javadoc-partials-end.html >> $DEST
