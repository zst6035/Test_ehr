package com.ehr.cases;

import com.alibaba.fastjson.JSONObject;
import com.ehr.config.TestConfig;
import com.ehr.utils.DatabaseUtil;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

//组织员工-任职管理
public class ehr_employ {
    @BeforeTest
    public void beforeTest() throws IOException {
        TestConfig.defaultHttpClient=new DefaultHttpClient();
        TestConfig.store=new BasicCookieStore();
        TestConfig.session= DatabaseUtil.getSqlSession();
        String cookie_value=TestConfig.session.selectOne("com.ehr.flowes.selvalue","cookie");
        //  System.out.println(cookie_value);
        BasicClientCookie basicClientCookie=new BasicClientCookie("EHR_COOKIES20190505ERWFSDF",cookie_value);
        TestConfig.store.addCookie(basicClientCookie);
        TestConfig.url=TestConfig.session.selectOne("com.ehr.flowes.selvalue","ip_test");
        basicClientCookie.setDomain(TestConfig.url);
        //basicClientCookie.setDomain("tehr.mandao.com");
        basicClientCookie.setPath("/");
        TestConfig.defaultHttpClient.setCookieStore(TestConfig.store);

    }

    //offer管理
    //1.offer查看,待发放，已发放，已接受，已拒绝，已入职
    @Test
    public void offer() throws Exception{
        String url=TestConfig.session.selectOne("selUrl","任职记录-offer查看");
         String body= TestConfig.session.selectOne("selBody","任职记录-offer查看");

        JSONObject jsonObject=JSONObject.parseObject(body);
        int[] status={1,2,3,4,5};
        for(int i=0;i<status.length;i++){
            jsonObject.put("status",status[i]);
            String result=TestConfig.getResult(url,body);
            System.out.println(status[i]);
            System.out.println(result);
            Assert.assertEquals("操作成功",result);
        }


    }
    //2.offer详情查看，直接走url;

//3.offer新建
    @Test
    public void OfferAdd()throws Exception{
        String url=TestConfig.session.selectOne("selUrl","任职管理-offer新建");
        String body= TestConfig.session.selectOne("selBody","任职管理-offer新建");
        JSONObject jsonObject=JSONObject.parseObject(body);
         //基本信息定义与替换
        String userName=TestConfig.getChineseName(1);
        String personalEmail=TestConfig.getEmail(5,19);
        String mobile=TestConfig.getTel();
        String idCardNo=TestConfig.getString(10);
        String idCardNocheck=TestConfig.getString(10);
        //替换
        jsonObject.put("userName",userName);
        jsonObject.put("personalEmail",personalEmail);
        jsonObject.put("mobile",mobile);
        jsonObject.put("idCardNo",idCardNo);
        jsonObject.put("idCardNocheck",idCardNocheck);
        String result=TestConfig.getResult(url,jsonObject.toString());
        Assert.assertEquals("新建成功",result);





    }
}
