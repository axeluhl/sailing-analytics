from setuptools import setup, find_packages
import os

version = '1.0'

setup(name='sailing.connector',
      version=version,
      description="Connector to TracTrac Application wrapped by Java",
      long_description=open("README.txt").read() + "\n" +
                       open(os.path.join("docs", "HISTORY.txt")).read(),
      # Get more strings from
      # http://pypi.python.org/pypi?:action=list_classifiers
      classifiers=[
        "Programming Language :: Python",
        ],
      keywords='',
      author='',
      author_email='',
      url='http://svn.plone.org/svn/collective/',
      license='GPL',
      packages=find_packages(exclude=['ez_setup']),
      namespace_packages=['sailing'],
      include_package_data=True,
      zip_safe=False,
      install_requires=[
          'setuptools',
          'pymongo',
          'mongokit',
          'python-cjson'
      ],
      entry_points="""
      # -*- Entry points: -*-
      """,
      )
