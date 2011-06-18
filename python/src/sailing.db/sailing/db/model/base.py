
import datetime

import mongokit
from pymongo.objectid import ObjectId

from sailing.db.model.util import queryContent, createContent
from sailing.db import config

MARKER = object

class BaseDocument(mongokit.Document):
    skip_validation = True
    safe_mode = True

    def _updateInternal(self, **kw):
        kw['safe'] = self.safe_mode
        if not hasattr(self, 'created') or \
                not kw.get('created', None):
            self['created'] = datetime.datetime.now()

        if not hasattr(self, 'typename') or \
                not kw.get('typename', None):
            self['typename'] = self.__class__.__name__

        return kw

    def save(self, *args, **kw):
        kw = self._updateInternal(**kw)
        super(BaseDocument, self).save(*args, **kw)

    def insert(self, *args, **kw):
        kw = self._updateInternal(**kw)
        super(BaseDocument, self).insert(*args, **kw)

    def update(self, *args, **kw):
        kw = self._updateInternal(**kw)
        super(BaseDocument, self).update(*args, **kw)

class ModelBase(object):

    @classmethod
    def collectionName(cls):
        return cls.linked_model._collection

    @classmethod
    def query(cls, uid, wrap=True):
        uid = str(uid).strip()
        klass = queryContent(cls.linked_model.__name__)
        backend_instance = klass.get_from_id(ObjectId(uid))

        if wrap is True:
            if backend_instance:
                if isinstance(backend_instance, list):
                    backend_instance = backend_instance[0]
                return cls(backend_instance)
        else:
            return backend_instance

        return None

    @classmethod
    def queryRaw(cls, input, limit=0, sortby='created', sortorder=config.DESCENDING):
        klass = queryContent(cls.linked_model.__name__)
        backend_instance = klass.find(input).limit(limit).sort(sortby, sortorder)

        if backend_instance:
            if backend_instance.count():
                return [cls(ba) for ba in backend_instance]

        return []

    @classmethod
    def queryBy(cls, limit=0, sortby='created', sortorder=config.DESCENDING, **fields):
        assert isinstance(limit, int)

        klass = queryContent(cls.linked_model.__name__)

        # update fields with typename
        if not fields.get('typename', None):
            fields['typename'] = cls.linked_model.__name__

        # better would be to use fetch here because this includes structure but
        # because we have one type per collection this doesn't matter
        backend_instance = klass.find(fields).limit(limit).sort(sortby, direction=sortorder)

        if backend_instance:
            if backend_instance.count():
                return [cls(ba) for ba in backend_instance]

        return []

    @classmethod
    def queryByCount(cls, limit=0, sortby='created', sortorder=config.DESCENDING, **fields):
        assert isinstance(limit, int)

        # update fields with typename
        if not fields.get('typename', None):
            fields['typename'] = cls.linked_model.__name__

        klass = queryContent(cls.linked_model.__name__)
        backend_instance = klass.find(fields).limit(limit).sort(sortby, direction=sortorder)
        return backend_instance.count()

    @classmethod
    def queryCount(cls, limit=0, sortby='created', sortorder=config.DESCENDING, **fields):
        # update fields with typename
        if not fields.get('typename', None):
            fields['typename'] = cls.linked_model.__name__

        klass = queryContent(cls.linked_model.__name__)
        backend_instance = klass.find(fields).limit(limit).sort(sortby, direction=sortorder)
        return backend_instance.count()

    @classmethod
    def queryRawCount(cls, input, limit=0, sortby='created', sortorder=config.DESCENDING):
        klass = queryContent(cls.linked_model.__name__)
        backend_instance = klass.find(input).limit(limit).sort(sortby, direction=sortorder)
        return backend_instance.count()

    @classmethod
    def queryOneBy(cls, **fields):
        # update fields with typename
        if not fields.get('typename', None):
            fields['typename'] = cls.linked_model.__name__

        klass = queryContent(cls.linked_model.__name__)
        backend_instance = klass.find_one(fields)

        if backend_instance:
            return cls(backend_instance)
        return None

    @classmethod
    def removeAllBy(cls, **fields):
        if not fields.get('typename', None):
            fields['typename'] = cls.linked_model.__name__

        klass = queryContent(cls.linked_model.__name__)
        klass.collection.remove(spec_or_id=fields, atomic=True)

        # assuming that we clean all objects
        klass.collection.drop_indexes()

    def __getstate__(self):
        # we need to return a state where there is no connection
        # to mongodb directly established - we just save the internal
        # UID so to be able to rebuild the state later on
        
        dc = self.__dict__.copy()
        inst = dc['_instance']

        if getattr(inst, '__dict__', None):
            # we preserve the instance to avoid reloading it
            # on unpickle - just preserving raw values
            idc = {}; footprint = inst.structure

            for k in footprint.keys():
                # model changes can yield non-complete instances
                if inst.has_key(k):
                    idc[str(k)] = inst[k]

            idc['_id'] = dc['_instance']['_id']

            del dc['_instance']
            dc['_instance'] = idc

        return dc

    def __setstate__(self, state):
        self.__dict__ = state

    def __init__(self, instance = None, *args, **kw):
        self._instance = instance

    def __getattr__(self, key):
        # instance attributes are held by backend
        value = self.get(key, default=object)

        # make sure to fall back to this class
        if value is object:
            return getattr(self.__class__, key)

        return value

    def get(self, key, default=None):
        if key in self.__class__.linked_model.structure or key in ['_id', ]:
            inst = self.__dict__['_instance']
            if inst.has_key(key):
                return inst.get(key)
        return default

    @property
    def uid(self):
        uid = getattr(self, '_id', None)
        if uid:
            return unicode(str(uid))
        return None

    def _backend(self):
        return self.__class__.linked_model

    def _instance_(self):
        # use this method only for direct operations (crud)
        inst = self.__dict__['_instance']
        if inst:
            # unpickling returns dict and not real mongokit instance
            if not hasattr(inst, '__dict__'):
                self.__dict__['_instance'] = self.query(inst['_id'], wrap=False)
                return self.__dict__['_instance']
        return inst

    def getURL(self):
        raise Exception, 'NotImplementedHere'

    def getTitle(self):
        raise Exception, 'NotImplementedHere'

    def creationTime(self, pretty=False):
        if pretty is True:
            return pretty_date(self.created)
        return self.created

    def reload(self):
        self._instance = self.query(self.uid, wrap=False)

    def delete(self):
        uid = self.uid
        if not uid:
            return False

        self._instance_().delete()
        return True

    def update(self, dictionary=None, dosave=True):
        klassname = self._backend().__name__

        # make sure to make a difference between new and update
        # new objects should always have empty uid field
        if self.uid is None:
            self._instance = createContent(klassname)
            iis = self._instance_()

            if hasattr(self, '_before_update'):
                self._before_update(dictionary, creation=True)

            # check that defaults are NOT set on backend
            if hasattr(iis.__class__, 'default_values'):
                if len(iis.__class__.default_values.keys())>0:
                    raise Exception, 'Implementation error: instance can not have defaults! Set defaults in model! Model: %s' % self._instance.__class__

            # make sure to activate defaults
            if hasattr(self.__class__, 'default_values'):
                for dk,dv in self.__class__.default_values.items():
                    if dictionary is None \
                            or not dk in dictionary:
                        if callable(dv):
                            dv = dv()

                        iis[dk] = dv
        else:
            if self._instance_() is None:
                self._instance = self.query(self.uid, wrap=False)

            iis = self._instance_()
            if hasattr(self, '_before_update'):
                self._before_update(dictionary, creation=False)

        if dictionary:
            for k,v in dictionary.items():
                iis[k] = v # need explicit set here

        # save also works for update process
        if dosave is True:
            iis.save()

