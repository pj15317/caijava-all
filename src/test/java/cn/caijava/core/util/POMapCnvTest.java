package cn.caijava.core.util;


import cn.caijava.core.converter.util.POMapCnv;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class POMapCnvTest {

    @Test
    public void toMap() {
        User user = new User();
        user.setUsername("zhang san");
        user.setPassword("123456");
        Map<String, Object> userMap = POMapCnv.toMap(user);
        String expected = "{\n" +
                "\t\"password\":\"123456\",\n" +
                "\t\"username\":\"zhang san\"\n" +
                "}";
        String actual = JSONObject.toJSONString(userMap, true);
        Assert.assertEquals(expected, actual);
    }
}