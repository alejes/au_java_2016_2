#!/bin/bash
green='\033[0;32m'
red='\033[0;31m'
lightRed='\033[1;31m'
orange='\033[0;33m'
nc='\033[0m'

./gradlew jar
WORKDIR=`echo 'tests/workdir'`
rm -rf $WORKDIR && mkdir -p $WORKDIR
cd $WORKDIR
echo -e "${orange}Status test${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar init
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`
if [[ "$RESULT" != `echo -ne "On branch master:\nChanges not staged for commit:\nNEW file1.txt\nNEW file2.txt"` ]]
then
	echo -e "${red}Wrong status #1${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar add file1.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`
if [[ "$RESULT" != `echo -ne "On branch master:\nChanges to be committed:\nfile1.txt\nChanges not staged for commit:\nNEW file2.txt"` ]]
then
	echo -e "${red}Wrong status #2${nc}"
fi

echo "file3" >> file1.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`

if [[ "$RESULT" != `echo -ne "On branch master:\nChanges to be committed:\nfile1.txt\nChanges not staged for commit:\nMODIFIED file1.txt\nNEW file2.txt"` ]]
then
	echo -e "${red}Wrong status #3${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar add file2.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`


if [[ "$RESULT" != `echo -ne "On branch master:\nChanges to be committed:\nfile1.txt\nfile2.txt\nChanges not staged for commit:\nMODIFIED file1.txt"` ]]
then
	echo -e "${red}Wrong status #4${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar add file1.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`

if [[ "$RESULT" != `echo -ne "On branch master:\nChanges to be committed:\nfile1.txt\nfile2.txt"` ]]
then
	echo -e "${red}Wrong status #5${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar reset file1.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`

if [[ "$RESULT" != `echo -ne "On branch master:\nChanges to be committed:\nfile2.txt\nChanges not staged for commit:\nNEW file1.txt"` ]]
then
	echo -e "${red}Wrong status #6${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar commit "initial commit"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`


if [[ "$RESULT" != `echo -ne "On branch master:\nChanges not staged for commit:\nNEW file1.txt"` ]]
then
	echo -e "${red}Wrong status #7${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar rm file2.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`

if [[ "$RESULT" != `echo -ne "On branch master:\nFiles will be deleted:\nfile2.txt\nChanges not staged for commit:\nNEW file1.txt"` ]]
then
	echo -e "${red}Wrong status #8${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar reset file2.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar status`

if [[ "$RESULT" != `echo -ne "On branch master:\nChanges not staged for commit:\nNEW file1.txt"` ]]
then
	echo -e "${red}Wrong status #9${nc}"
fi

RESULT=`cat file2.txt`

if [[ "$RESULT" != `echo -ne "file2"` ]]
then
	echo -e "${red}Wrong file after reset delete #10${nc}"
fi