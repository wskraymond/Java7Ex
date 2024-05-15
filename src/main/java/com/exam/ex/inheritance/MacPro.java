package com.exam.ex.inheritance;

import java.io.*;
public class MacPro extends Laptop {
public static void main(String[] args) {
new MacPro().crunch();
}
void crunch() { }
}
class Laptop {
void crunch() throws IOException { }
}