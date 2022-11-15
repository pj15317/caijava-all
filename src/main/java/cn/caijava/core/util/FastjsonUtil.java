package cn.caijava.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FastjsonUtil {

    /**
     * json注释排序
     * @param in
     * @return
     */
    public static Object sortJSON(Object in, Comparator<? super String> c) {
        if (in instanceof JSONArray) {
            JSONArray ja=((JSONArray) in);
            JSONArray jacp = new JSONArray();
            for (int i = 0; i < ja.size(); i++) {
                jacp.add(sortJSON(ja.get(i),c));
            }
            return jacp;
        } else if (!(in instanceof Map)) {
            return in;
        }
        Map<String, Object> json = (Map) in;
        List<String> keys = Lists.newArrayList(json.keySet());
        keys.sort(c);

        JSONObject newObject = new JSONObject(true);
        for (String key : keys) {
            newObject.put(key, sortJSON(json.get(key),c));
        }
        return newObject;
    }

    public static Object sortJSON(Object in) {
        return sortJSON(in, Ordering.natural().reverse());
    }
}
