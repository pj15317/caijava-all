package cn.caijava.core.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Param<T> {
    /**
     * spring bean class
     */
    private Class<?> springBeanCLass;
    /**
     * 最终返回结果实体实例
     */
    private T resultInstance;
    /**
     * 请求参数列表
     */
    private Object[] params;

}