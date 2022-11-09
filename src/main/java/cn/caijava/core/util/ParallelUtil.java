package cn.caijava.core.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Builder;
import lombok.Data;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
public class ParallelUtil {

    private static AsyncListenableTaskExecutor executor = new SimpleAsyncTaskExecutor();

    private static <T> T setterThat(T result, List<FutureResult> futureResults) {
        if (result == null) {
            return null;
        }
        futureResults.stream()
                .filter(Objects::nonNull)
                .filter(futureResult -> StrUtil.isNotBlank(futureResult.getFieldName()) &&
                        futureResult.getClazz() != null &&
                        futureResult.getResult() != null)
                .forEach(futureResult -> {
                    String methodName = StrUtil.concat(false, "set", StrUtil.upperFirst(futureResult.getFieldName()));
                    Method setterMethod = ClassUtil.getDeclaredMethod(
                            result.getClass(), methodName, futureResult.getClazz());
                    if (setterMethod != null) {
                        ReflectUtil.invoke(result, methodName, futureResult.getResult());
                    }
                });
        // 错误处理
        futureResults.stream().forEach(fr-> {
            if (fr != null && StrUtil.isNotBlank(fr.getFieldName()) && fr.getThrowable() != null) {
                System.err.println(fr.getFieldName()+" 异常:");
                fr.getThrowable().printStackTrace();
            }
        });

        return result;
    }

    private static List<FutureResult> parallelRun(Map<Callable<?>, String> callableMap) {
        Collection<Callable<?>> callables;
        if (callableMap == null ||
                callableMap.isEmpty() ||
                ArrayUtil.isEmpty(callables = callableMap.keySet())) {
            return Collections.emptyList();
        }

        return callables.parallelStream()
                .map((Callable<?> callable) -> {
                    long st = 0;
                    long et;
                    Object obj = null;
                    final Throwable[] throwables = {null};
                    ListenableFuture<?> future = executor.submitListenable(callable);
                    future.addCallback(result -> {
                    }, ex -> throwables[0] = ex);
                    try {
                        st = System.currentTimeMillis();
                        obj = future.get();
                    } catch (Exception e) {
                    } finally {
                        et = System.currentTimeMillis();
                    }
                    return FutureResult.builder()
                            .result(obj)
                            .startMs(st)
                            .endMs(et)
                            .throwable(throwables[0])
                            .clazz(obj == null ? null : obj.getClass())
                            .fieldName(callableMap.get(callable))
                            .build();
                }).collect(Collectors.toList());
    }

    public static <T> T parallelRunService(Object service, T result, Object[] params) {
        if (service == null || service.getClass() == null || result == null) {
            return result;
        }
        Class<?> implClass;
        if (AopUtils.isAopProxy(service)) {
            implClass = AopUtils.getTargetClass(service);
        } else {
            implClass = service.getClass();
        }
        List<String> methodNames = Arrays.stream(ClassUtil.getDeclaredFields(result.getClass()))
                .map(field -> StrUtil.upperFirstAndAddPre(field.getName(), "get"))
                .collect(Collectors.toList());

        List<Method> methods = Arrays.stream(ClassUtil.getDeclaredMethods(implClass))
                .parallel()
                .filter(method -> methodNames.contains(method.getName()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(methods)) {
            return result;
        }
        // 设置访问权限
        methods.forEach(method -> method.setAccessible(true));
        Class<?>[] paramsTypeList = Arrays.stream(params).parallel()
                .filter(Objects::nonNull)
                .map(Object::getClass)
                .collect(Collectors.toList())
                .toArray(new Class<?>[0]);

        // 参数类型过滤
        methods = methods.stream().filter(method ->
                        method.getParameterTypes().length == paramsTypeList.length &&
                                ArrayUtil.containsAll(method.getParameterTypes(), paramsTypeList))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(methods)) {
            return result;
        }
        Map<Callable<?>, String> callableMap = methods.parallelStream().collect(Collectors.toMap(
                method -> () -> method.invoke(service, params),
                method -> StrUtil.removePreAndLowerFirst(method.getName(), "get"),
                (m1, m2) -> m1));
        return setterThat(result, parallelRun(callableMap));
    }


    @Data
    @Builder
    public static class FutureResult {
        private Object result;
        private Class<?> clazz;
        private String fieldName;
        private long startMs;
        private long endMs;
        private Throwable throwable;

        @Override
        public String toString() {
            return "FutureResult{" +
                    "result=" + result +
                    ", clazz=" + clazz +
                    ", fieldName=" + fieldName +
                    ", startMs=" + startMs +
                    ", endMs=" + endMs +
                    ", throwable=" + throwable +
                    ", costMs=" + DateUtil.formatBetween(endMs - startMs) +
                    ", st=" + LocalDateTimeUtil.of(startMs).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                    ", et=" + LocalDateTimeUtil.of(endMs).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                    '}';
        }
    }


}