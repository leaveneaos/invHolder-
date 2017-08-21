package com.rjxx.taxeasy.utils;

import java.util.Random;

/**
 * Created by Administrator on 2017/8/21 0021.
 */
public class NumberUtil {

    public static char getRandomLetter(){
        String chars = "abcdefghijklmnopqrstuvwxyz";
        return chars.charAt(new Random().nextInt(26));
    }
}
