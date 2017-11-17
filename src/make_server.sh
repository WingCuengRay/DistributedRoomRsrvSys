#/bin/bash

./killBg.sh RoomResrvSys
javac -g -cp . ./RoomResrvSys/*.java -d ../bin/
javac -g -cp . ./tools/*.java -d ../bin/
cd ../bin
rm -f *.log
java RoomResrvSys.RequestWorker 13320 DVL 25560 &
cd ../src
