#!/bin/bash
./gradlew jar
WORKDIR=`echo 'tests/workdir'`
mkdir -p $WORKDIR

echo "Log test"

cd $WORKDIR
echo "file2" >> file2.txt
echo "file1" >> file1.txt

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "second"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs log | wc -l`
if [[ "$RESULT" -ne "7" ]]
then
	echo "Wrong log after 2 commits"
fi
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs log | wc -l`
if [[ "$RESULT" -ne "1" ]]
then
	echo "Not empty log after checkout"
fi

java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first2"
RESULT=`java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs log | wc -l`
if [[ "$RESULT" -ne "4" ]]
then
	echo "Not only one commit afrer checkout and commit"
fi

cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo "Commits test"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"

RESULT=`diff -r . .vcs/1/1 | grep .`
ANSWER="Only in .: .vcs"

if [[ "$RESULT" != "$ANSWER" ]]
then 
	echo "Wrong commit result"
fi



cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo "Checkout test"
echo "file1" >> file1.txt
echo "file2" >> file2.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs init
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs commit "first"
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout -b "branch"
echo "file3" >> file3.txt
java -jar ../../build/libs/vcs-1.0-SNAPSHOT.jar vcs checkout "master"

if [[ "$RESULT" != "file1.txt  file2.txt" ]]
then 
	echo "Wrong checkout on branch"
fi


cd .. && rm -rf workdir && mkdir workdir && cd workdir
echo "Merge test"
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
echo "Merge test 2"
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