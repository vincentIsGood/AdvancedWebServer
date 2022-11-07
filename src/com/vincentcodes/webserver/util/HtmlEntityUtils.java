package com.vincentcodes.webserver.util;

public class HtmlEntityUtils {
    public static String escapeArrowsOnly(String str){
        String result = null;
        result = str.replaceAll("<", "&lt;");
        result = str.replaceAll(">", "&gt;");
        return result;
    }

    public static String escapeDecimal(String str){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < str.length(); i++)
            builder.append("&#").append((int)str.charAt(i)).append(";");
        return builder.toString();
    }

    /**
     * This method does not check for syntax errors, be careful.
     * The name of this method should be unescapeDecimalUnsafe(String)
     */
    public static String unescapeDecimal(String str){
        if(str.indexOf("&#", 0) == -1)
            return str;
        int previousPosition = 0;
        int currentPosition = 0;
        StringBuilder builder = new StringBuilder();
        while((currentPosition = str.indexOf("&#", currentPosition)) != -1){
            int lowerbound = currentPosition+2;
            int upperbound = str.indexOf(";", currentPosition);
            builder.append(str.substring(previousPosition, currentPosition)).append((char)Integer.parseInt(str.substring(lowerbound, upperbound)));
            currentPosition = upperbound;
            previousPosition = currentPosition+1; // skip ";"
        }
        return builder.append(str.substring(previousPosition, str.length())).toString();
    }
}
