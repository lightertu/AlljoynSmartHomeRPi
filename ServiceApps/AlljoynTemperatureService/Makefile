HOSTDIR=./
REMOTEDIR=~/Projects/alljoynTempSensorService/
RSYNC_FLAG=-rt
REMOTE_ADDR=pi@192.168.1.113
EXCLUDE=
all:
	mvn compile

clean:
	mvn clean

sync:
	rsync $(RSYNC_FLAG) --exclude-from=./.git/ $(HOSTDIR) $(REMOTE_ADDR):$(REMOTEDIR)
