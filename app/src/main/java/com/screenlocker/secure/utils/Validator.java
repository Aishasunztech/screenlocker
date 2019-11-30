package com.screenlocker.secure.utils;

public class Validator {

    public static boolean validAndMatch(String p1,String p2) {

        if (p1.length()!=0 && p2.length()!=0){
            return p1.equals(p2);
        }else {
            return false;
        }
    }
}
