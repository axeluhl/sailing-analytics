
import datetime

from base import ModelBase
from util import registerClass, queryContent, createContent

from entities import *

class EventImpl(ModelBase):
    linked_model = Event

class RaceImpl(ModelBase):
    linked_model = Race

    default_values = {
        'running': False,
    }

class CompetitorImpl(ModelBase):
    linked_model = Competitor

    default_values = {
        'races': [],
        'marks': [[]]
    }

    @classmethod
    def sortedBy(cls, eventname, raceindex=None, markindex=None, valueindex=None):
        """ Returns competitors sorted by given index. If index
        is None no sorting is performed on this index. If all index
        params are None sorting is performed by total. """

        def sorter(x, y):
            xval = x.total
            yval = y.total

            if valueindex is not None:
                try:
                    xval = x.values[raceindex][markindex][valueindex]
                    yval = y.values[raceindex][markindex][valueindex]
                except IndexError:
                    xval = 0.0
                    yval = 0.0

            elif markindex is not None:
                xval = x.marks[raceindex][markindex]
                yval = y.marks[raceindex][markindex]

            elif raceindex is not None:
                xval = x.races[raceindex]
                yval = y.races[raceindex]

            return cmp(xval, yval)

        competitors = cls.queryBy(event=eventname)
        competitors.sort(sorter)
        return competitors

