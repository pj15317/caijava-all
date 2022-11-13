<#list importList as import>
import ${import};
</#list>
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.hutool.core.date.DateUtil;
import java.util.concurrent.CompletableFuture;
import ${resultDataImport};
import com.google.common.collect.*;
import java.util.*;

@Service
public class ${className} {

    private Logger logger = LoggerFactory.getLogger(${className}.class);

    public ${resultData} query(${paramString}) {
        // 开始时间
        long st = System.currentTimeMillis();
        // 结束时间
        long et;
        ${resultData} data = null;

        try {
            <#list methodList as methodInfo>
            /**
            * ${methodInfo.comment}
            */
            CompletableFuture<${methodInfo.returnType}> ${methodInfo.methodName}Future = CompletableFuture.supplyAsync(() -> ${methodInfo.methodName}(${varNames}));
            </#list>
            data = ${resultData}.builder()
            <#list methodList as methodInfo>
                .${methodInfo.fieldName}(${methodInfo.methodName}Future.get())
            </#list>
            .build();
        } catch(Exception e) {
            logger.error("${className}#query 异常", e);
        } finally {
            et = System.currentTimeMillis();
            logger.debug("${className}#query 耗时:{}", DateUtil.formatBetween(et - st));
        }
        return data;
    }
<#list methodList as methodInfo>
    /**
    * ${methodInfo.comment}
    */
    private ${methodInfo.returnType} ${methodInfo.methodName}(${paramString}) {
        <#if methodInfo.isList=="true">
            ${methodInfo.returnType} rtn = Lists.newArrayList();
        <#elseif methodInfo.isMap=="true">
            ${methodInfo.returnType} rtn = Maps.newHashMap();
        <#elseif methodInfo.isSet=="true">
            ${methodInfo.returnType} rtn = Sets.newHashSet();
        <#elseif methodInfo.isArray=="true">
            ${methodInfo.returnType} rtn = {};
        <#else>
            ${methodInfo.returnType} rtn = null;
        </#if>
        return rtn;
    }
</#list>
}