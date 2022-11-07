package com.vincentcodes.webserver.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.vincentcodes.webserver.util.FileExtUtils;

/**
 * Background / Pain suffered.
 * HTTPS is tested from another project "working" project, "HttpsServerDemo".
 * To be honest, it is somewhat working, but at that moment I have not idea 
 * what TrustStore and KeyStore is. After more than 12 hours of searching
 * through the internet, only by then, I solved the "certificate_unknown"
 * problem.
 * 
 * Currently, SSLUpgrader only supports jks / pfx / p12 file extensions
 * @see https://www.baeldung.com/java-keystore-truststore-difference
 */
public class SSLUpgrader {
    private File javaKeyStore;
    private String keyStorePass;
    private SSLContext sslContext;

    public SSLUpgrader(File javaKeyStore, String keyStorePass){
        String ext = FileExtUtils.extractFileExtension(javaKeyStore.getName());
        if(!ext.equals("jks") && !ext.equals("pfx"))
            throw new IllegalArgumentException("Unsupported keystore type.");
        this.javaKeyStore = javaKeyStore;
        this.keyStorePass = keyStorePass;
    }

    public SSLContext createSSLContext(){
        try{
            // Java KeyStore
            KeyStore keyStore = getKeyStoreInstance();
            keyStore.load(new FileInputStream(javaKeyStore), keyStorePass.toCharArray());

            // KeyStore trustStore = getKeyStoreInstance();
            // trustStore.load(new FileInputStream("./cacerts"), "changeit".toCharArray());

            // A Java keystore stores private key entries, certificates with public keys or just secret keys
            KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmFactory.init(keyStore, keyStorePass.toCharArray());
            KeyManager[] km = kmFactory.getKeyManagers();

            // In Java, we use it to trust the third party (server) we're about to communicate with.
            // We, the client, then look up the associated certificate in our truststore
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init(keyStore);
            TrustManager[] tm = tmFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.getServerSessionContext().setSessionCacheSize(100);
            sslContext.init(km, tm, null);
            this.sslContext = sslContext;

            return sslContext;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private KeyStore getKeyStoreInstance() throws KeyStoreException, NullPointerException{
        switch(FileExtUtils.extractFileExtension(javaKeyStore.getName())){
            case "pfx": return KeyStore.getInstance("PKCS12");
            case "p12": return KeyStore.getInstance("PKCS12");
        }
        return KeyStore.getInstance("JKS");
    }

    public SSLServerSocket getSSLServerSocket(int port) throws IOException{
        SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
        return (SSLServerSocket)serverSocketFactory.createServerSocket(port);
    }

    public File getJavaKeyStore() {
        return javaKeyStore;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public SSLContext getSSlContext() {
        return sslContext;
    }
}
