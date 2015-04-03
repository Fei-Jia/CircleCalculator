package com.feijia.circlecalculator;

public class EquationParse {
    public static String Parse (String s, boolean Radian){
        String ss = new String(s);
        ss=ss.replace("×", "*");
        ss=ss.replace("\u00B3\u221A", "cbrt");  //Unicode for cube root: we don't use \u221B the real one because some phones don't show
        ss=ss.replace("√", "sqrt");           //Cubic root must be parsed before square root because they both have unicode \u221A
        ss=ss.replace("%", "*0.01");
        ss=ss.replace("E", "*1E");
        ss=ss.replace("ln", "log");
        ss=ss.replace("lg2", "log2");
        ss=ss.replace("lg10", "log10");
        ss=ss.replace("°", "*0.017453292519943295");
        if(!Radian){
        	ss=ss.replace("asin", "57.29577951308232*asin");
        	ss=ss.replace("acos", "57.29577951308232*acos");
        	ss=ss.replace("atan", "57.29577951308232*atan");
        }
        return ss;
    }
}