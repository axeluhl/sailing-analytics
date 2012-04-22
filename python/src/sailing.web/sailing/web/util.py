
import os
import pkg_resources

from pyramid.path import caller_package
from pyramid.asset import resolve_asset_spec

import logging
log = logging.getLogger(__name__)

def reloader_watch_files(global_config, **settings):
    """ Adds some files to the reloader to ensure that templates get reloaded
    as the chameleon reloader does not work correctly. """

    if settings.get('force_reload_templates') and \
        settings.get('force_reload_templates') == 'true':

            import os
            from paste import reloader
            import fnmatch

            paths = settings.get('reload_templates_path').split()

            for path in paths:
                folder, glob = path.split(',')
                for f in os.listdir(folder):
                    if fnmatch.fnmatch(f, glob):
                        fullname = os.path.join(folder, f)
                        log.debug('Registering %s at reloader' % fullname)
                        reloader.watch_file(fullname)

def decorator(decorator):
    """This decorator can be used to turn simple functions
    into well-behaved decorators, so long as the decorators
    are fairly simple. If a decorator expects a function and
    returns a function (no descriptors), and if it doesn't
    modify function attributes or docstring, then it is
    eligible to use this. Simply apply @simple_decorator to
    your decorator and it will automatically preserve the
    docstring and function attributes of functions to which
    it is applied."""

    def new_decorator(f):
        g = decorator(f)
        g.__name__ = f.__name__
        g.__doc__ = f.__doc__
        g.__dict__.update(f.__dict__)
        return g
    # Now a few lines needed to make simple_decorator itself
    # be a well-behaved decorator.
    new_decorator.__name__ = decorator.__name__
    new_decorator.__doc__ = decorator.__doc__
    new_decorator.__dict__.update(decorator.__dict__)
    return new_decorator

@decorator
def jsonize(func):
    def reply(*args, **kw):
        data = func(*args, **kw)

        from webob import Response
        import cjson

        return Response(content_type='application/json', body=cjson.encode(data))

    return reply

def get_abspath(path):
    if not os.path.isabs(path):
        spec = resolve_asset_spec(path, caller_package(level=3).__name__)

        pkg, path = spec
        if not pkg_resources.resource_exists(pkg, path):
            raise ValueError('Missing template resource: %s (%s)' % (pkg, path))
        abspath = pkg_resources.resource_filename(pkg, path)

    else:
        if not os.path.exists(path):
            raise ValueError('Missing template file: %s' % path)
        abspath = path

    return abspath

