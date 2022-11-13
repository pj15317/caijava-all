package cn.caijava.core.util.freemarker;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerUtilTest extends TestCase {
    @Test
    public void test() {
        String ftl = "HelloWorld.ftl";
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("className", "HelloWorld");
        objectMap.put("content", "hello pj");

        File file = new File("/Users/pj/git/pj/caijava-all/src/main/resources/tmp/HelloWorld.java");
        FreeMarkerUtil.process(ftl, objectMap, file);
    }

}