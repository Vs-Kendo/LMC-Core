/*
 * Author: Yun-Shan
 * Date: 2017/06/11
 */
package org.yunshanmc.lmc.core.utils;

import org.yunshanmc.lmc.core.resource.Resource;
import org.yunshanmc.lmc.core.resource.URLResource;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 反射相关工具
 */
public class ReflectUtils {
    
    /**
     * 根据调用栈追踪指定资源
     * <p>
     * 会通过每个调用栈Class获取指定资源
     *
     * @param stackTrace 调用栈
     * @param resPath    资源路径，会自动转换为根路径(/xxx)
     * @return 追踪到的资源列表
     */
    @Nonnull
    public static List<Resource> traceResources(StackTraceElement[] stackTrace, String resPath) {
        resPath = Resource.pathToRoot(resPath);
        List<Resource> ress = new ArrayList<>();
        for (StackTraceElement stack : stackTrace) {
            try {
                Class<?> cls = Class.forName(stack.getClassName());
                URL url = cls.getResource(resPath);
                ress.add(new URLResource(url));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();// TODO 测试该异常被抛出的时机，再进行相应处理
            }
        }
        Collections.reverse(ress);
        return ress;
    }
    
}