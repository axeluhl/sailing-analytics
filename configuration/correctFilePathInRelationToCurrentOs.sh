#!/bin/bash
# editing the filepath, so it fits for operating system;
# currently safely works for cygwin, gitbash and linux
correct_file_path()
{
    if [[ "$1" == '/' ]] || [[ "$1" == "" ]]; then
        echo ""
        return 0
    fi
    
    if [ "$OSTYPE" == "cygwin" ]; then
        echo $1 | grep -q '^/cygdrive/'
	if [ "$?" = "0" ]; then
            echo $1 | sed 's/^\/cygdrive\/\([a-zA-Z]\)\//\1:\//'
        else
	    echo "c:/cygwin64${1}"
        fi
    elif [ "$OSTYPE" == "msys" ]; then
      echo $1 | sed 's/^\/\([a-zA-Z]\)\//\1:\//'
    else
      echo $1
    fi
}
