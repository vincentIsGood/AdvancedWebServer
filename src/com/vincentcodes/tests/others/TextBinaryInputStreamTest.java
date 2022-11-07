package com.vincentcodes.tests.others;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vincentcodes.tests.utils.RequestGenerator;
import com.vincentcodes.webserver.helper.TextBinaryInputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testing class TextBinaryInputStream")
public class TextBinaryInputStreamTest {
    @Test
    public void read_simple_request() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.generateRequest("/asd").getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            String line = null;
            while((line = tbis.readLine()) != null){
                System.out.println(line);
            }
        }
    }
    
    @Test
    public void read_complex_request() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            String line = null;
            while((line = tbis.readLine()) != null){
                System.out.println(line);
            }
        }
    }

    @Test
    public void read_until_character_eol() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            System.out.println(tbis.readUntil('\n', 4));
        }
    }

    @Test
    public void read_until_string_user_agent() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            System.out.println(tbis.readUntil("User-Agent"));
        }
    }

    @Test
    public void read_until_bytes_array_read() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            System.out.println("Line: " + tbis.readLine());
            byte[] bytes = new byte[15];
            int bytesRead = tbis.read(bytes);
            System.out.println(bytesRead + " '" + new String(bytes) + "'");
        }
    }
}
