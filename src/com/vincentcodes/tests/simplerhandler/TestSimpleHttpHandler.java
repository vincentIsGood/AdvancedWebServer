package com.vincentcodes.tests.simplerhandler;

import java.io.File;

import com.vincentcodes.json.CannotMapFromObjectException;
import com.vincentcodes.tests.simplerhandler.entity.Person;
import com.vincentcodes.webserver.annotaion.SimplerHttpHandler;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.request.RequestParam;
import com.vincentcodes.webserver.annotaion.response.JsonResponse;

@SimplerHttpHandler
public class TestSimpleHttpHandler {
    @RequestMapping("/readme.txt")
    public String handleSomethingMethod(String param1, @RequestParam("get") double get, @RequestParam(value = "json", payloadType = RequestParam.Type.JSON) Person person){
        return String.format("%s %f %s", param1, get, person);
    }

    @JsonResponse
    @RequestMapping("/others")
    public Person returnsJson(@RequestParam(value = "some", nullable = true) String shouldBeNull) throws CannotMapFromObjectException{
        System.out.println("Null? " + shouldBeNull);
        return new Person("vincent", 19, "I love anime");
    }

    @JsonResponse
    @RequestMapping("/others_array")
    public Person[] returnsJsonArray() throws CannotMapFromObjectException{
        return new Person[]{new Person("vincent", 19, "I love anime")};
    }

    @JsonResponse
    @RequestMapping("/null")
    public Person[] returnNullAsValue() throws CannotMapFromObjectException{
        return null;
    }

    @JsonResponse
    @RequestMapping("/unknown_object")
    public File returnUnknownObject() throws CannotMapFromObjectException{
        return new File("./");
    }
}
