# Advanced Web Server
Light-weight Java Web Server

This is a self-made webserver which supports HTTP2 and WebSocket. In order to learn how HTTP 
really works, a robust and easily modifiable webserver is needed. AdvancedWebServer is here 
to allow you to import server extensions on startup.

A simple command line tool is provided for this webserver.

## Command line tool
```sh
$ java -jar advwebserver_vX.Y.Z.jar --help
java -jar webserver.jar [<download / ssl>] [options]
    --help                    get this help list
    -h                        get this help list
    -d <directory>            base url
    -f <.jks file>            jks file for https
    -p <port>                 defaults to 5050 port
    --password <jks password> password is required
    --log                     Enable logging to logs/
    --no-defaults             do not use default handlers. This is often used with '--extension' flag
    --extension               <path/to/1.jar>[,<path/to/2.jar>,...]
    --http2                   Beta. Force the use of protocol http2. Must be used with TLS.
    --multi                   Enable multi-threading
    --debug                   Enable debugging (Verbosity value: Not supported yet)
# [] = optional arguments 
```

### Examples
Serve local files
```sh
java -Dfile.encoding=utf-8 -jar advwebserver_vX.Y.Z.jar
```

Serve local files with http2 protocol, logging to a file and ssl/tls enabled. `multi` is often required to serve multiple clients (eg. browsers).
```sh
java -Dfile.encoding=UTF-8 -jar advwebserver_vX.Y.Z.jar ssl --multi --http2 -f key.jks --password password_for_keyjks --log --multi
```

Customize server behavior with `your_ext.jar`
```sh
java -Dfile.encoding=utf-8 -jar advwebserver_vX.Y.Z.jar --no-defaults --extension your_ext.jar
```

## WebServer Extensions
See `examples/` for basic and detailed examples.