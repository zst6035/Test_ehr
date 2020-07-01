package com.ehr;

import com.ehr.config.TestConfig;
import com.ehr.utils.DatabaseUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String [] args)throws Exception{
        List<String> emaillist=new ArrayList<>();
        TestConfig.session=DatabaseUtil.getSqlSession();
        String s= TestConfig.session.selectOne("selvalue","useremails");
        System.out.println(s);
        String [] ss=s.split(",");
        for(int i=0;i<ss.length;i++){
            System.out.println(ss[i]);
        }
        File file=TestConfig.orderByDate();
       TestConfig.sendMail(ss,file);
    }
}
