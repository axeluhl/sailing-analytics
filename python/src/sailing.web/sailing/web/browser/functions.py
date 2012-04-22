
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

    if not request.params.get('event'):
        from sailing.db.monitoring import dropDB
        dropDB()

    else:
        model.CompetitorImpl.removeAllBy(event=request.params.get('event'))
        model.RaceImpl.removeAllBy(event=request.params.get('event'), name=request.params.get('race'))
        model.EventImpl.removeAllBy(name=request.params.get('event'))

    return HTTPFound(location='/')

def startListenerThreads(conf, eventlist, delay=None):
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
                    t = provider.LiveDataReceiver(conf.host, conf.port, event['name'], racename, delay) 
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

        if request.POST.get('race-conf'):
            # multiple races configured

            for raceurl in request.POST.getall('race-conf'):
                conf.setCommand(config.ADD_RACE)
                conf.setParameters(dict(paramURL=raceurl, 
                                        liveURI=request.POST.get('liveURI'), 
                                        storedURI=request.POST.get('storedURI'),
                                        windstore=request.POST.get('windstore')))
                conf.trigger()


        elif request.POST.get('eventJSONURL'):
            conf.setCommand(config.ADD_EVENT)
            conf.setParameters(dict(eventJSONURL=request.POST.get('eventJSONURL'), 
                                    liveURI=request.POST.get('liveURI'), 
                                    storedURI=request.POST.get('storedURI'),
                                    windstore=request.POST.get('windstore')))

        elif request.POST.get('paramURL'):
            conf.setCommand(config.ADD_RACE)
            conf.setParameters(dict(paramURL=request.POST.get('paramURL'), 
                                    liveURI=request.POST.get('liveURI'), 
                                    storedURI=request.POST.get('storedURI'),
                                    windstore=request.POST.get('windstore')))

            if not view.session.get('race.url_history'):
                view.session['race.url_history'] = []

            if request.POST.get('paramURL') not in view.session['race.url_history']:
                view.session['race.url_history'].append(request.POST.get('paramURL'))
                view.session.save()

        else:
            return view.yieldMessage('Please specify an event or race URL!')

        if not request.POST.get('race-conf'):
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

                delay = request.POST.get('delay', '')
                startListenerThreads(conf, eventlist, delay)

                if request.POST.get('expedition_port'):
                    expedition_port = request.POST.get('expedition_port')

                    # persist expedition port
                    view.session['listener-conf'].expedition = expedition_port

                    for event in eventlist:
                        eventname = event['name']
                        for race in event['races']:
                            racename = race['name']

                            conf.setContext(config.ADMIN)
                            conf.setCommand(config.START_EXP_WIND)
                            conf.setParameters(dict(port=expedition_port, eventname=eventname, racename=racename))
                            conf.trigger()
                            log.info('Configured expedition wind for event %s, race %s on port %s' % (eventname, racename, expedition_port))

            except Exception, ex:
                return view.yieldMessage('Could not gather any data! Seems that Java listener is not ready yet. Please reconnect. Error: %s' % str(ex))

        # mark all stuff done and listener running
        view.session['listener-started'] = True
        view.session.save()

    elif request.POST.get('emergency-reload', None):
        state = reloadData(context, request)
        if state is True:
            return view.yieldMessage('Data refreshed! Can take a while until listener threads recover. Make sure to also reload running Leaderboards!')
        return view.yieldMessage('Could not refresh data - this should NEVER happen :-)')

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
        delay = request.POST.get('delay', '')
        startListenerThreads(conf, eventlist, delay)

        view.session['listener-started'] = True
        view.session['listener-conf'] = conf
        view.session.save()

        return view.yieldMessage('Event handler refreshed!')

    elif request.POST.get('leaderboard-default-event', None):
        core.current_leaderboard_event = request.POST.get('event')

    elif request.POST.get('set-averaging', None):
        conf.setContext(config.ADMIN)
        conf.setCommand(config.SET_AVERAGING)

        eventname = request.POST.get('event', None)
        racename = request.POST.get('race', None)

        aWind = request.POST.get('averagingWind')
        aSpeed = request.POST.get('averagingSpeed')
        conf.setParameters(dict(eventname=eventname, racename=racename, windaveragingintervalmillis=aWind, speedaveragingintervalmillis=aSpeed))

        conf.trigger()
        return view.yieldMessage('Averaging set!')

    return HTTPFound(location='/')

@jsonize
def configuredListeners(context, request):

    out = []
    for key, listener in threaded_listener.items():
        out.append( {'host': listener.host, 'port': listener.port,
                        'eventname': listener.eventname, 'last_update': listener.last_update and listener.last_update.strftime('%d.%m %H:%M:%S') or '-',
                        'paused' : listener.paused, 'running': listener.running, 'xid' : str(key),
                        'racename': listener.racename, 'error': listener.error} )

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
    if race is None:
        return 'No current configured race found'

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

    columns = view.configuredColumns('TOP')

    results = ''
    for pos in range(len(currentlegs)):
        legpos = currentlegs[pos]

        results += '\nLAST SHOWRACE CALL: %s (UPCOUNT PARAM: %s)\nLEG: %s (FROM: %s TO: %s)\nRACE-START: %s NEWEST EVENT: %s WIND: (%s %s %s)\n' % (t_up, t_upcount, legpos+1, competitors[0].marknames[racepos][legpos][0], competitors[0].marknames[racepos][legpos][1], view.millisToDatetime(race.start), view.millisToDatetime(race.timeofnewestevent), race.wind_source, race.wind_bearing, race.wind_speed)
        results += 'NAME'.ljust(16) + 'TOTAL'.ljust(7) + 'CRANK'.ljust(7) + 'RRANK'.ljust(7) 
        results += 'MRANK'.ljust(7) + ''.join([title[0].ljust(9) for title in columns])
        results += '\n'

        # sort competitors by rank in current leg
        competitors.sort(lambda x,y:cmp(x.values[racepos][legpos] and x.values[racepos][legpos].get('rank') or 6000, y.values[racepos][legpos] and y.values[racepos][legpos].get('rank') or 6000))

        for c in competitors:
            results += '%s %s %s' % (c.name.ljust(15), str(c.total).ljust(6), str(c.current_rank).ljust(6))
            results += '%s %s' % (str(c.races[racepos]).ljust(7), str(c.marks[racepos][legpos]).ljust(7))
                
            for v in columns:
                results += view.displayLegValue(v, c.values[racepos][legpos].get(v[-1])).ljust(9)

            results += '\n'

    if not results:
        return 'This race seems to be finished or not yet started!'

    return results

@jsonize
def moderatorLiveData(context, request):
    """ Returns data for the moderators leaderboard """

    lock = threading.Lock()
    with lock:
        view = core.BaseView(context, request)
        event = view.currentLeaderboardEvent()

        sortby = request.params.get('sortby', 'name')
        race_range = request.params.get('races', '1:3')
        competitor_range = request.params.get('competitors', '1:20')
        direction = request.params.get('direction', 'asc');
        colmode = request.params.get('colmode')

        columns = view.configuredColumns(colmode)
        
        if race_range in ['null', 'undefined']:
            race_range = '1:3'

        race_start_index, race_end_index = race_range.split(':')
        races_list = event.races[int(race_start_index)-1:int(race_end_index)]

        competitors = view.competitorsSortedBy(event.name, sortby.strip(), colmode, direction)

        if direction == 'desc':
            competitors.reverse()

        races = []; current_legs = []; current_race = None
        for racename in races_list:
            rimpl = model.RaceImpl.queryOneBy(name=racename, event=event.name)
            if len(rimpl.current_legs) > 0:
                current_legs = rimpl.current_legs
                current_race = event.races.index(rimpl.name)

            races.append(rimpl)

        # list of competitors with corresponding data
        data = []

        for competitor in competitors:
            racedata = competitor.races[int(race_start_index)-1:int(race_end_index)]

            # for each race found compute the marks and values
            markranks = []; legvalues = []; racecounter = 0
            for racename in races_list:
                real_racepos = event.races.index(racename)
                if len(markranks) < racecounter+1:
                    markranks.append([])
                    legvalues.append([])

                markranks[racecounter] = markranks[racecounter] + competitor.marks[real_racepos]

                cvalues = []
                for legvalue_for_race in competitor.values[real_racepos]:
                    result = []
                    for column in columns:
                        result.append(view.displayLegValue(column, legvalue_for_race.get(column[-1])))

                    cvalues.append(result)

                legvalues[racecounter] = legvalues[racecounter] + cvalues

                racecounter += 1

            dc = {}
            dc.update({'name': competitor.name[:9], 'raceranks': racedata, 'markranks': markranks, 'legvalues': legvalues, 
                'nationality': competitor.nationality, 'nationality_short': competitor.nationality_short.lower(), 'global_rank': competitor.total, 'current_race': '', 'current_legs' : [],
                'current_rank' : competitor.current_rank, 'total_points' : getattr(competitor, 'total_points', 0),
                'net_points': getattr(competitor, 'net_points', 0), 'races_shown' : range(int(race_start_index), int(race_end_index))})

            try:
                dc.update({'current_legs': [lg+1 for lg in current_legs], 'current_race': current_race+1})
            except:
                pass

            data.append(dc)

        c_range_start, c_range_end = competitor_range.split(':')

    return data[int(c_range_start)-1:int(c_range_end)]

def loadRacesForEvent(context, request):
    view = core.BaseView(context, request)

    host = request.params.get('host')
    port = request.params.get('port')
    eventJSONURL = request.params.get('eventJSONURL')

    conf = URIConfigurator(host, port)
    view.session['listener-conf'] = conf
    view.session.save()

    conf.setContext(config.ADMIN)
    conf.setCommand(config.LIST_RACES_IN_EVENT)
    conf.setParameters(dict(eventJSONURL=eventJSONURL))

    data = jsonByUrl(conf)

    races = data.sort(lambda x,y:cmp(x['name'], y['name']))
    return render_to_response('templates/java-connector-select-races.pt', {'races' : data, 'view': view}, request=request)

@jsonize
def showAveraging(context, request):
    eventname = request.params.get('eventname')
    racename = request.params.get('racename')

    race = model.RaceImpl.queryOneBy(event=eventname, name=racename)
    return 'Wind: %s, Speed: %s' % (getattr(race, 'averagingwind', 'n/a'), getattr(race, 'averagingspeed', 'n/a'))

def reloadData(context, request):
    # drops all data in database and reload all running listeners

    view = core.BaseView(context, request)

    # pause all listener threads
    for listener in threaded_listener.values():
        if listener.paused == False:
            listener.paused = True

    # give threads time to pause
    time.sleep(10)

    # drop DB with all data in it
    from sailing.db.monitoring import dropDB
    dropDB()

    # reload event data completely
    configurator = view.listenerConf()
    configurator.setContext(config.MODERATOR)
    configurator.setCommand(config.LIST_EVENTS)
    provider.eventConfiguration(configurator) 

    # start threads again
    for listener in threaded_listener.values():
        if listener.paused == True:
            listener.paused = False

    return True
