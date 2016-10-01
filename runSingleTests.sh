#!/bin/bash
green='\033[0;32m'
red='\033[0;31m'
lightRed='\033[1;31m'
orange='\033[0;33m'
nc='\033[0m'

./gradlew jar
WORKDIR=`echo 'tests/workdir'`
rm -rf $WORKDIR && mkdir -p $WORKDIR

echo -e "${orange}Log test${nc}"

cd $WORKDIR
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
echo "file12" >> file1.txt
echo "file3" >> file3.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "second"

