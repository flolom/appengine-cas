appengine-cas
=============

A Central Authentication Service (CAS) implementation for Google App Engine

This implementation is focused on the use of the CAS with a third-party service,
who whants to authenticate its user with a CAS server

A demo can be found on [this website](http://cas-engine.appspot.com)

Be careful
----------

The appengine implementation on a development environment (your computer) seems
to retain some cookies and prevent this implementation to work properly.

But on a (pre)production environment (while deploying to appengine), this issue
doesn't occur.

See also
--------

1. http://www.jasig.org/cas
2. http://cas-engine.appspot.com
