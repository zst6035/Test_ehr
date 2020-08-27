package com.ehr.cases;

import com.alibaba.fastjson.JSONObject;
import com.ehr.config.TestConfig;
import com.ehr.model.ehrutil;
import com.ehr.utils.DatabaseUtil;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;

public class ehr_people {


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
    //新增在职人员
    @Test(description = "在职人员新增")
    public void userInfoAdd()throws Exception{
        String url= TestConfig.session.selectOne("selUrl","在职人员-新增");
        String body= TestConfig.session.selectOne("selBody","在职人员-新增");
     //   System.out.println(body);
        JSONObject jsonObject=JSONObject.parseObject(body);
        JSONObject ehrUserDTO=jsonObject.getJSONObject("ehrUserDTO");
       //用户信息
       String  userName=TestConfig.getChineseName(1);
        String nickName=TestConfig.getString(4);
        //数据库获取编号
       String  code=TestConfig.session.selectOne("selvalue","jobcode");
       int i=Integer.parseInt(code);
        String jobCode="bf"+i;
        i=i-1;
        System.out.println(jobCode);
        System.out.println(i);
        ehrutil ehrutil1=new ehrutil(1,"jobcode",String.valueOf(i));
        TestConfig.session.update("UpdateJobcode",ehrutil1);

        String email=nickName+"@baofu.com";
        String personalEmail=TestConfig.getEmail(6,9);
        String identifyCode=nickName;
        String mobile=TestConfig.getTel();
        String idCardNo=TestConfig.getString(10);
        //替换ehrUserDTO中某些信息

        ehrUserDTO.put("userName",userName);
        ehrUserDTO.put("nickName",nickName);
        ehrUserDTO.put("jobCode",jobCode);
        ehrUserDTO.put("email",email);
        ehrUserDTO.put("personalEmail",personalEmail);
        ehrUserDTO.put("identifyCode",identifyCode);
        ehrUserDTO.put("mobile",mobile);
        ehrUserDTO.put("idCardNo",idCardNo);
        jsonObject.put("ehrUserDTO",ehrUserDTO);
        //替换body
        body=jsonObject.toString();
      //  System.out.println(body);
        String result=TestConfig.getResult(url,body);
        Assert.assertEquals(result,"新建成功");

    }

    @Test(description = "实习生添加")
    public void  traineeAdd() throws Exception{
        String url= TestConfig.session.selectOne("selUrl","实习生-添加");
        String body= TestConfig.session.selectOne("selBody","实习生-添加");
        JSONObject jsonObject=JSONObject.parseObject(body);
        JSONObject ehrUserDTO=jsonObject.getJSONObject("ehrUserDTO");
        //用户信息
        String  userName=TestConfig.getChineseName(1);
        String nickName=TestConfig.getString(4);
        //jobecode是一个序列，不能重复，所以这里要从数据库里取，然后再改掉
        String  code=TestConfig.session.selectOne("selvalue","jobcode");
        int i=Integer.parseInt(code);
        String jobCode="bf"+i;
        System.out.println(jobCode);
        i=i-1;
        ehrutil ehrutil1=new ehrutil(1,"jobcode",String.valueOf(i));
        TestConfig.session.update("UpdateJobcode",ehrutil1);
        String email=nickName+"@baofu.com";
        String personalEmail=TestConfig.getEmail(6,9);
        String identifyCode=nickName;
        String mobile=TestConfig.getTel();
        String idCardNo=TestConfig.getString(10);
        //替换ehrUserDTO中某些信息
        ehrUserDTO.put("userName",userName);
        ehrUserDTO.put("nickName",nickName);
        ehrUserDTO.put("jobCode",jobCode);
        ehrUserDTO.put("email",email);
        ehrUserDTO.put("personalEmail",personalEmail);
        ehrUserDTO.put("identifyCode",identifyCode);
        ehrUserDTO.put("mobile",mobile);
        ehrUserDTO.put("idCardNo",idCardNo);
        jsonObject.put("ehrUserDTO",ehrUserDTO);
        //替换body
        body=jsonObject.toString();
        //  System.out.println(body);
        String result=TestConfig.getResult(url,body);
        Assert.assertEquals(result,"新建成功");

    }
//搜索在职人员



    //

}
