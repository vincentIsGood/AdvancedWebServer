package com.vincentcodes.tests.others;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.vincentcodes.tests.utils.RequestGenerator;
import com.vincentcodes.webserver.helper.ReadUntilResult;
import com.vincentcodes.webserver.helper.TextBinaryInputStream;

@DisplayName("Testing class TextBinaryInputStream")
public class TextBinaryInputStreamTest {

    @Test
    public void normal_inputstream_read() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            tbis.readLine(); // skip a line

            byte[] bytes = new byte[15];
            int bytesRead = tbis.read(bytes);
            assertEquals(bytesRead, 15);
            assertEquals(new String(bytes), "Host: 127.0.0.1");
        }
    }

    @Test
    public void read_simple_request() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.generateRequest("/asd").getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            List<String> finalResult = new ArrayList<>();
            String line = null;
            while((line = tbis.readLine()) != null){
                finalResult.add(line);
            }
            assertLinesMatch(List.of(
                "GET /asd HTTP/1.1",
                "Host: 127.0.0.1:5050",
                "User-Agent: curl/7.58.0",
                "Accept: */*",
                ""
            ), finalResult);
        }
    }
    
    @Test
    public void read_complex_request() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            List<String> finalResult = new ArrayList<>();
            String line = null;
            while((line = tbis.readLine()) != null){
                finalResult.add(line);
            }
            assertLinesMatch(List.of(
                "GET /post/index.html HTTP/1.1",
                "Host: 127.0.0.1:5050",
                "Connection: keep-alive",
                "Upgrade-Insecure-Requests: 1",
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36",
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
                "Sec-GPC: 1",
                "Sec-Fetch-Site: none",
                "Sec-Fetch-Mode: navigate",
                "Sec-Fetch-User: ?1",
                "Sec-Fetch-Dest: document",
                "Accept-Encoding: gzip, deflate, br",
                "Accept-Language: en-US,en;q=0.9",
                ""
            ), finalResult);
        }
    }

    @Test
    public void read_until_character_eol() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        String expectedResult = 
            "GET /post/index.html HTTP/1.1\r\n"
            + "Host: 127.0.0.1:5050\r\n"
            + "Connection: keep-alive\r\n"
            + "Upgrade-Insecure-Requests: 1\r\n"
            + "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36\r";

        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            String finalResult = tbis.readUntil('\n', 4);
            assertEquals(expectedResult, finalResult);
        }
    }

    @Test
    public void read_until_string_user_agent() throws Exception{
        InputStream is = new ByteArrayInputStream(RequestGenerator.GET.standardGet().getBytes());
        String expectedResult = 
            "GET /post/index.html HTTP/1.1\r\n"
            + "Host: 127.0.0.1:5050\r\n"
            + "Connection: keep-alive\r\n"
            + "Upgrade-Insecure-Requests: 1\r\n";

        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            String finalResult = tbis.readUntil("User-Agent");
            assertEquals(expectedResult, finalResult);
        }
    }

    @Test
    public void read_until_bytes_length_simple() throws Exception{
        String sentence = "Just read this long sentence.";
        InputStream is = new ByteArrayInputStream(sentence.getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            ReadUntilResult result;
            result = tbis.readBytesUntil("sentence", 8);
            assertEquals("Just rea", new String(result.data()));
            result = tbis.readBytesUntil("sentence", 8);
            assertEquals("d this l", new String(result.data()));
            result = tbis.readBytesUntil("sentence", 8);
            assertEquals("ong ", new String(result.data()));
            assertTrue(result.isMatchingStrFound());
            result = tbis.readBytesUntil("sentence", 8);
            assertEquals(".", new String(result.data()));
        }
    }

    @Test
    public void read_until_bytes_length_with_edge_case() throws Exception{
        String sentence = "Just read this long sent_nce. sentence";
        InputStream is = new ByteArrayInputStream(sentence.getBytes());
        try(TextBinaryInputStream tbis = new TextBinaryInputStream(is)){
            ReadUntilResult result;
            result = tbis.readBytesUntil("sentence", 8);
            assertEquals("Just rea", new String(result.data()));
            result = tbis.readBytesUntil("sentence", 8);
            assertEquals("d this l", new String(result.data()));
            result = tbis.readBytesUntil("sente", 5);
            assertEquals("ong s", new String(result.data()));
            result = tbis.readBytesUntil("sente", 5);
            assertEquals("ent_n", new String(result.data()));
            result = tbis.readBytesUntil("sente", 15);
            assertEquals("ce. ", new String(result.data()));
            assertTrue(result.isMatchingStrFound());
            result = tbis.readBytesUntil("asd", 15);
            assertEquals("nce", new String(result.data()));
            result = tbis.readBytesUntil("asd", 15);
            assertNull(result.data());
        }
    }
}
