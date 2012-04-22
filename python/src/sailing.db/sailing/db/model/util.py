from sailing.db.database import getConnection, getCollection, getConnectionByCollectionName

classes = dict()

def registerClass(cls):
    """ Register class with MongoKit """
    
    collection = cls._collection
    conn = getConnectionByCollectionName(collection)
    conn.register([cls])
    classes[cls.__name__] = cls

def createContent(cls_name, **kw):

    try:
        collection = getCollection(classes[cls_name]._collection)
    except KeyError:
        raise ValueError('Class "%s" is not registered' % cls_name)

    inst = getattr(collection, cls_name)()
    for k,v in kw.items():
        inst[k] = v
    return inst

def queryContent(cls_name):
    try:
        collection = getCollection(classes[cls_name]._collection)
    except KeyError:
        raise ValueError('Class "%s" is not registered' % cls_name)

    return getattr(collection, cls_name)

