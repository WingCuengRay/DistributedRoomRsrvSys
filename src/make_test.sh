#/bin/bash

javac -g -cp . ./Client/*.java -d ../bin/
cd ../bin
java Client.TestClass 
cd ../src
