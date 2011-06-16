
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

        # start thread paused - in real life there should only
        # be one race running and therefore we only need one running listener
        self.paused = True

        threading.Thread.__init__(self)

    def run(self):
        log.info('START listener for %s:%s (%s, %s)' % (self.host, self.port, self.eventname, self.racename))
        self.running = True

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
            conf.setLogging(False)

            try:
                # need to lock because other threads could overwrite information
                print >>sys.stderr, '*',
                sys.stderr.flush()
                updatecount = liveRaceInformation(conf)
                print >>sys.stderr, '.',
                sys.stderr.flush()

                self.last_update = datetime.datetime.now()
                self.updatecount = updatecount

            except Exception, ex:
                # print error but do not kill thread as this could be recoverable (connection problems, ...)
                log.error('Error in thread for %s %s' % (self.eventname, self.racename), exc_info=True)

                if str(ex).find('Race not found')>=0:
                    # if race is not found then pause
                    log.error('   Pausing listener because race is invalid.')
                    self.paused = True
                    continue

                # avoid filling console with errors
                time.sleep(5)

            # avoid hammering java server - can occur if call does not block correctly
            time.sleep(2)

        log.info('STOP listener for %s:%s (%s, %s)' % (self.host, self.port, self.eventname, self.racename))

def computeRank(newvalue, oldvalue):
    return newvalue

def eventConfiguration(configurator):
    """ Reads current event configuration from server """

    # this can block if there are no tracked races available
    events = jsonByUrl(configurator) 

    for event in events:
        # erroneous data
        if not event.has_key('boatclass'):
            continue

        dbevent = model.EventImpl.queryOneBy(name=event['name'])
        if not dbevent:
            dbevent = model.EventImpl()

        # create or update competitors for the whole event
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

            dbrace.update(dict(current_legs=[]))

            # make sure to include start to enable sorting
            if rc.has_key('start'):
                dbrace.update(dict(start=rc['start']))

            marknames = []
            for leg in rc['legs']:
                # including all legs where something ends into model
                # in this case we get the last mark that equals the
                # points for the whole race
                if leg.has_key('end'):
                    marknames.append(leg['end'])
                else: marknames.append('UNKNOWN')

            rcmarks.append(marknames)

        rcnames.sort(lambda x,y: cmp(x[1], y[1]))

        # fill competitor ranks and leg values with empty data
        for cp in cobjects:
            races = [0.0 for r in range(raceindex)]
            
            marks = cp.marks; values = cp.values; 
            marknames = cp.marknames

            # update races and marks - support add operation
            # removal not really supported - needs complete refresh
            for r in range(raceindex):
                if len(races) < r+1:
                    races.append(0.0)

                if len(marks) < r+1:
                    marks.append([])

                if len(marknames) < r+1:
                    marknames.append([])

                if len(values) < r+1:
                    values.append([])

                for m in range(len(rcmarks[r])):
                    if len(marks[r]) < m+1:
                        marks[r].append(0.0)

                    if len(marknames[r]) < m+1:
                        marknames[r].append('')

                    if len(values[r]) < m+1:
                        values[r].append({})

            # recognize if races and/or marks changed
            if cp.races and len(cp.races) != len(races):
                log.error('Difference between races for competitor %s found. Old: %s New: %s' % (cp.name, cp.races, races))

            if cp.marks and len(cp.marks) != len(marks):
                log.error('Difference between marks for competitor %s found. Old: %s New: %s' % (cp.name, cp.marks, marks))

            cp.update(dict(races=races, marks=marks, marknames=marknames))

        # finally update event
        dbevent.update(dict(name=event['name'], 
                            boatclass=event['boatclass'], 
                            competitors=cpnames, 
                            races=[rname[0] for rname in rcnames], 
                            marks=rcmarks))

    # return all received events
    return events

def liveRaceInformation(configurator):
    """ Reads live race information from provider. Should be called
    in a thread because execution can block until new data are available. """

    data = jsonByUrl(configurator)

    lock = threading.Lock()
    with lock:

        racename = configurator.race.name
        eventname = configurator.race.event

        if not data.has_key('legs'):
            raise Exception, 'Data returned by showrace for %s, %s seems to be invalid! Data: %s' % (eventname, racename, data)

        # it is important to know which position the race has
        # so we look it up in the Event itself
        event = model.EventImpl.queryOneBy(name=eventname)
        raceindex = event.races.index(racename)

        # check that legs still match the expected value
        legcount = len(data['legs'])
        if legcount:
            legcheck_competitor = model.CompetitorImpl.queryOneBy(name=data['legs'][0]['competitors'][0]['name'], event=eventname)

            known_leg_count = len(legcheck_competitor.marks[raceindex])
            if known_leg_count != legcount:

                # length of saved legs and incoming legs does not match
                # in this case we need to reload all events and then continue
                # this can lead to competitors that loose data
                configurator.setContext(config.MODERATOR)
                configurator.setCommand(config.LIST_EVENTS)
                eventConfiguration(configurator)

                # refresh event from database
                event = model.EventImpl.queryOneBy(name=eventname)
                log.error('Refreshed event %s information because legs (Old: %s New: %s) seem to have changed.' % (eventname, known_leg_count, legcount))

        lcount = 0; current_legs = {}
        for leg in data['legs']:
            leg_id = '%s,%s' % (leg['fromwaypointid'], leg['towaypointid'])

            for competitor in leg['competitors']:
                comp = model.CompetitorImpl.queryOneBy(name=competitor['name'], event=eventname)
                if not comp:
                    # strange things can happen - make such errors visible
                    raise Exception, 'Could not find competitor %s for event %s' % (competitor['name'], eventname)

                # compute total rank for competitor
                c_current_rank = competitor.get('rank', 0)

                # search thru ranks provided and use the
                # rank there as the current one
                if data.has_key('ranks'):
                    for c in data['ranks']:
                        if c['competitor'] == competitor['name']:
                            if c_current_rank == 0:
                                c_current_rank = c['rank']
                            break

                c_races = comp.races
                c_marks = comp.marks
                c_values = comp.values
                c_total_rank = comp.total
                c_marknames = comp.marknames

                # if competitor hasn't started yet we just ignore this leg
                # and don't update the competitors information for the given leg
                if competitor.get('started', False) is False:
                    comp.update(dict(current_leg=None))
                    continue

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
                dc_leg_values = {}
                for key in competitor.keys():
                    kvalue = competitor.get(key, None)

                    # treat nulled numbers as unset
                    if kvalue in [None, 'null', 'Null', 'nUlL', '']:
                        kvalue = None

                    # check for cases where calculations from backend yield
                    # strange numbers - this happens in cases where the competitor
                    # has no speed during mark passing
                    if isinstance(kvalue, (float, int)) and (kvalue in ['Infinity', 'Infinite'] or float(kvalue) > 150000.0):

                        # use a magic number (that is very unlikely to occur)
                        # to indicate that competitor has no speed
                        # hint: it is not the question it is the answer
                        kvalue = 42.260426041982

                    dc_leg_values[key] = kvalue

                dc_leg_values['upOrDownWindLeg'] = leg['upordownwindleg']

                if not dc_leg_values.get('rank'):
                    dc_leg_values['rank'] = c_current_rank

                # compute gains and losses (places, )
                if lcount == 0:
                    # for the first leg just put 0 into data
                    dc_leg_values['gainsAndLossesInPlaces'] = 0
                else:
                    # search for the last place
                    last_rank_computed = c_marks[raceindex][lcount-1]
                    if dc_leg_values['rank']:
                        dc_leg_values['gainsAndLossesInPlaces'] = last_rank_computed - dc_leg_values['rank']
                    else:
                        dc_leg_values['gainsAndLossesInPlaces'] = 0

                c_values[raceindex][lcount] = dc_leg_values

                # save marknames for the given leg
                c_marknames[raceindex][lcount] = (leg['from'], leg['to'])

                # compute total and net points of competitor

                # net points: sum'd ranks of all races
                net_points = 0

                # total points: sum'd over ranks of all races but
                # but for more than ten races discard some values
                # XXX
                total_points = 0

                comp.update(dict(current_rank=c_current_rank,
                                    races=c_races, 
                                    marks=c_marks, 
                                    values=c_values,
                                    total=c_total_rank,
                                    net_points=net_points,
                                    total_points=total_points))

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
            mdup['wind_speed'] = data['wind']['knotspeed']

        mdup['current_legs'] = current_legs.keys()
        dbrace.update(mdup)

        return data['updatecount']

