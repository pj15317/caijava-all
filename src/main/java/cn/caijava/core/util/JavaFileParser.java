package cn.caijava.core.util;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class JavaFileParser {
    public static void main(String[] args) {
        String filePath = "/Users/pj/git/pj/caijava-all/src/test/java/cn/caijava/core/util/freemarker/bean/TestBean.java";
        Map<String, String> commentMap = JavaFileParser.parseFieldComment(filePath);
    }

    @Deprecated
    public static Map<String, String> parseFieldComment(String path) {

        List<String> lines = FileUtil.readUtf8Lines(path);
        if (CollectionUtils.isEmpty(lines)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.contains("private")) {
                String key = StrUtil.subAfter(line, " ", true).replace(";", "");
                map.put(key, sb.toString());
                sb = new StringBuilder();
            } else if (StringUtils.contains(line, "/**")) {
                sb = new StringBuilder();
                sb.append(line).append("\n");
            } else if (StringUtils.contains(line, "*/")) {
                sb.append(line);
            } else if (StringUtils.contains(line, "*")) {
                sb.append(line).append("\n");
            }

        }
        return map;
    }

    public static Map<String, String> parseFieldCommentFromAnnotation(Class<?> voCLass, Class<? extends Annotation> clazz) {
        if (clazz == null || voCLass == null) {
            return Collections.emptyMap();
        }
        Field[] allField = ClassUtil.getDeclaredFields(voCLass);
        return Arrays.stream(allField).parallel()
                .collect(Collectors.toMap(
                        Field::getName,
                        v -> AnnotationUtil.getAnnotationValue(v, clazz, "desc"),
                        (m1, m2) -> m1));
    }
}
