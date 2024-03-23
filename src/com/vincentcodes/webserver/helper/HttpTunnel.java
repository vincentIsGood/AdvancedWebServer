package com.vincentcodes.webserver.helper;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.component.response.ResponseParser;

/**
 * This class is used to establish a connection to 
 * another HTTP server. It can be used inside a 
 * handler to change the current response like this:
 * <pre>
 * ResponseBuilder response = tunnel.sendOnce(req);
 * res.setResponseCode(response.getResponseCode());
 * res.setBody(response.getBody());
 * res.getHeaders().add(response.getHeaders());
 * </pre>
 */
public class HttpTunnel implements Closeable {
    private static final int READ_TIMEOUT = WebServer.TUNNEL_READ_TIMEOUT_MILSEC;
    private final String host;
    private final int port;
    private final boolean ssl;
    private Socket socket;
    private SSLSocketFactory sslSocketFactory;

    /**
     * Connection is made to the destination when 
     * any of the send method is called.
     */
    public HttpTunnel(String host, int port, boolean ssl){
        this(host, port, ssl, false);
    }
    public HttpTunnel(String host, int port){
        this(host, port, false, false);
    }
    public HttpTunnel(String host, int port, boolean ssl, boolean dangerous){
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        if(ssl){
            try {
                sslSocketFactory = dangerous? 
                    createDangerousSSLSocketFactory() : (SSLSocketFactory)SSLSocketFactory.getDefault();
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void open() throws IOException{
        socket = createSocket();
    }

    @Override
    public void close() throws IOException{
        if(socket != null)
            socket.close();
    }

    /**
     * Just send the request. Will not expect server response.
     */
    public void sendOneWay(HttpRequest request) throws IOException{
        if(socket == null) open();

        OutputStream os = socket.getOutputStream();
        writeRequestToOutputStream(os, request);
    }

    /**
     * If you want to send request once, use {@link #sendOnce(HttpRequest)} instead.
     * <p>
     * Lazying loading is implemented. If {@link #send(HttpRequest)}
     * is called the first time, a connection will be made to the 
     * destination.
     * @param request a request to be tunneled
     * @return a parsed response coming from the dest webserver.
     */
    public ResponseBuilder send(HttpRequest request) throws IOException{
        sendOneWay(request);
        
        InputStream is = socket.getInputStream();
        return ResponseParser.parse(is);
    }

    /**
     * Send a request through the tunnel. After it's sent and a
     * response is recevied, the connection is closed automatically.
     * @param request a request to be tunneled
     * @return a parsed response coming from the dest webserver.
     */
    public ResponseBuilder sendOnce(HttpRequest request) throws IOException{
        try(Socket reverseProxy = createSocket()){
            OutputStream os = reverseProxy.getOutputStream();
            InputStream is = reverseProxy.getInputStream();

            writeRequestToOutputStream(os, request);
            
            return ResponseParser.parse(is);
        }
    }

    public Socket getSocket(){
        return this.socket;
    }

    private Socket createSocket() throws IOException{
        Socket socket;
        if(ssl){
            socket = createSSLSocket();
        }else socket = new Socket(host, port);
        socket.setSoTimeout(READ_TIMEOUT);
        return socket;
    }

    private Socket createSSLSocket() throws IOException{
        return sslSocketFactory.createSocket(host, port);
    }

    /**
     * Trust all certs
     */
    private SSLSocketFactory createDangerousSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException{
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        context.init(null, new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
        }, new SecureRandom());
        return context.getSocketFactory();
    }

    public static void writeRequestToOutputStream(OutputStream os, HttpRequest request) throws IOException{
        if(request.getWholeRequest() == null){
            os.write(request.toHttpString().getBytes());
        }else{
            os.write(request.getWholeRequest().getBytes());
        }
        if(request.getBody() != null && request.getBody().length() > 0){
            os.write(request.getBody().getBytes());
        }
        os.flush();
    }
    
    public static void streamToUntilClose(InputStream is, OutputStream os) throws IOException{
        byte[] buffer = new byte[4096];
        int numOfBytesRead = 0;
        while((numOfBytesRead = is.read(buffer)) != -1){
            os.write(buffer, 0, numOfBytesRead);
            os.flush();
        }
    }
}
