package com.ehr.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ehr.config.TestConfig;
import com.ehr.model.ehrcalendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Test01 {
    public static void main(String[] args)throws Exception {
String aa=getString();
        System.out.println(aa);


    }

    public static String getString(){
      String  base = "abcdefghijklmnopqrstuvwxyz0123456789";
      int length=getNum(5,6);
      int index=getNum(0,base.length()+1);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = (int)(Math.random()*base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
    public static int getNum(int start,int end) {
        return (int)(Math.random()*(end-start+1)+start);
    }
}
