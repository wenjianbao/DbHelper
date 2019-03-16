package wen.jianbao.helper;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import wen.jianbao.helper.sqlhelper.Paging;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 模型 助手类
 *
 * @author php-note.com
 */
@SuppressWarnings({"serial"})
public class ModelHelper<M extends Model<M>> extends SqlHelper<ModelHelper<M>> {
    private M dao;    // 模型实例，专门用来执行SQL的

//    /**
//     * 创建 ModelHelper 实例
//     *【注意】因为泛型被擦除，此方法没达到预期效果
//     */
//    public static <T extends Model<T>> ModelHelper create(T model) {
//        return new ModelHelper<>(model);
//    }

    /**
     * 构造方法
     *
     * @param dao 模型实例
     */
    public ModelHelper(M dao) {
        this.dao = dao;
    }

    /**
     * 获取 查询记录列表
     */
    public List<M> findAll(String... table) {
        String sql = getCompiledSelect(table);
        return dao.find(sql);
    }

    /**
     * 获取 单条 查询记录
     */
    public M findFirst(String... table) {
        limit(1);
        String sql = getCompiledSelect(table);
        return dao.findFirst(sql);
    }

    /**
     * 查询分页
     */
    public Page<M> findPage(int pageNumber, int... pageSize) {
        int    _pageSize   = pageSize.length > 0 ? pageSize[0] : 20;
        String totalRowSql = getCompiledSelectCount("", false);
        String findSql     = getCompiledSelect();

        return dao.paginateByFullSql(pageNumber, _pageSize, totalRowSql, findSql);
    }

    /**
     * 查询分页
     *
     * @param request    请求对象
     * @param pageNumber 当前分页数
     * @param pageSize   每页记录数
     * @return 分页对象
     */
    public Paging<M> findPaging(HttpServletRequest request, int pageNumber, int... pageSize) {
        int     _pageSize   = pageSize.length > 0 ? pageSize[0] : 20;
        String  totalRowSql = getCompiledSelectCount("", false);
        String  findSql     = getCompiledSelect();
        Page<M> page        = dao.paginateByFullSql(pageNumber, _pageSize, totalRowSql, findSql);

        return new Paging<>(request, page);
    }

}
