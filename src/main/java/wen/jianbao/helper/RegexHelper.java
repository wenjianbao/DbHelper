package wen.jianbao.helper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    public static int SPLIT_DEFAULT       = 0;
    public static int SPLIT_NO_EMPTY      = 1;
    public static int SPLIT_DELIM_CAPTURE = 2;

    /**
     * 分割字符串为字符串数组
     * 模拟PHP函数：preg_split()
     *
     * @param text  字符串
     * @param regex 正则表达式
     * @param limit 分割次数
     * @param flags 分割标识，默认为 0
     * @return 字符串数组
     */
    public static String[] split(String text, String regex, int limit, int flags) {
        if (limit < 0) {
            limit = 0;
        }
        if (flags < 0) {
            flags = SPLIT_DEFAULT;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        String  _flags              = Integer.toBinaryString(flags);
        boolean split_no_empty      = _flags.charAt(0) == '1';
        boolean split_delim_capture = (_flags.length() >= 2) && (_flags.charAt(1) == '1');

        List<String> list  = new ArrayList<>();
        int          prev  = 0;
        String       item  = "";
        int          count = 0;

        while (matcher.find()) {
            if (limit > 0 && count >= limit) {
                break;
            }

            item = text.substring(prev, matcher.start());
            prev = matcher.end();

            if (!split_no_empty || StringUtils.isNotBlank(item)) {
                list.add(item);
            }

            if (split_delim_capture && matcher.groupCount() > 0) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    item = matcher.group(i);
                    if (!split_no_empty || StringUtils.isNotBlank(item)) {
                        list.add(item);
                    }
                }
            }
            count++;
        }

        // 字符串剩余的部分
        if (prev < text.length() - 1) {
            item = text.substring(prev);
            if (!split_no_empty || StringUtils.isNotBlank(item)) {
                list.add(item);
            }
        }

        return list.toArray(new String[]{});
    }

    /**
     * 转义正则表达式字符
     * 模拟PHP函数：preg_quote()
     */
    public static String quote(String str, char... delimiter) {
        char[]        chars = new char[]{'.', '\\', '+', '*', '?', '[', '^', ']', '$', '(', ')', '{', '}', '=', '!', '<', '>', '|', ':', '-'};
        StringBuilder sb    = new StringBuilder();
        char          ch;
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (ArrayUtils.contains(chars, ch)) {
                sb.append('\\');
            } else if ((delimiter.length > 0) && (ch == delimiter[0])) {
                sb.append('\\');
            }
            sb.append(ch);
        }

        return sb.toString();
    }
}
