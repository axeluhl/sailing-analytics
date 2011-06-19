
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
    def sortedBy(cls, eventname, raceindex=None, markindex=None, valueindex=None, columns=None, direction='desc', current_rank=False):
        """ Returns competitors sorted by given index. If index
        is None no sorting is performed on this index. If all index
        params are None sorting is performed by total. """

        def matchDirection(v, ignore_zeros=False):
            rs = [None, ]
            if ignore_zeros is True:
                rs = [None, 0.0, '0']

            if direction == 'desc' \
                    and v in rs:
                return 0.0
            elif direction == 'asc' \
                    and v in rs:
                return 10000.0

            return v

        def legValueOf(c):
            valuekey = columns[valueindex][-1]
            return (matchDirection(c.values[raceindex][markindex].get(valuekey, None)), c)

        def markRankOf(c):
            return (matchDirection(c.marks[raceindex][markindex], ignore_zeros=True), c)

        def raceRankOf(c):
            return (matchDirection(c.races[raceindex], ignore_zeros=True), c)

        def currentOf(c):
            return (matchDirection(c.current_rank, ignore_zeros=True), c)

        def totalOf(c):
            return (matchDirection(c.total, ignore_zeros=True), c)

        competitors = cls.queryBy(event=eventname)
        if valueindex is not None:
            intermediate = map(legValueOf, competitors)

        elif markindex is not None:
            intermediate = map(markRankOf, competitors)

        elif raceindex is not None:
            intermediate = map(raceRankOf, competitors)

        elif current_rank is True:
            intermediate = map(currentOf, competitors)

        else:
            intermediate = map(totalOf, competitors)

        intermediate.sort(lambda x,y:cmp(x[0], y[0]))
        return [cp[1] for cp in intermediate]

