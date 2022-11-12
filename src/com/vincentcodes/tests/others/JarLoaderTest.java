package com.vincentcodes.tests.others;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.vincentcodes.webserver.helper.loader.JarLoader;
import com.vincentcodes.webserver.helper.loader.JarRegister;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class JarLoader")
public class JarLoaderTest {
    JarLoader loader;
    JarRegister register;

    @BeforeAll
    public void setup(){
        register = new JarRegister();
        loader = new JarLoader(register);

        // we may not have the extensions on Github. Can't test that.
        File jar = new File("./extensions/httpserver-hls-test.jar");
        if(jar.exists())
            register.add(jar);
    }

    @Test
    public void testJarLoader() throws Exception{
        loader.loadJars().size(); // make sure we have no errors
        // assertTrue(loader.loadJars().size() >= 1);
    }
}
