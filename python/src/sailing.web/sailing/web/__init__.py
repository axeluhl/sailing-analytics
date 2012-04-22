
from pyramid.config import Configurator

from pyramid_beaker import BeakerSessionFactoryConfig,\
    session_factory_from_settings, set_cache_regions_from_settings

def init_app(request):
    return {}

def app(global_config, **settings):
    """ This function returns a pyramid.router.Router object.
    It is usually called by the PasteDeploy framework during ``paster serve``.
    """

    # paster app config callback
    import sailing.web

    # beaker stuff for session and caching
    session_factory = session_factory_from_settings(settings)
    set_cache_regions_from_settings(settings)

    from sailing.web.util import reloader_watch_files
    reloader_watch_files(global_config, **settings)

    config = Configurator(settings=settings, root_factory=init_app, session_factory=session_factory)
    config.include('pyramid_zcml')
    config.load_zcml('configure.zcml')

    return config.make_wsgi_app()
