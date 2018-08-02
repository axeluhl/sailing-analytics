#!/usr/bin/env bash

if [ "$#" == 1 ]
then
	version=${1}
    echo "creating distribution with version ${version}..."

	tar -cvzf ./orchestration-${version}.tgz -T ./includeFilesDistro
	ret=$?
	if [ "$ret" != "0" ]
	then
		echo "did not create distribution correctly."
	else
		echo "successfully created distribution."
	fi

	exit ${ret}
else
    echo "version not specified/entered wrong number of parameters"
	exit 1
fi

