
import sys, threading, datetime, time

import cjson
import urllib, urllib2

from sailing.db import database
from sailing.db import model
from sailing.connector import jsonByUrl

from sailing.web import config
from sailing.connector import URIConfigurator

import logging
logging.basicConfig(level=logging.INFO)
log = logging.getLogger('sailing.connector.run#main')

class LiveDataReceiver(threading.Thread):

    def __init__(self, host, port, eventname, racename):
        self.host = host
        self.port = port
        self.eventname = eventname
        self.racename = racename

        self.last_update = None
        self.updatecount = 0

        self.running = False
        self.paused = True

        threading.Thread.__init__(self)

    def run(self):
        log.info('START listener for %s:%s (%s, %s)' % (self.host, self.port, self.eventname, self.racename))
        self.running = True

        lock = threading.Lock()
        while self.running is True:

            # make it possible to pause listener
            if self.paused is True:
                time.sleep(1)
                continue

            conf = URIConfigurator(self.host, self.port)
            conf.setContext(config.MODERATOR)
            conf.setCommand(config.LIVE_EVENTS)
            conf.setRace(model.RaceImpl.queryOneBy(event=self.eventname, name=self.racename))

            # blocking call by updatecount setting
            conf.setParameters(dict(eventname=self.eventname, racename=self.racename, sinceupdate=self.updatecount))

            try:
                # need to lock because other threads could overwrite information
                with lock:
                    updatecount = liveRaceInformation(conf)

                self.last_update = datetime.datetime.now()
                self.updatecount = updatecount

            except Exception, ex:
                # print error but do not kill thread as this could be recoverable
                log.error('Error in thread for %s %s' % (self.eventname, self.racename), exc_info=True)

                # avoid filling console with errors
                time.sleep(2)

            # avoid hammering java server - can occur if call does not block correctly
            time.sleep(1)

        log.info('STOP listener for %s:%s (%s, %s)' % (self.host, self.port, self.eventname, self.racename))

def computeRank(newvalue, oldvalue):
    return newvalue

def eventConfiguration(configurator):
    """ Reads current event configuration from server """

    # this can block if there are no tracked races available
    events = jsonByUrl(configurator) 

    for event in events:
        dbevent = model.EventImpl.queryOneBy(name=event['name'])
        if not dbevent:
            dbevent = model.EventImpl()

        # create competitors for the whole event
        cpnames = []; cobjects = []
        for cp in event['competitors']:
            cpnames.append(cp['name'])

            cfound = model.CompetitorImpl.queryOneBy(name=cp['name'], event=event['name'])
            if not cfound:
                competitor = model.CompetitorImpl()
                competitor.update(dict(name=cp['name'], nationality=cp['nationality'], event=event['name']))
                cobjects.append(competitor)
            else:
                cobjects.append(cfound)

        # prepare race and mark names
        rcnames = []; rcmarks = []
        raceindex = 0;
        for rc in event['races']:
            rcnames.append( (rc['name'], rc['start']) )
            raceindex += 1

            dbrace = model.RaceImpl.queryOneBy(event=event['name'], name=rc['name'])
            if not dbrace:
                dbrace = model.RaceImpl()
                dbrace.update(dict(event=event['name'], name=rc['name']))

            # make sure to include start to enable sorting
            if rc.has_key('start'):
                dbrace.update(dict(start=rc['start']))

            marknames = []
            for leg in rc['legs']:
                # including all legs where something ends into model
                # in this case we get the last mark that equals the
                # points for the whole race
                marknames.append(leg['end'])

            rcmarks.append(marknames)

        rcnames.sort(lambda x,y: cmp(x[1], y[1]))

        # fill competitors with empty data
        for cp in cobjects:
            races = [0.0 for r in range(raceindex)]
            
            marks = cp.marks; values = cp.values
            for r in range(raceindex):
                if len(races) < r+1:
                    races.append(0.0)

                if len(marks) < r+1:
                    marks.append([])

                if len(values) < r+1:
                    values.append([])

                for m in range(len(rcmarks[r])):
                    if len(marks[r]) < m+1:
                        marks[r].append(0.0)

                    if len(values[r]) < m+1:
                        values[r].append([])

            cp.update(dict(races=races, marks=marks))

        # finally create or update event
        dbevent.update(dict(name=event['name'], boatclass=event['boatclass'], competitors=cpnames, races=[rname[0] for rname in rcnames], marks=rcmarks))

    # return all received events
    return events

def liveRaceInformation(configurator):
    """ Reads live race information from provider. Should be called
    in a thread because execution can block until new data are available. """

    data = jsonByUrl(configurator)

    racename = configurator.race.name
    eventname = configurator.race.event

    # it is important to know which position the race has
    # so we look it up in the Event itself
    event = model.EventImpl.queryOneBy(name=eventname)
    raceindex = event.races.index(racename)

    # this should help us finding out which legs are still running
    # we assume that only one race at time is running so we stick
    # to the legs

    lcount = 0; current_legs = {}
    for leg in data['legs']:
        for competitor in leg['competitors']:
            comp = model.CompetitorImpl.queryOneBy(name=competitor['name'], event=eventname)
            if not comp:
                raise Exception, 'Could not find competitor %s for event %s' % (competitor['name'], eventname)

            c_races = comp.races
            c_marks = comp.marks
            c_values = comp.values
            c_total_rank = comp.total

            # if competitor hasn't yet started we just ignore this leg
            if competitor.get('started', False) is False:
                continue

            # we take to into account because this is the one we have saved before
            mark_name = leg['to']

            # last mark in race?
            is_last_mark = event.marks[raceindex][-1] == mark_name

            # competitor finished this leg?
            finished = competitor['finished']

            # we are now in race[raceindex].legs[legcount] and need to map this
            # to competitor structures

            if finished:
                # competitor has finished this leg so we need to save rank
                # to the next mark (e.g. mark_name is 'Mark 1' and finished 
                # then competitor.marks[raceindex][lcount] = rank). This works
                # because leg[position] equals marks[position] in list
                if competitor.has_key('rank'):
                    c_marks[raceindex][lcount] = computeRank(competitor['rank'], c_marks[raceindex][lcount])

                    # if finished and last mark then update race information
                    if is_last_mark:
                        c_races[raceindex] = computeRank(competitor['rank'], c_races[raceindex])

                        # for the last race just update total rank
                        if raceindex+1 == len(c_races):
                            c_total_rank = computeRank(competitor['rank'], c_total_rank)

                else:
                    c_marks[raceindex][lcount] = 0.0
                    if is_last_mark:
                        c_races[raceindex] = 0.0

                    if raceindex+1 == len(c_races):
                        c_total_rank = 0.0

            else:
                # leg not finished so put this leg into unfinished legs
                current_legs[lcount] = 1

            # now update values for the current position
            for key in config.SORTED_LEG_VALUE_NAMES:

                # dynamically extend this one
                attr = config.LEG_VALUE_DATA[key]
                if len(c_values[raceindex][lcount]) < attr[0]+1:
                    c_values[raceindex][lcount].append(0.0)

                k = competitor.get(key, None)
                if k in [None, 'null', 'Null', '']:
                    k = 0.0

                c_values[raceindex][lcount][attr[0]] = k

            # compute total rank for competitor
            rank = competitor.get('rank', 0)
            if data.has_key('ranks'):
                for c in data['ranks']:
                    if c['competitor'] == competitor['name']:
                        rank = c['rank']
                        break

            comp.update(dict(current_rank=rank,races=c_races, marks=c_marks, values=c_values, total=c_total_rank))

        # next leg
        lcount += 1

    # update race with current information
    dbrace = model.RaceImpl.queryOneBy(event=eventname, name=racename)
    mdup = dict(startoftracking=data['startoftracking'],
                    start=data['start'], finish=data['finish'],
                    timeofnewestevent=data['timeofnewestevent'],
                    updatecount=data['updatecount'])

    if data.get('wind'):
        mdup['wind_source'] = data['wind']['source']
        mdup['wind_bearing'] = data['wind']['truebearingdeg']
        mdup['wind_speed'] = data['wind']['knowspeed']

    mdup['current_legs'] = current_legs.keys()
    dbrace.update(mdup)

    return data['updatecount']

