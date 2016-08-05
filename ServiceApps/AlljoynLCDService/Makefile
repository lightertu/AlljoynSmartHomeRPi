HOSTDIR=./
REMOTEDIR=~/Projects/alljoynLCDService
RSYNC_FLAG=-rt
REMOTE_ADDR=pi@192.168.1.118
LIBRARY_PATH=~/SDKs/Alljoyn/alljoyn/core/alljoyn/alljoyn_java/bin/libs
MAIN_CLASS="org.netsec.iot.lcd.Client"
EXCLUDE=".*"

all:
	mvn compile

clean:
	mvn clean

sync:
	rsync $(RSYNC_FLAG) --progress --exclude=$(EXCLUDE) $(HOSTDIR) $(REMOTE_ADDR):$(REMOTEDIR)
