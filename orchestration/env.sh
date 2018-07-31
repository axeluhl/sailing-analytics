#!/usr/bin/env bash
export PREFIX="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export GOPATH=${HOME}/.go:${PREFIX}
export GOMAXPROCS=4
