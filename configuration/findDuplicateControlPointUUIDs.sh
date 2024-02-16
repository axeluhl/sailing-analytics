#!/bin/bash
export BASE_FOLDER=/home/trac/static/TracTracTracks
export TMP_DIR="$( mktemp --tmpdir -d TracTracControlPointsByEvent_XXXXX )"
find "${BASE_FOLDER}" -name '*.txt' | while read FILE; do
  EVENT="$(basename $(dirname ${FILE} ))"
  echo "Analyzing marks of event ${EVENT} ..."
  UUIDs=$( cat "${FILE}" \
   | grep "\(^ControlPoint[0-9]*UUID:\)\|\(ControlPoint[0-9]*DataSheet:.*###P[12]\.UUID=\)" \
   | sed -e 's/^ControlPoint[0-9]*UUID://' -e 's/ControlPoint[0-9]*DataSheet:.*P1\.UUID=\([^#]*\).*\(P2\.UUID=\([^#]*\)\)\?.*$/\1 \3/' )
  for UUID in ${UUIDs}; do
    UUID_FILE="${TMP_DIR}/${UUID}"
    if [ -f "${UUID_FILE}" ]; then
      if ! grep -q "${EVENT}" "${UUID_FILE}"; then
        echo "${EVENT}" >>"${UUID_FILE}"
      fi
    else
      echo "${EVENT}" >"${UUID_FILE}"
    fi
  done
done
find "${TMP_DIR}" -type f | while read UUID_FILE; do
  NUMBER_OF_EVENTS_FOR_UUID=$( cat "${UUID_FILE}" | wc -l )
  if [ "${NUMBER_OF_EVENTS_FOR_UUID}" != "1" ]; then
    echo "ControlPoint ID $( basename ${UUID_FILE} ) used in multiple events: $( cat "${UUID_FILE}" | tr '\n' ' ' )" >&2
  fi
done
rm -rf "${TMP_DIR}"
echo "No control point ID duplication found in any event under ${BASE_FOLDER}"
