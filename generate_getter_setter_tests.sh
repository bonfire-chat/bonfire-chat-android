#!/bin/bash

object=$2

while read line
do
  for type in Get Set
  do
    echo "public void test$type$(echo $line | cut -d: -f1 | perl -ne 'print ucfirst($_)')(){"
    if [ $type == "Set" ] 
    then
      value=$(echo $line | cut -d: -f3)
      echo "$object.set$(echo $line | cut -d: -f1 | perl -ne 'print ucfirst($_)')($value);"
    else
      value=$(echo $line | cut -d: -f2)
    fi 
      echo "assertEquals($value, $object.get$(echo $line | cut -d: -f1 | perl -ne 'print ucfirst($_)')());"
    echo "}"
    echo ""
  done
done < $1
