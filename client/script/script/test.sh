#!/bin/sh

filename=./bluetooth.conf
sed '/<policy user="root">(.|\n)*?<\/policy>/i \ line10/g' $filename
