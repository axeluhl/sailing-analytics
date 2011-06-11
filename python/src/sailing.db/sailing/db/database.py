import logging

import mongokit
import mongokit.master_slave_connection

from sailing.db import DB_CONFIG

logging.basicConfig(level=logging.INFO)
LOG = logging.getLogger('sailing.db')

__all__ = ('getCollection',)

def setupDatabases():
    """ Setup MongoDB database connections and collections """

    global collections, databases, connections, collection2database

    for d in DB_CONFIG.values():

        dbname = d['dbname']
        slave_configuration = dict(host=d['host'], port=d['port'])

        if d['master_host']:
            # master and slaves share the same configuration - except
            # the hostname of the master host
            master_configuration = slave_configuration.copy()
            master_configuration['host'] = d['master_host']
            LOG.info('Setting up database as master-slave (database: %s, master: %s, slave: %s)' %
                      (dbname, master_configuration, slave_configuration))
            conn = mongokit.master_slave_connection.MasterSlaveConnection(master_configuration, [slave_configuration])
        else:
            LOG.info('Setting up database (database:%s , master: %s)' % (dbname, slave_configuration))
            conn = mongokit.Connection(**slave_configuration)

        db = mongokit.Database(conn, dbname)
        databases[dbname] = db
        connections[dbname] = conn

        for collection in d['collections'][0].strip().split(' '):
            collections[collection] = mongokit.Collection(db, collection)
            collection2connection[collection] = conn

        LOG.info('Attached collections to database %s: %s' % (dbname, ', '.join(d['collections'])))

def getCollection(collection):
    """ Return a MongoDB collection by its name """
    coll = collections.get(collection)
    if not coll:
        raise ValueError('No collection %s configured' % collection)
    return coll

def getConnection(database):
    """ Return connection for a given database """
    conn = connections.get(database)
    if not conn:
        raise ValueError('No connection for database %s found' % database)
    return conn

def getConnectionByCollectionName(collection):
    """ Return connection object for a given collection name """
    db = collection2connection.get(collection)
    if not db:
        raise ValueError('No database for collection %s found' % collection)
    return db

collection2connection = dict()
connections = dict()
databases = dict()
collections = dict()
setupDatabases()

