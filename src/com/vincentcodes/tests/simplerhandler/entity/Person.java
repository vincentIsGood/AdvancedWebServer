package com.vincentcodes.tests.simplerhandler.entity;

import com.vincentcodes.json.annotation.JsonSerializable;

@JsonSerializable
public class Person {
    public String name;
    public int age;
    public String desc;

    public Person(){}

    public Person(String name, int age, String desc) {
        this.name = name;
        this.age = age;
        this.desc = desc;
    }

    public String toString(){
        return String.format("{name: %s, age: %d, desc: %s}", name, age, desc);
    }
    
}
