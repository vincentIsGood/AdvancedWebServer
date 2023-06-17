package com.vincentcodes.webserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.vincentcodes.webserver.annotaion.AutoInjected;
import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.HttpGet;
import com.vincentcodes.webserver.annotaion.request.HttpPost;
import com.vincentcodes.webserver.annotaion.request.HttpPut;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.response.Mutatable;
import com.vincentcodes.webserver.component.request.FormData;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.MultipartFormData;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.util.FileControl;

@HttpHandler
@RequestMapping("/post")
public class PostRequestHandler{
    @AutoInjected
    WebServer server;

    @HttpPut
    @Mutatable
    @RequestMapping("/*")
    public void handlePutRequest(HttpRequest request, ResponseBuilder response){
        String path = request.getBasicInfo().getPath().get();
        // insecure checking
        if(request.getBody().length() > 0 && !path.endsWith("/") && path.lastIndexOf('.') != -1){
            File file = new File("./" + path.substring(path.lastIndexOf('/')+1)); // create file on the root path
            try(FileOutputStream outputStream = new FileOutputStream(file)){
                System.out.println("[+] Writing file of length: " + request.getBody().length());
                request.getBody().streamBytesTo(outputStream);
                System.out.println("[+] File written to: " + file.getName());
            }catch(IOException e){
                e.printStackTrace();
            }
            response.setResponseCode(403);
        }
    }

    @HttpGet
    @RequestMapping("/*")
    public File handleGetRequest(HttpRequest request){
        return FileControl.get(request, server.getConfiguration());
    }

    @HttpPost
    @Mutatable
    @RequestMapping("/*")
    public void handlePostRequest(HttpRequest request, ResponseBuilder response) throws IOException{
        if(request.hasMultipartData()){
            MultipartFormData formData = request.getMultipartFormData();
            FormData fileData = formData.getData("uploadfile");
            if(fileData.getFilename() != null){
                File file = new File("./" + fileData.getFilename());
                if(!file.exists()){
                    file.createNewFile();
                    try(FileOutputStream outputStream = new FileOutputStream(file)){
                        System.out.println("[+] Writing file of length: " + fileData.getBody().length());
                        fileData.getBody().streamBytesTo(outputStream);
                        System.out.println("[+] File written to: " + file.getName());
                    }catch(IOException e){
                        e.printStackTrace();
                        throw new IOException(e);
                    }
                }
            }else{
                response.setResponseCode(404);
            }
        }else{
            response.setResponseCode(404);
        }
    }
}