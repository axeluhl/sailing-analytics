
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
        'marknames': [[]]
    }

    @classmethod
    def sortedBy(cls, eventname, raceindex=None, markindex=None, valueindex=None, columns=None, direction='desc'):
        """ Returns competitors sorted by given index. If index
        is None no sorting is performed on this index. If all index
        params are None sorting is performed by total. """

        def sorter(x, y):
            xval = x.total
            yval = y.total

            if valueindex is not None:
                valuekey = columns[valueindex][-1]
                xval = x.values[raceindex][markindex].get(valuekey, None)
                yval = y.values[raceindex][markindex].get(valuekey, None)

                # 9 to 0: so lets put invalid values to the end
                if direction == 'desc':
                    if xval is None: xval = 0
                    if yval is None: yval = 0
                else: # 0 to 9
                    if xval is None: xval = 100000
                    if yval is None: yval = 100000

            elif markindex is not None:
                xval = x.marks[raceindex][markindex]
                yval = y.marks[raceindex][markindex]

                # 9 to 0: so lets put invalid values to the end
                if direction == 'desc':
                    if xval is None: xval = 0
                    if yval is None: yval = 0
                else: # 0 to 9
                    if xval is None: xval = 100000
                    if yval is None: yval = 100000

            elif raceindex is not None:
                xval = x.races[raceindex]
                yval = y.races[raceindex]

                # 9 to 0: so lets put invalid values to the end
                if direction == 'desc':
                    if xval is None: xval = 0
                    if yval is None: yval = 0
                else: # 0 to 9
                    if xval is None: xval = 100000
                    if yval is None: yval = 100000

            return cmp(xval, yval)

        competitors = cls.queryBy(event=eventname)
        competitors.sort(sorter)
        return competitors

