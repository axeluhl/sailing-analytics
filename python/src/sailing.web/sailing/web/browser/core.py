
import time, datetime

import urllib

from webob.exc import HTTPFound
from pyramid.renderers import get_renderer, render_to_response

from sailing.connector import provider, URIConfigurator, jsonByUrl
from sailing.db import model
from sailing.db import monitoring

from sailing.web import config

import logging
log = logging.getLogger(__name__)
logging.basicConfig()

current_leaderboard_event = None

class BaseView(object):

    def __init__(self, context, request):
        self.context = context
        self.request = request

        # don't forget to call save() if you changed s/th on the session
        self.session = request['beaker.session']

    def __call__(self):
        return {'main': self.mainTemplate()}

    def yieldMessage(self, message):
        parameters = urllib.urlencode(dict(message=message))
        return HTTPFound(location='/?%s' % parameters)

    def mainTemplate(self):
        return get_renderer('templates/maintemplate.pt').implementation()

    def eventUrlHistory(self):
        return self.session.get('event.url_history', ['No URLs to TracTrac events gathered until now'])

    def raceUrlHistory(self):
        return self.session.get('race.url_history', ['No URLs to TracTrac races gathered until now'])

    def dbDatabaseStats(self):
        return monitoring.dbStats()

    def dbCollectionStats(self):
        return monitoring.collectionStats()

    def dbServerStatus(self):
        return monitoring.serverStatus()

    def millisToDatetime(self, millis, short=False):
        if millis is None or millis >= 9023372036854775807:
            return None

        try:
            tm = time.gmtime(millis/1000)

            if tm.tm_year >= 2000:
                dt = datetime.datetime(year=tm.tm_year, month=tm.tm_mon, day=tm.tm_mday, hour=tm.tm_hour, minute=tm.tm_min, second=tm.tm_sec)
                return dt
        except:
            log.error('Error', exc_info=True)
        
        return None

    def datetimeToMillis(self, dt):
        if dt:
            dtnew = datetime.datetime(year=dt.year, month=dt.month, day=dt.day, hour=dt.hour+2, minute=dt.minute, second=dt.second)
            return time.mktime(dtnew.timetuple())*1000
        return 0

    def parseDatetime(self, dt, format='%m/%d/%Y %H:%M:%S'):
        return datetime.datetime.strptime(dt.strip(), format)

    def listenerStarted(self):
        return self.session.get('listener-started', False)

    def listenerConf(self):
        if self.listenerStarted():
            return self.session['listener-conf'].copy()
        return None

    def currentRace(self):
        if not self.session.has_key('current-race'):
            raise Exception, 'No current race could be determined'

        return model.RaceImpl.queryOneBy(event=self.session['current-event'], name=self.session['current-race'])

    def currentEvent(self):
        return model.EventImpl.queryOneBy(name=self.session['current-event'])

    def currentLeaderboardEvent(self):
        cev = model.EventImpl.queryOneBy(name=current_leaderboard_event)

        # XXX remove this - only for tests
        if not cev:
            return model.EventImpl.queryBy()[0]

        return cev

    def raceBy(self, event, name):
        return model.RaceImpl.queryOneBy(event=event, name=name)

    def currentWind(self):
        conf = self.listenerConf()
        race = self.currentRace()

        conf.setContext(config.ADMIN)
        conf.setCommand(config.SHOW_WIND)
        conf.setParameters(dict(eventname=race.event, racename=race.name))
       
        data = jsonByUrl(conf)

        for name in ['WEB', 'EXPEDITION']:
            if data and data.get(name):
                expwind = data[name]
                expwind.reverse()
                data[name] = expwind

        return data

    def allEvents(self):
        return model.EventImpl.queryBy()

    def allRacesFor(self, event=None):
        if event is None:
            allevents = self.allEvents()

            races = []
            for event in allevents:
                races += event.races

            return races

        return model.EventImpl.queryOneBy(name=event).races

    def allCompetitorsFor(self, event):
        return model.CompetitorImpl.queryBy(event=event)

    def competitorsSortedBy(self, eventname, sortparam, columnmode):
        param = sortparam
        if param == 'name':
            competitors = model.CompetitorImpl.sortedBy(eventname=eventname)
            competitors.sort(lambda x,y: cmp(x.name, y.name))

        elif param == 'total':
            competitors = model.CompetitorImpl.sortedBy(eventname=eventname)

        elif param.find(',')>0:
            params = param.strip().split(',')

            if len(params) == 2:
                competitors = model.CompetitorImpl.sortedBy(eventname=eventname, raceindex=int(params[0])-1, markindex=int(params[1])-1)
            elif len(params) == 3:
                columns = self.configuredColumns(columnmode)
                competitors = model.CompetitorImpl.sortedBy(eventname=eventname, raceindex=int(params[0])-1, markindex=int(params[1])-1, valueindex=int(params[2])-1, columns=columns)

        else:
            param = int(param.strip())
            competitors = model.CompetitorImpl.sortedBy(eventname=eventname, raceindex=param-1)

        return competitors

    def configuredColumns(self, modename):
        return config.COLUMN_MODE_NAMES.get(modename)

    def displayLegValue(self, name, value):
        if isinstance(value, float) \
                and value == 42.260426041982:
            val = 'ANCH'
        else:
            if value in [None, 'None']:
                return ''

            val = name[2] % value

        if name[0] == 'ETASEC':
            val = '%.f:%.f' % (int(value) / 60, int(value)-( (int(value)/60)*60 ))

        return val

def callFunction(context, request):
    """ Calls a function dynamically """

    import functions
    return getattr(functions, request.matchdict.get('function'))(context, request)

def renderTemplate(context, request):
    """ Renders a template dynamically """

    view = BaseView(context, request)
    return render_to_response('templates/%s.pt' % request.matchdict.get('template'),
                                {'context' : context, 'view':view, 
                                 'main' : view.mainTemplate()},
                                request=request)

def renderCSS(context, request):
    """ Renders a CSS template dynamically """

    response = render_to_response('static/css/%s.txt' % request.matchdict.get('template'),
                                {'context' : context},
                                request=request)

    # could be more generic
    response.content_type = 'text/css'
    return response


