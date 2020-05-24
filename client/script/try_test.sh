#!/bin/sh

install_apt_dependencies()
{
dependencies=$1
for element in $dependencies;do
    if ! dpkg -s $element >/dev/null 2>&1; then
        sudo apt-get install $element
    fi
    echo "$element installed"
done
}


install_apt_dependencies 'libglib2.0-dev libdbus-1-dev libudev-dev libical-dev libreadline6 libreadline-dev'

#if ! dpkg -s $dependencies >/dev/null 2>&1; then
#  echo $dependencies
#fi