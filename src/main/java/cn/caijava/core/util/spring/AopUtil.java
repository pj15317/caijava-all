package cn.caijava.core.util.spring;

import cn.hutool.core.collection.CollUtil;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.List;

/**
 * spring aop util
 */
public class AopUtil {

    /**
     * 为spring代理对象增加指定切面, 并返回新的代理对象
     * @param targetObject spring代理对象
     * @param aspectInstances 增强切面列表
     * @param proxyTargetClass 是否使用cglib代理
     * @param exposeProxy true: 可以使用{@link org.springframework.aop.framework.AopContext#currentProxy()}获取代理对象
     * @return T 结果实体
     */
    public static <T> T getAspectJProxy(Object targetObject,
                                 List<Object> aspectInstances,
                                 boolean proxyTargetClass,
                                 boolean exposeProxy
                                 ) {
        AspectJProxyFactory factory = new AspectJProxyFactory(targetObject);
        if (CollUtil.isNotEmpty(aspectInstances)) {
            aspectInstances.forEach(factory::addAspect);
        }
        factory.setProxyTargetClass(proxyTargetClass);
        factory.setExposeProxy(exposeProxy);
        return factory.getProxy();
    }

    /**
     * 为spring代理对象增加指定切面, 并返回新的代理对象proxyTargetClass和exposeProxy都为true
     * @param targetObject spring代理对象
     * @param aspectInstances 增强切面列表
     * @return T 结果实体
     */
    public static <T> T getAspectJProxy(Object targetObject, List<Object> aspectInstances) {
        return getAspectJProxy(targetObject, aspectInstances, true, true);
    }
}
