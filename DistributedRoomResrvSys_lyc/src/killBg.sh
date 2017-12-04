ps aux | grep -e $* | grep -v grep | awk '{print $2}' | xargs -i kill {}
ps -ef | grep Server
rm ../bin/*.log -f
