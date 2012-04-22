
import os, logging
from ConfigParser import ConfigParser

LOG = logging.getLogger('sailing.db')

ALL_KEYS = ('host', 'port', 'dbname', 'username', 'password', 'collections', 'master_host')
MANDATORY_KEYS = ('host', 'port', 'dbname', 'collections')

def readDatabaseConfiguration(db_ini='db.ini'):
    """ Read the database configuration """

    if not os.path.exists(db_ini):
        raise ConfigurationError('No database configuration found (%s)' % db_ini)

    CP = ConfigParser()
    CP.read(db_ini)
    result = dict()
    for section in CP.sections():
        d = dict()
        for k in ALL_KEYS:
            v = None
            if CP.has_option(section, k):
                if k == 'port':
                    v = CP.getint(section, k)
                elif k == 'collections':
                    v = [x.strip() for x in CP.get(section, k).split(',') if x.strip()]
                else:
                    v = CP.get(section, k)

            if k in MANDATORY_KEYS and not v:
                raise ConfigurationError('Database configuration error: [%s]>%s in "%s" is mandatory' % (section, k, db_ini))
            d[k] = v
        result[section] = d
    return result


# Read the database configuration either from $YSH_DATABASE_CONFIGURATION
# or <BUILDOUT_DIR>/etc/db.ini

filename = os.environ.get('YSH_DATABASE_CONFIGURATION', 
                          os.path.join(os.getcwd(), 'etc', 'db.ini'))
LOG.info('Reading database configuration from %s' % filename)
DB_CONFIG  = readDatabaseConfiguration(filename)

