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

# change to home directory
cd ~

# update the system
sudo apt-get update
sudo apt-get upgrade

# install git
install_apt_dependencies 'git'

# install cmake
install_apt_dependencies 'cmake'

# install java jdk-8
java_installed=$(which java | wc -l)
java_version=$(sudo find "/" -name "java-8-openjdk-armhf" | wc -l)

if [ "$java_installed" -eq 0 ] || [ "$java_version" -eq 0 ]; then
  echo "installing right java version"

  # purge every available jdk becaus we need a specific one
  sudo apt-get purge openjdk*

  # sudo find / -name java-8-openjdk-armhf # looks if java is installed correctly
  sudo apt-get install openjdk-8-jdk

  # export environment variable for JAVA_HOME
  echo "\nexport JAVA_HOME=/usr/lib/jvm/java-8-openjdk-armhf/" >> ~/.bashrc

  # reload bash session
  bash
fi

echo "java is installed"

# install maven
install_apt_dependencies 'maven'

# install dependencies for BlueZ
install_apt_dependencies 'libglib2.0-dev libdbus-1-dev libudev-dev libical-dev libreadline6 libreadline6-dev'

echo "installed blueZ dependencies"

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
sudo make install

# check if version 5.47
#/usr/libexec/bluetooth/bluetoothd --version

# input policy group for bluetooth
sudo sed '0,/<\/policy>/s//<\/policy>\n\n  <policy group="bluetooth">\n    <allow send_destination="org.bluez"\/>\n  <\/policy>/'

# Add user to openhab
sudo adduser --system --no-create-home --group --disabled-login openhab
sudo usermod -a -G bluetooth openhab

# reload system definition
sudo systemctl daemon-reload

# reload blueZ
sudo systemctl restart bluetooth

#sudo systemctl status bluetooth | grep "daemon"

# install tinyB dependency
install_apt_dependencies 'graphviz doxygen'

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
sudo make
sudo make install

# exit

