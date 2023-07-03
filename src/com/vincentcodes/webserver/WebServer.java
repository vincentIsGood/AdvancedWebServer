package com.vincentcodes.webserver;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import com.vincentcodes.files.FileWatcher;
import com.vincentcodes.logger.Logger;
import com.vincentcodes.net.SSLUpgrader;
import com.vincentcodes.net.UpgradableSocket;
import com.vincentcodes.webserver.annotaion.AutoInjected;
import com.vincentcodes.webserver.annotaion.Unreferenced;
import com.vincentcodes.webserver.annotaion.request.HttpConnect;
import com.vincentcodes.webserver.annotaion.request.HttpGet;
import com.vincentcodes.webserver.annotaion.request.HttpHead;
import com.vincentcodes.webserver.annotaion.request.HttpOptions;
import com.vincentcodes.webserver.annotaion.request.HttpPost;
import com.vincentcodes.webserver.annotaion.request.HttpPri;
import com.vincentcodes.webserver.annotaion.request.HttpPut;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestValidator;
import com.vincentcodes.webserver.component.request.RequestValidatorConfig;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.defaults.DefaultHandler;
import com.vincentcodes.webserver.defaults.Http2UpgradeHandler;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.dispatcher.operation.DispatcherOperation;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory.InvocationTypes;
import com.vincentcodes.webserver.dispatcher.operation.impl.HttpDispatcherOperation;
import com.vincentcodes.webserver.dispatcher.operation.impl.SimplerHttpDispatcherOperation;
import com.vincentcodes.webserver.exposed.BeanDefinitions;
import com.vincentcodes.webserver.helper.FieldsInjector;
import com.vincentcodes.webserver.helper.ObjectPool;
import com.vincentcodes.webserver.helper.loader.JarLoader;
import com.vincentcodes.webserver.helper.loader.JarRegister;
import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * <p>
 * Date of creation: 10/4/2020 (MM/DD/YYYY)
 * <p>
 * This web server is a successor to my previous WebServer project which was 
 * created on 8/14/2018. That is why "Advanced" is prepended to the project 
 * name this time.
 * <p>
 * Inspired by one of the most used protocol in that day and age (as of 2018),
 * the old WebServer project had been created. As of 2020, I have an urge to 
 * create a more robust and modifiable version of webserver built from Java 
 * ground up.
 * <p>
 * I was not willing to make this project go public. I will re-evaluate this  
 * statement again since I would like it to improve it and let those who wants 
 * a lightweight webserver to use it.
 * <p>
 * One more important factor to make the project public is for my future
 * career. I spent lots of time improving the server and I want people to know 
 * it. Improvements == Programming skill enhancement (Level Up!)
 * <p>
 * {@link Main} is the entry point. It calls {@link WebServer}.
 * 
 * @author Vincent Ko
 */
@Unreferenced(ClassReferences.class)
public class WebServer {
    public static String SERVER_NAME = "vws";

    public static final int MAX_THREAD_POOL_SIZE = 10;

    public static final int CONNECTION_READ_TIMEOUT_MILSEC = 6000; // 6s
    public static final int CONNECTION_WRITE_TIMEOUT_MILSEC = 120*(60*1000); // 120mins

    public static final int WEBSOCKET_PING_INTERVAL_MILSEC = 30*(60*1000); // 30mins

    public static final int MAX_HTTP2_STREAMS_INDEX = 65536-1; // default: 2**31-1

    public static final boolean THROW_ERROR_WHEN_SEND_ON_CLOSED = true;

    /**
     * Note. You need at least 1MiB to keep the streaming service 
     * running smooth (esp. for video streaming services)
     * <p>
     * 2 MiB (used for streaming files, including the starting byte)
     */
    public static final int MAX_PARTIAL_DATA_LENGTH = 1024 * 1024 * 2 -1;
    // public static final int MAX_PARTIAL_DATA_LENGTH = 1024 * 1024 * 512 -1;

    public static final Logger logger = new Logger("logs/output", false, true){
        {
            enable(LogType.DEBUG);
        }
    };
    public static boolean lowLevelDebugMode = false;

    public static final ObjectPool PUBLIC_POOL = new ObjectPool();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
    public static final Map<String, Class<? extends Annotation>> SUPPORTED_REQUEST_METHOD = Collections.unmodifiableMap(new HashMap<>(){
        {
            put("GET", HttpGet.class);
            put("HEAD", HttpHead.class);
            put("POST", HttpPost.class);
            put("PUT", HttpPut.class);
            put("DELETE", HttpPut.class);

            put("OPTIONS", HttpOptions.class);
            put("CONNECT", HttpConnect.class);

            put("PRI", HttpPri.class); // for HTTP2 upgrades
        }
    });

    private final WebServer.Configuration configuration;
    private FileWatcher keystoreWatcher;

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    static{
        // HTTP dates are always expressed in GMT, never in local time -- MDN
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        logger.debug("Setting time zone to GMT (mandatory) with date format: " + DATE_FORMAT.toPattern());
    }

    private WebServer(WebServer.Configuration configuration) throws IOException{
        this.configuration = configuration;

        if(configuration.forceHttp2)
            ExtensionRegister.register(new Http2UpgradeHandler());
        
        if(configuration.useDefaultHandlers){
            ExtensionRegister.register(new DefaultHandler());
        }
        if(configuration.extensions.size() > 0){
            JarLoader loader = new JarLoader(configuration.extensions);
            ExtensionRegister.register(loader.loadJars());
        }

        if(configuration.useSSL && configuration.keyStoreFile != null && configuration.keyStorePassword != null){
            logger.warn("Creating an SSL webserver");
            if(configuration.forceHttp2()){
                logger.warn("Force http2 on all connections");
            }
            SSLUpgrader upgrader = new SSLUpgrader(configuration.getKeyStoreFile(), configuration.getKeyStorePassword());
            SSLContext sslContext = upgrader.createSSLContext();
            if(sslContext == null)
                throw new IllegalStateException("Unable to create SSLContext from the provided keystore");
            UpgradableSocket.setSSLUpgrader(upgrader);
            
            keystoreWatcher = new FileWatcher(configuration.keyStoreFile.getCanonicalFile().getParent());
            keystoreWatcher.registerListener(new KeystoreFileWatcher(configuration));
        }else{
            logger.warn("Creating a non-ssl webserver");
        }
        // allow server take in raw bytes, and detect if SSL/TLS is needed
        this.serverSocket = new ServerSocket(configuration.port);

        executorService = configuration.useMultithread()? 
            Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE) : 
            Executors.newSingleThreadExecutor();
    }

    /**
     * Remember to {@link WebServer#close() close} it
     */
    public void start() {
        if(keystoreWatcher != null)
            keystoreWatcher.start();

        // TODO: maybe add some kind of ways to make it more customizable?
        RequestValidatorConfig validatorConfig = new RequestValidatorConfig();
        validatorConfig.addMandatoryHeader("host");
        HttpRequestValidator requestValidator = new HttpRequestValidator(validatorConfig);

        OperationStrategyFactory opStrategyFactory = new OperationStrategyFactory(configuration);
        List<DispatcherOperation<HttpRequest, ResponseBuilder, MethodDecorator>> dispatcherOperations = new ArrayList<>();
        dispatcherOperations.add(new HttpDispatcherOperation(opStrategyFactory.create(InvocationTypes.NORMAL_HTTP)));
        dispatcherOperations.add(new SimplerHttpDispatcherOperation(opStrategyFactory.create(InvocationTypes.SIMPLER_HTTP)));

        HttpRequestDispatcher requestDispatcher = HttpRequestDispatcher.createInstance(dispatcherOperations);

        injectDependencies();

        logger.info("Web Server started on port " + serverSocket.getLocalPort());
        logger.info("Connection read timeout is set to " + CONNECTION_READ_TIMEOUT_MILSEC + "ms");
        logger.info("Connection write timeout is set to " + CONNECTION_WRITE_TIMEOUT_MILSEC + "ms");

        int countTillGc = 0;
        while (true) {
            try{
                ServerThread thread = new ServerThread(new UpgradableSocket(serverSocket.accept()), configuration, requestValidator, requestDispatcher);
                executorService.execute(thread);
            }catch(Exception e){
                WebServer.logger.err("Catching a "+e.getClass().getName()+": " + e.getMessage());
            }

            countTillGc++;
            if(countTillGc > 10){
                countTillGc = 0;
                System.gc();
            }
        }
    }

    public void injectDependencies(){
        ObjectPool pool = new ObjectPool();
        pool.put(WebServer.class, this);
        BeanInitializer beanInitializer = new BeanInitializer(ConfigurationRegister.get().findAllClassAssignableTo(BeanDefinitions.class));
        beanInitializer.start().addToPool(pool);
        
        FieldsInjector injector = new FieldsInjector(HttpHandlerRegister.getRegistry().findAllFieldsWithAnnotation(AutoInjected.class), pool);
        injector.createMissingObjectForFields();
        injector.inject();
    }

    public WebServer.Configuration getConfiguration(){
        return this.configuration;
    }

    public void close() throws IOException{
        serverSocket.close();
        shutdownExecutor();
    }
    private void shutdownExecutor(){
        executorService.shutdown();
        try{
            if(!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)){
                executorService.shutdownNow();
            }
        }catch(InterruptedException e){
            executorService.shutdownNow();
        }
        if(executorService.isShutdown()){
            logger.info("Shutdown completed");
        }else{
            logger.warn("Encountered problem(s) while shutting down executor service");
        }
    }

    public static boolean canIgnoreException(Exception e){
        if(e.getMessage() == null)
            return false;
        return e.getMessage().startsWith("An established") 
            || e.getMessage().startsWith("Socket is closed") 
            || e.getMessage().startsWith("Connection reset")
            || e.getMessage().startsWith("Read timed out")
            || e.getMessage().startsWith("Connection or outbound has closed")
            || e.getMessage().startsWith("readHandshakeRecord");
    }

    public static class Configuration{
        // parameters
        private int port = 5050;
        private File keyStoreFile;
        private String keyStorePassword;
        private boolean useDefaultHandlers = true;
        private boolean useSSL = false;
        private boolean useMultithread = false;
        private String homeDirectory = "./";
        private JarRegister extensions = new JarRegister();
        private boolean forceHttp2 = false;

        protected Configuration readonly(){
            Configuration conf = new Configuration();
            conf.port = port;
            conf.keyStoreFile = keyStoreFile;
            conf.keyStorePassword = keyStorePassword;
            conf.useDefaultHandlers = useDefaultHandlers;
            conf.useSSL = useSSL;
            conf.useMultithread = useMultithread;
            conf.homeDirectory = homeDirectory;
            conf.extensions = extensions;
            conf.forceHttp2 = forceHttp2;
            return conf;
        }

        public int getPort() {
            return port;
        }

        public File getKeyStoreFile() {
            return keyStoreFile;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public boolean useDefaultHandlers() {
            return useDefaultHandlers;
        }

        public boolean useSSL() {
            return useSSL;
        }

        public boolean useMultithread(){
            return useMultithread;
        }

        public String getHomeDirectory() {
            return homeDirectory;
        }

        public List<File> getExternalHandlers(){
            return Collections.unmodifiableList(extensions.get());
        }

        public boolean forceHttp2(){
            return forceHttp2;
        }
    }
    public static class Builder extends Configuration{
        /**
         * Web server port
         */
        public Builder setPort(int port) {
            super.port = port;
            return this;
        }

        /**
         * Key Store File is used for SSL enabled servers.
         * use {@link Builder#setUseSSL(boolean)}
         */
        public Builder setKeyStoreFile(File keyStoreFile) {
            super.keyStoreFile = keyStoreFile;
            return this;
        }

        public Builder setKeyStorePassword(String keyStorePassword) {
            super.keyStorePassword = keyStorePassword;
            return this;
        }

        /**
         * Default handlers are defined inside com.vincentcodes.webserver.handler.defaults
         * @see com.vincentcodes.webserver.handler.listeners.DefaultHandler
         */
        public Builder setUseDefaultHandlers(boolean useDefaultHandlers){
            super.useDefaultHandlers = useDefaultHandlers;
            return this;
        }

        /**
         * To enable ssl / https, you must provide KeyStore file and its password
         * @see Builder#setKeyStoreFile(File)
         * @see Builder#setKeyStorePassword(String)
         */
        public Builder setUseSSL(boolean useSSL){
            super.useSSL = useSSL;
            return this;
        }

        public Builder setUseMultithread(boolean useMultithread){
            super.useMultithread = useMultithread;
            return this;
        }

        public Builder setHomeDirectory(String homeDirectory) {
            if(!homeDirectory.endsWith("/"))
                homeDirectory = homeDirectory + "/";
                super.homeDirectory = homeDirectory;
            return this;
        }

        public Builder addExternalHandler(File file){
            super.extensions.add(file);
            return this;
        }

        public Builder setForceHttp2(boolean forceHttp2){
            super.forceHttp2 = forceHttp2;
            return this;
        }

        public WebServer build() throws IOException{
            return new WebServer(readonly());
        }
    }
}