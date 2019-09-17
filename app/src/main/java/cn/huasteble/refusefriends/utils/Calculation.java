package cn.huasteble.refusefriends.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author teble
 * @date 2019/9/1 20:25
 */
public class Calculation {
    public static int getBkn(String sKey) {
        int base = 5381;
        for (int i = 0; i < sKey.length(); i++) {
            base += (base << 5) + sKey.charAt(i);
        }
        return base & 2147483647;
    }

    public static String getSKey(String cookies) {
        String sKey;
        String[] strings = cookies.split("; ");
        Map<String, String> map = new HashMap<>();
        for (String string : strings) {
            if (string.isEmpty()) {
                continue;
            }
            String[] str = string.split("=");
            if (str.length == 2) {
                map.put(str[0], str[1]);
            } else {
                map.put(str[0], "");
            }
        }
        sKey = map.get("skey");
        return sKey;
    }
}
