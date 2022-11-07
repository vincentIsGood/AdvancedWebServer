package com.vincentcodes.webserver.helper.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.vincentcodes.webserver.exception.BadConversionException;
import com.vincentcodes.webserver.helper.Registry;

public class JarRegister extends Registry<File>{
    
    @Override
    public boolean add(File obj) {
        if(!obj.exists() || obj.isDirectory()){
            throw new IllegalArgumentException("Invalid file: " + obj.getAbsolutePath());
        }
        return super.add(obj);
    }

    public File[] getFiles(){
        return toArray(new File[0]);
    }

    public URL[] toURLs(){
        return register.stream().map(this::convertFileToUrl).toArray(URL[]::new);
    }

    private URL convertFileToUrl(File file){
        try{
            return file.toURI().toURL();
        }catch(MalformedURLException e){
            throw new BadConversionException(e);
        }
    }
}
