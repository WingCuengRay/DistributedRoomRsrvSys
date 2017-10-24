#/bin/bash

./killBg.sh Server
idlj -td ./ -fall ./ServerRemote.idl
javac -g -cp . ./RemoteInterface/*.java -d ../bin/
javac -g -cp . ./RoomResrvSys/*.java -d ../bin/
cd ../bin
java RoomResrvSys.ServerRemoteImpl -ORBInitialHost localhost -ORBInitialPort 1050 -campus DVL -udpPort 25560 &
java RoomResrvSys.ServerRemoteImpl -ORBInitialHost localhost -ORBInitialPort 1050 -campus KKL -udpPort 25561 &
java RoomResrvSys.ServerRemoteImpl -ORBInitialHost localhost -ORBInitialPort 1050 -campus WST -udpPort 25562 &
cd ../src
