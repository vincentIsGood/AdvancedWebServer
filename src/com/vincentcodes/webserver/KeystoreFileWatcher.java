package com.vincentcodes.webserver;

import java.io.File;
import java.nio.file.Path;

import javax.net.ssl.SSLContext;

import com.vincentcodes.files.OnFileCreated;
import com.vincentcodes.files.SpecificFileListener;
import com.vincentcodes.net.SSLUpgrader;
import com.vincentcodes.net.UpgradableSocket;

public class KeystoreFileWatcher implements OnFileCreated, SpecificFileListener{
    private final WebServer.Configuration configuration;

    public KeystoreFileWatcher(WebServer.Configuration configuration){
        this.configuration = configuration;
    }

    @Override
    public File getFileListeningFor() {
        return configuration.getKeyStoreFile();
    }

    @Override
    public void handleOnFileCreated(Path fileChanged) {
        WebServer.logger.warn("Keystore changed. Renewing SSLUpgrader");
        SSLUpgrader upgrader = new SSLUpgrader(configuration.getKeyStoreFile(), configuration.getKeyStorePassword());
        SSLContext sslContext = upgrader.createSSLContext();
        if(sslContext != null)
            UpgradableSocket.setSSLUpgrader(upgrader);
    }
    
}
