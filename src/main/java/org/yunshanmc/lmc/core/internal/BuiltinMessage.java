package org.yunshanmc.lmc.core.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// 内置信息
public class BuiltinMessage {

    private static final Map<Locale, Map<String, String>> messages;

    private static Map<String, String> current;

    static {
        messages = new HashMap<>();
        Map<String, String> chinese = new HashMap<>();
        chinese.put("DefaultErrorHandler", "§2[§eLMCCore§2]§e[0]§b插件出现异常，异常信息如下：\n" +
                                           "§2[§eLMCCore§2]§6异常类型：§a[1]\n" +
                                           "§2[§eLMCCore§2]§6异常说明：§a[2]\n" +
                                           "§2[§eLMCCore§2]§6异常栈：§f\n" +
                                           "[3]\n" +
                                           "§2[§eLMCCore§2]以上为§e[0]§b插件的异常信息");
        messages.put(Locale.CHINESE, chinese);
    }

    public static String getMessage(String key, Object... args) {
        String msg = current.get(key);
        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("[" + i + "]", String.valueOf(args[i]));
        }
        return msg;
    }

    public static void setLocale(Locale locale) {
        current = messages.getOrDefault(locale, current);
    }

}
