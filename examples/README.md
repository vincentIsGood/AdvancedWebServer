# Server Extensions
This directory, you will find a few examples regarding creating server extensions. 

## Basics
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

### Explanation
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
1. create a jar
2. execute the webserver command to add that extension to the server

Package the `.class` files into a jar to create `your_ext.jar`. Then run the webserver with the
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

### Notes
Sometimes adding new libraries to the extension is **not enough**, you may need to pack those 
libraries `.class` files right **into** the `advwebserver_vX.Y.Z.jar` instead of `your_ext.jar`.