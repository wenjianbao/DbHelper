package wen.jianbao.helper.sqlhelper;

import com.jfinal.plugin.activerecord.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 分页类
 */
public class Paging<T> {
    private HttpServletRequest request;
    private Page<T>            page;

    public Paging(HttpServletRequest request, Page<T> page) {
        this.request = request;
        this.page = page;
    }

    public List<T> getList() {
        return page.getList();
    }

    public int getPageNumber() {
        return page.getPageNumber();
    }

    public int getPageSize() {
        return page.getPageSize();
    }

    public int getTotalPage() {
        return page.getTotalPage();
    }

    public int getTotalRow() {
        return page.getTotalRow();
    }

    @Override
    public String toString() {
        return page.toString();
    }

    /**
     * 获取 分页条
     */
    public String getPageStr() {
        return new Pagination(request, getTotalRow(), getPageSize(),
                getPageNumber()).show();
    }
}
