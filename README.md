# Alljoyn Smart Home Using Raspberry Pi

###Hardware Requirements:
* Raspberry Pis with wifi enabled
* Sensors:
  - Humiture Sensor Module    x 1
  - Temperature Sensor Module x 1
  - Gas Sensor Module         x 1
  - Flame Sensor Module       x 1
  - PCF8591 Module            x TBD

* Output Modules:
  - LCD 1602 with I2C Module  x 1
  - RGB LED Module            x 1
  - Active Buzzer Module      x 1
  - Motor                     x 1

* Others:
  - Bread boards and jump wires

###Software Requirements:
* `Alljoyn SDK` with `Java` binding
* `Make` build tool
* `Maven` the `Java` build tool
* `liballjoyn_java.so` file is located at `~/SDKs/Alljoyn/alljoyn/core/alljoyn/alljoyn_java/bin/libs`

###Run each Application
* `make`
* `./run`

###To clean compiled binaries
* `make clean`

###Topology
![Topology](./misc/Topology.PNG?raw=true "Topology")
