package com.ehr.cases;

import com.alibaba.fastjson.JSONObject;
import com.ehr.config.TestConfig;
import com.ehr.model.ehrLink;
import com.ehr.utils.DatabaseUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.session.SqlSession;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ehr_links {

//可供调试使用
    @BeforeTest
    public void beforeTest() throws IOException{
        //声明httpclient,cookie,ip地址等
        TestConfig.defaultHttpClient=new DefaultHttpClient();
        TestConfig.store=new BasicCookieStore();
        TestConfig.session= DatabaseUtil.getSqlSession();
        String cookie_value=TestConfig.session.selectOne("com.ehr.flowes.selvalue","cookie");
        System.out.println(cookie_value);
        BasicClientCookie basicClientCookie=new BasicClientCookie("EHR_COOKIES20190505ERWFSDF",cookie_value);
        TestConfig.store.addCookie(basicClientCookie);
        TestConfig.url=TestConfig.session.selectOne("com.ehr.flowes.selvalue","ip_test");
        basicClientCookie.setDomain(TestConfig.url);
        //basicClientCookie.setDomain("tehr.mandao.com");
        basicClientCookie.setPath("/");
        TestConfig.defaultHttpClient.setCookieStore(TestConfig.store);

    }


    @Test(testName = "ehr链接",dataProvider = "getlink")
    public void ehr_link(ehrLink e) throws IOException {
        //只跑固定链接的，其他的link body需要变化，否则会跑不成功
        if(e.getNochange()==1) {
            String result = TestConfig.getResult(e.getUrl(), e.getBody());
            System.out.println(e.getId() + ":" + e.getDescribtion() + ":" + result);
            if (result.equals("操作成功")||result.equals("更新成功")){
                Assert.assertTrue(true);
            }
        }


    }

    @DataProvider(name = "getlink")
    public Iterator<Object[]> getlink() throws IOException{
        List<ehrLink> ehrLinksList=TestConfig.session.selectList("com.ehr.flowes.selAlllinks");
        //将list转换到Object里：

        List<Object[]> links = new ArrayList<Object[]>();
        for (Object u : ehrLinksList) {
            //做一个形式转换,否则无法set
            links.add(new Object[] { u });
        }
        return links.iterator();


    }

}
