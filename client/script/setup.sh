#!/bin/sh

if [ $# -eq 0 ]; then
    echo "setup.sh <path_to_client_root>"
    exit 1
fi

if [ -z "$1" ]; then
    echo "setup.sh <path_to_client_root>"
    exit 1
fi

if [ ! -d "$1" ]; then
    echo "path to client must link to valid path"
    echo "setup.sh <path_to_client_root>"
    exit 1
fi

FILE=$1/pom.xml
if [ ! -f "$FILE" ]; then
    echo "path doesn't link to project root"
    echo "setup.sh <path_to_client_root>"
    exit 1
fi

mkdir -p "$HOME"/.config/prodiga

echo "$1" > "$HOME"/.config/prodiga/prodigarc

sh "$PWD/install_dependencies.sh"

sh "$PWD/start_client.sh"

