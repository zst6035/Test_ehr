package com.ehr.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

//获取最新的测试报告
public class FileList {

    public static File orderByDate() throws Exception{
        //获取文件夹路径
        File file = new File("");
        String filePath=file.getCanonicalPath()+"\\test-output";

        File file1=new File(filePath);
       File[] files=file1.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }
        });
        System.out.println(files[files.length-1].getName());
        return files[files.length-1];

    }
    public static void main(String [] args)throws Exception{
      File file=  orderByDate();

    }
}
