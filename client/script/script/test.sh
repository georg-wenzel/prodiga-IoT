#!/bin/sh

filename=./bluetooth.conf
sed '/<policy user="root">(.|\n)*?<\/policy>/a line10' $filename
