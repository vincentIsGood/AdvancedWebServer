package com.vincentcodes.tests.utils;

public class RequestGenerator {
    public static class GET{
        public static String generateRequest(String path){
            StringBuilder builder = new StringBuilder();
            builder.append("GET "+ path +" HTTP/1.1\r\n");
            builder.append("Host: 127.0.0.1:5050\r\n");
            builder.append("User-Agent: curl/7.58.0\r\n");
            builder.append("Accept: */*\r\n");
            builder.append("\r\n");
            return builder.toString();
        }

        public static String standardGet(){
            String result = "GET /post/index.html HTTP/1.1\r\n"
            + "Host: 127.0.0.1:5050\r\n"
            + "Connection: keep-alive\r\n"
            + "Upgrade-Insecure-Requests: 1\r\n"
            + "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36\n"
            + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\n"
            + "Sec-GPC: 1\r\n"
            + "Sec-Fetch-Site: none\r\n"
            + "Sec-Fetch-Mode: navigate\r\n"
            + "Sec-Fetch-User: ?1\r\n"
            + "Sec-Fetch-Dest: document\r\n"
            + "Accept-Encoding: gzip, deflate, br\r\n"
            + "Accept-Language: en-US,en;q=0.9\r\n\r\n";
            return result;
        }
    }
    
    /**
     * Allowed "content-type":
     * application/x-www-form-urlencoded
     * multipart/form-data
     * text/plain
     * 
     * Ref:
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST
     */
    public static class POST{
        public static String generateRequest(String path, String content){
            StringBuilder builder = new StringBuilder();
            builder.append("POST "+ path +" HTTP/1.1\r\n");
            builder.append("Host: 127.0.0.1:5050\r\n");
            builder.append("User-Agent: curl/7.58.0\r\n");
            builder.append("content-length: " + content.length() + "\r\n");
            builder.append("content-type: x-www-form-urlencoded\r\n");
            builder.append("Accept: */*\r\n");
            builder.append("\r\n");
            builder.append(content);
            return builder.toString();
        }

        public static String generateMultipartRequest(String path){
            String boundary = "----WebKitFormBoundary";
            String body = generateMultipartBody(boundary);
            
            StringBuilder builder = new StringBuilder();
            builder.append("POST "+ path +" HTTP/1.1\r\n");
            builder.append("Host: 127.0.0.1:5050\r\n");
            builder.append("User-Agent: curl/7.58.0\r\n");
            builder.append("content-length: "+ body.length() +"\r\n");
            builder.append("content-type: multipart/form-data; boundary="+ boundary +"\r\n");
            builder.append("Accept: */*\r\n");
            builder.append("\r\n");
            builder.append(body);
            return builder.toString();
        }

        private static String generateMultipartBody(String boundary){
            StringBuilder builder = new StringBuilder();
            builder.append("--" + boundary + "\r\n");
            builder.append("Content-Disposition: form-data; name=\"testfile\"; filename=\"&#21427;&#12375;&#12356;.txt\"\r\n");
            builder.append("Content-Type: application/octet-stream\r\n");
            builder.append("\r\n");
            builder.append("This is a test file.\n");
            builder.append("This is a test file.\r\n");
            builder.append("--" + boundary + "\r\n");
            builder.append("Content-Disposition: form-data; name=\"submit\"\r\n");
            builder.append("\r\n");
            builder.append("Submit\r\n");
            builder.append("--" + boundary + "--\r\n");
            return builder.toString();
        }
    }
}
