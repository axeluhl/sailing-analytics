# Install chrome to work for jenkins selenium test

[[_TOC_]]


### Chrome:
execute the install_google_chrome.sh from this folder, 
it will take quite a while and will install all dependencies that are required.
It will try to resolve missing dependencies via CentOs repositiories.
It will also binary patch the chrome, to actually use the downloaded binarys instead of the usual linker resolve path
Make a symbolic link from /usr/bin/google-chrome to /usr/bin/google-chrome-stable so that selenium can resolve it without further arguments
ln -s /usr/bin/google-chrome-stable /usr/bin/google-chrome

### Chromedriver:
Download and extract the chromedriver https://sites.google.com/a/chromium.org/chromedriver/downloads
upon executing it a missing dependency for glibc 2.14 will appear,
compile it yourself:

Warning: 
If the glibc is not properly compiled to be installed parallel to the old one, the linux is bricked and cannot be saved/booted as most system tools will be broken.

mkdir ~/glibc_install; cd ~/glibc_install
wget http://ftp.gnu.org/gnu/glibc/glibc-2.14.tar.gz
tar zxvf glibc-2.14.tar.gz
cd glibc-2.14
mkdir build
cd build
../configure --prefix=/opt/glibc-2.14
make -j4
sudo make install
export LD_LIBRARY_PATH=/opt/glibc-2.14/lib
Test if the chromedriver starts via ./chromedriver

create a wrapper script next to the chromedriver, to hide the LD_Library_Path hack from the build and selenium

#!/bin/sh
export LD_LIBRARY_PATH=/opt/glibc-2.14/lib && /opt/chromedriver
