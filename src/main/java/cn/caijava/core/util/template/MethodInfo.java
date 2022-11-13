package cn.caijava.core.util.template;

import lombok.Data;


@Data
public class MethodInfo {
    /**
     * 方法注释
     */
    private String comment;
    /**
     * 返回值类型
     */
    private String returnType;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 变量名称
     */
    private String varNames;

    private String isList;
    private String isMap;
    private String isSet;
    private String isArray;
}
