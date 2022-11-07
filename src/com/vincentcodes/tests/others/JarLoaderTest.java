package com.vincentcodes.tests.others;

import static org.junit.Assert.assertTrue;

import java.io.File;

import com.vincentcodes.webserver.helper.loader.JarLoader;
import com.vincentcodes.webserver.helper.loader.JarRegister;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class JarLoader")
public class JarLoaderTest {
    JarLoader loader;
    JarRegister register;

    @BeforeAll
    public void setup(){
        register = new JarRegister();
        loader = new JarLoader(register);

        File jar = new File("D:/Downloads_D/zPrograms/Java/0_OwnProjects/0_SmallPrograms/AdvancedWebServer/extensions/httpserver-hls-test.jar");
        register.add(jar);
    }

    @Test
    public void testJarLoader() throws Exception{
        assertTrue(loader.loadJars().size() == 1);
    }
}
