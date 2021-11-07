package cn.caijava.core.spring.jdbc;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdbcTemplateUtil {

    public static List<Map<String, Object>> execSql(String sql, Map<String, Object> args) {
        preCheck();
        return getNamedParameterJdbcTemplate().queryForList(sql, args);
    }

    public static List<Map<String, Object>> execSql(String sql) {
        return execSql(sql, Collections.emptyMap());
    }

    public static long count(String countSql, Map<String, Object> args) {
        preCheck();
        return getNamedParameterJdbcTemplate().queryForObject(countSql, args, Long.class);
    }

    public static long count(String countSql) {
        //
        return count(countSql, Collections.emptyMap());
    }

    public static <T> List<T> execSqlByType(String sql, Class<T> clazz) {
        return execSqlByType(sql, Collections.emptyMap(), clazz);
    }

    public static <T> List<T> execSqlByType(String sql, Map<String, Object> args, Class<T> clazz) {
        preCheck();
        List<Map<String, Object>> maps = execSql(sql, args);
        if (ObjectUtil.isEmpty(maps)) {
            return Collections.emptyList();
        }
        return maps.parallelStream()
                .map(map -> JSON.toJavaObject(new JSONObject(map), clazz))
                .collect(Collectors.toList());
    }

    /**
     * 前置检查
     */
    private static void preCheck() {
        if (ObjectUtil.isEmpty(getJdbcTemplate())) {
            String errMsg = "can't find the JdbcTemplate bean in spring";
            throw new IllegalArgumentException(errMsg);
        }
        if (ObjectUtil.isEmpty(getNamedParameterJdbcTemplate())) {
            String errMsg = "can't find the NamedParameterJdbcTemplate bean in spring";
            throw new IllegalArgumentException(errMsg);
        }
    }

    private static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return SpringUtil.getBean(NamedParameterJdbcTemplate.class);
    }

    private static JdbcTemplate getJdbcTemplate() {
        return SpringUtil.getBean(JdbcTemplate.class);
    }


}
