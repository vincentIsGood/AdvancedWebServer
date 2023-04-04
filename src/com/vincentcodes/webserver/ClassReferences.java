package com.vincentcodes.webserver;

import com.vincentcodes.webserver.annotaion.Unreferenced;
import com.vincentcodes.webserver.component.response.ResponseParser;
import com.vincentcodes.webserver.dispatcher.reflect.DirectoryOnly;
import com.vincentcodes.webserver.dispatcher.reflect.NoDirectory;
import com.vincentcodes.webserver.helper.HttpTunnel;
import com.vincentcodes.webserver.util.DirectoryListing;
import com.vincentcodes.webserver.util.HttpRedirecter;
import com.vincentcodes.webserver.util.RedirectUrlsListing;
import com.vincentcodes.webserver.util.WebSocketUpgrader;

/**
 * <p>
 * This class is used to reference the unreferenced classes.
 * Since I thought this maybe useful for extension creators
 * to make use of these classes. 
 * 
 * <p>
 * This reference thingy is a work around to compile source 
 * codes, which are not referenced and include them in a jar 
 * file easily. (learned from a C++ trick by using 
 * UNREFERENCED_PARAMETER, though it was an intellisense issue)
 * 
 * <p>
 * I used VScode to work on this project, I may switch to
 * Intellij if needed.
 * 
 * @see WebServer
 * @author Vincent Ko
 */
public class ClassReferences {
    @Unreferenced(ResponseParser.class)
    @Unreferenced(HttpTunnel.class)
    @Unreferenced(HttpRedirecter.class)
    @Unreferenced(WebSocketUpgrader.class)
    @Unreferenced(DirectoryListing.class)
    @Unreferenced(RedirectUrlsListing.class)
    public static class UtilClasses{
    }

    // @Unreferenced(BeforeInvocation.class)
    // @Unreferenced(AfterInvocation.class)
    // @Unreferenced(InvocationCondition.class)
    @Unreferenced(DirectoryOnly.class)
    @Unreferenced(NoDirectory.class)
    public static class Conditionals{

    }
}
