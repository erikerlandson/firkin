# firkin

storage, aging, and access for small structured data

### usage

Fire up a Firkin server with 

    com.freevariable.firkin.Firkin.start()(colossus.IOSystem())

or `sbt server/console`.

You'll then have a server running on port 4091.  You can store and access data via the following HTTP routes:

* `POST /cache` with a payload of JSON data:  this will store the object you provided and redirect you to its location in the cache
* `GET /cache`:  this will return a list of all the hashes currently in the cache
* `GET /cache/$HASH`:  this will return the object in the cache with hash `$HASH` or 404

Submitting empty POST data or submitting invalid JSON as POST data will result in various 4xx errors.

### answers to frequently anticipated questions

1.  A firkin is 4.091 L but we can't have fractional ports.
2.  In order:  it's useful to have a sink for relatively small structured data that you generate with one thing and want to consume with another.  Also: NIH.

