package com.ehr.config;

import com.alibaba.fastjson.JSONObject;
import com.ehr.utils.DatabaseUtil;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.session.SqlSession;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;


//可以看到变量名都是静态的全局变量，不需要new对象；
@Data
public class TestConfig {

    //公共变量
    public static  String url;
    public static  DefaultHttpClient defaultHttpClient ;
    public static  CookieStore store;
    public static SqlSession session;


    //公共方法,获取请求结果 状态message
    public static  String getResult(String url,String body)throws IOException {

        HttpPost post=new HttpPost("http://"+ TestConfig.url+url);
        post.setHeader("Content-Type", "application/json;charset=utf-8");
        //  System.out.println(body);
        post.setEntity(new StringEntity(body));
        HttpResponse response=TestConfig.defaultHttpClient.execute(post);
        String  result= EntityUtils.toString(response.getEntity(),"utf-8");
        JSONObject resultJson =JSONObject.parseObject(result);
        // System.out.println(resultJson.get("message"));
        String result2=(String) resultJson.get("message");
        return result2;
    }

//公共方法，获取请求结果，以便截取，替换
    public static  String getResponse(String url,String body)throws IOException{
    HttpPost post=new HttpPost("http://"+ TestConfig.url+url);
    post.setHeader("Content-Type", "application/json;charset=utf-8");
    post.setEntity(new StringEntity(body));
    HttpResponse response=TestConfig.defaultHttpClient.execute(post);
    String  result= EntityUtils.toString(response.getEntity(),"utf-8");
    return result;
}

//获取最新的测试报告
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
//发送带附件的邮件
public static void sendMail(String[] tos,File file) throws Exception {
    //获得session对象
    final Properties props = new Properties();

    //下面两段代码是设置ssl和端口，不设置发送不出去。
    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    props.setProperty("mail.smtp.socketFactory.port", "465");
    // 发送服务器需要身份验证
    props.setProperty("mail.transport.protocol", "smtp");// 发送邮件协议名称
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.host", "smtp.qq.com");//QQ邮箱的服务器 如果是企业邮箱或者其他邮箱得更换该服务器地址
    // 发件人的账号
    props.put("mail.user", "1148744992@qq.com");
    // 访问SMTP服务时需要提供的密码 
    props.put("mail.password", "nejjmhrmkkjnhffg");
    // 构建授权信息，用于进行SMTP进行身份验证
    Authenticator authenticator = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            // 用户名、密码
            String userName = props.getProperty("mail.user");
            String password = props.getProperty("mail.password");
            //发件邮箱
            return new PasswordAuthentication(userName, password);
        }
    };
    //上面的都是基础设置



    Session se = Session.getInstance(props, authenticator);
    //创建一个表示邮件的对象message
    Message mes = new MimeMessage(se);
    try {
        mes.setFrom(new InternetAddress("1148744992@qq.com"));//发件人
        //定义收件人地址为一个数组，可以添加多个收件人
        Address[] address=new Address[tos.length];
        for (int i = 0; i < tos.length; i++) {
            address[i] = new InternetAddress(tos[i]);
        }
        mes.setRecipients(Message.RecipientType.TO, address);
        mes.setSubject("ehr测试报告");//主题
        //设置邮件内容
        // mes.setContent("你好啊，大家", "text/html;charset=UTF-8");
        Multipart multipart = new MimeMultipart();
        // 设置邮件的文本内容
        BodyPart contentPart = new MimeBodyPart();
        contentPart.setText("Hello,附件为ehr测试报告，请查收！");
        multipart.addBodyPart(contentPart);
        //声明附件地址
//            File file = new File("");
//            String filePath = file.getCanonicalPath();
//            String affix=filePath+"\\test-output\\202006301304.html";
//            //声明附件名称
//            String affixName="测试报告";
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(file);
        messageBodyPart.setDataHandler(new DataHandler(source));

        sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();


//           设置后，附件不会出现乱码
        messageBodyPart.setFileName("=?UTF-8?B?"
                + enc.encode(file.getName().getBytes("utf-8")) + "?=");

        multipart.addBodyPart(messageBodyPart);
        mes.setContent(multipart);
        mes.saveChanges();
        //发送邮件transport
        Transport.send(mes);
    } catch (AddressException e) {
        e.printStackTrace();
    } catch (MessagingException e) {
        e.printStackTrace();
    }
}
}
