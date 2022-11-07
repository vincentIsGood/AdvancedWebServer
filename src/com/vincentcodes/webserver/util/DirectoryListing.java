package com.vincentcodes.webserver.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vincentcodes.webserver.component.request.HttpRequest;

/**
 * Sub-project
 */
public class DirectoryListing {
    private static final String STYLE = "*{    box-sizing: border-box;    margin: 0;    padding: 0;}:root{    /* font-size: 12px; */    font-size: clamp(0.5rem, 2vw, 1rem);}body{    background-color: #212121;    font-family: Arial, Helvetica, sans-serif;    color: white;}a{    text-decoration: none;    color: unset;}.title{    font-size: 2rem;    margin-bottom: 1rem;}.main-content{    --local-margin: 4rem;    width: calc(100% - var(--local-margin) * 2);    margin: var(--local-margin) var(--local-margin);}.files-table{    border: solid #303030;    min-height: 5rem;    font-size: 1rem;    border-radius: 0.5rem;}.file-entry{    display: flex;    flex-direction: row;    align-items: center;    border-bottom: solid #303030;    padding: 0.5rem 0.5rem;}.file-entry.desc{    background-color: #303030;}.file-entry.desc:hover{    background-color: #303030;}.file-entry:hover{    background-color: #262626;}.file-entry .icon{    width: 1.5rem;    height: 1.5rem;    fill: #707070;    margin-right: 0.8rem;}.file-entry .name{    width: 60%;}.file-entry .date{    width: 22%;}.file-entry .size{    width: 5%;}";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

    public static String getDirectoryListingHtml(HttpRequest req, File folder){
        return getDirectoryListingHtml(req.getBasicInfo().getPath().get(), folder);
    }

    public static String getDirectoryListingHtml(String requestPath, File folder){
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("<head><meta http-equiv='Content-Type' content='text/html;charset=UTF-8'></head>");
        builder.append("<style>").append(STYLE).append("</style>");
        builder.append("<body><div class='main-content'>");
        builder.append("<div class='title'>Showing entries for: "+ requestPath +"</div>");
        builder.append("<div class='files-table'>");
        builder.append("<div class='file-entry desc'>")
            .append("<svg class='icon' viewBox='0 0 16 16'></svg>")
            .append("<div class='name'>Name</div>")
            .append("<div class='date'>Date</div>")
            .append("<div class='size'>Size</div>")
            .append("</div>");
        
        if(!requestPath.endsWith("/")){
            requestPath = requestPath + "/";
        }
        for(File file : sortByTypeThenName(folder.listFiles())){
            if(file.isDirectory()){
                createDirectoryEntryHtml(builder, requestPath, file);
            }else{
                createFileEntryHtml(builder, requestPath, file);
            }
        }

        builder.append("</div></div></body></html>");
        return builder.toString();
    }

    private static void createFileEntryHtml(StringBuilder builder, String requestPath, File file){
        Date fileLastModified = Date.from(Instant.ofEpochMilli(file.lastModified()));
        builder.append("<div class='file-entry'>")
            .append("<svg class='icon' viewBox='0 0 16 16'><path fill-rule='evenodd' d='M3.75 1.5a.25.25 0 00-.25.25v11.5c0 .138.112.25.25.25h8.5a.25.25 0 00.25-.25V6H9.75A1.75 1.75 0 018 4.25V1.5H3.75zm5.75.56v2.19c0 .138.112.25.25.25h2.19L9.5 2.06zM2 1.75C2 .784 2.784 0 3.75 0h5.086c.464 0 .909.184 1.237.513l3.414 3.414c.329.328.513.773.513 1.237v8.086A1.75 1.75 0 0112.25 15h-8.5A1.75 1.75 0 012 13.25V1.75z'></path></svg>")
            .append("<div class='name'><a href='"+ (requestPath+file.getName()) +"'>"+ file.getName() +"</a></div>")
            .append("<div class='date'>").append(DATE_FORMAT.format(fileLastModified)).append("</div>")
            .append("<div class='size'>").append(Math.ceil(file.length()*1.0/1000)).append("KB</div>")
            .append("</div>");
    }
    private static void createDirectoryEntryHtml(StringBuilder builder, String requestPath, File file){
        Date fileLastModified = Date.from(Instant.ofEpochMilli(file.lastModified()));
        builder.append("<div class='file-entry'>")
            .append("<svg class='icon' viewBox='0 0 16 16'><path fill-rule='evenodd' d='M1.75 1A1.75 1.75 0 000 2.75v10.5C0 14.216.784 15 1.75 15h12.5A1.75 1.75 0 0016 13.25v-8.5A1.75 1.75 0 0014.25 3h-6.5a.25.25 0 01-.2-.1l-.9-1.2c-.33-.44-.85-.7-1.4-.7h-3.5z'></path></svg>")
            .append("<div class='name'><a href='"+ (requestPath+file.getName()) +"'>"+ file.getName() +"</a></div>")
            .append("<div class='date'>").append(DATE_FORMAT.format(fileLastModified)).append("</div>")
            .append("<div class='size'>").append("-</div>")
            .append("</div>");
    }

    private static List<File> sortByTypeThenName(File[] files){
        List<File> fileList = new ArrayList<>();
        for(File file : files){
            if(file.isDirectory())
                fileList.add(file);
        }
        for(File file : files){
            if(!file.isDirectory())
                fileList.add(file);
        }
        return fileList;
    }
}
