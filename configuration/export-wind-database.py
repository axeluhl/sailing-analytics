#!/usr/bin/python2.7

import re
import sys
import pymongo

if __name__ == '__main__':
    if len(sys.argv) != 7:
        print '%s <host> <port> <database> <prefix> <exportfile> <interval in millis>' % sys.argv[0]
        print 'Example: 127.0.0.1 27017 winddb "ESS 2014" /tmp/wind-data-ess.csv 5000'
        sys.exit(0)

    program, host, port, database, prefix, exportfile, interval = sys.argv
    print 'Connecting to server %s:%s with database %s and searching for tracks with prefix %s and exporting to %s' % (host, port, database, prefix, exportfile)
    client = pymongo.MongoClient(host, int(port))
    db = client[database]
    wind_tracks = db['WIND_TRACKS']
    csv = open(exportfile, 'w')
    csv.write('Event Name;Latitude;Longitude;MeasurementTimePointAsMillisSince01011970;KnotSpeed;BearingDegreeFrom;SourceName\n')
    counter = 0
    print 'Storing all wind track data in memory in order to overcome MongoDB 32MB sort limit...'
    data_container = []; regattas = {}
    # anchored regexp runs pretty fast compared to iterating over all fixes. but beware:
    # using options like re.IGNORECASE will render the search almost unusable
    prefix_pattern = re.compile('^%s' % prefix)
    for fix in wind_tracks.find({'REGATTA_NAME' : prefix_pattern}):
        data = (fix['REGATTA_NAME'], str(fix['WIND']['LAT_DEG']), 
                str(fix['WIND']['LNG_DEG']), str(fix['WIND']['TIME_AS_MILLIS']), str(fix['WIND']['KNOT_SPEED']), 
                str(fix['WIND']['DEGREE_BEARING']))
        wind_source_id = fix['WIND_SOURCE_NAME']
        if hasattr(fix, 'WIND_SOURCE_ID'):
            wind_source_id += '-'+fix['WIND_SOURCE_ID']
        data += (wind_source_id, )
        counter += 1
        if data[0].startswith(prefix):
            regattas[fix['REGATTA_NAME']] = regattas.get(fix['REGATTA_NAME'], 0)+1
            data_container.append(data)
        if counter % 1000 == 0:
            sys.stdout.write('.')
            if counter % 100000 == 0:
                sys.stdout.write(str(counter))
            sys.stdout.flush()

    print '\nSorting %s fixes in memory for regattas %s' % (len(data_container), regattas)
    sorted_fixes = sorted(data_container, key=lambda windfix: windfix[3], reverse=True)
    counter = 0; last_timepoint = {}
    print 'Starting export of %s wind fixes in interval %sms to %s' % (len(sorted_fixes), long(interval), exportfile)
    for cached_fix in sorted_fixes:
        if last_timepoint.get(cached_fix[0])==None or ((long(last_timepoint.get(cached_fix[0]))-long(cached_fix[3])) >= long(interval)):
            counter += 1
            csv.write(';'.join(cached_fix))
            csv.write('\n')
            last_timepoint[cached_fix[0]] = cached_fix[3]
            if counter % 1000 == 0:
                sys.stdout.write('#')
                sys.stdout.flush()

    print '\nExported %s fix(es) that have a minimum time distance of %s milliseconds' % (counter, interval)
    csv.close()
    print 'Exported the following prefix-matching regattas: %s' % last_timepoint.keys()
