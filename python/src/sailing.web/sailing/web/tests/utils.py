from pyramid import testing

class DummySession(dict):

    def save(self):
        pass

class DummyRequest(testing.DummyRequest):
    """ A MockRequest used for tests  """

    def get(self, name, default=None):
        return self.__dict__.get(name, default)

    def keys(self):
        return self.__dict__.keys()

    def has_key(self, name):
        return self.__dict__.has_key(name)

    __getitem__ = get

    def __setitem__(self, name, value):
        self.__dict__[name] = value

    set = __setitem__

    def __delitem__(self, name):
        del self.__dict__[name]

    def __nonzero__(self):
        return True

    def __len__(self):
        return len(self.keys())

    def __contains__(self, name):
        return name in self.keys()

    __iter__ = keys

dummy_request = DummyRequest()
def get_request(is_get=True):
    dummy_request['REQUEST_METHOD'] = is_get and 'GET' or 'POST'
    dummy_request['X-TESTS-RUNNING'] = True

    if not dummy_request.has_key('beaker.session'):
        dummy_request['beaker.session'] = DummySession()

    return dummy_request

