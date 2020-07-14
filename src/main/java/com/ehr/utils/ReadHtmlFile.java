package com.ehr.utils;

import com.ehr.config.TestConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ReadHtmlFile {
    public static void main(String []age)throws Exception{
      String strArray=null;
       strArray=getFileToString(TestConfig.orderByDate());
        System.out.println(strArray);




    }
    //传入一个文件，返回一个文件array
    public static String getFileToString(File file) throws Exception {
        ArrayList<String> strArray = new ArrayList<>();
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line = "";
        line = br.readLine();
        while (line != null) {
            strArray.add(line);
            line = br.readLine();
        }
     return strArray.toString();

    }



}
