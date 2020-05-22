#!/bin/sh

filename=./bluetooth.conf
sed -i '0,/<\/policy>/s//<\/policy>\n\n  <policy group="bluetooth">\n    <allow send_destination="org.bluez"\/>\n  <\/policy>/' $filename
