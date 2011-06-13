
import urllib, urllib2
import time
import threading

from webob.exc import HTTPFound
from webob import Response

from pyramid.exceptions import NotFound
from pyramid.renderers import render_to_response

from sailing.db import model
from sailing.db.monitoring import dropDB as realDropDB

from sailing.connector import provider, URIConfigurator, jsonByUrl

from sailing.web import config
from sailing.web.util import jsonize

import core

import logging
log = logging.getLogger(__name__)
logging.basicConfig()

# map holding association of host:port -> threads
threaded_listener = {}

def dropDB(context, request):
    from sailing.db.monitoring import dropDB
    dropDB()
    return HTTPFound(location='/')

def startListenerThreads(conf, eventlist):
    # now we should have an event configuration
    # read this configuration and for each event and race
    # start a listener thread

    lock = threading.Lock()
    with lock:
        for event in eventlist: 
            event_impl = model.EventImpl.queryOneBy(name=event['name'])

            if not event_impl:
                raise Exception, 'Could not find event %s in database' % event['name']

            for racename in event_impl.races:
                key = hash('%s-%s-%s-%s' % (conf.host, conf.port, event['name'], racename))

                # remove dead threads
                lstn = threaded_listener.get(key, None)
                if lstn and lstn.running is False and lstn.is_alive() is False:
                    del threaded_listener[key]

                # now there should be only running threads left
                if not threaded_listener.has_key(key):
                    t = provider.LiveDataReceiver(conf.host, conf.port, event['name'], racename) 
                    threaded_listener[key] = t
                    t.start()

# addEvent
def configureListener(context, request):
    host = request.POST.get('host', None)
    port = request.POST.get('port', None)

    lock = threading.Lock()

    view = core.BaseView(context, request)

    if host and port:
        conf = URIConfigurator(host, port)
        view.session['listener-conf'] = conf

    else: 
        # reuse listener conf
        conf = view.listenerConf()

    if request.POST.get('listener-start', None):
        if conf is None:
            return view.yieldMessage('Please set host and port!')

        conf.setContext(config.ADMIN)

        if request.POST.get('eventJSONURL'):
            conf.setCommand(config.ADD_EVENT)
            conf.setParameters(dict(eventJSONURL=request.POST.get('eventJSONURL'), 
                                    liveURI=request.POST.get('liveURI'), 
                                    storedURI=request.POST.get('storedURI')))

        elif request.POST.get('paramURL'):
            conf.setCommand(config.ADD_RACE)
            conf.setParameters(dict(paramURL=request.POST.get('paramURL'), 
                                    liveURI=request.POST.get('liveURI'), 
                                    storedURI=request.POST.get('storedURI')))

            if not view.session.get('race.url_history'):
                view.session['race.url_history'] = []

            if request.POST.get('paramURL') not in view.session['race.url_history']:
                view.session['race.url_history'].append(request.POST.get('paramURL'))
                view.session.save()

        else:
            return view.yieldMessage('Please specify an event or race URL!')

        try:
            result = conf.trigger()

            if result.find('Exception')>0:
                raise Exception, 'Server triggered exception: %s' % result[:550]

        except Exception, ex:
            return view.yieldMessage('Error: %s' % str(ex))

        # can take a while until event data is ready
        if request.POST.get('drop_db'):
            realDropDB()

        time.sleep(3)

        # load event listing here
        with lock:
            eventlist = []
            try:
                conf.setContext(config.MODERATOR)
                conf.setCommand(config.LIST_EVENTS)
                eventlist = provider.eventConfiguration(conf)

                # do not accept empty event list
                if len(eventlist) == 0:
                    raise Exception, 'No events configured.'

            except Exception, ex:
                return view.yieldMessage('Could not gather any data! Seems that Java listener is not ready yet. Please reconnect. Error: %s' % str(ex))

            startListenerThreads(conf, eventlist)

        # mark all stuff done and listener running
        view.session['listener-started'] = True
        view.session.save()

    elif request.POST.get('disconnect-server', None):
        # disconnect does not stop listeners but just disconnects the
        # session of the user from server
        view.session['listener-started'] = False
        del view.session['listener-conf']
        view.session.save()

    elif request.POST.get('listener-stop', None):

        eventname = request.POST.get('event', None)
        racename = request.POST.get('race', None)

        conf.setContext(config.ADMIN)

        if racename:
            conf.setCommand(config.STOP_RACE)
            conf.setParameters(dict(eventname=eventname, racename=racename))
        else: 
            conf.setCommand(config.STOP_EVENT)
            conf.setParameters(dict(eventname=eventname))

        # remove listener started before any error can occur
        view.session['listener-started'] = False
        del view.session['listener-conf']
        view.session.save()

        racenames = []
        if not racename:
            racenames = model.EventImpl.queryOneBy(name=eventname).races
        else: racenames = [racename, ]

        for rname in racenames:
            key = hash('%s-%s-%s-%s' % (conf.host, conf.port, eventname, rname))
            if threaded_listener.has_key(key):
                threaded_listener[key].running = False

                # remove this thread - will die later if still blocking
                del threaded_listener[key]

        try:
            conf.trigger()
        except Exception, ex:
            return view.yieldMessage('Error: %s' % str(ex))

    elif request.POST.get('listener-event-refresh', None):
        if conf is None:
            return view.yieldMessage('Please set host and port!')

        with lock:
            conf.setContext(config.MODERATOR)
            conf.setCommand(config.LIST_EVENTS)
            eventlist = provider.eventConfiguration(conf)

        if eventlist == [] or eventlist[0].get('races', []) == []:
            return view.yieldMessage('Seems that java server (listener) is not initialized correctly. Yields no events and/or races! Try to reconfigure...')

        # start listener thread if it is not yet active
        startListenerThreads(conf, eventlist)

        view.session['listener-started'] = True
        view.session['listener-conf'] = conf
        view.session.save()

        return view.yieldMessage('Event handler refreshed!')

    elif request.POST.get('leaderboard-default-event', None):
        core.current_leaderboard_event = request.POST.get('event')

    return HTTPFound(location='/')

@jsonize
def configuredListeners(context, request):

    out = []
    for key, listener in threaded_listener.items():
        out.append( {'host': listener.host, 'port': listener.port,
                        'eventname': listener.eventname, 'last_update': listener.last_update and listener.last_update.strftime('%d.%m %H:%M:%S') or '-',
                        'paused' : listener.paused, 'running': listener.running, 'xid' : str(key),
                        'racename': listener.racename} )

    return out

@jsonize
def pauseListener(context, request):
    tid = int(request.params.get('tid', 0))

    if request.params.get('all'):
        for v in threaded_listener.values():
            v.paused = True
    else:
        if threaded_listener.has_key(int(tid)):
            threaded_listener.get(tid).paused = True
    return True

@jsonize
def unPauseListener(context, request):
    tid = int(request.params.get('tid', 0))

    if request.params.get('all'):
        for v in threaded_listener.values():
            v.paused = False
    else:
        if threaded_listener.has_key(int(tid)):
            threaded_listener.get(tid).paused = False
    return True

@jsonize
def stopListener(context, request):
    tid = int(request.params.get('tid', 0))
    if threaded_listener.has_key(int(tid)):
        threaded_listener.get(tid).running = False
    return True

def saveEventRace(context, request):
    eventname = request.params.get('eventname', request.params.get('event'))
    racename = request.params.get('racename', request.params.get('race'))

    lock = threading.Lock()

    view = core.BaseView(context, request)
    if not racename:
        racename = view.currentRace().name

    view.session['current-event'] = eventname
    view.session['current-race'] = racename
    view.session.save()

    if request.params.get('template'):
        return render_to_response('templates/%s.pt' % request.params.get('template'),
                {'view': view, 'additional': request.params.get('additional')}, request=request)

@jsonize
def mapCompetitors(context, request):
    # returns competitors with their current rank at the given timepoint

    view = core.BaseView(context, request)

    _at = request.params.get('at')
    at_millis = view.datetimeToMillis(view.parseDatetime(_at))

    conf = view.listenerConf()
    conf.setContext(config.MODERATOR)
    conf.setCommand(config.LIVE_EVENTS)

    race = view.currentRace()
    conf.setRace(race)
    conf.setParameters(dict(eventname=race.event, racename=race.name, timeasmillis='%.f' % at_millis))
    
    data = jsonByUrl(conf)

    if data.get('ranks'):
        data = data['ranks']
        data.sort(lambda x,y: cmp(x['rank'], y['rank']))

    return data

@jsonize
def windSettings(context, request):
    view = core.BaseView(context, request)
    conf = view.listenerConf()
    conf.setContext(config.ADMIN)
    conf.setRace(view.currentRace())

    if request.POST.get('setsource'):
        conf.setCommand(config.SET_WIND_SOURCE)
        conf.setParameters(dict(sourcename=request.POST.get('windsource')))
        conf.trigger()

    elif request.POST.get('setcustom'):

        # first set custom wind
        conf.setCommand(config.SET_WIND_SOURCE)
        conf.setParameters(dict(sourcename='WEB'))
        conf.trigger()

        conf.setContext(config.ADMIN)
        conf.setRace(view.currentRace())
        conf.setCommand(config.SET_WIND)

        dc = {}
        for k in ['truebearingdegrees', 'knotspeed', 'lngdeg', 'latdeg', 'timeasmillis']:
            if request.POST.get(k):
                dc[k] = request.POST.get(k)

        conf.setParameters(dc)
        conf.trigger()

    return True

@jsonize
def readWind(context, request):
    view = core.BaseView(context, request)

    wind = view.currentWind()
    source = wind[wind['currentwindsource']]

    if source and len(source) > 0:
        return source[0]
    return {}

@jsonize
def readWaypoints(context, request):
    return readWaypointsImpl(context, request)

def readWaypointsImpl(context, request):
    view = core.BaseView(context, request)

    conf = view.listenerConf()
    conf.setContext(config.MODERATOR)
    conf.setCommand(config.SHOW_WAYPOINTS)
    conf.setRace(view.currentRace())

    data = jsonByUrl(conf)

    # we want to show unique buoys so lets iter thru data
    result = {}
    for wp in data:
        for by in wp['buoys']:
            result[by['name']] = by

    return result.values()

@jsonize
def loadTracks(context, request):
    view = core.BaseView(context, request)

    _at = request.POST.get('at')
    at_millis = view.datetimeToMillis(view.parseDatetime(_at))

    # lets take 2 secs before and after given time
    from_millis = at_millis - 1000
    to_millis = at_millis + 1000

    conf = view.listenerConf()
    conf.setContext(config.MODERATOR)
    conf.setCommand(config.SHOW_BOAT_POSITIONS)
    conf.setRace(view.currentRace())
    conf.setParameters(dict(sinceasmillis='%.f' % from_millis, toasmillis='%.f' % to_millis))

    data = jsonByUrl(conf)

    # filter out the position that is the most current one
    results = []
    for c in data['competitors']:
        if len(c['track'])>0:
            fix = c['track'][-1] or {}
        else: fix = {}
        results.append(dict(name=c['name'], **fix))

    return results

@jsonize
def determineWindDirection(context, request):
    """ Returns the wind for all known marks """

    buoys = readWaypointsImpl(context, request)

    view = core.BaseView(context, request)

    conf = view.listenerConf()
    conf.setContext(config.ADMIN)
    conf.setCommand(config.ADD_WIND)
    conf.setRace(view.currentRace())

    _at = request.params.get('at')
    if _at:
        at_millis = view.datetimeToMillis(view.parseDatetime(_at))
        conf.setParameters(dict(timeasmillis='%.f' % at_millis))

    else:
        # take start of race
        conf.setParameters(dict(timeasmillis='%.f' % view.currentRace().start))

    params = []
    for buoy in buoys:
        if buoy.has_key('lat'):
            params.append('latdeg=%s' % buoy['lat'])
            params.append('lngdeg=%s' % buoy['lng'])

    conf.parameters += '&%s' % '&'.join(params)
    
    try:
        data = jsonByUrl(conf)
    except Exception, ex:
        log.error('Error', exc_info=True)
        return str(ex)

    # simply return the list
    return data

@jsonize
def listWindTrackers(context, request):
    view = core.BaseView(context, request)

    conf = view.listenerConf()
    conf.setContext(config.ADMIN)
    conf.setCommand(config.LIST_WIND_TRACKERS)

    data = jsonByUrl(conf)
    return data

@jsonize
def stopReceiver(context, request):
    view = core.BaseView(context, request)

    saveEventRace(context, request)

    conf = view.listenerConf()
    conf.setContext(config.ADMIN)
    conf.setCommand(config.STOP_EXP_WIND)
    conf.setRace(view.currentRace())
    conf.trigger()

    return 1

@jsonize
def startWindTracker(context, request):
    view = core.BaseView(context, request)

    saveEventRace(context, request)

    conf = view.listenerConf()
    conf.setContext(config.ADMIN)
    conf.setCommand(config.START_EXP_WIND)
    conf.setRace(view.currentRace())

    correct = request.POST.get('declination', False) and 'true' or 'false'
    conf.setParameters(dict(port=request.POST.get('port'), correctexpeditionwindbearingbydeclination=correct))
    conf.trigger()

    return 1

@jsonize
def adminLiveData(context, request):
    # searches for all running races and displays information for all competitors

    view = core.BaseView(context, request)
    
    race = view.currentRace()
    event = model.EventImpl.queryOneBy(name=race.event)
    racepos = event.races.index(race.name)

    competitors = model.CompetitorImpl.queryBy(event=event.name)
    currentlegs = race.current_legs

    if currentlegs:
        if currentlegs[0] != 0:
            # always show the last leg
            currentlegs.insert(0, currentlegs[0]-1)
    
    if len(threaded_listener.values()) == 0:
        return 'The listener does not seem to be started properly. Please reconfigure your connection!'

    t_up = threaded_listener.values()[0].last_update.strftime('%H:%M:%S')
    t_upcount = threaded_listener.values()[0].updatecount

    results = ''
    for pos in range(len(currentlegs)):
        legpos = currentlegs[pos]

        results += '\nLAST SHOWRACE CALL: %s (UPCOUNT PARAM: %s)\nLEG: %s (FROM: %s TO: %s)\nRACE-START: %s NEWEST EVENT: %s WIND: (%s %s %s)\n' % (t_up, t_upcount, legpos+1, competitors[0].marknames[racepos][legpos][0], competitors[0].marknames[racepos][legpos][1], view.millisToDatetime(race.start), view.millisToDatetime(race.timeofnewestevent), race.wind_source, race.wind_bearing, race.wind_speed)
        results += 'NAME'.ljust(16) + 'TOTAL'.ljust(7) + 'CRANK'.ljust(7) + 'RRANK'.ljust(7) 
        results += 'MRANK'.ljust(7) + 'LRANK'.ljust(9) + 'SPD'.ljust(9) + 'DSTTRV'.ljust(9) + 'VMG'.ljust(9) + 'AVMG'.ljust(9) + 'SGAP'.ljust(9) + 'ETA'.ljust(9) + 'DSTGO'.ljust(9) + 'STARTD'.ljust(9) + 'FINISHD'.ljust(9) + 'UPDOWNWIND'.ljust(12)
        results += '\n'

        # sort competitors by rank in current leg
        competitors.sort(lambda x,y:cmp(x.values[racepos][legpos] and x.values[racepos][legpos][0] or 6000, y.values[racepos][legpos] and y.values[racepos][legpos][0] or 6000))

        for c in competitors:
            results += '%s %s %s' % (c.name.ljust(15), str(c.total).ljust(6), str(c.current_rank).ljust(6))
            results += '%s %s' % (str(c.races[racepos]).ljust(7), str(c.marks[racepos][legpos]).ljust(7))
                
            for v in c.values[racepos][legpos]:
                if v == 42.260426041982:
                    results += 'ANCHOR'.ljust(9)
                else:
                    results += str('%.2f' % v).ljust(9)

            try:
                results += str(c.additional[racepos][legpos][0]).ljust(9)
                results += str(c.additional[racepos][legpos][1]).ljust(9)
            except:
                results += "N/A".ljust(9)
                results += "N/A".ljust(9)

            results += str(c.upordownwind[racepos][legpos]).ljust(12)

            results += '\n'

    if not results:
        return 'This race seems to be finished!'

    return results

