package com.ehr.cases;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ehr.config.TestConfig;
import com.ehr.model.ehrcalendar;
import com.ehr.utils.DatabaseUtil;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
* 当前用户
* 补签
* 加班
* 请假
* */
public class ehr_attendance  {
  public   List<Integer> idlist=new ArrayList<>();
  public List<String> taskid=new ArrayList<>();
    Map<Integer,String> map=new HashMap<>();

    //存储我的待办，然后批量审批
    JSONObject jsonObject3;


    //存储可以直接发起加班撤销的加班数据=审批已通过
    JSONArray overTimeArray=new JSONArray();


    //存储可以直接发起销假的请假数据
  JSONArray jsonmyVacatoinArray=new JSONArray();


//供调试单个用例执行时使用
    @BeforeTest
    public void beforeTest() throws IOException{
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
    //获取考勤异常数据
    @Test(description = "获取异常考勤数据")
    public void findErroAttendace() throws IOException{
       // SqlSession session=DatabaseUtil.getSqlSession();
        String url= TestConfig.session.selectOne("com.ehr.flowes.selUrl","我的考勤-异常");
        String body= TestConfig.session.selectOne("com.ehr.flowes.selBody","我的考勤-异常");
        //需要替换body中考勤期间；读取数据库吧，判断逻辑写起来有点费事
        JSONObject body1=JSONObject.parseObject(body);
        String startTime=TestConfig.session.selectOne("selvalue","startTime");
        String endTime=TestConfig.session.selectOne("selvalue","endTime");
        body1.put("cardDateStart",startTime);
        body1.put("cardDateEnd",endTime);
       String result=TestConfig.getResponse(url,body1.toString());
        Reporter.log("异常数据请求结果为"+result);
        //System.out.println(result);
       JSONObject jsonObject=JSONObject.parseObject(result);
       //获取data是一个json字符串；不是一个list,因为没有[]
      JSONObject data=jsonObject.getJSONObject("data");
        //list是一个jsonArray
            JSONArray list= data.getJSONArray("list");
            //用于存储考勤状态为异常的id
        //    List<Integer> idlist=new ArrayList<>();
            for(int j=0;j<list.size();j++){
                //考勤状态
                // String attendanceStatusName=list.getJSONObject(j).getString("attendanceStatusName");
                //补签状态
                String checkStatusName=list.getJSONObject(j).getString("checkStatusName");
                //缺勤时长
                String absenceDurationStr=list.getJSONObject(j).getString("absenceDurationStr");
                //请假备注,后面请假需要没有请假的
                String effectRemarkVacation=list.getJSONObject(j).getString("effectRemarkVacation");
                //获取考勤状态是异常，且补签状态不在审批中的，且缺勤时长是8小时0分钟的
                if(!(checkStatusName.equals("审批中"))&&absenceDurationStr.equals("8小时0分钟")&&effectRemarkVacation.equals("")){
                    int id=list.getJSONObject(j).getInteger("id");
                    String cardDate=list.getJSONObject(j).getString("cardDate");
                    map.put(id,cardDate);
                //将id放入list中
                idlist.add(id);
                }
            }
        System.out.println("考勤异常数据的id:"+idlist);
        System.out.println(idlist.size());
        if(idlist.size()>=3){
            Assert.assertTrue(true);
        }else {
            Assert.assertTrue(false);
        }
        System.out.println(map);
    }


    //补签，针对考勤异常的数据才能发起补签，补签只需要一个异常考勤的id就可以
    @Test(dependsOnMethods = {"findErroAttendace"},description = "补签")
    public void fillCheckApply() throws IOException{
        Reporter.log("获取补签url");
        //获取补签url
        String url= TestConfig.session.selectOne("selUrl","补签");
        String body= TestConfig.session.selectOne("selBody","补签");
        Reporter.log(url+body);
        //将string转换为json;
        JSONObject jsonObject=JSONObject.parseObject(body);
     //   int id= jsonObject.getInteger("id");
        //替换id的值，取第一个异常的考勤数据
        jsonObject.put("id",idlist.get(0));
        System.out.println(jsonObject.get("id"));
        body=jsonObject.toString();
        System.out.println(url+":"+body);
       String result= TestConfig.getResult(url,body);
       Assert.assertEquals(result,"操作成功");
    }

     //请假，需要请假日期，请假开始，结束时间
    @Test(dependsOnMethods = {"findErroAttendace"},description = "请假")
    public void vacationApply() throws IOException{

        String url= TestConfig.session.selectOne("selUrl","请假");
        String body= TestConfig.session.selectOne("selBody","请假");
        System.out.println(url+":"+body);
        JSONObject jsonObject=JSONObject.parseObject(body);
        //首先获取考勤异常id,然后选择第二条，尽量已补签的数据不再请假
        int id=idlist.get(1);
        //再通过考勤异常的id找到对应的考勤异常日期
        String date=map.get(id);
        //时间拼接，注意前面的空格，同时需要发起当前用户的请假
        jsonObject.put("vacationStartTime",date+" 09:00");
        jsonObject.put("vacationEndTime",date+" 18:00");
        jsonObject.put("userId", TestConfig.userId);
        body=jsonObject.toString();
       String result=TestConfig.getResult(url,body);
       Assert.assertEquals(result,"操作成功");

    }

    //加班
    @Test(description = "加班")
    public void overtimeApply() throws Exception{
        String url=TestConfig.session.selectOne("selUrl","工作日加班");
        String body=TestConfig.session.selectOne("selBody","工作日加班");
        //转换成json格式，替换其中的数据
        JSONObject body1=JSONObject.parseObject(body);
        body1.put("userId", TestConfig.userId);
        //提交当月第一天的加班，不过当月第一天有可能是双休日，节假日，先不考虑那么细了
        //应该将考勤日历新建一个表放入数据库，根据表判断是否什么日子;在判断这个天加班是换调休还是加班费；
        //获取当月考勤第一天；
        String startTime=TestConfig.session.selectOne("selvalue","startTime");

        //获取当天加班的话，相关信息
        ehrcalendar ehrcalendar=TestConfig.session.selectOne("selDayMessage",startTime);
        //替换body中相关信息
        body1.put("overtimeType", ehrcalendar.getDtype());
        body1.put("workDate",startTime);
        body1.put("compensation",ehrcalendar.getCompensation());
        body1.put("compensationName",ehrcalendar.getCompensationName());
        String result=TestConfig.getResult(url,body1.toString());


        //如果已存在加班，则取下一天:
        while(result.equals("已存在审批中或审批通过的加班申请")){
           //string类型的转换成日期，然后再往上加一天
            startTime =TestConfig.getNextDate(startTime);
            ehrcalendar=TestConfig.session.selectOne("selDayMessage",startTime);
            //替换body中相关信息
            body1.put("overtimeType", ehrcalendar.getDtype());
            body1.put("workDate",startTime);
            body1.put("compensation",ehrcalendar.getCompensation());
            body1.put("compensationName",ehrcalendar.getCompensationName());
              result=TestConfig.getResult(url,body1.toString());
        }
        System.out.println("加班日期："+startTime);
          Assert.assertEquals(result,"操作成功");
    }

    //我的加班
    @Test(description = "我的加班-已审批")
    public void myOvertimeListPage()throws Exception{
        String url=TestConfig.session.selectOne("selUrl","我的加班-已审批");
        String body=TestConfig.session.selectOne("selBody","我的加班-已审批");
        String response=TestConfig.getResponse(url,body);
        System.out.println(response);
        JSONObject jsonObject1=JSONObject.parseObject(response);
        JSONObject data=jsonObject1.getJSONObject("data");
        JSONArray list=data.getJSONArray("list");


        //现在要从list中获取每个object;
           for(int i=0;i<list.size();i++){
           JSONObject jsonObject2= list.getJSONObject(i);
            JSONObject jsonOverTime=new JSONObject();
            jsonOverTime.put("id",jsonObject2.getInteger("id"));
            jsonOverTime.put("checkStatus",jsonObject2.getString("checkStatus"));
            jsonOverTime.put("overtimeType",jsonObject2.getString("overtimeType"));
            jsonOverTime.put("workDate",jsonObject2.getString("workDate"));
            jsonOverTime.put("startTime",jsonObject2.getString("startTime"));
            jsonOverTime.put("endTime",jsonObject2.getString("endTime"));
            jsonOverTime.put("overtimeHour",jsonObject2.getString("overtimeHour"));
            jsonOverTime.put("overtimeHourUnit",jsonObject2.getString("overtimeHourUnit"));
            jsonOverTime.put("reason",jsonObject2.getString("reason"));
            overTimeArray.add(jsonOverTime);
        }
        System.out.println("加班审批通过数据："+overTimeArray);
        if(overTimeArray.size()>=1){
            Assert.assertTrue(true);
        }else {
            Assert.assertTrue(false);
        }
    }

  //加班撤销，可以针对上面发起的加班进行撤销
    @Test(description = "加班撤销",dependsOnMethods = {"myOvertimeListPage"})
    public void OvertimeRevocation()throws Exception{
        String url=TestConfig.session.selectOne("selUrl","加班撤销");
        //任意选择一条加班审批通过的数据
        JSONObject jsonObject=overTimeArray.getJSONObject(TestConfig.intRandom(overTimeArray.size()));
        String result=TestConfig.getResult(url,jsonObject.toString());
      Assert.assertEquals(result,"操作成功");
    }


    //我的请假
    @Test(description = "我的请假-已审批")
        public void myVacatoinPage() throws IOException{
            String url=TestConfig.session.selectOne("selUrl","我的请假-已审批");
            String body=TestConfig.session.selectOne("selBody","我的请假-已审批");
            String response=TestConfig.getResponse(url,body);
            System.out.println(response);
            JSONObject jsonObject1=JSONObject.parseObject(response);
            JSONObject data=jsonObject1.getJSONObject("data");
            JSONArray list=data.getJSONArray("list");
            //现在要从list中获取每个object一些信息，并存入新的jsonObject,再将这个新的object存入array,这样array
            //就是需要的销假数据
            for(int i=0;i<list.size();i++){
                JSONObject jsonObject2= list.getJSONObject(i);
                JSONObject jsonObjectVacation=new JSONObject();
                jsonObjectVacation.put("id",jsonObject2.getInteger("id"));
                jsonObjectVacation.put("checkStatus",jsonObject2.getString("checkStatus"));
                jsonObjectVacation.put("vacationItemName",jsonObject2.getString("vacationItemName"));
                jsonObjectVacation.put("vacationItem",jsonObject2.getString("vacationItem"));
                jsonObjectVacation.put("vacationStartTime",jsonObject2.getString("vacationStartTime"));
                jsonObjectVacation.put("vacationEndTime",jsonObject2.getString("vacationEndTime"));
                jsonObjectVacation.put("vacationHourUnit",jsonObject2.getString("vacationHourUnit"));
                jsonObjectVacation.put("vacationHour",jsonObject2.getString("vacationHour"));
                jsonObjectVacation.put("businessAddress",jsonObject2.getString("businessAddress"));
                jsonObjectVacation.put("reason",jsonObject2.getString("reason"));
                jsonmyVacatoinArray.add(jsonObjectVacation);
            }
            System.out.println("请假审批通过数据："+jsonmyVacatoinArray);
        System.out.println("请假审批通过的条数为："+jsonmyVacatoinArray.size());
            if(jsonmyVacatoinArray.size()>=1){
                Assert.assertTrue(true);
            }else {
                Assert.assertTrue(false);
            }
        }


    //请假撤销
    @Test(description = "销假",dependsOnMethods = {"myVacatoinPage"})
    public void VacationRevocation()throws IOException{
        String url=TestConfig.session.selectOne("selUrl","销假");
        //任意选一条进行销假；
        int i=TestConfig.intRandom(jsonmyVacatoinArray.size());
        JSONObject jsonObject=jsonmyVacatoinArray.getJSONObject(i);
        String result=TestConfig.getResult(url,jsonObject.toString());
        Assert.assertEquals(result,"操作成功");



    }





    //审批-流程监控不含offer
    //此步骤需要获取taskid
  //  @Test(description = "流程监控",dependsOnMethods = {"vacationApply","fillCheckApply"})
    @Test(description = "流程监控")
    public void getAllPageTask () throws Exception{
        String url= TestConfig.session.selectOne("selUrl","流程监控");
        String body= TestConfig.session.selectOne("selBody","流程监控");
        JSONObject body1=JSONObject.parseObject(body);
        //替换请求，查询发起人为当前用户的
        body1.put("applyUserId",TestConfig.userId);
        String reponse=TestConfig.getResponse(url,body);
        //获取发起人id为当前用户的taskid，并存入数组；
        //1.将结果转换为json
        JSONObject jsonObject=JSONObject.parseObject(reponse);
        //提取list
        JSONObject jsonObject1=jsonObject.getJSONObject("data");
        JSONArray tasklist=jsonObject1.getJSONArray("list");
        System.out.println(""+tasklist.size());
        for(int i=0;i<tasklist.size();i++){
            String id=tasklist.getJSONObject(i).getString("taskId");
            System.out.println(i);
            taskid.add(id);
        }
       System.out.println(taskid);
       System.out.println(taskid.size());
        //当需要转办的条数>=1条时，才发生下面的转办
        if(taskid.size()>=1){
            Assert.assertTrue(true);
        }
    }

    //转办，将当前登录用户发起的请求都转办给当前登录用户，只能一条一条转办
    @Test(dependsOnMethods = {"getAllPageTask"},description = "发起转办")
    public void taskTransfer() throws Exception{
        String url= TestConfig.session.selectOne("selUrl","转办");
        String body= TestConfig.session.selectOne("selBody","转办");
        //替换taskid的值,转办给当前登录用户
        for(String s:taskid){
            JSONObject jsonObject=JSONObject.parseObject(body);
            jsonObject.put("taskId",s);
            int[] userid=new int[1];
            userid[0]=TestConfig.userId;
            jsonObject.put("toUserIdSet",userid);

            body=jsonObject.toString();
            System.out.println(body);
            String resutl=  TestConfig.getResult(url,body);
            Assert.assertEquals(resutl,"操作成功");
        }
    }


    //批量审批，两步，我的待办->批量审批
    @Test(description = "我的待办",dependsOnMethods = "taskTransfer")
    public void  TaskToDo() throws IOException{
        String url= TestConfig.session.selectOne("selUrl","流程-审批中心-我的待办");
        String body= TestConfig.session.selectOne("selBody","流程-审批中心-我的待办");
        String reponse=TestConfig.getResponse(url,body);
        System.out.println(reponse);
        //1.将结果转换为json
        JSONObject jsonObject=JSONObject.parseObject(reponse);
        //提取data,data是一个jsson
        JSONObject jsonObject1=jsonObject.getJSONObject("data");
        //在提取list,list是一个jsonArray
        JSONArray tasklist=jsonObject1.getJSONArray("list");
        JSONArray jsonArray2=new JSONArray();
        for(int i=0;i<tasklist.size();i++){
            int businessId=tasklist.getJSONObject(i).getInteger("businessId");
            String procDefKey=tasklist.getJSONObject(i).getString("processKey");
            String taskId=tasklist.getJSONObject(i).getString("taskId");
            String approval="agree";
            String approvalMessage="同意";
            JSONObject jsonObject2=new JSONObject();
            jsonObject2.put("businessId",businessId);
            jsonObject2.put("procDefKey",procDefKey);
            jsonObject2.put("taskId",taskId);
            jsonObject2.put("approval",approval);
            jsonObject2.put("approvalMessage",approvalMessage);
            jsonArray2.add(jsonObject2);
       //  jsonObject2.clear();
        }
        jsonObject3=new JSONObject();
        jsonObject3.put("ehrApproveDTOList",jsonArray2);
        System.out.println(jsonObject3.toString());
    }

    //批量审批
    @Test(description = "批量审批",dependsOnMethods = {"TaskToDo"})
    public void  taskCompleteBatch() throws IOException{
        String url= TestConfig.session.selectOne("selUrl","批量审批");
        String body=jsonObject3.toString();
        String result=TestConfig.getResult(url,body);
        Assert.assertEquals(result,"操作成功");
    }




}
