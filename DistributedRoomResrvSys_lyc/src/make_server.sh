#/bin/bash

./killBg.sh Replica
./killBg.sh RoomResrvSys
javac -g -cp . ./RoomResrvSys/*.java -d ../bin/
javac -g -cp . ./tools/*.java -d ../bin/
cd ../bin
rm -f *.log
java RoomResrvSys.RequestWorker Replica_1 13320 DVL 25560 &
java RoomResrvSys.RequestWorker Replica_1 13321 KKL 25561 &
java RoomResrvSys.RequestWorker Replica_1 13322 WST 25562 &

cd ../src
