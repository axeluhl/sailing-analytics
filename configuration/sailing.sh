# Script to be linked from /etc/profile.d
# Appends to PATH, sets DISPLAY for VNC running on :2, exports JAVA_HOME and Amazon EC2 variables

ulimit -n 30000
ulimit -u 40000

export EC2_HOME=/opt/amazon/ec2-api-tools-1.6.8.0
export EC2_URL=https://ec2.eu-west-1.amazonaws.com
# SAP JVM
export JAVA_HOME=/opt/sapjvm_8
#export JAVA_HOME=/opt/jdk1.8.0_45
export JAVA_1_7_HOME=/opt/jdk1.7.0_75
export ANDROID_HOME=/opt/android-sdk-linux

export PATH=$PATH:$JAVA_HOME/bin:/opt/amazon/ec2-api-tools-1.6.8.0/bin:/opt/amazon/bin:/opt/apache-maven-3.2.1/bin

export DISPLAY=:2.0

alias sa='eval `ssh-agent`; ssh-add ~/.ssh/id_dsa'
alias ll="ls -lh --color"
