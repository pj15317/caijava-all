package cn.caijava.core.util.resilience4j;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
// https://www.baeldung.com/resilience4j-backoff-jitter
// https://github.com/eugenp/tutorials/blob/master/patterns/design-patterns-cloud/src/test/java/com/baeldung/backoff/jitter/BackoffWithJitterTest.java
public class RetryTest {

    @Ignore
    @Test
    public void test_timeout_then_return() {
        AtomicInteger at = new AtomicInteger();

        LocalDateTime now = LocalDateTime.now();
        System.err.println("当前时间: " + DateUtil.formatLocalDateTime(now));
        LocalDateTime plantEndTime = now.plusSeconds(30);
        System.err.println("计划结束时间: " + DateUtil.formatLocalDateTime(plantEndTime));

        Function<String, String> testRetry = getRetryableFn("testRetry", arg -> {
                    String msg = MessageFormat.format("第{0}次执行 当前时间:{1}",
                            at.getAndIncrement(), DateUtil.now());
                    System.err.println(msg);
                    return "fail";
                },
                unused -> LocalDateTime.now().isBefore(plantEndTime));

        String result = testRetry.apply("???");

        LocalDateTime actEndTime = LocalDateTime.now();
        System.err.println("最终执行结果: " + result

                + " 计划结束时间:" + DateUtil.formatLocalDateTime(plantEndTime)
                + " 实际结束时间:" + DateUtil.formatLocalDateTime(actEndTime));

        long betweenMillis = ChronoUnit.MILLIS.between(plantEndTime, actEndTime);

        System.err.println("计划结束时间 和 实际结束时间相差 毫秒数:" + betweenMillis);
        Assert.isTrue("fail".equals(result),"实际返回结果应为失败: fail");

        Assert.isTrue(betweenMillis < 1000,"计划结束时间 与 实际结束时间 间隔应小于1000毫秒");

    }

    @Test
    public void test_success_then_return() {
        AtomicInteger at = new AtomicInteger();
        System.err.println(at.get());

        Function<String, String> testRetry = getRetryableFn("testRetry", arg -> {
                    String msg = MessageFormat.format("第{0}次执行 当前时间:{1}",
                            at.getAndIncrement(), DateUtil.now());
                    System.err.println(msg);
                    if (at.get() == 2) {
                        return "success";
                    }
                    return "fail";
                },
                unused -> true);

        String result = testRetry.apply("???");


        Assert.isTrue(at.get() == 2, "实际执行次数应为2");

        Assert.isTrue("success".equals(result),"实际返回结果应为失败: success");

    }




    private Function<String, String> getRetryableFn(String retryName,
                                                    Function<String, String> serviceFn,
                                                    Predicate<Void> timeOutPredicate) {

        RetryConfig retryConfig = RetryConfig.custom()
                .waitDuration(Duration.ofMillis(1000))
                .maxAttempts(Integer.MAX_VALUE)
                .retryOnResult(s -> !"success".equals(s) && timeOutPredicate.test(null))
                .build();

        Retry retry = Retry.of(retryName, retryConfig);
        return Retry.decorateFunction(retry, arg -> serviceFn.apply(arg));
    }

}
