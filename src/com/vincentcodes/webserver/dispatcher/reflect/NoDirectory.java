package com.vincentcodes.webserver.dispatcher.reflect;

import java.io.File;

import com.vincentcodes.webserver.util.FileControl;

public class NoDirectory implements ConditionalWrapper {

    @Override
    public boolean evaluate(DispatcherEnvironment env) {
        File file = FileControl.get(env.getRequest(), env.getServerConfig());
        return file.isFile();
    }
    
}
