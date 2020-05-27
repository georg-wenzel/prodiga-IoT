#!/bin/sh

conf_path="$HOME/.config/prodiga"

if [ ! -d $conf_path ]; then
    echo "The config path doesn't exist"
    echo "Did you run the setup.sh"
    echo "You could also make it manually in $HOME/.config/prodiga"
    exit 1
fi

conf_file="$HOME/.config/prodiga/prodigarc"

if [ ! -f $conf_file ]; then
    echo "The config file doesn't exist"
    echo "Did you run the setup.sh"
    echo "You could also make it manually in $HOME/.config/prodiga/prodigarc"
    exit 1
fi

client_path=$(cat "$HOME/.config/prodiga/prodigarc")

if [ -z "$client_path" ]; then
    echo "Client path doesn't exist"
    echo "Did you run the setup.sh"
    echo "You could also add it manually in $HOME/.config/prodiga/prodigarc"
    exit 1
fi

if [ ! -d $client_path ]; then
    echo "path to client must link to valid path"
    echo "To fix it manually change it in $HOME/.config/prodiga/prodigarc"
    exit 1
fi

FILE="$client_path/pom.xml"
if [ ! -f "$FILE" ]; then
    echo "path to client must link to valid client root"
    echo "To fix it manually change it in $HOME/.config/prodiga/prodigarc"
    exit 1
fi

cd $client_path

TARGET=$client_path/target
if [ ! -d $TARGET ]; then
    mvn clean install
fi

# password und ip wird dan generiert
sudo java -cp target/prodiga_client-1.0.0.jar:./lib/tinyb.jar:./target/lib/* uibk.ac.at.prodigaclient.Client @ipAddress @password

