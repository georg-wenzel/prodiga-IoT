#!/bin/sh

if [ $# -eq 0 ]; then
    echo "start_client.sh <path_to_client_root>"
    exit 1
fi

if [ -z "$1" ]; then
    echo "start_client.sh <path_to_client_root>"
    exit 1
fi

if [ ! -d $1 ]; then
    echo "path to client must link to valid path"
    echo "start_client.sh <path_to_client_root>"
    exit 1
fi

FILE=$1/pom.xml
if [ ! -f $FILE ]; then
    echo "path doesn't link to project root"
    echo "start_client.sh <path_to_client_root>"
    exit 1
fi

TARGET=$1/target
if [ ! -d $TARGET ]; then
    mvn clean install
fi

cd $1

# password und ip wird dan generiert
sudo java -cp target/prodiga_client-1.0.0.jar:./lib/tinyb.jar:./target/lib/* uibk.ac.at.prodigaclient.Client http://10.0.0.166:8080/ password


