package cn.caijava.core.util.template;

import cn.caijava.core.util.JavaFileParser;
import cn.caijava.core.util.freemarker.FreeMarkerUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.system.SystemUtil;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceTemplateUtil {
    private static final String FILE_SEPARATOR = SystemUtil.get(SystemUtil.FILE_SEPARATOR);
    private static final String SERVICE_FTL = "Service.ftl";

    public static void writeTemplate(
            String className,
            Class<?> resultDataClass,
            String[][] params,
            Class<? extends Annotation> commentAnnotationClass) {
        Set<String> classPaths = ClassUtil.getClassPaths(ClassUtil.getPackage(resultDataClass));

        String beanPath = classPaths.stream().findFirst()
                .orElse("")
                .replace(StrUtil.concat(false,
                                "target", FILE_SEPARATOR, "classes"),
                        StrUtil.concat(false,
                                "src", FILE_SEPARATOR, "main", FILE_SEPARATOR, "java")
                );
        String targetPath = StringUtils.substringBefore(classPaths.stream().findFirst()
                                .orElse(""),
                        StrUtil.concat(false,
                                "target", FILE_SEPARATOR, "classes"))
                .concat(

                        StrUtil.concat(false,
                                "src", FILE_SEPARATOR,
                                "main", FILE_SEPARATOR,
                                "resources", FILE_SEPARATOR,
                                "temp", FILE_SEPARATOR
                        )
                );

        if (!FileUtil.exist(targetPath)) {
            FileUtil.mkdir(targetPath);
        }
        String ftl = SERVICE_FTL;
        Field[] fields = ClassUtil.getDeclaredFields(resultDataClass);
        Map<String, String> fieldNameAndTypeMap = new HashMap<>();
        Map<String, Type> fieldNameAndTypeClassMap = new HashMap<>();
        Set<Class> importList = Sets.newHashSet();
        for (Field field : fields) {
            Type actualType = TypeUtil.getActualType(field.getType(), field);
            Set<Class> classes = FreeMarkerUtil.recursionType(actualType);
            importList.addAll(classes);
            String tn = actualType.getTypeName();
            fieldNameAndTypeMap.put(field.getName(), tn);
            fieldNameAndTypeClassMap.put(field.getName(), actualType);

        }

        Map<String, String> importMap = importList.parallelStream()
                .collect(Collectors.toMap(
                        Class::getName,
                        Class::getSimpleName,
                        (m1, m2) -> m1));


        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for (String[] param : params) {
            sb.append(param[0]);
            sb.append(" ");
            sb.append(param[1]);
            sb.append(", ");

            sb2.append(param[1]);
            sb2.append(", ");
        }
        String varNames = StrUtil.removeSuffix(sb2.toString(), ", ");
        String paramString = StrUtil.removeSuffix(sb.toString(), ", ");



        String filePath = beanPath + SystemUtil.get(SystemUtil.FILE_SEPARATOR) +
                resultDataClass.getSimpleName() +
                ".java";
        Map<String, String> commentMap;
        // 读取注释
        if (commentAnnotationClass == null) {
            commentMap= JavaFileParser.parseFieldComment(filePath);
        } else {
            commentMap = JavaFileParser.parseFieldCommentFromAnnotation(resultDataClass, commentAnnotationClass);
        }

        List<MethodInfo> methodInfos = new ArrayList<>();
        fieldNameAndTypeMap.forEach((String fieldName, String typeClass) -> {
            MethodInfo methodInfo = new MethodInfo();
            String comment = commentMap.get(fieldName);
            methodInfo.setComment(comment);
            String origin = fieldNameAndTypeMap.get(fieldName);
            methodInfo.setReturnType(FreeMarkerUtil.doWithTypeName(importMap, origin));
            methodInfo.setMethodName(StrUtil.upperFirstAndAddPre(fieldName, "get"));
            methodInfo.setFieldName(fieldName);

            Type returnType = fieldNameAndTypeClassMap.get(fieldName);

            methodInfo.setIsList((returnType instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl)returnType).getRawType().isAssignableFrom(List.class))+"");
            methodInfo.setIsMap((returnType instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl)returnType).getRawType().isAssignableFrom(Map.class))+"");
            methodInfo.setIsSet((returnType instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl)returnType).getRawType().isAssignableFrom(Set.class))+"");
            methodInfo.setIsArray(returnType.getClass().isArray()+"");

            methodInfos.add(methodInfo);
        });

        File file = new File(targetPath + className + ".java");
        Map<String, Object> obj = new HashMap<>();
        obj.put("methodList", methodInfos);
        obj.put("className", className);
        obj.put("importList", importMap.keySet());
        obj.put("resultData", resultDataClass.getSimpleName());
        obj.put("resultDataImport", resultDataClass.getName());
        obj.put("paramString", paramString);
        obj.put("varNames", varNames);
        FreeMarkerUtil.process(ftl, obj, file);

    }

    public static void writeTemplate(
            String className,
            Class<?> resultDataClass,
            String[][] params) {

        writeTemplate(className, resultDataClass, params, null);
    }
}
