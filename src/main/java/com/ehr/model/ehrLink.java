package com.ehr.model;

import lombok.Data;

@Data
public class ehrLink {
    private int id;
    private String describtion;
    private String url;
    private String body;
    private int nochange;

    //toString 只写出了describtion,是因为想在测试报告输出的内容中显示的好看一点；
    @Override
    public String toString() {
        return "ehr链接" +"-"+
                  describtion
                ;
    }
}
