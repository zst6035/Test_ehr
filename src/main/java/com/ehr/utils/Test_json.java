package com.ehr.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.xdevapi.JsonArray;

public class Test_json {
    public static void main(String [] args){
        String s = "{\"error\":0,\"status\":\"success\",\"results\":[{\"currentCity\":\"青岛\",\"index\":[{\"title\":\"穿衣\",\"zs\":\"较冷\",\"tipt\":\"穿衣指数\",\"des\":\"建议着厚外套加毛衣等服装。年老体弱者宜着大衣、呢外套加羊毛衫。\"},{\"title\":\"紫外线强度\",\"zs\":\"最弱\",\"tipt\":\"紫外线强度指数\",\"des\":\"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。\"}],}]}";
        JSONObject jsonObject=JSONObject.parseObject(s);
        int erro=jsonObject.getInteger("error");
        System.out.println(erro);
        String status=jsonObject.getString("status");
        System.out.println(status);

        JSONArray results=jsonObject.getJSONArray("results");
        for(int i=0;i<results.size();i++){
            //取第一个jsonArray里的值
            String currentCity=results.getJSONObject(i).getString("currentCity");
            System.out.println(currentCity);
        //去index的值
            JSONArray index=results.getJSONObject(i).getJSONArray("index");
            for(int j=0;j<index.size();j++){
                String title=index.getJSONObject(j).getString("title");
                System.out.println(title);
            }

        }

    }
}
