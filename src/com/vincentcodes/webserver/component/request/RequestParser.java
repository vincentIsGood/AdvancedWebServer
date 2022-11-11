package com.vincentcodes.webserver.component.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.body.HttpBodyFileStream;
import com.vincentcodes.webserver.component.body.HttpBodyStream;
import com.vincentcodes.webserver.component.header.EntityInfo;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.exception.UnsupportedHttpMethodException;
import com.vincentcodes.webserver.helper.ReadUntilResult;
import com.vincentcodes.webserver.helper.TextBinaryInputStream;

/**
 * The class names used in request.* package is mostly based on 
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages
 */
public class RequestParser {
    /**
     * Parse an http request. Please also note that as soon as an error is catched,
     * the HttpRequest immediately becomes invalid. Use {@link HttpRequest#isValid()}
     * to see if it is still valid or not.
     * @param is An {@link InputStream} which is not previously read or modified.
     * @return A parsed http {@link com.vincentcodes.webserver.request.HttpRequest request}.
     */
    public static HttpRequest parse(InputStream is) {
        TextBinaryInputStream reader = new TextBinaryInputStream(is);

        HttpRequest request = new HttpRequest();
        HttpHeaders headers = new HttpHeaders();
        HttpRequestBasicInfo basicInfo = null;
        MultipartFormData multipart = null;
        HttpBody body = new HttpBodyStream();

        // For the purpose of logging the whole request 
        // (Necessary? No, maybe I'll add an option to disable it)
        StringBuilder wholeRequest = new StringBuilder();
        try{
            boolean requestBodyIncoming = false;
            String line = null;

            line = reader.readLine();
            wholeRequest.append(line).append("\r\n");
            basicInfo = parseFirstLine(line);
            
            // Add a little decoded text
            // if(line.contains("%")){
            //     wholeRequest.append("('").append(basicInfo.getPath()).append("')\n");
            // }

            while((line = reader.readLine()) != null){
                // System.out.println(line); // if something went wrong, uncomment this to debug.
                
                wholeRequest.append(line).append("\r\n");
                if(line.isEmpty()){
                    requestBodyIncoming = true;
                }

                if(!requestBodyIncoming){
                    String key = line.substring(0, line.indexOf(':')).trim();
                    String val = line.substring(line.indexOf(':')+1).trim();
                    headers.add(key, val);
                }else{
                    EntityInfo entityInfo = headers.getEntityInfo();
                    int length = entityInfo.getLength();
                    
                    if(length > 0){
                        if(entityInfo.getType().equals("multipart/form-data")){
                            multipart = parseMultipartFormData(reader, entityInfo, wholeRequest);
                            if(multipart == null)
                                request.invalid();
                        }else{
                            // 64MB -> use tmp file
                            if(length > 67108864){
                                body = new HttpBodyFileStream();
                                int sizeRead = 0;
                                byte[] content = new byte[4096];
                                while((sizeRead = reader.read(content)) != -1){
                                    // wholeRequest.append(new String(content)).append('\n');
                                    body.writeToBody(content, sizeRead);
                                }
                            }else{
                                body.writeToBody(reader.readNBytes(length));
                            }
                        }
                    }else if(basicInfo.getMethod().equals("PRI")){
                        // https://tools.ietf.org/html/rfc7540#section-3.5
                        reader.readLine(); // Skip "SM\r\n"
                        reader.readLine(); // Skip "\r\n"
                        wholeRequest.append("SM\r\n\r\n");
                    }
                    break;
                }
            }
            
            request.setHeaders(headers);
            request.setBody(body);
            request.setBasicInfo(basicInfo);
            if(multipart != null){
                request.setMultipartFormData(multipart);
            }
        }catch(IOException e){
            WebServer.logger.err("Catching a "+e.getClass().getName()+": " + e.getMessage());
            // An established connection was aborted by the software in your host machine
            if(!WebServer.canIgnoreException(e)){
                e.printStackTrace();
                request.invalid();
            }
        }catch(Exception e){
            WebServer.logger.err("Catching a "+e.getClass().getName()+": " + e.getMessage());
            e.printStackTrace();
            // request.invalid();
            // throw new CannotParseRequestException("An error occured while parsing an Http Request", e);
        }
        request.setWholeRequest(wholeRequest.toString());
        if(request.getWholeRequest().startsWith("PRI")){
            WebServer.logger.debug("HTTP2 upgrade sequence received");
            return request;
        }
        WebServer.logger.debug("\n" + request.getWholeRequest());
        return request;
    }

    /**
     * Similar to {@link RequestParser#parse(InputStream)}
     */
    public static HttpRequest parse(String wholeRequest) {
        return parse(new ByteArrayInputStream(wholeRequest.getBytes()));
    }

    /**
     * @param reader [required]
     * @param entityInfo [required]
     * @param wholeRequest [required]
     * @return null if it has no boundary
     */
    public static MultipartFormData parseMultipartFormData(TextBinaryInputStream reader, EntityInfo entityInfo, StringBuilder wholeRequest) throws IOException{
        MultipartFormData multipart = new MultipartFormData();
        HttpHeaders headers = new HttpHeaders();
        String boundary = entityInfo.getBoundary();
        if(boundary == null){
            return null;
        }

        String line;
        boolean startBody = false;
        while((line = reader.readLine()) != null){
            if(line.equals("--" + boundary)){
                wholeRequest.append("--" + boundary).append("\n");
                headers = new HttpHeaders();
                startBody = false;
                continue;
            }

            if(line.isEmpty()){
                wholeRequest.append("\n");
                startBody = true;
            }else if(!startBody){
                wholeRequest.append(line).append("\r\n");
                
                String key = line.substring(0, line.indexOf(':')).trim();
                String val = line.substring(line.indexOf(':')+1).trim();
                headers.add(key, val);
            }

            if(startBody){
                HttpBody body = new HttpBodyFileStream();
                // byte[] content = reader.readBytesUntil("\r\n--" + boundary);
                // // If EOF is reached, do not do the processing
                // if(content == null) break;
                // body.writeToBody(content);

                ReadUntilResult result;
                do{
                    result = reader.readBytesUntil("\r\n--" + boundary, 4096);
                    body.writeToBody(result.data());
                }while(result.data() != null && !result.isMatchingStrFound());
                
                // wholeRequest.append(body.string()).append("\n");
                wholeRequest.append("--" + boundary);
                wholeRequest.append(reader.readLine()).append("\n"); // skip the "\n" or "--\n"
                // WebServer.logger.warn(wholeRequest.toString());
                multipart.put(headers, body);
                startBody = false;
                String endString = "--" + boundary + "--";
                if(wholeRequest.substring(wholeRequest.length()-endString.length()-2).lastIndexOf(endString) != -1)
                    break;
            }
        }
        return multipart;
    }

    /**
     * Parse the first line of an http request
     * @param line
     * @return StartLine if nothing went wrong. null if the request method is not supported or in an invalid format
     */
    public static HttpRequestBasicInfo parseFirstLine(String line) throws CannotParseRequestException{
        String method = line.substring(0, line.indexOf(' ')); // till the 1st space
        String path = line.substring(line.indexOf(' ')+1, line.lastIndexOf(' ')); // the middle bit
        String version = line.substring(line.lastIndexOf(' ')+1); // the last bit
        if(!HttpRequest.SUPPORTED_METHODS.contains(method)){
            throw new UnsupportedHttpMethodException("Method "+ method +" is not supported.");
        }

        // Cleaning up the path
        while(path.contains("//"))
            path = path.replaceAll("//", "/");
        HashMap<String, String> args = parseParametersSafe(path);
        
        if(args.size() > 0){
            path = path.substring(0, path.indexOf("?"));
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
            return new HttpRequestBasicInfo(method, path, version, args);
        }
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        return new HttpRequestBasicInfo(method, path, version);
    }

    private static HashMap<String, String> parseParametersSafe(String path){
        if(path.indexOf('?') >= path.length()-1 || path.indexOf('?') == -1){
            return new HashMap<>();
        }
        String strArgs = path.substring(path.indexOf('?')+1);
        return parseParameters(strArgs);
    }

    private static HashMap<String, String> parseParameters(String strArgs){
        HashMap<String, String> result = new HashMap<>();
        if(strArgs.contains("&")){
            String[] args = strArgs.split("&");
            for(String arg : args){
                if(arg.indexOf('=') == -1){
                    if(!arg.trim().equals(""))
                        result.put(arg, "");
                }else{
                    try{
                        String[] splited = arg.split("=");
                        if(splited.length == 2)
                            result.put(splited[0], URLDecoder.decode(splited[1], "utf-8"));
                        else
                            result.put(splited[0], "");
                    }catch(UnsupportedEncodingException ignored){}
                }
            }
        }else{
            if(strArgs.indexOf('=') == -1){
                if(!strArgs.trim().equals(""))
                    result.put(strArgs, "");
            }else{
                try{
                    String[] splited = strArgs.split("=");
                    if(splited.length == 2)
                        result.put(splited[0], URLDecoder.decode(splited[1], "utf-8"));
                    else
                        result.put(splited[0], "");
                }catch(UnsupportedEncodingException ignored){}
            }
        }
        return result;
    }
}
