package com.vincentcodes.webserver.helper.loader;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class JarLoader {
    private JarRegister jars;
    private URLClassLoader classLoader;
    private ArrayList<Class<?>> loadedClasses;

    public JarLoader(JarRegister jars) {
        this.jars = jars;
    }
    
    /**
     * Entries which triggered errors will be skipped
     * and the error will be printed out.
     */
    public List<Class<?>> loadJars() throws IOException {
        classLoader = URLClassLoader.newInstance(jars.toURLs(), this.getClass().getClassLoader());
        loadedClasses = new ArrayList<>();

        File[] files = jars.getFiles();
        for(File file : files){
            try{
                ZipFile jar = new ZipFile(file);
                for(Enumeration<? extends ZipEntry> entries = jar.entries(); entries.hasMoreElements();){
                    loadEntry(entries.nextElement());
                }
                jar.close();
            }catch(ZipException e){
                e.printStackTrace();
            }
        }
        return loadedClasses;
    }

    /**
     * If an entry is a .class file and it is not a nested class, 
     * the class gets loaded.
     * @param entry
     */
    private void loadEntry(ZipEntry entry){
        try{
            String entryName = entry.getName();
            if(entryName.endsWith(".class") && !entryName.contains("$")){
                String classPath = entry.getName().replaceAll("/", ".").replace(".class", "");
                loadedClasses.add(Class.forName(classPath, true, classLoader));
            }
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
