package com.vincentcodes.webserver.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.vincentcodes.webserver.component.request.HttpRequest;

public class RedirectUrlsListing {
    /**
     * @return null if the file is not readable
     */
    public static String getRedirectListingHtml(HttpRequest req, File redirectLinks){
        try {
            if(!FileExtUtils.extractFileExtension(redirectLinks).equals("rdir"))
                return null;
            List<String> urls = Files.readAllLines(Paths.get(redirectLinks.getAbsolutePath()));
            return getRedirectListingHtml(req, urls);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getRedirectListingHtml(HttpRequest req, List<String> urls){
        return getRedirectListingHtml(req.getBasicInfo().getPath().get(), urls);
    }

    public static String getRedirectListingHtml(String requestPath, List<String> urls){
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("<head><meta http-equiv='Content-Type' content='text/html;charset=UTF-8'></head>");
        builder.append("<style>").append(DirectoryListing.STYLE).append("</style>");
        builder.append("<body><div class='main-content'>");
        builder.append("<div class='title'>Showing entries for: "+ requestPath +"</div>");
        builder.append("<div class='files-table'>");
        builder.append("<div class='file-entry desc'>")
            .append("<svg class='icon' viewBox='0 0 16 16'></svg>")
            .append("<div class='name'>Name</div>")
            .append("</div>");

        if(!requestPath.endsWith("/")){
            requestPath = requestPath + "/";
        }
        
        for(String url : urls)
            createDirectoryEntryHtml(builder, requestPath, url);

        builder.append("</div></div></body></html>");
        return builder.toString();
    }
    
    private static void createDirectoryEntryHtml(StringBuilder builder, String requestPath, String url){
        builder.append("<div class='file-entry'>")
            .append("<svg class='icon' viewBox='0 0 16 16'><path fill-rule='evenodd' d='M1.75 1A1.75 1.75 0 000 2.75v10.5C0 14.216.784 15 1.75 15h12.5A1.75 1.75 0 0016 13.25v-8.5A1.75 1.75 0 0014.25 3h-6.5a.25.25 0 01-.2-.1l-.9-1.2c-.33-.44-.85-.7-1.4-.7h-3.5z'></path></svg>")
            .append("<div class='name'><a href='"+ url +"'>"+ url +"</a></div>")
            .append("</div>");
    }
}
