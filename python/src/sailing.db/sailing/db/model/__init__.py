
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
        'marks': [[]],
        'marknames': [[]],
        'in_race': False
    }

    @classmethod
    def sortedBy(cls, eventname, raceindex=None, markindex=None, valueindex=None, columns=None, direction='desc'):
        """ Returns competitors sorted by given index. If index
        is None no sorting is performed on this index. If all index
        params are None sorting is performed by total. """

        def matchDirection(v):
            if direction == 'desc' and v is None:
                return 0.0
            elif direction == 'asc' and v is None:
                return 10000.0
            return v

        def legValueOf(c):
            valuekey = columns[valueindex][-1]
            return (matchDirection(c.values[raceindex][markindex].get(valuekey, None)), c)

        def markRankOf(c):
            return (matchDirection(c.marks[raceindex][markindex]), c)

        def raceRankOf(c):
            return (matchDirection(c.races[raceindex]), c)

        def currentOf(c):
            return (matchDirection(c.current_rank), c)

        competitors = cls.queryBy(event=eventname)
        if valueindex is not None:
            intermediate = map(legValueOf, competitors)

        elif markindex is not None:
            intermediate = map(markRankOf, competitors)

        elif raceindex is not None:
            intermediate = map(raceRankOf, competitors)

        else:
            intermediate = map(currentOf, competitors)

        intermediate.sort(lambda x,y:cmp(x[0], y[0]))
        return [cp[1] for cp in intermediate]

