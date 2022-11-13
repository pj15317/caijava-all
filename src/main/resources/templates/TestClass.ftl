package ${packageName};

<#list importList as implement>
${implement}

</#list>
import com.alibaba.testable.core.annotation.MockMethod;
import com.alibaba.testable.core.tool.PrivateAccessor;
import static com.alibaba.testable.core.tool.TestableTool.MOCK_CONTEXT;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import cn.hutool.core.util.ObjectUtil;

public class ${className}Test {


    @Spy
    @InjectMocks
    private ${className} service;

    @Before
    public void initMock() {
        MockitoAnnotations.initMocks(this);
    }

    public static class Mock {
        <#list methodList as methodInfo>

            @MockMethod(targetMethod = "${methodInfo.methodName}", targetClass = ${className}.class)
            ${methodInfo.accessModifier} ${methodInfo.returnType} ${methodInfo.methodName}(${methodInfo.paramsDefinition}) {

            switch (ObjectUtil.toString(MOCK_CONTEXT.get("case"))) {
                default:
                <#if !methodInfo.voidReturnType>
                    return ${methodInfo.returnDefaultValue};
                </#if>
                }
            }
        </#list>

    }

<#list methodList as methodInfo>

    @Test
    public void test_${methodInfo.methodName}() {
        MOCK_CONTEXT.put("case", "test_${methodInfo.methodName}");
        // 方法变量列表
    <#list methodInfo.fieldList as field>
        ${field.type} ${field.fieldName} = ${field.value};

    </#list>

    <#assign isPublic=methodInfo.publicMethod=='true'>
    <#assign isStatic=methodInfo.staticMethod=='true'>

    <#if isPublic && !isStatic>
        // public方法
        service.${methodInfo.methodName}(${methodInfo.params});
    <#elseif !isPublic && !isStatic>
        // private方法
        PrivateAccessor.invoke(service, "${methodInfo.methodName}",
        new Object[]{${methodInfo.params}});
    <#elseif isStatic>
        // static方法
        PrivateAccessor.invokeStatic(${className}.class, "${methodInfo.methodName}",
        new Object[]{${methodInfo.params}});
    <#else>
        // 不知道什么方法???
    </#if>



    }
</#list>


}