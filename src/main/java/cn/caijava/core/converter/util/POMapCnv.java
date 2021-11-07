package cn.caijava.core.converter.util;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class POMapCnv {
    static {
        JSON.DEFFAULT_DATE_FORMAT = DatePattern.NORM_DATETIME_PATTERN;
    }

    public static <T> List<T> toListBean(List<Map<String, Object>> rtn, Class<T> clazz) {
        return rtn.parallelStream().map((Map<String, Object> map) ->
                JSON.toJavaObject(new JSONObject(map), clazz))
                .collect(Collectors.toList());
    }

    public static <T> List<Map<String, Object>> toListMap(List<T> rtn) {
        return rtn.parallelStream().map(POMapCnv::toMap)
                .collect(Collectors.toList());
    }

    public static <T> List<Map<String, Object>> toListMapWithFilter(List<T> rtn, PropertyPreFilter filter) {
        return rtn.parallelStream().map(bean -> toMap(bean, filter))
                .collect(Collectors.toList());
    }

    public static <T> Map<String, Object> toMap(T bean) {
        //
        return toMap(bean, null);
    }

    @SuppressWarnings("all")
    public static <T> Map<String, Object> toMap(T bean, PropertyPreFilter filter) {
        return JSON.parseObject(JSON.toJSONString(
                bean,
                filter,
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.WriteBigDecimalAsPlain),
                Map.class);
    }

    public static <T> T toBean(Map<String, Object> map, Class<T> clazz) {
        return JSON.toJavaObject(new JSONObject(map), clazz);
    }

}