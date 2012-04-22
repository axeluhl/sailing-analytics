
import cjson, datetime
import urllib, urllib2

from sailing.web.util import get_abspath

import logging
logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

class URIConfigurator(object):

    def __init__(self, host, port, log=True):
        self.host = host
        self.port = port
        self.do_log = log
        self.expedition = None

    def copy(self):
        inst = URIConfigurator(self.host, self.port)
        inst.__dict__ = self.__dict__.copy()
        return inst

    def key(self):
        return '%s:%s' % (self.host, self.port)

    def clear(self):
        try:
            del self.__dict__['parameters']
            del self.__dict__['_parameters']
        except:
            pass

    def setLogging(self, state):
        self.do_log = state

    def setContext(self, context):
        self.clear()
        self.context = context

    def setCommand(self, command):
        self.command = command

    def setRace(self, race):
        self.race = race

    def setParameters(self, parameters={}):
        if hasattr(self, 'command'):
            parameters['action'] = self.command

        if hasattr(self, 'race') and self.race:
            parameters['eventname'] = self.race.event
            parameters['racename'] = self.race.name

        self.parameters = urllib.urlencode(parameters)
        self._parameters = parameters

    def hasParameter(self, name):
        return self._parameters.has_key(name)

    def URI(self):
        if not hasattr(self, 'parameters'):
            # ensure that parameters are set correctly
            self.setParameters()

        uri = 'http://%s:%s/%s?%s' % (self.host, self.port, self.context, self.parameters)
        return uri

    def trigger(self, request=None):
        if self.do_log is True:
            log.info(' %s Triggering URI %s' % (datetime.datetime.now(), self.URI()))
        return urllib2.urlopen(self.URI()).read()

def jsonByUrl(configurator):
    """ Connects to specified REST service and returns parsed data """

    # small hack to make tests easier
    if configurator.port is None:
        handle = open(get_abspath('sailing.web:tests/data/%s' % configurator.host), 'r')
        data = handle.read()
        handle.close()

    else:
        data = configurator.trigger()

        if data.find('Exception')>0:
            raise Exception, 'Java triggered exception: %s' % data[:550]

    return cjson.decode(data.strip())

