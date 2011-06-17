
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
LIST_RACES_IN_EVENT = 'listracesinevent'
LIVE_EVENTS = 'showrace'

SHOW_WAYPOINTS = 'showwaypoints'
SHOW_BOAT_POSITIONS = 'showboatpositions'

# context ADMIN
ADD_WIND = 'addwindtomarksforonehour'

LIST_WIND_TRACKERS = 'listwindtrackers'
STOP_EXP_WIND = 'stopreceivingexpeditionwind'
START_EXP_WIND = 'receiveexpeditionwind'

VN = {
        'CSPDKNOT': ('CSPEEDKN', 'Current Speed (kn)', '%.1f', 'currentSpeedOverGroundInKnots'),
        'CSPDMS' : ('CSPDMS', 'Current Speed (m/s)', '%.1f', 'currentSpeedOverGroundInMetersPerSecond'),
        'ASPDKNOT': ('ASPDKN', 'Avg Speed (kn)', '%.2f', 'averageSpeedOverGroundInKnots'),
        'ASPDMS' : ('ASPDMS', 'Avg Speed (m/s)', '%.1f', 'averageSpeedOverGroundInMetersPerSecond'),
        'DSTTRV' : ('DSTTRV', 'Dist. Trav. (m)', '%.f', 'distanceTraveledOverGroundInMeters'),
        'VMGKNOT': ('VMGKN', 'VMG (kn)', '%.1f', 'velocityMadeGoodInKnots'),
        'VMGMS' : ('VMGMS', 'VMG (m/s)', '%.1f', 'velocityMadeGoodInMetersPerSecond'),
        'AVMGKNOT': ('AVMGKN', 'Avg VMG (kn)', '%.1f', 'averageVelocityMadeGoodInKnots'),
        'AVMGMS' : ('AVMGMS', 'Avg VMG (m/s)', '%.1f', 'averageVelocityMadeGoodInMetersPerSecond'),
        'ETA' : ('ETASEC', 'ETA (s)', '%.f', 'estimatedTimeToNextMarkInSeconds'),
        'DSTGO' : ('DSTGO', 'Dist. To Go (m)', '%.f', 'windwardDistanceToGoInMeters'),
        'GAPSEC' : ('SGAP', 'Gap To Leader (s)', '%.1f','gapToLeaderInSeconds'),
        'UPDOWNWIND' : ('UPDOWN', 'Up or Downwind?', '%s', 'upOrDownWindLeg'),
        'GLP' : ('GLP', 'G & L (Places)', '%s', 'gainsAndLossesInPlaces'),
        'RANK' : ('LRANK', 'Rank in Leg', '%s', 'rank'),
        'STARTED' : ('STARTD', 'Started?', '%s','started'),
        'FINISHED' : ('FINISHD', 'Finished?', '%s','finished')
        }

COLUMN_MODE_NAMES = {
        'TOP': 
            [VN['RANK'], VN['CSPDKNOT'], VN['ASPDKNOT'], VN['DSTTRV'], VN['VMGKNOT'], VN['DSTGO'], VN['ETA'], VN['STARTED'], VN['FINISHED'], VN['UPDOWNWIND']],
        'LEADERBOARD-STATIC': 
            [VN['RANK'], VN['CSPDMS'], VN['DSTTRV'], VN['VMGMS'], VN['GLP']],
        'LEADERBOARD-F1':
        [VN['RANK'], VN['GAPSEC'], VN['ASPDKNOT'], VN['DSTTRV'], VN['GLP']], # TODO: rank, speed, distance (rank, gap, eta)
        'LEADERBOARD-F2':
            [VN['RANK'], VN['GAPSEC'], VN['GLP']],
        }

