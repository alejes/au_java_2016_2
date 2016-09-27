#!/bin/bash
green='\033[0;32m'
red='\033[0;31m'
lightRed='\033[1;31m'
orange='\033[0;33m'
nc='\033[0m'

./gradlew jar
WORKDIR=`echo 'tests/workdir'`
mkdir -p $WORKDIR

echo -e "${orange}Log test${nc}"

cd $WORKDIR
echo "file2" >> file2.txt
echo "file1" >> file1.txt

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "second"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs log | wc -l`
if [[ "$RESULT" -ne "7" ]]
then
	echo -e "${red}Wrong log after 2 commits${nc}"
fi
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs log | wc -l`
if [[ "$RESULT" -ne "1" ]]
then
	echo -e "${red}Not empty log after checkout${nc}"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first2"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs log | wc -l`
if [[ "$RESULT" -ne "4" ]]
then
	echo -e "${red}Not only one commit afrer checkout and commit${nc}"
fi

cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Commits test${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"

RESULT=`diff -r . .vcs/1/1 | grep .`

if [[ "$RESULT" != 'Only in .: .vcs' ]]
then 
	echo -e "${red}Wrong commit result${nc}"
fi



cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Checkout test${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
echo "file3" >> file3.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "master"
RESULT=`ls`
if [[ "$RESULT" != `echo -ne "file1.txt\nfile2.txt"` ]]
then 
	echo -e "${red}Wrong checkout on branch${nc}"
fi


cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Checkout not found test${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "branch"`
if [[ "$RESULT" != `echo -ne "VCS exception: Not found target of checkout"` ]]
then
	echo -e "${red}Cannot found exception of wrong branch${nc}"
fi


cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Checkout new and old branch${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
echo "file3" >> file3.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "second"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "master"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "branch"
RESULT=`ls`
if [[ "$RESULT" != `echo -ne "file1.txt\nfile2.txt\nfile3.txt"` ]]
then
	echo -e "${red}Wrong checkout on branch${nc}"
fi

cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Checkout on commit${nc}"
echo "file1" >> file1.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "second"
echo "file3" >> file3.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "third"
echo "file4" >> file4.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "fourth"
echo "file5" >> file5.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "fifth"
echo "file6" >> file6.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "sixth"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "master"
echo "file7" >> file7.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "seventh"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "5"
RESULT=`ls`
if [[ "$RESULT" != `echo -ne "file1.txt\nfile2.txt\nfile3.txt\nfile4.txt\nfile5.txt"` ]]
then
	echo -e "${red}Wrong checkout on branch${nc}"
fi


cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Delete branch${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
echo "file3" >> file3.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "second"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs branch -d "master"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "master"`
if [[ "$RESULT" != `echo -ne "VCS exception: Not found target of checkout"` ]]
then
	echo -e "${red}Wrong deleted branch${nc}"
fi

cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Merge test${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
echo "file1_!" >> file1.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "branchcommit"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "master"
echo "file1_!from" >> file1.txt
echo "file1_!from2" >> file1.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs merge "branch"
echo -ne 'file1\n+file1_!from\n-file1_!\n+file1_!from2' > file1.diff
diff file1.diff file1.txt


cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Merge test 2${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
echo "file1_!" >> file1.txt
echo "file1_2" >> file1.txt
echo "file1_common1" >> file1.txt
echo "file1_common2" >> file1.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "branchcommit"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "master"
echo "file1_!from" >> file1.txt
echo "file1_!from2" >> file1.txt
echo "file1_common1" >> file1.txt
echo "file1_common2" >> file1.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs merge "branch"
echo -ne 'file1\n+file1_!from\n+file1_!from2\n-file1_!\n-file1_2\nfile1_common1\nfile1_common2' > file1.diff
diff file1.diff file1.txt

cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo -e "${orange}Commit without init test${nc}"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"`
if [[ "$RESULT" != 'VCS exception: database is corrupted' ]]
then 
	echo -e "${red}failed${nc}"
fi



