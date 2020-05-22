#!/bin/sh

# change to home directory
cd ~

# sudo -s

# update the system
apt-get update
apt-get upgrade

# install git
apt-get install git

# install cmake
apt-get install cmake

# purge every available jdk becaus we need a specific one
apt-get purge openjdk*

# install java jdk-8
# sudo find / -name java-8-openjdk-armhf # looks if java is installed correctly
apt-get install openjdk-8-jdk

# export environment variable for JAVA_HOME
echo "\nexport JAVA_HOME=/usr/lib/jvm/java-8-openjdk-armhf/" >> ~/.bashrc

# reload bash session
bash

# install maven
apt-get install maven

# install dependencies for BlueZ
apt-get install libglib2.0-dev libdbus-1-dev libudev-dev libical-dev libreadline6 libreadline6-dev

# ensure that you are in the home directory
cd ~ 

# make binary_files folder
mkdir binary_files && cd binary_files

# download blueZ source_code
wget http://www.kernel.org/pub/linux/bluetooth/bluez-5.47.tar.xz

# extract blueZ
tar -xf bluez-5.47.tar.xz && cd bluez-5.47

# configure blueZ
./configure --prefix=/usr --mandir=/usr/share/man --sysconfdir=/etc -- localstatedir=/var

# build blueZ
make
make install

# check if version 5.47
#/usr/libexec/bluetooth/bluetoothd --version

# input policy group for bluetooth
# TODO: SED befehl hinzufügen
sed '0,/<\/policy>/s//<\/policy>\n\n  <policy group="bluetooth">\n    <allow send_destination="org.bluez"\/>\n  <\/policy>/'

# Add user to openhab
adduser --system --no-create-home --group --disabled-login openhab
usermod -a -G bluetooth openhab

# reload system definition
systemctl daemon-reload

# reload blueZ
systemctl restart bluetooth

#systemctl status bluetooth | grep "daemon"

# install tinyB dependency
apt-get install graphviz
apt-get install doxygen

# change to standard binary files directory
cd ~/binary_files 

# clone tinyB from github
git clone https://github.com/intel-iot-devkit/tinyb.git && cd tinyb

# make build directory
mkdir build

# change to build directory
cd build

# build tinyB with cmake
sudo -E cmake -DBUILDJAVA=ON -DCMAKE_INSTALL_PREFIX=/usr ..

# make and make install of tinyB
make
make install

exit

