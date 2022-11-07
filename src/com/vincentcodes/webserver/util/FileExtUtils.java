package com.vincentcodes.webserver.util;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class FileExtUtils {
    public static String determineMimeType(File file){
        String fileExt = FileExtUtils.extractFileExtension(file.getName());
        return FileExtUtils.getMimeTypeOfCommonTextExt(fileExt);
    }

    public static String extractFileExtension(String filename){
        int index = filename.lastIndexOf('.')+1;
        if(index == -1)
            return null;
        return filename.substring(index);
    }

    public static boolean isCommonTextFile(String extension){
        switch(extension){
            case "txt": 
            case "html":
            case "css": 
            case "csv": 
            case "ts": 
            case "js": 
            case "mjs": 
            case "xml": 
            case "json": return true;
        }
        return false;
    }
    
    public static boolean fileIsImage(String extension){
        switch(extension){
            case "png": return true;
            case "gif": return true;
            case "jpg": return true;
            case "jpeg": return true;
            case "tiff": return true;
        }
        return false;
    }

    public static boolean fileIsAudio(String extension){
        switch(extension){
            case "mp3": return true;
            case "m4a": return true;
            case "wav": return true;
            case "aac": return true;
            case "m4p": return true; //apple
            case "oga": return true;
            case "weba": return true;
        }
        return false;
    }

    public static boolean fileIsVideo(String extension){
        switch(extension){
            case "avi": return true;
            case "mp4": return true;
            case "ogv": return true;
            case "webm": return true;
        }
        return false;
    }

    /**
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
     * Deals with the most common types of files. 
     * @param extension file extension
     * @return defaults to "text/plain"
     */
    public static String getMimeTypeOfCommonTextExt(String extension){
        // Supported media files
        if(fileIsImage(extension)){
            switch(extension){
                case "jpg": return "image/jpeg";
                default: return "image/" + extension;
            }
        }else if(fileIsAudio(extension)){
            switch(extension){
                case "mp3": return "audio/mpeg";
                case "oga": return "audio/ogg";
                case "weba": return "audio/webm";
                default: return "audio/" + extension;
            }
        }else if(fileIsVideo(extension)){
            switch(extension){
                case "ogv": return "video/ogg";
            }
            return "video/" + extension;
        }

        // others
        switch(extension){
            case "txt": return "text/plain";
            case "html": return "text/html";
            case "css": return "text/css";
            case "csv": return "text/csv";
            case "ts": return "text/x.typescript";
            case "js": return "application/javascript";
            case "mjs": return "application/javascript";
            case "xml": return "application/xml";
            case "json": return "application/json";

            case "ico": return "image/vnd.microsoft.icon";
            case "svg": return "image/svg+xml";
            case "swf": return "application/x-shockwave-flash";
            case "pdf": return "application/pdf";

            case "tar": return "application/x-tar";
            case "gz": return "application/gzip";
            case "bz": return "application/x-bzip";
            case "bz2": return "application/x-bzip2";
            case "zip": return "application/zip";
            case "jar": return "application/java-archive";
            case "7z": return "application/x-7z-compressed";
            case "rar": return "application/vnd.rar";
        }
        // although it is possible to check for the first few 
        // bytes to see whether it is a binary file or not
        return "text/plain";
    }

    // This seems kinda slow
    public static boolean isUtf8(byte[] bytes){
        try{
            StandardCharsets.UTF_8.newDecoder()
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .onMalformedInput(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
        }catch(CharacterCodingException e){
            return false;
        }
        return true;
    }
}
