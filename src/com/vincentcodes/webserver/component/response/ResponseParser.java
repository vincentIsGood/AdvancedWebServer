package com.vincentcodes.webserver.component.response;

import java.io.IOException;
import java.io.InputStream;

import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.body.HttpBodyStream;
import com.vincentcodes.webserver.component.header.EntityInfo;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.helper.TextBinaryInputStream;

public class ResponseParser {
    public static ResponseBuilder parse(InputStream is){
        TextBinaryInputStream reader = new TextBinaryInputStream(is);

        HttpHeaders headers = new HttpHeaders();
        HttpBody body = new HttpBodyStream();
        ResponseBuilder response = ResponseBuilder.getDefault(body);

        try{
            response.setResponseCode(parseStatus(reader.readLine()));

            boolean requestBodyIncoming = false;
            String line;
            while((line = reader.readLine()) != null){
                if(line.isEmpty()){
                    requestBodyIncoming = true;
                }
                
                if(!requestBodyIncoming){
                    String key = line.substring(0, line.indexOf(':')).trim();
                    String val = line.substring(line.indexOf(':')+1).trim();
                    headers.add(key, val);
                }else{
                    EntityInfo entityInfo = headers.getEntityInfo();
                    long length = entityInfo.getLength();

                    if(entityInfo.getTransferEncoding().equals("chunked")){
                        int realLength = 0;
                        // see example request to understand this part, when hex is 0, we'll stop
                        while((realLength = Integer.parseInt(reader.readLine(), 16)) != 0){
                            byte[] content = new byte[realLength];
                            reader.read(content);
                            body.writeToBody(content);
                            reader.readLine(); // skip line break
                        }
                    }else if(length > 0){
                        byte[] content = new byte[4096];
                        int bytesRead;
                        while((bytesRead = reader.read(content)) != -1){
                            body.writeToBody(content, bytesRead);
                        }
                    }
                    break;
                }
            }
            response.getHeaders().add(headers);
            response.setBody(body);
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        return response;
    }

    public static int parseStatus(String line){
        int beginIndex = line.indexOf(' ')+1;
        int endingIndex = line.indexOf(' ', beginIndex);
        return Integer.parseInt(line.substring(beginIndex, endingIndex == -1? line.length() : endingIndex));
    }
}
