package cn.caijava.core.util.freemarker;

import cn.caijava.core.util.template.ServiceTemplateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import cn.hutool.system.SystemUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class FreeMarkerUtil {

    private static final Configuration cfg = getConfiguration();

    private FreeMarkerUtil() {
        throw new IllegalStateException("Utility class");
    }



    public static void process(String ftl, Object dataMap, File targetFile) {
        Template template = getTemplate(ftl);
        Objects.requireNonNull(template);
        try {
            template.process(dataMap, new FileWriter(targetFile));
        } catch (TemplateException | IOException e) {
            Log.get().error(e);
        }
    }


    private static Template getTemplate(String name) {
        Objects.requireNonNull(cfg);
        try {
            cfg.setDirectoryForTemplateLoading(copyJarResourceToTemp(name));
            return cfg.getTemplate(name);
        } catch (IOException e) {
            Log.get().error(e);
            return null;
        }
    }
    private static File copyJarResourceToTemp(String ftl) {
        String tempPath= FileUtil.getTmpDirPath()+"cn.caijava"+ SystemUtil.get(SystemUtil.FILE_SEPARATOR);
        if(!FileUtil.exist(tempPath)){
            FileUtil.mkdir(tempPath);
        }
        InputStream in = ServiceTemplateUtil.class.getResourceAsStream("/templates/"+ftl);
        File ftlFile = FileUtil.writeFromStream(in, tempPath + ftl);
        return ftlFile.getParentFile();
    }

    /**
     * 生成freemarker引擎配置
     *
     * @return
     */
    private static Configuration getConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        // 设置文件编码
        cfg.setDefaultEncoding(Charsets.UTF_8.displayName());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        return cfg;
    }

    public static Set<Class> recursionType(Type t) {
        if (t instanceof Class) {
            return Sets.newHashSet((Class<?>) t);
        }
        ParameterizedType pt = (ParameterizedType) t;
        Set<Class> l = Sets.newHashSet((Class) pt.getRawType());
        for (Type tt : pt.getActualTypeArguments()) {
            l.addAll(recursionType(tt));
        }
        return l;
    }

    public static String doWithTypeName(Map<String, String> importMap, String origin) {
        Set<String> strings = importMap.keySet();
        for (String s : strings) {
            if (origin.contains(s)) {
                String simpleName = importMap.get(s);
                origin = StringUtils.replace(origin, s, simpleName);
            }
        }
        return origin;
    }

}