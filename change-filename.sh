#!/bin/bash
oldStr="xz"
newStr="ggx"


for i in $(ls); do 

if [ -d $i ] 
then 
echo $i;
cd $i;
for j in $(ls); do mv $j ${j/$oldStr/$newStr};  done
cd ../;
fi

done

for i in $(ls); do mv $i ${i/$oldStr/$newStr};  done