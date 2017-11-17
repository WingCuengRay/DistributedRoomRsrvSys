#/bin/bash

./killBg.sh Server
javac -g -cp . ./RoomResrvSys/*.java -d ../bin/
cd ../bin
rm -f *.log
java RoomResrvSys.ServerPublisher &
cd ../src
