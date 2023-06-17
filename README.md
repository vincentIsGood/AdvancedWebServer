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
To add extensions to the server, I'll demonstrate it using a simple example for listing
out the current directory.

```java
import com.vincentcodes.webserver.dispatcher.reflect.DirectoryOnly;
...

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
and use `@Bean` to annotate a method which creates the object you want). For example:

```java
class SampleDefinitions extends BeanDefinitions{
    // Interface is not supported. Duplication not allowed.
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

## Developing an Extension
### Setup
In order to do so, you will need 2 jars (minimal):
1. AdvancedWebServer with java byte code
2. Source of AdvancedWebServer
3. [optional] Add `logger-vX.Y.jar` to your project

Then, for **VSCode**, you need a project structure like this:
```
Project
|- lib
   |- advwebserver_vX.Y.Z.jar
   |- advwebserver_vX.Y.Z-sources.jar
|- src
   |- com/vincentcodes/extension/
      |- DirectoryListingExtension.java
```

Open vscode in this project directory, and vscode (w/ Java Extensions installed) will be
able to detect the Java project by default. You may also need to right click on `src/` and 
`Add Folder to Java Source Path`, if it doesn't work.

For **Intellij**, the process should be similar.

### Running it
To run it, you are required to 
1. create an executable jar
2. execute the webserver command to add that extension to the server

Package the `.class` files into an executable jar. Then run the webserver with the
following command:
```sh
java -Dfile.encoding=utf-8 -jar advwebserver_vX.Y.Z.jar --no-defaults --extension your_ext.jar
```

To automate this process, I used Gradle with the following configuration:
```gradle
task packDependencies(type: Jar) {
    baseName = "dependencies"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}

// Create tasks: https://www.baeldung.com/gradle-run-java-main
task runWebServer(type: Exec){
    dependsOn jar
    group = "Execution"
    description = "Run the output executable jar with ExecTask"
    commandLine "java", "-Dfile.encoding=utf-8",
            "-jar", "advwebserver_vX.Y.Z_mod.jar",
            "--no-defaults",
            "--extension", jar.archiveFile.get()
}
```

## Examples
Not yet available. Working on it.