#!/bin/bash

for file in $@
do
  while read var
  do
    tbr="$(echo "$var" | cut -d / -f 1)"
    rep="$(echo "$var" | cut -d / -f 2)"
    sed -i s/"$tbr"/"$rep"/g "$file"
  done < translation 
done

