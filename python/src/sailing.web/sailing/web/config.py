
# contexts for sailing.connector.URIConfigurator
MODERATOR = 'moderator'
ADMIN = 'admin'

# actions for URIConfigurator
ADD_EVENT = 'addevent'
ADD_RACE = 'addrace'

STOP_EVENT = 'stopevent'
STOP_RACE = 'stoprace'

SHOW_WIND = 'showwind'
SET_WIND = 'setwind'
SET_WIND_SOURCE = 'selectwindsource'

LIST_EVENTS = 'listevents'
LIVE_EVENTS = 'showrace'

SHOW_WAYPOINTS = 'showwaypoints'
SHOW_BOAT_POSITIONS = 'showboatpositions'

# context ADMIN
ADD_WIND = 'addwindtomarksforonehour'

LIST_WIND_TRACKERS = 'listwindtrackers'
STOP_EXP_WIND = 'stopreceivingexpeditionwind'
START_EXP_WIND = 'receiveexpeditionwind'

# keys for competitor board leg values
LEG_VALUE_DATA = {
        'rank': [0, ],
        'averageSpeedOverGroundInKnots': [1, ],
        'distanceTraveledOverGroundInMeters': [2, ],
        'velocityMadeGoodInKnots': [3, ],
        'averageVelocityMadeGoodInKnots': [4, ],
        'gapToLeaderInSeconds': [5, ],
        'estimatedTimeToNextMarkInSeconds': [6, ],
        'windwardDistanceToGoInMeters' : [7, ],
        }

SORTED_LEG_VALUE_NAMES = LEG_VALUE_DATA.keys()
SORTED_LEG_VALUE_NAMES.sort(lambda x,y: cmp(LEG_VALUE_DATA[x][0], LEG_VALUE_DATA[y][0]))

