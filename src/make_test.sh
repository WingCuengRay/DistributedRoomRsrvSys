#/bin/bash

javac -g -cp . ./RoomResrvSys/*.java -d ../bin/
cd ../bin
java RoomResrvSys.TestClass -ORBInitialHost localhost -ORBInitialPort     1050
cd ../src
