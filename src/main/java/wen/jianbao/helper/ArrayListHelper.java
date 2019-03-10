package wen.jianbao.helper;

import java.util.*;

/**
 * ArrayList 助手类
 */
public class ArrayListHelper {
    /**
     * 并集
     */
    @SafeVarargs
    public static <T> ArrayList<T> merge(final ArrayList<T>... lists) {
        ArrayList<T> mergeList = new ArrayList<>();
        
        if (lists.length == 0) {
            return mergeList;
        }
        
        for (ArrayList<T> list : lists) {
            mergeList.addAll(list);
        }

        return mergeList;
    }

    /**
     * 交集
     * 相同的元素
     */
    @SafeVarargs
    public static <T> ArrayList<T> intersect(final ArrayList<T>... lists) {
        ArrayList<T> intersectList = new ArrayList<>();
        
        if (lists.length == 0) {
            return intersectList;
        }

        // 生成一组以数组元素为key，相同元素的个数为value的中间键值对
        HashMap<T, Integer> hashMap = new HashMap<>();
        for (ArrayList<T> list : lists) {
            for (T item : list) {
                if (hashMap.containsKey(item)) {
                    hashMap.put(item, hashMap.get(item) + 1);
                } else {
                    hashMap.put(item, 1);
                }
            }
        }

        // 统计出相同元素个数正好是数组（参数）个数的元素，即每个数组中都有的元素
        for (Map.Entry<T, Integer> entry : hashMap.entrySet()) {
            if (entry.getValue() == lists.length) {
                intersectList.add(entry.getKey());
            }
        }

        return intersectList;
    }

    /**
     * 差集
     * 属于 list1 而不属于任何一个 list2, list3, list4...
     */
    @SafeVarargs
    public static <T> ArrayList<T> diff(final ArrayList<T>... lists) {
        ArrayList<T> diffList = new ArrayList<>();
        
        if (lists.length == 0) {
            return diffList;
        }

        // 生成一组以数组元素为key，相同元素的个数为value的中间键值对
        HashMap<T, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < lists.length; i++) {
            if (i == 0) {
                for (T item : lists[i]) {
                    hashMap.put(item, 1);
                }
            } else {
                for (T item : lists[i]) {
                    if (hashMap.containsKey(item)) {
                        hashMap.put(item, hashMap.get(item) + 1);
                    }
                }
            }

        }

        // 统计出相同元素个数为1的元素，即只在第一个数组中出现的元素
        for (Map.Entry<T, Integer> entry : hashMap.entrySet()) {
            if (entry.getValue() == 1) {
                diffList.add(entry.getKey());
            }
        }

        return diffList;
    }

    /**
     * 去重
     * 无重复并集
     */
    public static <T> ArrayList<T> unique(final ArrayList<T> list) {
        Set<T> hashSet = new HashSet<>(list);
        return new ArrayList<>(hashSet);
    }

    /**
     * 反转
     * 如果允许直接改变原数组，可用Java自带方法 Collections.reverse()
     */
    public static  <T> ArrayList<T> reverse(final ArrayList<T> list) {
        ArrayList<T> reverseList = new ArrayList<>();
        
        if (list == null) {
            return reverseList;
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            reverseList.add(list.get(i));
        }

        return reverseList;
    }
}
