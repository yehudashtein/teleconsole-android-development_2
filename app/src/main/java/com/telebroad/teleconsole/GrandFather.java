package com.telebroad.teleconsole;

import java.util.ArrayList;
import java.util.List;

public class GrandFather {
    private father  father;
    private String name;
    public GrandFather(String granFatherName, String Fathename, List<String> childName){
        this.name = granFatherName;
        this.father= new father(Fathename,childName);
    }
    public class father{
        String name;
        List<Children> children;
        public father(String name,List<String> childName){
            this.name = name;
            this.children = new ArrayList<>();
            for (String c:childName){
                this.children.add(new Children(c));
            }

        }

        public class Children {
            String name;
            public Children(String name){
                this.name = name;
            }
        }
    }
}
