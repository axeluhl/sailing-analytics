from setuptools import setup, find_packages
import os

version = '1.0'

setup(name='sailing.web',
      version=version,
      description="Web applications in context of sailing",
      long_description=open("README.txt").read() + "\n" +
                       open(os.path.join("docs", "HISTORY.txt")).read(),
      classifiers=[
        "Programming Language :: Python",
        ],
      keywords='',
      author='',
      author_email='',
      url='http://www.sap.com',
      license='CLOSED',
      packages=find_packages(exclude=['ez_setup']),
      namespace_packages=['sailing'],
      include_package_data=True,
      zip_safe=False,
      install_requires=[
          'setuptools',
          'WebError',
          'pyramid',
          'pyramid_zcml',
          'pyramid_beaker',
          'repoze.browserid',
          'pymongo',
          'mongokit'
      ],
      entry_points="""
        [paste.app_factory]
        main = sailing.web:app
      """,
      )
