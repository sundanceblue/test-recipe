package com.roger;

public class A {

    public int x;
    public String y;

    public void foo(){x=1;}
    private String foo(B b) { b.hello(); goodBye(); return ""; }
    public void goodBye() {foo(); System.out.println("goodbye"); }
}