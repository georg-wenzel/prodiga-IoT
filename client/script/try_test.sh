#!/bin/sh

#install_apt_dependencies()
#{
#dependencies=$1
#for element in $dependencies;do
#    if ! dpkg -s $element >/dev/null 2>&1; then
#        echo "$element not installed"
#    else
#        echo "$element installed"
#    fi
#done
#}
#
#
#install_apt_dependencies 'blueZ'

running=$(sudo systemctl status bluetooth | grep "daemon" | wc -l)
if [ $running -eq 0 ]; then
    echo test
fi

#if ! dpkg -s $dependencies >/dev/null 2>&1; then
#  echo $dependencies
#fi