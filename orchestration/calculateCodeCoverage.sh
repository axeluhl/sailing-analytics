#!/usr/bin/env bash
regex='(ok)[[:space:]]+([[:blank:]])(.*?)([[:blank:]])(.*)(coverage:)[[:space:]]([0-9]{1,3})+(\.[0-9][0-9]?)(.*)'
total=0
percent=0
while IFS='' read -r line || [[ -n "$line" ]]; do
  if [[ $line =~ $regex ]];
  then
    let percent=$percent+${BASH_REMATCH[7]}
    let total=$total+1
  fi
done < "$1"
let total=$percent/$total
echo "Total code coverage: $total%"
