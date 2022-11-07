package com.vincentcodes.webserver.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;

/**
 * This is a util class which is used for everyone who wants
 * to serve files.
 */
public class FileControl {
    /**
     * Will not check whether the file exists or not. Since this 
     * is not the job of this method.
     * 
     * @return null if the file is invalid (eg. /asd.mp4/ is invalid 
     * even though file system said it's fine)
     */
    public static File get(HttpRequest req, WebServer.Configuration config){
        return get(req, config.getHomeDirectory());
    }
    public static File get(HttpRequest req, String directoryPath){
        String completePath = directoryPath + req.getBasicInfo().getPath().get();
        File result = new File(completePath);
        if(req.getBasicInfo().getPath().get().endsWith("/")){
            File indexFile = new File(completePath + "index.html");
            if(indexFile.isFile())
                return indexFile;
            if(result.isFile())
                return null;
        }
        return result;
    }

    /**
     * Will not alter response. However, save only works on PUT where 
     * file data is put directly into the body to the request. 
     */
    public static boolean save(HttpRequest req, WebServer.Configuration config){
        return save(req, config.getHomeDirectory());
    }
    public static boolean save(HttpRequest req, String directoryPath){
        if(req.getBasicInfo().getPath().get().endsWith("/"))
            return false;
        
        try{
            File file = new File(directoryPath + req.getBasicInfo().getPath().get());
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            try(FileOutputStream outputStream = new FileOutputStream(file)){
                outputStream.write(req.getBody().getBytes());
            }
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
