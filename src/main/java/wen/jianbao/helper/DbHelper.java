package wen.jianbao.helper;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wen.jianbao.helper.sqlhelper.Paging;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 数据库 助手类
 *
 * @author php-note.com
 */
@SuppressWarnings({"serial", "unchecked"})
public class DbHelper extends SqlHelper<DbHelper> {
    private String configName = "";
    private Logger logger     = LoggerFactory.getLogger(DbHelper.class);

    /**
     * 创建 DbHelper 实例
     */
    public static DbHelper create(String... configName) {
        return new DbHelper(configName);
    }

    /**
     * 切换 数据库连接
     */
    private static DbPro use(String configName) {
        if (StringUtils.isNotBlank(configName)) {
            return Db.use(configName);
        }
        return Db.use();
    }
    
    /**
     * 构造方法
     *
     * @param configName 数据库配置节点名
     */
    public DbHelper(String... configName) {
        if (configName.length > 0) {
            this.configName = configName[0];
        }
    }

    /**
     * 获取 所有 查询记录
     */
    public List<Record> findAll(String... table) {
        String sql = getCompiledSelect(table);
        return use(configName).find(sql);
    }

    /**
     * 获取 单条 查询记录
     */
    public Record findFirst(String... table) {
        limit(1);
        String sql = getCompiledSelect(table);
        return use(configName).findFirst(sql);
    }

    /**
     * 统计查询结果记录数
     */
    public int findCount(String table, boolean... reset) {
        String sql = getCompiledSelectCount(table, reset);
        return use(configName).queryInt(sql);
    }

    public int findCount(boolean... reset) {
        return findCount("", reset);
    }

    /**
     * 查询分页
     */
    public Page<Record> findPage(int pageNumber, int... pageSize) {
        int    _pageSize   = pageSize.length > 0 ? pageSize[0] : 20;
        String totalRowSql = getCompiledSelectCount("", false);
        String findSql     = getCompiledSelect();

        return use(configName).paginateByFullSql(pageNumber, _pageSize, totalRowSql, findSql);
    }

    /**
     * 查询分页
     *
     * @param request    请求对象
     * @param pageNumber 当前分页数
     * @param pageSize   每页记录数
     * @return 分页对象
     */
    public Paging<Record> findPaging(HttpServletRequest request, int pageNumber, int... pageSize) {
        int          _pageSize   = pageSize.length > 0 ? pageSize[0] : 20;
        String       totalRowSql = getCompiledSelectCount("", false);
        String       findSql     = getCompiledSelect();
        Page<Record> page        = use(configName).paginateByFullSql(pageNumber, _pageSize, totalRowSql, findSql);

        return new Paging<>(request, page);
    }

    /**
     * 批量 插入
     *
     * @param table  要插入的表
     * @param sets   插入值的关联数组
     * @param escape 是否转义值和标识符
     * @return 影响记录数
     */
    public int insertBatch(String table, List<Map<String, Object>> sets, boolean... escape) {
        if (sets != null) {
            setInsertBatch(sets, escape);
        }

        // 没有有效的数据数组
        if (shSetInsertBatch.size() == 0) {
            return _getErrorReturn("db_must_use_setInsertBatch", 0);
        }

        if (StringUtils.isBlank(table)) {
            if (shFrom.size() == 0) {
                return _getErrorReturn("db_must_set_table", 0);
            }
            table = shFrom.get(0);
        }

        // 批处理
        String sql          = "";
        int    affectedRows = 0;
        int    i            = 0;
        int    inc          = 2;
        int    toInt        = 0;

        try {
            for (i = 0; i < shSetInsertBatch.size(); i += inc) {
                toInt = i + inc >= shSetInsertBatch.size() ? shSetInsertBatch.size() : i + inc;
                sql = _insertBatch(protectIdentifiers(table, true, false, escape), shSetInsertKeys, shSetInsertBatch.subList(i, toInt));
                affectedRows += use(configName).update(sql);
            }
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        _resetWrite();

        return affectedRows;
    }

    public int insertBatch(boolean... escape) {
        return insertBatch("", null, escape);
    }

    /**
     * 插入（单条）记录
     */
    public int insert(String table, Map<String, Object> set, boolean... escape) {
        if (set != null) {
            set(set, escape);
        }

        if (!_validateInsert(table)) {
            return 0;
        }

        String sql = _insert(
                protectIdentifiers(shFrom.get(0), true, false, escape),
                MapHelper.keys(shSet),
                MapHelper.values(shSet)
        );

        _resetWrite();

        int affectedRows = 0;
        try {
            affectedRows = use(configName).update(sql);
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        return affectedRows;
    }

    public int insert(String table, boolean... escape) {
        return insert(table, null, escape);
    }

    public int insert(boolean... escape) {
        return insert("", null, escape);
    }

    /**
     * 替换 记录
     */
    public int replace(String table, Map<String, Object> set) {
        if (set != null) {
            set(set);
        }

        if (shSet.size() == 0) {
            return _getErrorReturn("db_must_use_set", 0);
        }

        if (StringUtils.isBlank(table)) {
            if (shFrom.size() == 0) {
                return _getErrorReturn("db_must_set_table", 0);
            }
            table = shFrom.get(0);
        }

        String sql = _replace(
                protectIdentifiers(table, true, false),
                MapHelper.keys(shSet),
                MapHelper.values(shSet)
        );

        _resetWrite();

        int affectedRows = 0;
        try {
            affectedRows = use(configName).update(sql);
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        return affectedRows;
    }

    public int replace(String table) {
        return replace("", null);
    }
    
    public int replace(Map<String, Object>set) {
        return replace("", set);
    }

    /**
     * 更新 UPDATE
     *
     * @param table    操作的表
     * @param set      设置数据
     * @param whereStr 查询条件字符串
     * @param whereMap 查询条件Map
     * @param limit    限制的数量
     * @return 影响的行数
     */
    protected int _update(String table, Map<String, Object> set, String whereStr, Map<String, Object> whereMap, int... limit) {
        // 合并缓存
        _mergeCache();

        if (set != null) {
            set(set);
        }

        if (!_validateUpdate(table)) {
            return 0;
        }

        if (StringUtils.isNotBlank(whereStr)) {
            where(whereStr);
        }

        if (whereMap != null) {
            where(whereMap);
        }

        if (limit.length > 0) {
            limit(limit[0]);
        }

        String sql = _update(shFrom.get(0), shSet);
        _resetWrite();

        int affectedRows = 0;
        try {
            affectedRows = use(configName).update(sql);
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        return affectedRows;
    }

    public int update(String table, Map<String, Object> set, String whereStr, int... limit) {
        return _update(table, set, whereStr, null, limit);
    }

    public int update(String table, Map<String, Object> set, Map<String, Object> whereMap, int... limit) {
        return _update(table, set, "", whereMap, limit);
    }

    public int update(String table, Map<String, Object> set, int... limit) {
        return _update(table, set, "", null, limit);
    }
    
    public int update(String table, int... limit) {
        return _update(table, null, "", null, limit);
    }

    public int update(int... limit) {
        return _update("", null, "", null, limit);
    }

    /**
     * 批量更新
     *
     * @param table 表名
     * @param sets  批量更新的数据
     * @param index [必填] 查询条件关键字
     * @return int
     */
    public int updateBatch(String table, List<Map<String, Object>> sets, String index) {
        // 合并缓存
        _mergeCache();

        if (StringUtils.isBlank(index)) {
            return _getErrorReturn("db_must_use_index", 0);
        }

        if (sets != null) {
            setUpdateBatch(sets, index);
        }

        if (shSetUpdateBatch.size() == 0) {
            return _getErrorReturn("db_must_use_setUpdateBatch", 0);
        }

        if (StringUtils.isBlank(table)) {
            if (shFrom.size() == 0) {
                return _getErrorReturn("db_must_set_table", 0);
            }
            table = shFrom.get(0);
        }

        String sql          = "";
        int    affectedRows = 0;
        int    i            = 0;
        int    inc          = 100;
        int    toInt        = 0;

        try {
            for (i = 0; i < shSetUpdateBatch.size(); i += inc) {
                toInt = i + inc >= shSetUpdateBatch.size() ? shSetUpdateBatch.size() : i + inc;
                sql = _updateBatch(
                        protectIdentifiers(table, true, false),
                        shSetUpdateBatch.subList(i, toInt),
                        protectIdentifiers(index)
                );
                affectedRows += use(configName).update(sql);
                shWhere.clear();
            }
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        _resetWrite();

        return affectedRows;
    }

    /**
     * 删除表记录
     */
    public int emptyTable(String... table) {
        String _table;
        if (table.length == 0 || StringUtils.isBlank(table[0])) {
            if (shFrom.size() == 0) {
                return _getErrorReturn("db_must_set_table", 0);
            }
            _table = shFrom.get(0);
        } else {
            _table = protectIdentifiers(table[0], true, false);
        }

        String sql = _delete(_table);
        _resetWrite();

        int affectedRows = 0;
        try {
            affectedRows = use(configName).update(sql);
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        return affectedRows;
    }

    /**
     * 清空表
     */
    public int truncate(String... table) {
        String _table;
        if (table.length == 0 || StringUtils.isBlank(table[0])) {
            if (shFrom.size() == 0) {
                return _getErrorReturn("db_must_set_table", 0);
            }
            _table = shFrom.get(0);
        } else {
            _table = protectIdentifiers(table[0], true, false);
        }

        String sql = _truncate(_table);
        _resetWrite();

        int affectedRows = 0;
        try {
            affectedRows = use(configName).update(sql);
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        return affectedRows;
    }

    /**
     * 删除 记录
     *
     * @param table    操作的表名
     * @param whereStr 查询条件字符串
     * @param whereMap 查询条件Map
     * @param limit    记录数
     * @param reset    是否重置查询器
     * @return 删除的数量
     */
    protected int _delete(String table, String whereStr, Map<String, Object> whereMap, int limit, boolean... reset) {
        // 合并缓存
        _mergeCache();

        if (StringUtils.isBlank(table)) {
            if (shFrom.size() == 0) {
                return _getErrorReturn("db_must_set_table", 0);
            }
            table = shFrom.get(0);
        } else {
            table = protectIdentifiers(table, true, false);
        }

        if (StringUtils.isNotBlank(whereStr)) {
            where(whereStr);
        }

        if (whereMap != null) {
            where(whereMap);
        }

        if (limit > 0) {
            limit(limit);
        }

        if (shWhere.size() == 0) {
            return _getErrorReturn("db_del_must_use_where", 0);
        }

        String sql = _delete(table);

        if (reset.length == 0 || reset[0]) {
            _resetWrite();
        }

        int affectedRows = 0;
        try {
            affectedRows = use(configName).update(sql);
        } catch (Exception ex) {
            logger.error("db_query_error", ex);
        }

        return affectedRows;
    }

    public int delete(String table, Map<String, Object> whereMap, int limit, boolean... reset) {
        return _delete(table, "", whereMap, limit, reset);
    }

    public int delete(String table, String whereStr, int limit, boolean... reset) {
        return _delete(table, whereStr, null, limit, reset);
    }

    public int delete(String table, Map<String, Object> whereMap, boolean... reset) {
        return _delete(table, "", whereMap, 0, reset);
    }
    
    public int delete(String table, String whereStr, boolean... reset) {
        return _delete(table, whereStr, null, 0, reset);
    }
    
    public int delete(String table, boolean... reset) {
        return _delete(table, "", null, 0, reset);
    }

    public int delete(boolean... reset) {
        return _delete("", "", null, 0, reset);
    }
}
