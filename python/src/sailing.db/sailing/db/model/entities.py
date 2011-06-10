
import datetime

from base import BaseDocument
from util import registerClass, queryContent, createContent

class Event(BaseDocument):
    _collection = 'events'
    skip_validation = True

    structure = {
        'name': unicode,
        'boatclass': unicode,
        'competitors': [unicode, ],
        'races': [unicode, ],
        'marks': [[unicode, ]],
        'typename': unicode,
        'created': datetime.datetime
    }

    indexes = [
        {
            'fields' : ['name'],
            'unique': True
        },
    ]

registerClass(Event)

class Race(BaseDocument):
    _collection = 'races'
    skip_validation = True

    structure = {
        'name': unicode,
        'event': unicode,
        'current_legs': [int, ], # holds a list of positions of current legs
        'startoftracking': float,
        'start': float,
        'finish' : float,
        'timeofnewestevent': float,
        'updatecount': float,
        'wind_source': unicode,
        'wind_bearing': float,
        'wind_speed': float,
        'typename': unicode,
        'created': datetime.datetime
    }

    indexes = [
        {
            'fields' : ['name', 'event'],
            'unique': True
        },
    ]

registerClass(Race)

class Competitor(BaseDocument):
    _collection = 'competitors'
    skip_validation = True

    structure = {
        'name': unicode,
        'event': unicode,
        'nationality': unicode,

        'total': float,
        'current_rank': int, # current rank for given time
        'races' : [float, ],
        'marks' : [[float, ], ],
        'values' : [[[float, ]]]
    }

    indexes = [
        {
            'fields' : ['name', 'event'],
            'unique': True
        },
    ]

registerClass(Competitor)

