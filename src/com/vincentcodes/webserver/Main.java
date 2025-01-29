package com.vincentcodes.webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.vincentcodes.util.commandline.Command;
import com.vincentcodes.util.commandline.CommandLineParser;
import com.vincentcodes.util.commandline.ParserConfig;
import com.vincentcodes.webserver.defaults.DownloadOnlyHandler;

// TODO: add logging to low level shit http2
public class Main{
    private static ParserConfig config;

    public static void setCommandLineConfig(){
        config = new ParserConfig();
        config.addOption("--help", true, "get this help list");
        config.addOption("-h", true, "get this help list");
        
        config.addOption("-d", false, "<directory>", "base url");
        config.addOption("-f", false, "<.jks file>", "jks file for https");
        config.addOption("-p", false, "<port>", "defaults to 5050 port");
        config.addOption("--bind", false, "<host>", "bind to a host / interface. defaults to 0.0.0.0");
        config.addOption("--password", false, "<jks password>", "password is required");

        config.addOption("--log", true, "Enable logging to logs/");
        config.addOption("--no-defaults", true, "do not use default handlers. This is often used with '--extension' flag");
        config.addOption("--extension", false, "<path/to/1.jar>[,<path/to/2.jar>,...]");

        config.addOption("--http2", true, "Beta. Force the use of protocol http2. Must be used with TLS.");
        config.addOption("--multi", true, "Enable multi-threading");
        config.addOption("--debug", true, "Enable debugging");
        config.addOption("--name", false, "<name>", "Name used in server header");
    }

    public static void main(String[] args) throws IOException{
        setCommandLineConfig();
        CommandLineParser parser = new CommandLineParser(config);
        Command cmd = parser.parse(args);

        if(cmd.hasOption("--help") || cmd.hasOption("-h")){
            printHelp();
            return;
        }

        WebServer.Builder serverBuilder = null;
        if(args.length > 0 && cmd.getParameters().size() > 0){
            if(cmd.getParameter(0).equals("download")){
                HttpHandlerRegister.register(new DownloadOnlyHandler());
                serverBuilder = new WebServer.Builder().setUseDefaultHandlers(false);
            }else if(cmd.getParameter(0).equals("ssl")){
                serverBuilder = new WebServer.Builder().setUseSSL(true);
            }
        }else{
            serverBuilder = new WebServer.Builder();
        }

        if(cmd.hasOption("-d")){
            String path = cmd.getOptionValue("-d");
            throwErrorIfFileNotFound(new File(path));
            serverBuilder.setHomeDirectory(path);
        }
        if(cmd.hasOption("-f")){
            if(cmd.getParameters().size() > 0 && cmd.getParameter(0).equals("ssl")){
                if(!cmd.hasOption("--password")){
                    throw new IllegalArgumentException("'--password' option is needed for the keystore.");
                }
                File keystore = new File(cmd.getOptionValue("-f"));
                throwErrorIfFileNotFound(keystore);
                serverBuilder.setKeyStoreFile(keystore);
            }else{
                throw new IllegalArgumentException("Please use 'ssl' as described in '-h' / '--help'");
            }
        }
        if(cmd.hasOption("--password")){
            serverBuilder.setKeyStorePassword(cmd.getOptionValue("--password"));
        }
        if(cmd.hasOption("-p")){
            serverBuilder.setPort(Integer.parseInt(cmd.getOptionValue("-p")));
        }
        if(cmd.hasOption("--bind")){
            serverBuilder.setBindHost(cmd.getOptionValue("--bind"));
        }
        if(cmd.hasOption("--no-defaults")){
            if(cmd.getParameters().size() > 0 && cmd.getParameter(0).equals("download")){
                throw new IllegalArgumentException("'download' parameter do not support '--no-defaults'");
            }
            serverBuilder.setUseDefaultHandlers(false);
        }
        if(cmd.hasOption("--extension")){
            for(String path : cmd.getOptionValue("--extension").split(",")){
                File extension = new File(path);
                throwErrorIfFileNotFound(extension);
                serverBuilder.addExternalHandler(extension);
            }
        }
        if(cmd.hasOption("--log")){
            // temporary solution: Create logs/ ourselves
            File logsFolder = new File("logs");
            logsFolder.mkdir();
            WebServer.logger.setLogToFile(true);
        }

        if(cmd.hasOption("--http2")){
            if(cmd.getParameters().size() > 0 && cmd.getParameter(0).equals("ssl")){
                if(!cmd.hasOption("-f")){
                    throw new IllegalArgumentException("'-f' option is needed.");
                }
                if(!cmd.hasOption("--password")){
                    throw new IllegalArgumentException("'--password' option is needed.");
                }
                serverBuilder.setForceHttp2(true);
            }else{
                throw new IllegalArgumentException("http2 must be used with 'ssl'");
            }
        }
        
        if(cmd.hasOption("--multi")){
            serverBuilder.setUseMultithread(true);
        }

        if(cmd.hasOption("--debug")){
            // int verbosity = Integer.parseInt(cmd.getOptionValue("--debug"));
            WebServer.logger.warn("Debugging verbosity has not been implemented yet. Continue with 'all' debug messages");
            WebServer.lowLevelDebugMode = true;
        }

        if(cmd.hasOption("--name")){
            String name = cmd.getOptionValue("--name");
            if(name.trim().isEmpty())
                throw new IllegalArgumentException("Value of option '--name' is invalid");
            WebServer.SERVER_NAME = name;
        }

        if(serverBuilder != null){
            WebServer server = serverBuilder.build();
            server.start();
            server.close();
        }else{
            printHelp();
        }
    }

    public static void printHelp(){
        System.out.println("java -jar webserver.jar [<download / ssl>] [options]");
        System.out.println(config.getOptionsHelpString());
    }

    public static void throwErrorIfFileNotFound(File file) throws FileNotFoundException{
        if(!file.exists()){
            throw new FileNotFoundException("Cannot find file or directory: " + file.getName());
        }
    }
}