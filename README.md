# Advanced Web Server
This is a self-made webserver which supports HTTP2. In order to learn how HTTP really works,
a robust and easily modifiable webserver is needed. AdvancedWebServer is here to allow you
to import server extensions on startup.

A simple command line tool is provided for this webserver.

## Command line tool
```
$ java -jar advwebserver_v15.5.0.jar --help
java -jar webserver.jar <download / ssl> [options]
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
    --debug                   Enable debugging (Verbosity value: 1, 2, 3)
```

### WebServer Extensions
To use extensions for the server, I'll demonstrate it using a simple example for listing
out the current directory.

```java
@HttpHandler
public class DirectoryListingExtension{
    @AutoInjected
    public WebServer server;
    
    @Mutatable
    @InvocationCondition({DirectoryOnly.class})
    @RequestMapping("/**")
    public Object catchAllDirectoryHandler(HttpRequest req, ResponseBuilder res){
        WebServer.Configuration config = server.getConfiguration();
        res.getHeaders().add("content-type", "text/html");
        return DirectoryListing.getDirectoryListingHtml(req, FileControl.get(req, config));
    }

    @Mutatable
    @InvocationCondition({NoDirectory.class})
    @RequestMapping("/**")
    public Object catchAllHandler(HttpRequest req, ResponseBuilder res){
        WebServer.Configuration config = server.getConfiguration();
        return FileControl.get(req, config);
    }
}
```

Use `@HttpHandler` to allow the server to use the class. `@AutoInjected` is used to create 
singletons (to use custom constructors, you need to extend a class with `BeanDefinitions`
and use `@Bean` to a annotate method which create the object you want). For example:

```java
class SampleDefinitions extends BeanDefinitions{
    // Interface is not supported.
    // 
    // Beans are not named, hence if String is used as return 
    // type many problems will occur.
    @Bean
    public ExampleImpl create(){
        return new ExampleImpl();
    }
}
@HttpHandler
class ExampleHandler{
    @AutoInject
    public ExampleImpl example;
}
```

`@Mutatable` shows that the http handler method will mutate the `ResponseBuilder` object.
`@RequestMapping` is an annotation which creates a mapping for your handler. This allows 
the server to properly dispatch user requests to your handler.