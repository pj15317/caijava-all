package cn.caijava.core.spring.jdbc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
// <bean id="namedParameterJdbcTemplate" class="org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate">
//<constructor-arg ref="mainDataSource"></constructor-arg>
//</bean>
//
//<bean id="mainDataSource" class="com.zaxxer.hikari.HikariDataSource">
//</bean>
@Component
public class JdbcTemplateUtil {

    public static List<Map<String, Object>> query(String sql, Map<String, Object> args) {
        preCheck();
        return getNamedParameterJdbcTemplate().queryForList(sql, args);
    }

    public static List<Map<String, Object>> query(String sql) {
        return query(sql, Collections.emptyMap());
    }

    public static long count(String countSql, Map<String, Object> args) {
        preCheck();
        return getNamedParameterJdbcTemplate().queryForObject(countSql, args, Long.class);
    }

    public static long count(String countSql) {
        //
        return count(countSql, Collections.emptyMap());
    }

    public static <T> List<T> queryByType(String sql, Class<T> clazz) {
        return queryByType(sql, Collections.emptyMap(), clazz);
    }

    public static <T> List<T> queryByType(String sql, Map<String, Object> args, Class<T> clazz) {
        preCheck();
        List<Map<String, Object>> maps = query(sql, args);
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

    /**
     * <pre>
     * 返回值含义 {@link java.sql.Statement#executeBatch()}
     * 和springJPA共用事务问题 {@link DataSourceUtils#getConnection(javax.sql.DataSource)}
     * </pre>
     *
     * @param pos 数据库实体列表
     * @param predicate 表字段过滤器
     * @return T 结果实体
     */
    public static <T> int[] saveOrUpdate(
            List<T> pos, Predicate<? super String> predicate) {
        if (getNamedParameterJdbcTemplate() == null) {
            throw new RuntimeException("NamedParameterJdbcTemplate can't be null");
        }

        if (CollUtil.isEmpty(pos)) {
            return new int[0];
        }
        Class<?> poClass = pos.get(0).getClass();
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(poClass);
        String tableName = StrUtil.subPre(StrUtil.toUnderlineCase(poClass.getSimpleName()), -3);

        String sql = makeInsertOnDuplicateKeyUpdateToTableSql(tableName, fieldMap, predicate);


        int[] rtn = new int[0];
        List<List<T>> partition = Lists.partition(pos, 500);
        for (List<T> ts : partition) {
            List<Map<String, Object>> batchValues =
                    ts.parallelStream()
                            .map(BeanUtil::beanToMap)
                            .collect(Collectors.toList());
            int[] ints = getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[ts.size()]));
            rtn = ArrayUtils.addAll(rtn, ints);
        }
        return rtn;
    }
    public static String makeInsertOnDuplicateKeyUpdateToTableSql(String tableName,
                                                                  Map<String, Field> fieldMap,
                                                                  Predicate<? super String> columnFilter) {
        Set<String> beanMemberVariableSet = fieldMap.keySet();
        Set<String> tableColumnNameSet = beanMemberVariableSet.parallelStream().map(s -> StrUtil.toUnderlineCase(s))
                .collect(Collectors.toSet());

        Set<String> columnNames;
        if (columnFilter != null) {
            columnNames = tableColumnNameSet
                    .parallelStream().filter(columnFilter)
                    .collect(Collectors.toSet());
        } else {
            columnNames = tableColumnNameSet;
        }

        String tableFields = columnNames.parallelStream().collect(Collectors.joining(","));
        String tableValues = beanMemberVariableSet.parallelStream()
                .map(name -> StrUtil.addPrefixIfNot(name, StrUtil.COLON))
                .collect(Collectors.joining(StrUtil.COMMA));

        String onDupSql = columnNames.parallelStream()
                .map(columnName -> {
                    return columnName +
                            " = VALUES(" +
                            columnName +
                            ")";
                })
                .collect(Collectors.joining(","));

        StringBuilder sql = new StringBuilder()
                .append("insert into ")
                .append(tableName)
                .append("(")
                .append(tableFields)
                .append(")")
                .append(" values (")
                .append(tableValues)
                .append(")")
                .append(" on duplicate key update \n")
                .append(onDupSql);
        return sql.toString();
    }

}
