package wen.jianbao.helper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map助手类
 */
public class MapHelper {
    private static int ORDER_BY_KEY   = 1;  // 按键名排序
    private static int ORDER_BY_VALUE = 2;  // 按值排序
    public static  int ORDER_NUMERIC  = 1;  // 把每一项作为数字来处理
    public static  int ORDER_STRING   = 2;  // 把每一项作为字符串来处理

    /**
     * 按键值 升序排列
     */
    public static Map<String, Object> asort(Map<String, Object> map, int... orderValueType) {
        return _sort(map, ORDER_BY_VALUE, "ASC", orderValueType);
    }

    /**
     * 按键值 降序排列
     */
    public static Map<String, Object> arsort(Map<String, Object> map, int... orderValueType) {
        return _sort(map, ORDER_BY_VALUE, "DESC", orderValueType);
    }

    /**
     * 按键名 升序排序
     */
    public static Map<String, Object> ksort(Map<String, Object> map, int... orderValueType) {
        return _sort(map, ORDER_BY_KEY, "ASC", orderValueType);
    }

    /**
     * 按键名 降序排序
     */
    public static Map<String, Object> krsort(Map<String, Object> map, int... orderValueType) {
        return _sort(map, ORDER_BY_KEY, "DESC", orderValueType);
    }

    /**
     * [一维] 排序
     * 注意：不改变原输入对象，即 返回的是新（排好序的）对象
     *
     * @param map            排序对象
     * @param orderBy        排序类型，1:按键名，2:按键值 排序
     * @param orderType      排序方式，ASC:升序，DESC:降序
     * @param orderValueType 排序值类型，1:数字类型，2:字符串类型
     * @return 排序结果
     */
    private static Map<String, Object> _sort(Map<String, Object> map, int orderBy, String orderType, int... orderValueType) {
        if (map == null) {
            return null;
        }
        int                             _orderValueType = orderValueType.length > 0 ? orderValueType[0] : ORDER_STRING;
        Map<String, Object>             sortedMap       = new LinkedHashMap<>();
        List<Map.Entry<String, Object>> entryList       = new ArrayList<>(map.entrySet());

        String _orderType = orderType.toUpperCase();
        entryList.sort(new Comparator<Map.Entry<String, Object>>() {
            @Override
            public int compare(Map.Entry<String, Object> entry1, Map.Entry<String, Object> entry2) {
                if (_orderValueType == ORDER_NUMERIC) {
                    if (_orderType.equals("ASC")) {   // 升序
                        return _getInt(
                                orderBy == ORDER_BY_KEY ? entry1.getKey() : entry1.getValue().toString()
                        ).compareTo(_getInt(
                                orderBy == ORDER_BY_KEY ? entry2.getKey() : entry2.getValue().toString()
                        ));
                    } else {                        // 降序
                        return _getInt(
                                orderBy == ORDER_BY_KEY ? entry2.getKey() : entry2.getValue().toString()
                        ).compareTo(_getInt(
                                orderBy == ORDER_BY_KEY ? entry1.getKey() : entry1.getValue().toString()
                        ));
                    }
                } else {
                    if (_orderType.equals("ASC")) {   // 升序
                        return (
                                orderBy == ORDER_BY_KEY ? entry1.getKey() : entry1.getValue().toString()
                        ).compareTo(
                                orderBy == ORDER_BY_KEY ? entry2.getKey() : entry2.getValue().toString()
                        );
                    } else {                        // 降序
                        return (
                                orderBy == ORDER_BY_KEY ? entry2.getKey() : entry2.getValue().toString()
                        ).compareTo(
                                orderBy == ORDER_BY_KEY ? entry1.getKey() : entry1.getValue().toString()
                        );
                    }
                }
            }
        });

        for (Map.Entry<String, Object> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private static Integer _getInt(Object str) {
        int i = 0;
        try {
            Pattern p = Pattern.compile("^\\d+");
            Matcher m = p.matcher(str.toString());
            if (m.find()) {
                i = Integer.valueOf(m.group());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return i;
    }

    /**
     * 获取 map keys 数组
     */
    public static String[] keys(Map<String, Object> map) {
        return map.keySet().toArray(new String[]{});
    }

    /**
     * 获取 map values 数组
     */
    public static Object[] values(Map<String, Object> map) {
        return map.values().toArray(new Object[]{});
    }

}
