package com.ehr.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Test06 {
    @Test
    public void test01(){

        //三个参数，最后一个指的是验证不通过时，输出的内容
        Assert.assertEquals("ab","aa","hello");


    }
}
