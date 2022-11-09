package cn.caijava.core.util;

import cn.caijava.core.util.annotation.RoundOff;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 四舍五入工具类
 */
public class RoundOffUtil {


    /**
     * 遍历
     * @param entity 自定义类型的实体
     */
    public static void that(Object entity) {
        if (entity == null) {
            return;
        }
        Class<?> beanClass = entity.getClass();
        // 自定义类
        if (!ClassUtil.isNormalClass(beanClass)) {
            return;
        }
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(beanClass);
        // 自定义类成员变量map不应为空
        if (fieldMap.isEmpty()) {
            return;
        }

        // 数值类型
        Collection<Field> numberTypes = fieldMap.values().parallelStream()
                .filter(Objects::nonNull)
                .filter(aField -> aField.getAnnotation(RoundOff.class) != null)
                // 值是数值
                .filter(aField -> NumberUtil.isNumber(ReflectUtil.getFieldValue(entity, aField) + ""))
                .collect(Collectors.toList());

        // 自定义类型
        Collection<Field> custumerTypes = fieldMap.values().parallelStream()
                .filter(Objects::nonNull)
                .filter(aField -> aField.getAnnotation(RoundOff.class) != null)
                .filter(aField ->
                        !ClassUtil.isJdkClass(aField.getType()) &&
                                !aField.getType().isPrimitive() &&
                                !aField.getType().isArray())
                .collect(Collectors.toList());

        // 集合类型
        Collection<Field> collectionTypes = fieldMap.values().parallelStream()
                .filter(Objects::nonNull)
                .filter(aField -> aField.getAnnotation(RoundOff.class) != null)
                .filter(aField -> ReflectUtil.getFieldValue(entity, aField) != null)
                .filter(aField -> Collection.class.isAssignableFrom(aField.getType()))
                .collect(Collectors.toList());

        // 数组类型
        Collection<Field> arrayTypes = fieldMap.values().parallelStream()
                .filter(Objects::nonNull)
                .filter(aField -> aField.getAnnotation(RoundOff.class) != null)
                .filter(aField -> ReflectUtil.getFieldValue(entity, aField) != null)
                .filter(aField -> aField.getType().isArray())
                .collect(Collectors.toList());

        // map类型
        Collection<Field> mapTypes = fieldMap.values().parallelStream()
                .filter(Objects::nonNull)
                .filter(aField -> aField.getAnnotation(RoundOff.class) != null)
                .filter(aField -> ReflectUtil.getFieldValue(entity, aField) != null)
                .filter(aField -> Map.class.isAssignableFrom(aField.getType()))
                .collect(Collectors.toList());


        if (CollUtil.isEmpty(numberTypes)
                && CollUtil.isEmpty(custumerTypes)
                && CollUtil.isEmpty(collectionTypes)
                && CollUtil.isEmpty(arrayTypes)
                && CollUtil.isEmpty(mapTypes)
        ) {
            return;
        }


        // 处理数值类型
        for (Field aField : numberTypes) {
            RoundOff ann = aField.getAnnotation(RoundOff.class);
            String strValue = ReflectUtil.getFieldValue(entity, aField) + "";
            BigDecimal roundValue = NumberUtil.round(strValue, ann.scale(), ann.roundingMode());
            if (String.class == aField.getType()) {
                ReflectUtil.setFieldValue(entity, aField, roundValue.toString());
            } else if (double.class == aField.getType() || Double.class == aField.getType()) {
                ReflectUtil.setFieldValue(entity, aField, roundValue.doubleValue());
            } else if (float.class == aField.getType() || Float.class == aField.getType()) {
                ReflectUtil.setFieldValue(entity, aField, roundValue.floatValue());
            } else if (BigDecimal.class == aField.getType()) {
                ReflectUtil.setFieldValue(entity, aField, roundValue);
            }
        }

        // 处理自定义类型
        for (Field field : custumerTypes) {
            that(ReflectUtil.getFieldValue(entity, field));
        }
        // 处理集合类型
        for (Field field : collectionTypes) {
            Collection<?> fieldValue = (Collection) ReflectUtil.getFieldValue(entity, field);
            thatCollection(fieldValue);
        }
        // 处理数组类型
        for (Field field : arrayTypes) {
            Object[] fieldValue = (Object[]) ReflectUtil.getFieldValue(entity, field);
            thatArr(fieldValue);
        }
        // 处理map类型
        for (Field field : mapTypes) {
            Map map = (Map) ReflectUtil.getFieldValue(entity, field);
            thatMap(map);
        }

    }

    private static void thatArr(Object[] arr) {
        for (Object o : arr) {
            dealByClass(o);
        }
    }

    private static void thatCollection(Collection collection) {
        for (Object o : collection) {
            if (o.getClass().isArray()) {
                thatArr((Object[]) o);
            } else if (Collection.class.isAssignableFrom(o.getClass())) {
                for (Object o1 : ((Collection<?>) o)) {
                    dealByClass(o1);
                }
            } else if (Map.class.isAssignableFrom(o.getClass())) {
                thatMap((Map) o);
            } else {
                that(o);
            }
        }
    }

    private static void thatMap(Map<?, ?> map) {
        // key是对象的情况
        for (Object o : map.keySet()) {
            dealByClass(o);
        }
        // value 是对象的情况
        for (Object o : map.values()) {
            dealByClass(o);
        }
    }

    private static void dealByClass(Object o) {
        if (o.getClass().isArray()) {
            thatArr((Object[]) o);
        } else if (Collection.class.isAssignableFrom(o.getClass())) {
            thatCollection((Collection) o);
        } else if (Map.class.isAssignableFrom(o.getClass())) {
            thatMap((Map) o);
        } else {
            that(o);
        }
    }



}
