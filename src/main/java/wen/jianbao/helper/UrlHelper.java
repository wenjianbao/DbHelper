package wen.jianbao.helper;

import com.jfinal.kit.StrKit;
import org.apache.http.client.utils.URIBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 网址 助手类
 */
public class UrlHelper {
    private static String CHARSET = "UTF-8";

    /**
     * 生成URL
     *
     * @param url         基本网址
     * @param queryParams 查询参数
     * @return URL
     */
    public static String url(String url, Object... queryParams) {
        if (queryParams.length % 2 != 0) {
            throw new RuntimeException("Wrong number parameter of UrlHelper.url(), queryParams must key-value pair");
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            String     key        = "";
            if (queryParams.length > 0) {
                for (int i = 0; i < queryParams.length; i++) {
                    if (i % 2 == 0) {
                        key = String.valueOf(queryParams[i]);
                    } else {
                        uriBuilder.addParameter(key, String.valueOf(queryParams[i]));
                    }
                }
            }
            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    /**
     * 创建URL
     *
     * @param url        基本网址
     * @param queryParas 参数列表
     * @return 完整网址
     */
    public static String buildUrl(String url, Map<String, String> queryParas) {
        if (queryParas == null || queryParas.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        boolean       isFirst;
        if (url.indexOf('?') == -1) {
            isFirst = true;
            sb.append('?');
        } else {
            isFirst = false;
        }

        for (Map.Entry<String, String> entry : queryParas.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append('&');
            }

            String key   = entry.getKey();
            String value = entry.getValue();
            if (StrKit.notBlank(value)) {
                try {
                    value = URLEncoder.encode(value, CHARSET);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            sb.append(key).append('=').append(value);
        }

        return sb.toString();
    }

    /**
     * 获取当前页面URL
     */
    public static String getCurrentPageUrl(HttpServletRequest request) {
        return request.getRequestURL().toString()
            + ((request.getQueryString() != null) ? "?" + request.getQueryString() : "");
    }
}
