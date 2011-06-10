from database import databases, collections

def dbStats():
    result = dict()
    for dbname in databases:
        db = databases[dbname]
        stats = db.command('dbstats')
        result[dbname] = stats
    return result

def serverStatus():
    db = databases[databases.keys()[0]]
    stats = db.command('serverStatus')
    return stats


def collectionStats():
    result = dict()
    for dbname in databases:
        db = databases[dbname]
        for coll in db.collection_names():
            result[coll] = db.command({'collstats' : coll})
            result[coll]['dbname'] = dbname
    return result

def dropDB():
    """ Drops all database data """

    from database import databases
    for k, db in databases.items():
        for name in db.collection_names():
            if name not in ('system.indexes',):
                db[name].drop_indexes()
                db.drop_collection(name)

