package com.ehr.cases;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ehr.config.TestConfig;
import org.testng.Assert;
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

////供调试单个用例执行时使用
//    @BeforeTest
//    public void beforeTest() throws IOException{
//        TestConfig.defaultHttpClient=new DefaultHttpClient();
//        TestConfig.store=new BasicCookieStore();
//       TestConfig.session= DatabaseUtil.getSqlSession();
//        String cookie_value=TestConfig.session.selectOne("com.ehr.flowes.selvalue","cookie");
//      //  System.out.println(cookie_value);
//        BasicClientCookie basicClientCookie=new BasicClientCookie("EHR_COOKIES20190505ERWFSDF",cookie_value);
//        TestConfig.store.addCookie(basicClientCookie);
//        TestConfig.url=TestConfig.session.selectOne("com.ehr.flowes.selvalue","ip_test");
//        basicClientCookie.setDomain(TestConfig.url);
//        //basicClientCookie.setDomain("tehr.mandao.com");
//        basicClientCookie.setPath("/");
//        TestConfig.defaultHttpClient.setCookieStore(TestConfig.store);
//
//    }
    //获取考勤异常数据
    @Test(description = "获取异常考勤数据")
    public void findErroAttendace() throws IOException{
       // SqlSession session=DatabaseUtil.getSqlSession();
        String url= TestConfig.session.selectOne("com.ehr.flowes.selUrl","我的考勤-异常");
        String body= TestConfig.session.selectOne("com.ehr.flowes.selBody","我的考勤-异常");
       String result=TestConfig.getResponse(url,body);
        System.out.println(result);
       JSONObject jsonObject=JSONObject.parseObject(result);
       //获取data是一个json字符串；不是一个list,因为没有[]
      JSONObject data=jsonObject.getJSONObject("data");
        //list是一个jsonArray
            JSONArray list= data.getJSONArray("list");
            //用于存储考勤状态为异常的id
        //    List<Integer> idlist=new ArrayList<>();
            for(int j=0;j<list.size();j++){
                //考勤状态
                String attendanceStatusName=list.getJSONObject(j).getString("attendanceStatusName");
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
        }
        System.out.println(map);
    }


    //补签，针对考勤异常的数据才能发起补签，补签只需要一个异常考勤的id就可以
    @Test(dependsOnMethods = {"findErroAttendace"},description = "补签")
    public void fillCheckApply() throws IOException{
        System.out.println("获取补签地址");

        //获取补签url
        String url= TestConfig.session.selectOne("selUrl","补签");
        String body= TestConfig.session.selectOne("selBody","补签");
        System.out.println(url+body);
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

     //请假，需要时间
    @Test(dependsOnMethods = {"findErroAttendace"},description = "请假")
    public void vacationApply() throws IOException{

        String url= TestConfig.session.selectOne("selUrl","请假");
        String body= TestConfig.session.selectOne("selBody","请假");
        System.out.println(url+":"+body);
        JSONObject jsonObject=JSONObject.parseObject(body);
        //首先获取考勤异常id,
        int id=idlist.get(1);
        //在通过考勤异常的id找到对应的考勤异常日期
        String date=map.get(id);
        //时间拼接，注意前面的空格
        jsonObject.put("vacationStartTime",date+" 09:00");
        jsonObject.put("vacationEndTime",date+" 18:00");
        body=jsonObject.toString();
       String result=TestConfig.getResult(url,body);
       Assert.assertEquals(result,"操作成功");

    }


    //审批-流程监控不含offer
    //此步骤需要获取taskid
    @Test(description = "流程监控",dependsOnMethods = {"vacationApply","fillCheckApply"})
    public void getAllPageTask () throws Exception{
        String url= TestConfig.session.selectOne("selUrl","流程监控");
        String body= TestConfig.session.selectOne("selBody","流程监控");
        String reponse=TestConfig.getResponse(url,body);
        //获取发起人id为993的taskid，并存入数组；
        //1.将结果转换为json
        JSONObject jsonObject=JSONObject.parseObject(reponse);
        //提取list
        JSONObject jsonObject1=jsonObject.getJSONObject("data");
        JSONArray tasklist=jsonObject1.getJSONArray("list");
        System.out.println(tasklist.size());
        for(int i=0;i<tasklist.size();i++){
            String id=tasklist.getJSONObject(i).getString("taskId");
            System.out.println(i);
            //将发起人是993的流程taskid提取出来，放入taskid中；
            int applyUserid=tasklist.getJSONObject(i).getInteger("applyUserId");
            System.out.println(applyUserid);
            if(applyUserid==993){
                taskid.add(id);
            }
        }
//        System.out.println(taskid);
//        System.out.println(taskid.size());
    }
    //发起转办，将993发起的请求都转办给993用户，只能一条一条转办
    @Test(dependsOnMethods = {"getAllPageTask"},description = "发起转办")
    public void taskTransfer() throws Exception{
        String url= TestConfig.session.selectOne("selUrl","转办");
        String body= TestConfig.session.selectOne("selBody","转办");
        //替换taskid的值,转办给993用户
        for(String s:taskid){
            JSONObject jsonObject=JSONObject.parseObject(body);
            jsonObject.put("taskId",s);
            body=jsonObject.toString();
            String resutl=  TestConfig.getResult(url,body);
            Assert.assertEquals(resutl,"操作成功");

        }
    }


    //批量审批，两步，我的待办->批量审批
    @Test(description = "我的待办",dependsOnMethods = "taskTransfer")
    public void  TaskToDo() throws IOException{
        String url= TestConfig.session.selectOne("selUrl","流程-我的待办");
        String body= TestConfig.session.selectOne("selBody","流程-我的待办");
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
