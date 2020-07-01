package com.ehr.cases;

import com.ehr.config.TestConfig;
import com.ehr.utils.DatabaseUtil;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.ibatis.session.SqlSession;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class SuiteCases {
    @BeforeSuite
    public void beforeSuite()throws Exception{
        //声明session ,httpclient,cookie,ip地址等
        TestConfig.defaultHttpClient=new DefaultHttpClient();
        TestConfig.store=new BasicCookieStore();
        TestConfig.session= DatabaseUtil.getSqlSession();
        String cookie_value= TestConfig.session.selectOne("selvalue","cookie");
      //  System.out.println(cookie_value);
        Reporter.log(cookie_value);
        BasicClientCookie basicClientCookie=new BasicClientCookie("EHR_COOKIES20190505ERWFSDF",cookie_value);
        TestConfig.store.addCookie(basicClientCookie);
        TestConfig.url= TestConfig.session.selectOne("selvalue","ip_test");
        basicClientCookie.setDomain(TestConfig.url);
        //basicClientCookie.setDomain("tehr.mandao.com");
        basicClientCookie.setPath("/");
        TestConfig.defaultHttpClient.setCookieStore(TestConfig.store);
    }

    @AfterSuite
    public void afterSuite()throws Exception{
        //关闭连接
        TestConfig.session.close();
    }
}
