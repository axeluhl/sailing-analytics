#!/usr/bin/env bash

function add_param() {
	if [ ! -z "$2" ]; then
		result=" --$1 $2"
	fi
	echo "$result"
}



