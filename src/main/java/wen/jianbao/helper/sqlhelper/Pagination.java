package wen.jianbao.helper.sqlhelper;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import wen.jianbao.helper.UrlHelper;

import javax.servlet.http.HttpServletRequest;

public class Pagination {
    private HttpServletRequest request;             // Http请求对象
    private int                totalRows   = 0;     // 总记录数
    private int                pageSize    = 10;    // 每页记录数
    private int                curPage     = 1;     // 当前分页
    private int                totalPages  = 1;     // 总共能显示的（有效）页数 ----> 计算出来的结果
    private int                maxPages    = 0;     // 最大能显示的页数（比如说，本来总共有250页，但是想让它只显示前50页）
    private int                columnPages = 5;     // 分页栏显示的页数
    private String             pageUrl     = "";    // 分页url地址
    private String             inAjax      = "";    // 是否为ajax请求，"1" 是，"0" 否
    private String             showId      = "";    // ajax显示的div
    private String             toDiv       = "";    // 要跳转到的div

    public Pagination(final HttpServletRequest request, int curPage, int pageSize, int totalRows) {
        this.request = request;
        this.curPage = curPage;
        this.pageSize = pageSize;
        this.totalRows = totalRows;
    }

    /**
     * 初始化
     */
    protected void _init() {
        // 分页url
        if (StringUtils.isBlank(pageUrl)) {
            pageUrl = UrlHelper.getCurrentPageUrl(request);
        }

        pageUrl = RegExUtils.replacePattern(pageUrl, "&?(?:page|inAjax|showId)=[^&=]+", "");

        // inAjax
        if (request.getParameter("inAjax") != null && request.getParameter("inAjax").equals("1")) {
            inAjax = "1";
        }

        // showId
        if (StringUtils.isBlank(showId) && inAjax.equals("1") && request.getParameter("showId") != null) {
            showId = request.getParameter("showId");
        }

        // columnPages
        if (request.getParameter("columnPages") != null && NumberUtils.isDigits(request.getParameter("columnPages"))) {
            columnPages = Integer.parseInt(request.getParameter("columnPages"));
        }

        // 分页数
        if (totalRows > pageSize) {
            totalPages = (int) Math.ceil((double) totalRows / pageSize);
            if (maxPages > 0 && maxPages < totalPages) {
                totalPages = maxPages;
            }

            // 校正当前页
            curPage = Math.max(curPage, 1);
            curPage = Math.min(curPage, totalPages);
        }
    }

    /**
     * 显示分页
     */
    public String show() {
        // 初始化
        _init();
        
        StringBuilder sb     = new StringBuilder();
        int           offset = (int) Math.ceil((double) columnPages / 2) - 1;
        int           from   = 1;
        int           to     = 1;

        if (totalPages < columnPages) {
            to = totalPages;
        } else {
            from = curPage - offset;
            to = from + columnPages - 1;

            if (from < 1) {
                from = 1;
                to = columnPages;
            } else if (to > totalPages) {
                from = totalPages - columnPages + 1;
                to = totalPages;
            }
        }

        String urlPlus = StringUtils.isNotBlank(toDiv) ? "#" : "";

        // 首页
        if (from > 1) {
            sb.append("<li><a ");
            if (inAjax.equals("1")) {
                sb.append("href=\"javascript:;\" onclick=\"ajax_send_get('")
                        .append(UrlHelper.url(pageUrl, "page", 1))
                        .append("','").append(showId).append("')\"");
            } else {
                sb.append("href=\"").append(UrlHelper.url(pageUrl, "page", 1))
                        .append(urlPlus).append("\"");
            }
            sb.append(" class=\"first\">首页</a></li>");
        }

        // 上一页
        if (curPage > 1) {
            sb.append("<li><a ");
            if (inAjax.equals("1")) {
                sb.append("href=\"javascript:;\" onclick=\"ajax_send_get('")
                        .append(UrlHelper.url(pageUrl, "page", curPage - 1))
                        .append("','").append(showId).append("')\"");
            } else {
                sb.append("href=\"").append(UrlHelper.url(pageUrl, "page", curPage - 1))
                        .append(urlPlus).append("\"");
            }
            sb.append(" class=\"prev\">上一页</a></li>");
        }

        // 中间数字页
        for (int i = from; i <= to; i++) {
            if (i == curPage) {
                sb.append("<li class=\"active\"><a>").append(i).append("</a></li>");
            } else {
                sb.append("<li><a ");
                if (inAjax.equals("1")) {
                    sb.append("href=\"javascript:;\" onclick=\"ajax_send_get('")
                            .append(UrlHelper.url(pageUrl, "page", i))
                            .append("','").append(showId).append("')\"");
                } else {
                    sb.append("href=\"").append(UrlHelper.url(pageUrl, "page", i))
                            .append(urlPlus).append("\"");
                }
                sb.append(">").append(i).append("</a></li>");
            }
        }

        // 下一页
        if (curPage < totalPages) {
            sb.append("<li><a ");
            if (inAjax.equals("1")) {
                sb.append("href=\"javascript:;\" onclick=\"ajax_send_get('")
                        .append(UrlHelper.url(pageUrl, "page", curPage + 1))
                        .append("','").append(showId).append("')\"");
            } else {
                sb.append("href=\"").append(UrlHelper.url(pageUrl, "page", curPage + 1))
                        .append(urlPlus).append("\"");
            }
            sb.append(" class=\"next\">下一页</a></li>");
        }

        // 末页
        if (to < totalPages) {
            sb.append("<li><a ");
            if (inAjax.equals("1")) {
                sb.append("href=\"javascript:;\" onclick=\"ajax_send_get('")
                        .append(UrlHelper.url(pageUrl, "page", totalPages))
                        .append("','").append(showId).append("')\"");
            } else {
                sb.append("href=\"").append(UrlHelper.url(pageUrl, "page", totalPages))
                        .append(urlPlus).append("\"");
            }
            sb.append(" class=\"last\">末页</a></li>");
        }

        // 总记录数
        if (sb.length() != 0) {
            sb.insert(0, "<ul class=\"pagination clearfix\">")
                    .append("<li><a style=\"cursor:default\">共")
                    .append(totalRows).append("条</a></li></ul>");
        }

        return sb.toString();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public int getColumnPages() {
        return columnPages;
    }

    public void setColumnPages(int columnPages) {
        this.columnPages = columnPages;
    }

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getToDiv() {
        return toDiv;
    }

    public void setToDiv(String toDiv) {
        this.toDiv = toDiv;
    }
}
