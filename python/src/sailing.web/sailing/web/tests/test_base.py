
import codecs

import unittest
import urllib, urllib2

import thread
from time import sleep

import pyramid
import pyramid_zcml
from pyramid.config import Configurator

import sailing.web

from sailing.db.monitoring import dropDB
from sailing.db.model import EventImpl, CompetitorImpl, RaceImpl

from sailing.connector import provider, URIConfigurator
from sailing.web import config

from sailing.web.browser import functions
from utils import get_request

# Make sure URLs are not triggered for tests
#URIConfigurator.trigger = lambda instance: None

def output(data):
    handle = codecs.open('boarddata.txt', 'a', 'utf-8')
    handle.write(data + '\n')
    handle.close()

    print data

def printLeaderBoard(competitors, eventname, markvalues=0, output=output):
    """ Prints leaderboard to the screen """

    event = EventImpl.queryOneBy(name=eventname)

    cols = ["T".ljust(3), "Name".ljust(15)]
    for racepos in range(len(event.races)):
        cols.append( ('R%s' % (racepos+1)).ljust(4) )

        for mpos in range(len(event.marks[racepos])):
            cols.append( ('M%s' % (mpos+1)).ljust(4) )

            if mpos == markvalues and racepos == markvalues:
                for key in config.SORTED_LEG_VALUE_NAMES:
                    attr = config.LEG_VALUE_DATA[key]
                    cols.append( key[:8].ljust(10) )

    output(''.join(cols))

    for c in competitors:
        cols = [str(c.total or 0).ljust(3), c.name[:14].ljust(15)]
        for racepos in range(len(c.races)):
            cols.append(str(c.races[racepos]).ljust(4))

            # show marks
            for mpos in range(len(c.marks[racepos])):
                cols.append(str(c.marks[racepos][mpos]).ljust(4))

                if mpos == markvalues:
                    for key in config.SORTED_LEG_VALUE_NAMES:
                        attr = config.LEG_VALUE_DATA[key]
                        try:
                            cols.append( ('%.3f' % c.values[racepos][mpos][attr[0]]).ljust(10) )
                        except:
                            cols.append('0.0'.ljust(10))

        output(''.join(cols))

class TestLoader(unittest.TestCase):

    def setupIntegration(self):
        self.config = Configurator(package=sailing.web)
        self.config.include(pyramid_zcml)

        self.config.begin(request=get_request())
        self.config.load_zcml('sailing.web:configure.zcml')

    def startTracSimulator(self):
        # configure trac simulator access
        auth = urllib2.HTTPBasicAuthHandler()
        auth.add_password(realm='JV',
                            user='tracsim', passwd='simming10',
                            uri='http://www.traclive.dk/')
        opener = urllib2.build_opener(auth)
        urllib2.install_opener(opener)

        def open_url():
            urllib2.urlopen('http://tracsim:simming10@www.traclive.dk/simulate/start.php?racenumber=7&speed=1&replaytime=sample')

        # we need a new thread here because call will block until simulation is finished
        thread.start_new_thread(open_url)

    def stopTracSimulator(self):
        handle = urllib2.urlopen('http://www.traclive.dk/simulate/kill.php')

    def setUp(self):
        dropDB()

    def testStartStopListener(self):
        self.setupIntegration()

        # calls addrace - for addevent we would need to trigger with POST param eventJSONURL
        get_request().POST = {'host': 'localhost', 'port': '8888', 
                    'paramURL': 'http://germanmaster.traclive.dk/events/event_20110505_SailingTea/clientparams.php?event=event_20110505_SailingTea&race=bd8c778e-7c65-11e0-8236-406186cbf87c', 
                    'liveURI': 'tcp://simulation.tracdev.dk:4410', 
                    'storedURI': 'tcp://simulation.tracdev.dk:4411',
                    'listener-start': '1'}

        # for tests this just triggers that event has been created
        functions.configureListener(None, request=get_request())

        from sailing.web.browser.core import BaseView
        view = BaseView(None, get_request())
        self.assertTrue(view.listenerStarted())
        self.assertEquals('8888', view.listenerConf().port)

        get_request().POST = {'event' : 'Dummy', 'listener-stop': '1'}
        functions.configureListener(None, request=get_request())
        self.assertFalse(view.listenerStarted())
        self.assertEquals(None, view.listenerConf())

    def testEventConfigurationRead(self):
        # Normally the server is configured for a specific event
        # and then reads races from there. For each event we get
        # initial configuration that prepares data structures in DB

        provider.eventConfiguration(URIConfigurator('events.txt', None))
        self.assertEquals(1, EventImpl.queryCount())
        self.assertEquals(29, CompetitorImpl.queryCount())
        self.assertEquals(1, RaceImpl.queryCount())

        c1 = CompetitorImpl.queryBy()[0]
        self.assertEquals(1, len(c1.races))
        self.assertEquals(1, len(c1.marks))
        self.assertEquals(8, len(c1.marks[0]))

    def testEventConfigurationRead2(self):
        provider.eventConfiguration(URIConfigurator('events2.txt', None))
        self.assertEquals(2, EventImpl.queryCount())
        self.assertEquals(58, CompetitorImpl.queryCount())

        e2 = EventImpl.queryOneBy(name='J90 Worlds')
        self.assertEquals('J90', e2.boatclass)
        self.assertEquals(2, len(e2.races))

        self.assertEquals(8, len(e2.marks[0]))
        self.assertEquals(7, len(e2.marks[1]))
        self.assertEquals('Finish', e2.marks[1][-1])
        self.assertEquals('Mark 1', e2.marks[1][0])

        c1 = CompetitorImpl.queryBy(event='J90 Worlds')[0]
        self.assertEquals(2, len(c1.races))
        self.assertEquals(2, len(c1.marks)) # two races with marks
        self.assertEquals(8, len(c1.marks[0]))
        self.assertEquals(7, len(c1.marks[1]))

    def testEventConfigurationRead2WithUpdate(self):
        provider.eventConfiguration(URIConfigurator('events2.txt', None))
        self.assertEquals(2, EventImpl.queryCount())
        self.assertEquals(58, CompetitorImpl.queryCount())

        # change a value and check if it is preserved by event update
        c1 = CompetitorImpl.queryOneBy(event='J90 Worlds', name='TABARES')
        self.assertEquals(7, len(c1.marks[1]))
        self.assertEquals(0.0, c1.marks[1][0])
        allmarks = c1.marks
        allmarks[1][0] = 26.82 
        c1.update(dict(marks=allmarks))

        # adds another race and one leg is removed from the second race
        uri = URIConfigurator('events2updated.txt', None)
        provider.eventConfiguration(uri)

        self.assertEquals(2, EventImpl.queryCount())
        self.assertEquals(58, CompetitorImpl.queryCount())

        e2 = EventImpl.queryOneBy(name='J90 Worlds')
        self.assertEquals('J90', e2.boatclass)
        self.assertEquals(3, len(e2.races))
        self.assertEquals(3, len(e2.marks)) # three races with marks
        self.assertEquals(8, len(e2.marks[0])) # marks for first race
        self.assertEquals(6, len(e2.marks[1])) # one mark has been altered

        self.assertEquals('Finish', e2.marks[1][-1])
        self.assertEquals('Mark 1', e2.marks[1][0])

        c1 = CompetitorImpl.queryOneBy(event='J90 Worlds', name='TABARES')
        self.assertEquals(3, len(c1.races))
        self.assertEquals(3, len(c1.marks)) # two races with marks
        self.assertEquals(8, len(c1.marks[0]))

        # information is not removed only added (!!)
        self.assertEquals(7, len(c1.marks[1]))
        self.assertEquals(26.82, c1.marks[1][0])

        # test that data initialized is empty
        self.assertEquals(0.0, c1.marks[2][4])

    def testRaceCreation(self):
        pass

    def testLiveRaceInformation(self):
        # Race information is recorded by a blocking call to the provider
        # Data coming are converted into a flat table like structure that
        # fits into a leaderboard display and is easily sortable

        provider.eventConfiguration(URIConfigurator('events.txt', None))
        self.assertEquals(1, EventImpl.queryCount())
        provider.liveRaceInformation(URIConfigurator('race_t1.txt', None), eventname='J80 Worlds', racename='Race 12 (Gold)')

        # the base structure shouldn't have changed
        self.assertEquals(1, EventImpl.queryCount())
        self.assertEquals(29, CompetitorImpl.queryCount())

        demones = CompetitorImpl.queryOneBy(event='J80 Worlds', name='DE MONES')

        # race 0, mark 0, value 0
        self.assertEquals(5.0958275058932445, demones.values[0][0][0])

        # race should have the end score
        self.assertEquals(24, demones.races[0])

        # first mark has rank 20
        self.assertEquals(20, demones.marks[0][0])

        # second mark has rank 20
        self.assertEquals(17, demones.marks[0][1])

        # last leg should have the same as total
        self.assertEquals(demones.marks[0][-1], demones.total)

    def testLeaderboardDataSorted(self):
        provider.eventConfiguration(URIConfigurator('events.txt', None))
        provider.liveRaceInformation(URIConfigurator('race_t1.txt', None), eventname='J80 Worlds', racename='Race 12 (Gold)')

        # sorted by total
        competitors = CompetitorImpl.sortedBy(eventname='J80 Worlds') 
        self.assertEquals('TABARES', competitors[0].name)

        competitors = CompetitorImpl.sortedBy(eventname='J80 Worlds', raceindex=0, markindex=0) 
        self.assertEquals('GOROSTEGUI', competitors[0].name)

    def printLeaderBoard(self):
        provider.eventConfiguration(URIConfigurator('events.txt', None))
        provider.liveRaceInformation(URIConfigurator('race_t1.txt', None), eventname='J80 Worlds', racename='Race 12 (Gold)')

        output("\nLeaderboard (sorted by total)")
        printLeaderBoard(competitors, eventname='J80 Worlds')

        output("\nLeaderboard (mark one open)")
        printLeaderBoard(competitors, eventname='J80 Worlds', markvalues=1)

        competitors = CompetitorImpl.sortedBy(eventname='J80 Worlds', raceindex=0, markindex=0) 
        output("\nLeaderboard (sorted by rank in first mark)")
        printLeaderBoard(competitors, eventname='J80 Worlds', markvalues=0)

        competitors = CompetitorImpl.sortedBy(eventname='J80 Worlds', raceindex=0, markindex=0, valueindex=0) 
        output("\nLeaderboard (sorted by averageSpeed)")
        printLeaderBoard(competitors, eventname='J80 Worlds', markvalues=0)

        competitors = CompetitorImpl.sortedBy(eventname='J80 Worlds', raceindex=0, markindex=0, valueindex=1) 
        output("\nLeaderboard (sorted by distance)")
        printLeaderBoard(competitors, eventname='J80 Worlds', markvalues=0)

