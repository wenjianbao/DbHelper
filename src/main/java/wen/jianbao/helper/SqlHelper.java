package wen.jianbao.helper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wen.jianbao.helper.sqlhelper.Groupby;
import wen.jianbao.helper.sqlhelper.Orderby;
import wen.jianbao.helper.sqlhelper.Where;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL生成器
 *
 * @author php-note.com
 */
@SuppressWarnings({"serial", "unchecked"})
public class SqlHelper<T extends SqlHelper> {
    protected boolean  dbDebug              = false;                               // 调试标记
    protected String   dbPrefix             = "";                                  // 表前缀
    protected String   swapPrefix           = "";                                  // 交换前缀
    protected boolean  _protectIdentifiers  = true;                                // 保护标识符标记（即是否需要转义 标识符）
    protected String[] _reservedIdentifiers = new String[]{"*"};                   // 不能转义的标识符（即哪些是不需要转义的标识符）
    protected String   _escapeChar          = "`";                                  // 标识符转义字符（通常为键盘左上角的转义字符）
    protected String   _likeEscapeStr       = " ESCAPE '%s' ";                      // 转义声明字符串
    protected String   _likeEscapeChr       = "!";                                  // 转义字符
    protected String[] _randomKeyword       = new String[]{"RAND()", "RAND(%s)"};   // 随机关键字排序
    protected String   _countString         = "SELECT COUNT(*) AS ";                // COUNT 字符串
    protected String[] _operators           = new String[12];                       // SQL 字符串分隔符

    protected List<String>  shSelect   = new ArrayList<>();                         // SELECT 数据
    protected boolean       shDistinct = false;                                     // DISTINCT 标识开关
    protected List<String>  shFrom     = new ArrayList<>();                         // FROM 数据
    protected List<String>  shJoin     = new ArrayList<>();                         // JOIN 数据
    protected List<Where>   shWhere    = new ArrayList<>();                         // WHERE 数据
    protected List<Groupby> shGroupby  = new ArrayList<>();                         // GROUP BY 数据
    protected List<Where>   shHaving   = new ArrayList<>();                         // HAVING 数据
    protected int           shLimit    = 0;                                         // LIMIT 数据
    protected int           shOffset   = 0;                                         // OFFSET 数据
    protected List<Orderby> shOrderby  = new ArrayList<>();                         // ORDER BY 数据

    /*********************************************************************
     * 数据 插入、修改 相关
     *********************************************************************/
    protected Map<String, Object>       shSet            = new LinkedHashMap<>();   // 插入、修改 单条记录
    protected List<String>              shSetInsertKeys  = new ArrayList<>();       // 批量插入 多条记录 所需【表字段】
    protected List<Object>              shSetInsertBatch = new ArrayList<>();       // 批量插入 多条记录
    protected List<Map<String, Object>> shSetUpdateBatch = new ArrayList<>();       // 批量修改 多条记录

    protected List<String> shAliasedTables     = new ArrayList<>();                 // 别名表列表
    protected boolean      shWhereGroupStarted = false;                             // Where组起始标识
    protected int          shWhereGroupCount   = 0;                                 // Where组计数

    /*********************************************************************
     * 缓存 变量
     *********************************************************************/
    protected boolean              shCaching       = false;                         // 缓存开关标识符
    protected Map<String, Boolean> shCacheExists   = new HashMap<>();               // 缓存存在列表（使用 Map 是为了方便去重）
    protected List<String>         shCacheSelect   = new ArrayList<>();             // 缓存 SELECT 数据
    protected List<String>         shCacheFrom     = new ArrayList<>();             // 缓存 FROM 数据
    protected List<String>         shCacheJoin     = new ArrayList<>();             // 缓存 JOIN 数据
    protected List<Where>          shCacheWhere    = new ArrayList<>();             // 缓存 WHERE 数据
    protected List<Groupby>        shCacheGroupby  = new ArrayList<>();             // 缓存 GROUP BY 数据
    protected List<Where>          shCacheHaving   = new ArrayList<>();             // 缓存 HAVING 数据
    protected List<Orderby>        shCacheOrderby  = new ArrayList<>();             // 缓存 ORDER BY 数据
    protected List<String>         shCacheSet      = new ArrayList<>();             // 缓存 修改 数据
    protected List<Boolean>        shNoEscape      = new ArrayList<>();             // 转义 数据
    protected List<Boolean>        shCacheNoEscape = new ArrayList<>();             // 缓存 转义数据

    private Logger logger = LoggerFactory.getLogger(SqlHelper.class);

    /**
     * 查询 字段
     * <p>
     * 生成查询的选择部分
     *
     * @param select 字段
     * @param escape 是否转义
     * @return SqlHelper
     */
    public T select(String select, boolean... escape) {
        if (StringUtils.isBlank(select)) {
            select = "*";
        }

        String[] _select = StringUtils.split(select, ",");

        // 是否转义
        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        for (String field : _select) {
            field = field.trim();

            if (StringUtils.isBlank(field)) {
                continue;
            }

            shSelect.add(field);
            shNoEscape.add(_escape);

            if (shCaching) {
                shCacheSelect.add(field);
                shCacheExists.put("select", true);
                shCacheNoEscape.add(_escape);
            }
        }

        return (T) this;
    }

    /**
     * 查询 最大值
     * <p>
     * 生成查询的SELECT MAX(字段)部分
     *
     * @param select 字段
     * @param alias  别名
     * @return SqlHelper
     */
    public T selectMax(String select, String... alias) {
        String _alias = alias.length > 0 ? alias[0] : "";
        return _maxMinAvgSum(select, _alias, "MAX");
    }

    /**
     * 查询 最小值
     * <p>
     * 生成查询的SELECT MIN(字段)部分
     *
     * @param select 字段
     * @param alias  别名
     * @return SqlHelper
     */
    public T selectMin(String select, String... alias) {
        String _alias = alias.length > 0 ? alias[0] : "";
        return _maxMinAvgSum(select, _alias, "MIN");
    }

    /**
     * 查询 平均值
     * <p>
     * 生成查询的SELECT AVG(字段)部分
     *
     * @param select 字段
     * @param alias  别名
     * @return SqlHelper
     */
    public T selectAvg(String select, String... alias) {
        String _alias = alias.length > 0 ? alias[0] : "";
        return _maxMinAvgSum(select, _alias, "AVG");
    }

    /**
     * 查询 求和值
     * <p>
     * 生成查询的SELECT SUM(字段)部分
     *
     * @param select 字段
     * @param alias  别名
     * @return SqlHelper
     */
    public T selectSum(String select, String... alias) {
        String _alias = alias.length > 0 ? alias[0] : "";
        return _maxMinAvgSum(select, _alias, "SUM");
    }

    /**
     * 查询 [MAX|MIN|AVG|SUM]()
     *
     * <code>
     * selecMax()
     * selectMin()
     * selectAvg()
     * selectSum()
     * </code>
     *
     * @param select 查询的字段
     * @param alias  字段别名
     * @param type   查询类型
     * @return SqlHelper
     */
    protected T _maxMinAvgSum(String select, String alias, String type) {
        select = select.trim();
        alias = alias.trim();
        if (StringUtils.isBlank(select)) {
            throw new IllegalArgumentException("db_invalid_query");
        }

        type = StringUtils.upperCase(type);
        String[] types = new String[]{"MAX", "MIN", "AVG", "SUM"};

        if (!ArrayUtils.contains(types, type)) {
            throw new IllegalArgumentException("Invalid function type: " + type);
        }

        if (StringUtils.isBlank(alias)) {
            alias = _createAliasFromTable(select);
        }

        String _select = type + "(" + protectIdentifiers(select) + ") AS " + escapeIdentifiers(alias);

        shSelect.add(_select);
        shNoEscape.add(false);

        if (shCaching) {
            shCacheSelect.add(_select);
            shCacheExists.put("select", true);
        }

        return (T) this;
    }

    /**
     * 根据表确定别名
     *
     * @param item 选项
     * @return 表别名
     */
    protected String _createAliasFromTable(String item) {
        if (StringUtils.contains(item, ".")) {
            String[] arr = StringUtils.split(item, ".");
            return arr[arr.length - 1];
        }

        return item;
    }

    /**
     * 查询 去重
     * <p>
     * 设置一个标识，告诉查询字符串编译器添加DISTINCT
     *
     * @param val 标识值
     */
    public T distinct(boolean... val) {
        shDistinct = val.length == 0 || val[0];
        return (T) this;
    }

    /**
     * 设置 表名
     * <p>
     * 生成查询的FROM部分
     *
     * @param from 表名，多个值以","分隔
     * @return SqlHelper
     */
    public T from(String from) {
        if (StringUtils.contains(from, ",")) {
            String[] tables = StringUtils.split(from, ",");
            for (String table : tables) {
                from(table.trim());
            }
        } else {
            // 提取可能存在的别名。我们在protectIdentifiers()方法中使用此信息来决定是否需要添加表前缀
            _trackAliases(from);
            from = protectIdentifiers(from, true, false, true);
            shFrom.add(from);

            if (shCaching) {
                shCacheFrom.add(from);
                shCacheExists.put("from", true);
            }
        }

        return (T) this;
    }

    /**
     * 设置 连接查询
     * <p>
     * 生成查询的连接部分
     *
     * @param table  表名
     * @param cond   连接条件
     * @param type   连接类型（指定连接的类型，有这样几种选择：left，right，outer，inner，left outer 和 right outer）
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T join(String table, String cond, String type, boolean... escape) {
        type = type.trim().toUpperCase();
        if (StringUtils.isNotBlank(type)) {
            if (!ArrayUtils.contains(new String[]{"LEFT", "RIGHT", "OUTER", "INNER", "LEFT OUTER", "RIGHT OUTER"}, type)) {
                type = "";
            } else {
                type += " ";
            }
        }

        // 提取可能存在的别名。我们在protectIdentifiers()方法中使用此信息来决定是否需要添加表前缀
        _trackAliases(table);

        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        // 分离多个条件
        String regex  = "(?i)(\\sAND\\s|\\sOR\\s)";
        String regex2 = "(?i)([\\[\\]\\w.'-]+)(\\s*[^\"\\[`'\\w]+\\s*)(.+)";

        if (_escape && Pattern.compile(regex).matcher(cond).find()) {
            String[] list    = RegexHelper.split(cond, regex, -1, RegexHelper.SPLIT_NO_EMPTY | RegexHelper.SPLIT_DELIM_CAPTURE);
            Pattern  pattern = Pattern.compile(regex2);
            for (int i = 0; i < list.length; i++) {
                Matcher matcher = pattern.matcher(list[i]);
                if (matcher.find()) {
                    list[i] = protectIdentifiers(matcher.group(1)) + matcher.group(2) + protectIdentifiers(matcher.group(3));
                }
            }

            cond = " ON " + StringUtils.join(list);
        }
        // 分离条件并保护标识符
        // 例如：cond 等于 comments.id = blogs.id
        // 则 第0个分组为 comments.id = blogs.id，第1个分组为 comments.id，第2个分组为 =，第3个分组为 blogs.id 
        else if (_escape && Pattern.compile(regex2).matcher(cond).find()) {
            Matcher matcher = Pattern.compile(regex2).matcher(cond);
            if (matcher.find()) {
                cond = " ON " + protectIdentifiers(matcher.group(1)) + matcher.group(2) + protectIdentifiers(matcher.group(3));
            }
        } else if (!_hasOperator(cond)) {
            cond = " USING (" + (_escape ? escapeIdentifiers(cond) : cond) + ")";
        } else {
            cond = " ON " + cond;
        }

        // 是否要转义表名?
        if (_escape) {
            table = protectIdentifiers(table, true, false);
        }

        // 组装连接语句
        String join = type + "JOIN " + table + cond;
        shJoin.add(join);

        if (shCaching) {
            shCacheJoin.add(join);
            shCacheExists.put("join", true);
        }

        return (T) this;
    }

    public T join(String table, String cond, boolean... escape) {
        return join(table, cond, "", escape);
    }

    /**
     * 设置查询条件
     * <p>
     * 生成查询的WHERE部分
     * 使用“AND”分隔多个调用
     */
    public T where(String key, Object value, boolean... escape) {
        return _wh("sh_where", key, value, "AND ", escape);
    }

    public T where(String key, boolean... escape) {
        return _wh("sh_where", key, null, "AND ", escape);
    }

    public T where(Map<String, Object> map, boolean... escape) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            _wh("sh_where", entry.getKey(), entry.getValue(), "AND ", escape);
        }
        return (T) this;
    }

    /**
     * 设置 OR WHERE
     * <p>
     * 生成查询的WHERE部分
     * 使用“OR”分隔多个调用
     */
    public T orWhere(String key, Object value, boolean... escape) {
        return _wh("sh_where", key, value, "OR ", escape);
    }

    public T orWhere(Map<String, Object> map, boolean... escape) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            _wh("sh_where", entry.getKey(), entry.getValue(), "OR ", escape);
        }
        return (T) this;
    }

    /**
     * 设置 WHERE, HAVING
     *
     * <code>
     * where()
     * orWhere()
     * having()
     * orHaving()
     * </code>
     *
     * @param sh_key 类型关键字，如 'sh_where' 或 'sh_having'
     * @param key    查询的字段
     * @param value  查询的值
     * @param type   连接类型，如：AND，OR
     * @param escape 是否要转义标识符
     * @return SqlHelper
     */
    protected T _wh(String sh_key, String key, Object value, String type, boolean... escape) {
        if (StringUtils.isBlank(key)) {
            return (T) this;
        }

        List<Where> _sh_key       = sh_key.equals("sh_having") ? shHaving : shWhere;
        List<Where> _sh_cache_key = sh_key.equals("sh_having") ? shCacheHaving : shCacheWhere;

        // 如果未设置转义值，则将其取全局设置
        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        String prefix = (_sh_key.size() == 0) && (_sh_cache_key.size() == 0)
                ? _groupGetType("")
                : _groupGetType(type);

        if (value != null) {
            if (_escape) {
                value = " " + escape(value);
            }

            if (!_hasOperator(key)) {
                key += " = ";
            }
        } else if (!_hasOperator(key)) { // value == null
            key += " IS NULL";
        } else {  // value == null时，如：key = "name = " 
            //String  regex = "(?x)(?i)   \\s*   (   !?=  |  <>  |  IS  (?:\\s+NOT)?   )   \\s*   $";
            String  regex   = "(?i)\\s*(!?=|<>|IS(?:\\s+NOT)?)\\s*$";
            Matcher matcher = Pattern.compile(regex).matcher(key);
            if (matcher.find()) {
                key = key.substring(0, matcher.start()) + (matcher.group(1).equals("=") ? " IS NULL" : " IS NOT NULL");
            }
        }

        Where where = new Where();
        where.setCondition(prefix + key + (value == null ? "" : value));
        where.setEscape(_escape);
        _sh_key.add(where);

        if (shCaching) {
            _sh_cache_key.add(where);
            shCacheExists.put(sh_key.substring(3), true);
        }

        return (T) this;
    }

    /**
     * 设置 WHERE IN
     * <p>
     * 生成一个Where查询条件 IN('item', 'item')
     * 在适当的情况下加上“AND”
     *
     * @param key    查询的字段
     * @param values 查询的值
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T whereIn(String key, List<Object> values, boolean... escape) {
        return _whereIn(key, values, false, "AND ", escape);
    }

    /**
     * 设置 OR WHERE IN
     * <p>
     * 生成一个Where查询：field IN('item', 'item')
     * 在适当的情况下加上“OR”
     *
     * @param key    查询的字段
     * @param values 查询的值
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T orWhereIn(String key, List<Object> values, boolean... escape) {
        return _whereIn(key, values, false, "OR ", escape);
    }

    /**
     * 设置 WHERE NOT IN
     * <p>
     * 生成一个Where查询：field NOT IN('item', 'item')
     * 在适当的情况下加上“AND”
     *
     * @param key    查询的字段
     * @param values 查询的值
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T whereNotIn(String key, List<Object> values, boolean... escape) {
        return _whereIn(key, values, true, "AND ", escape);
    }

    /**
     * 设置 OR WHERE NOT IN
     * <p>
     * 生成一个Where查询：field NOT IN('item', 'item')
     * 在适当的情况下加上“OR”
     *
     * @param key    查询的字段
     * @param values 查询的值
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T orWhereNotIn(String key, List<Object> values, boolean... escape) {
        return _whereIn(key, values, true, "OR ", escape);
    }

    /**
     * 设置 Internal WHERE IN
     *
     * <code>
     * whereIn()
     * orWhereIn()
     * whereNotIn()
     * orWhereNotIn()
     * </code>
     *
     * @param key    查询的字段
     * @param values 查询的值
     * @param not    是否需要加上前缀 NOT，如： NOT IN
     * @param type   连接类型，如：AND，OR
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    protected T _whereIn(String key, List<Object> values, boolean not, String type, boolean... escape) {
        if (key == null || values == null) {
            return (T) this;
        }

        // 因为是传递引用，为防止原值被篡改，需要重新拷贝一份
        List<Object> _values = new ArrayList<>(values);

        boolean _escape = escape.length == 0 ? _protectIdentifiers : escape[0];
        String  _not    = not ? " NOT" : "";

        if (_escape) {
            for (int i = 0; i < _values.size(); i++) {
                _values.set(i, escape(_values.get(i)));
            }
        }

        String prefix = ((shWhere.size() == 0) && (shCacheWhere.size() == 0))
                ? _groupGetType("")
                : _groupGetType(type);

        Where where = new Where();
        where.setCondition(prefix + key + _not + " IN(" + StringUtils.join(_values, ", ") + ")");
        where.setEscape(_escape);
        _addWhere(where);

        return (T) this;
    }

    /**
     * 设置 LIKE
     * <p>
     * 生成查询的%LIKE%部分
     * 使用“AND”分隔多个调用
     *
     * @param field  查询的字段
     * @param match  查询的值
     * @param side   %在哪边，如：none，before，after
     * @param escape 是否转义
     * @return SqlHelper
     */
    public T like(String field, Object match, String side, boolean... escape) {
        return _like(field, match, "AND ", side, "", escape);
    }

    public T like(String field, Object match, boolean... escape) {
        return _like(field, match, "AND ", "both", "", escape);
    }

    public T like(Map<String, Object> fields, String side, boolean... escape) {
        return _like(fields, "AND ", side, "", escape);
    }

    public T like(Map<String, Object> fields, boolean... escape) {
        return _like(fields, "AND ", "both", "", escape);
    }

    /**
     * 设置 NOT LIKE
     * <p>
     * 生成查询的 NOT %LIKE% 部分
     * 使用“AND”分隔多个调用
     *
     * @param field  查询的字段
     * @param match  查询的值
     * @param side   %在哪边，如：none，before，after
     * @param escape 是否转义
     * @return SqlHelper
     */
    public T notLike(String field, Object match, String side, boolean... escape) {
        return _like(field, match, "AND ", side, "NOT", escape);
    }

    public T notLike(String field, Object match, boolean... escape) {
        return _like(field, match, "AND ", "both", "NOT", escape);
    }

    public T notLike(Map<String, Object> fields, String side, boolean... escape) {
        return _like(fields, "AND ", side, "NOT", escape);
    }

    public T notLike(Map<String, Object> fields, boolean... escape) {
        return _like(fields, "AND ", "both", "NOT", escape);
    }

    /**
     * 设置 OR LIKE
     * <p>
     * 生成查询的 %LIKE% 部分
     * 使用“OR”分隔多个调用
     *
     * @param field  查询的字段
     * @param match  查询的值
     * @param side   %在哪边，如：none，before，after
     * @param escape 是否转义
     * @return SqlHelper
     */
    public T orLike(String field, Object match, String side, boolean... escape) {
        return _like(field, match, "OR ", side, "", escape);
    }

    public T orLike(String field, Object match, boolean... escape) {
        return _like(field, match, "OR ", "both", "", escape);
    }

    public T orLike(Map<String, Object> fields, String side, boolean... escape) {
        return _like(fields, "OR ", side, "", escape);
    }

    public T orLike(Map<String, Object> fields, boolean... escape) {
        return _like(fields, "OR ", "both", "", escape);
    }

    /**
     * 设置 OR NOT LIKE
     * <p>
     * 生成查询的 NOT %LIKE% 部分
     * 使用“OR”分隔多个调用
     *
     * @param field  查询的字段
     * @param match  查询的值
     * @param side   %在哪边，如：none，before，after
     * @param escape 是否转义
     * @return SqlHelper
     */
    public T orNotLike(String field, Object match, String side, boolean... escape) {
        return _like(field, match, "OR ", side, "NOT", escape);
    }

    public T orNotLike(String field, Object match, boolean... escape) {
        return _like(field, match, "OR ", "both", "NOT", escape);
    }

    public T orNotLike(Map<String, Object> fields, String side, boolean... escape) {
        return _like(fields, "OR ", side, "NOT", escape);
    }

    public T orNotLike(Map<String, Object> fields, boolean... escape) {
        return _like(fields, "OR ", "both", "NOT", escape);
    }

    /**
     * 设置 Internal LIKE
     *
     * <code>
     * like()
     * orLike()
     * notLike()
     * orNotLike()
     * </code>
     *
     * @param field  查询的字段
     * @param match  查询的值
     * @param side   %在哪边，如：none，before，after
     * @param not    是否需要加上前缀 NOT，如：NOT LIKE
     * @param escape 是否转义
     * @return SqlHelper
     */
    protected T _like(String field, Object match, String type, String side, String not, boolean... escape) {
        String  _match  = match.toString();
        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        // 统一把 side 转为小写，防止有人书写格式不一样，如：用 'before' 替换 'BEFORE'
        side = side.toLowerCase();

        String prefix = (shWhere.size() == 0 && shCacheWhere.size() == 0)
                ? _groupGetType("")
                : _groupGetType(type);

        if (_escape) {
            match = escapeLikeStr(_match);
        }

        String like = "";
        switch (side) {
            case "none":
                like = prefix + " " + field + " " + not + " LIKE '" + _match + "'";
                break;
            case "before":
                like = prefix + " " + field + " " + not + " LIKE '%" + _match + "'";
                break;
            case "after":
                like = prefix + " " + field + " " + not + " LIKE '" + _match + "%'";
                break;
            default:
                like = prefix + " " + field + " " + not + " LIKE '%" + _match + "%'";
                break;
        }

        // 有些平台需要对类似通配符的转义序列定义
        if (_escape && StringUtils.isNotBlank(_likeEscapeStr)) {
            like += String.format(_likeEscapeStr, _likeEscapeChr);
        }

        Where where = new Where();
        where.setCondition(like);
        where.setEscape(_escape);
        _addWhere(where);

        return (T) this;
    }

    protected T _like(Map<String, Object> fields, String type, String side, String not, boolean... escape) {
        if (fields == null) {
            return (T) this;
        }

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            _like(entry.getKey(), entry.getValue(), type, side, not, escape);
        }

        return (T) this;
    }

    /**
     * 启动查询组
     *
     * @param not  NOT前缀值，如：NOT
     * @param type 连接类型，如 AND，OR
     * @return SqlHelper
     */
    private T _groupStart(String not, String type) {
        type = _groupGetType(type);
        shWhereGroupStarted = true;
        String prefix = (shWhere.size() == 0 && shCacheWhere.size() == 0) ? "" : type;

        Where where = new Where();
        where.setCondition(prefix + not + StringUtils.repeat(" ", ++shWhereGroupCount) + " (");
        where.setEscape(false);

        shWhere.add(where);
        if (shCaching) {
            shCacheWhere.add(where);
        }

        return (T) this;
    }

    /**
     * 启动查询组
     *
     * @return SqlHelper
     */
    public T groupStart() {
        return _groupStart("", "AND ");
    }

    /**
     * 启动查询组，但对该组进行 OR 操作
     *
     * @return SqlHelper
     */
    public T orGroupStart() {
        return _groupStart("", "OR ");
    }

    /**
     * 启动查询组，但对该组进行 NOT 操作
     *
     * @return SqlHelper
     */
    public T notGroupStart() {
        return _groupStart("NOT", "AND ");
    }

    /**
     * 启动查询组，但对该组进行 OR NOT 操作
     *
     * @return SqlHelper
     */
    public T orNotGroupStart() {
        return _groupStart("NOT", "OR ");
    }

    /**
     * 结束查询组
     *
     * @return SqlHelper
     */
    public T groupEnd() {
        shWhereGroupStarted = false;

        Where where = new Where();
        where.setCondition(StringUtils.repeat(" ", shWhereGroupCount--) + ")");
        where.setEscape(false);

        shWhere.add(where);
        if (shCaching) {
            shCacheWhere.add(where);
        }

        return (T) this;
    }

    /**
     * 获取 分组类型
     *
     * <code>
     * groupStart()
     * _like()
     * _wh()
     * _whereIn()
     * </code>
     *
     * @param type 连接类型，如：AND，OR
     * @return 连接类型字符串
     */
    protected String _groupGetType(String type) {
        if (shWhereGroupStarted) {
            type = "";
            shWhereGroupStarted = false;
        }

        return type;
    }

    /**
     * 设置 GROUP BY
     *
     * @param by     排序字段，多个值以“,”分隔，如：name desc,age asc
     * @param escape 是否转义
     * @return SqlHelper
     */
    public T groupBy(String by, boolean... escape) {
        boolean  _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;
        String[] bys;
        if (_escape) {
            bys = StringUtils.split(by, ",");
        } else {
            bys = new String[]{by};
        }

        return groupBy(bys, escape);
    }

    public T groupBy(String[] bys, boolean... escape) {
        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        for (String by : bys) {
            by = by.trim();
            if (StringUtils.isBlank(by)) {
                continue;
            }

            Groupby groupby = new Groupby();
            groupby.setField(by);
            groupby.setEscape(_escape);
            shGroupby.add(groupby);

            if (shCaching) {
                shCacheGroupby.add(groupby);
                shCacheExists.put("groupby", true);
            }
        }

        return (T) this;
    }

    /**
     * 设置 HAVING
     * <p>
     * 用“AND”分隔多个调用
     *
     * @param key    查询字段
     * @param value  查询的值
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T having(String key, Object value, boolean... escape) {
        return _wh("sh_having", key, value, "AND ", escape);
    }

    public T having(String key, boolean... escape) {
        return _wh("sh_having", key, null, "AND ", escape);
    }

    public T having(Map<String, Object> map, boolean... escape) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            _wh("sh_having", entry.getKey(), entry.getValue(), "AND ", escape);
        }

        return (T) this;
    }

    /**
     * 设置 OR HAVING
     * <p>
     * 用“OR”分隔多个调用
     *
     * @param key    查询字段
     * @param value  查询的值
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T orHaving(String key, Object value, boolean escape) {
        return _wh("sh_having", key, value, "OR ", escape);
    }

    public T orHaving(String key, boolean... escape) {
        return _wh("sh_having", key, null, "OR ", escape);
    }

    public T orHaving(Map<String, Object> map, boolean... escape) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            _wh("sh_having", entry.getKey(), entry.getValue(), "OR ", escape);
        }

        return (T) this;
    }

    /**
     * 设置 ORDER BY
     *
     * @param orderby   排序字段
     * @param direction 排序方向，如：ASC(升序), DESC(降序) or RANDOM(随机)
     * @param escape    是否需要转义
     * @return SqlHelper
     */
    public T orderBy(String orderby, String direction, boolean... escape) {
        orderby = StringUtils.defaultIfBlank(orderby, "");
        direction = StringUtils.defaultIfBlank(direction, "");

        if (!direction.equals("")) {
            direction = direction.trim().toUpperCase();
        }

        if (direction.equals("RANDOM")) {
            direction = "";

            // 是否有种子值?
            orderby = NumberUtils.isDigits(orderby)
                    ? String.format(_randomKeyword[1], orderby)
                    : _randomKeyword[0];
        } else if (orderby.equals("")) {
            return (T) this;
        } else if (!direction.equals("")) {
            direction = ArrayUtils.contains(new String[]{"ASC", "DESC"}, direction) ? " " + direction : "";
        }

        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        List<Orderby> _sh_orderby = new ArrayList<>();
        if (!_escape) {
            Orderby order = new Orderby();
            order.setField(orderby);
            order.setDirection(direction);
            order.setEscape(false);
            _sh_orderby.add(order);
        } else {
            String  _field, _direction;
            String  _regex  = "\\s+(ASC|DESC)$";
            Pattern pattern = Pattern.compile(_regex, Pattern.CASE_INSENSITIVE);

            String[] orderbys = StringUtils.split(orderby, ",");

            for (String field : orderbys) {
                field = field.trim();
                _field = "";
                _direction = "";

                if (direction.equals("")) {
                    Matcher matcher = pattern.matcher(field);
                    if (matcher.find()) {
                        _field = field.substring(0, matcher.start());
                        _direction = matcher.group(1).toUpperCase();
                    }
                }

                Orderby order = new Orderby();
                order.setEscape(true);
                if (!_direction.equals("")) {
                    order.setField(_field);
                    order.setDirection(" " + _direction);
                } else {
                    order.setField(field);
                    order.setDirection(direction);
                }
                _sh_orderby.add(order);
            }
        }

        shOrderby.addAll(_sh_orderby);
        if (shCaching) {
            shCacheOrderby.addAll(_sh_orderby);
            shCacheExists.put("orderby", true);
        }

        return (T) this;
    }

    public T orderBy(int seed, String direction, boolean... escape) {
        direction = StringUtils.defaultIfBlank(direction, "");
        if (!direction.equals("RANDOM")) {
            return _getErrorReturn("db_direction_must_be_RANDOM", (T) this);
        }

        return orderBy(String.valueOf(seed), direction, escape);
    }

    public T orderBy(String orderby, boolean... escape) {
        return orderBy(orderby, "", escape);
    }

    /**
     * 设置 LIMIT
     *
     * @param offset 偏移量
     * @param limit  记录数量
     * @return SqlHelper
     */
    public T limit(int offset, int limit) {
        shOffset = offset;
        shLimit = limit;
        return (T) this;
    }

    public T limit(int limit) {
        shLimit = limit;
        return (T) this;
    }

    /**
     * 设置 偏移值
     *
     * @param offset 偏移值
     * @return SqlHelper
     */
    public T offset(int offset) {
        shOffset = offset;
        return (T) this;
    }

    /**
     * 设置 LIMIT 字符串
     * <p>
     * 生成特定于平台的LIMIT子串
     *
     * @param sql SQL查询
     * @return limit子串
     */
    protected String _limit(String sql) {
        return sql + " LIMIT " + (shOffset > 0 ? shOffset + ", " : "") + shLimit;
    }

    /**
     * "set" 方法
     * <p>
     * 插入或更新 的键值对
     *
     * @param key    字段名
     * @param value  字段值
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T set(String key, Object value, boolean... escape) {
        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;
        shSet.put(protectIdentifiers(key, false, true, _escape), _escape ? escape(value).toString() : value);
        return (T) this;
    }

    public T set(Map<String, Object> setMap, boolean... escape) {
        for (Map.Entry<String, Object> entry : setMap.entrySet()) {
            set(entry.getKey(), entry.getValue(), escape);
        }
        return (T) this;
    }

    /**
     * 获取 SELECT 查询字符串
     * <p>
     * 编译一个SELECT查询字符串并返回sql
     *
     * @param table 要从中选择的表名（可选）
     * @param reset 是否重置查询缓存值，true：重置，false:不重置，默认为 true 重置
     * @return 查询字符串
     */
    public String getCompiledSelect(String table, boolean... reset) {
        if (StringUtils.isNotBlank(table)) {
            _trackAliases(table);
            from(table);
        }

        String sql = _compileSelect();

        if (reset.length == 0 || reset[0]) {
            _resetSelect();
        }

        return sql;
    }

    public String getCompiledSelect(String... table) {
        return getCompiledSelect(table.length > 0 ? table[0] : "", true);
    }

    /**
     * 获取 SELECT COUNT(*) 查询字符串
     *
     * @param table 要从中选择的表名（可选）
     * @param reset 是否重置查询缓存值，true：重置，false:不重置，默认为 true 重置
     * @return 查询字符串
     */
    public String getCompiledSelectCount(String table, boolean... reset) {
        if (StringUtils.isNotBlank(table)) {
            _trackAliases(table);
            from(table);
        }

        String sql;
        if (shDistinct || shOrderby.size() != 0) {
            sql = _countString + protectIdentifiers("numrows") + "\nFROM (\n" + _compileSelect() + "\n) db_select_count";
        } else {
            sql = _compileSelect(_countString + protectIdentifiers("numrows"));
        }

        if (reset.length == 0 || reset[0]) {
            _resetSelect();
        }

        return sql;
    }

    public String getCompiledSelectCount(String... table) {
        return getCompiledSelectCount(table.length > 0 ? table[0] : "", true);
    }

    /**
     * 批量插入语句
     * <p>
     * 从提供的数据生成特定于平台的插入字符串
     *
     * @param table  表名
     * @param keys   键名列表
     * @param values 键值列表
     * @return 插入字符串
     */
    protected String _insertBatch(String table, List<String> keys, List<Object> values) {
        return "INSERT INTO " + table + " (" + StringUtils.join(keys, ", ") + ") VALUES " + StringUtils.join(values, ", ");
    }

    /**
     * 插入语句
     * <p>
     * 从提供的数据生成特定于平台的插入字符串
     *
     * @param table  表名
     * @param keys   键名列表
     * @param values 键值列表
     * @return 插入字符串
     */
    protected String _insert(String table, String[] keys, Object[] values) {
        return "INSERT INTO " + table + " (" + StringUtils.join(keys, ", ") + ") VALUES (" + StringUtils.join(values, ", ") + ")";
    }

    /**
     * 批量插入方法
     *
     * @param setMaps 批量设置值
     * @param escape  是否需要转义
     * @return SqlHelper
     */
    public T setInsertBatch(List<Map<String, Object>> setMaps, boolean... escape) {
        if (setMaps.size() == 0) {
            return (T) this;
        }

        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        ArrayList<String> keys = new ArrayList<>(setMaps.get(0).keySet());
        Collections.sort(keys);

        for (Map<String, Object> record : setMaps) {
            ArrayList<String> _keys = new ArrayList<>(record.keySet());
            if (ArrayListHelper.diff(keys, _keys).size() > 0 || ArrayListHelper.diff(_keys, keys).size() > 0) {
                // 必须保证 键名是相同的
                shSetInsertBatch.clear();
                return _getErrorReturn("db_must_set_the_same_key", (T) this);
            }

            // 将记录按与键相同的顺序排列
            record = MapHelper.ksort(record);

            if (_escape) {
                for (Map.Entry<String, Object> entry : record.entrySet()) {
                    record.put(entry.getKey(), escape(entry.getValue()));
                }
            }

            shSetInsertBatch.add("(" + StringUtils.join(record.values(), ",") + ")");
        }

        for (String key : keys) {
            shSetInsertKeys.add(protectIdentifiers(key, false, true, _escape));
        }

        return (T) this;
    }

    /**
     * 获取插入查询字符串
     * <p>
     * 编译插入查询并返回sql
     *
     * @param table 要插入的表
     * @param reset 是否需要重置查询SH缓存值，true：重置，false:不重置，默认为 true
     * @return 插入SQL
     */
    public String getCompiledInsert(String table, boolean... reset) {
        if (!_validateInsert(table)) {
            return "";
        }

        String[] keys   = new String[shSet.size()];
        Object[] values = new String[shSet.size()];
        int      i      = 0;
        for (Map.Entry<String, Object> entry : shSet.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        String sql = _insert(protectIdentifiers(shFrom.get(0), true, false), keys, values);

        if ((reset.length == 0) || reset[0]) {
            _resetWrite();
        }

        return sql;
    }

    public String getCompiledInsert(boolean... reset) {
        return getCompiledInsert("", reset);
    }

    /**
     * 验证插入操作的有效性
     * <p>
     * insert()和 getCompileInsert()都使用这个方法来验证是否设置了数据，以及是否选择了要插入的表。
     *
     * @param table 要插入数据的表
     * @return 验证结果
     */
    protected boolean _validateInsert(String... table) {
        if (shSet.size() == 0) {
            return _getErrorReturn("db_must_use_set", false);
        }

        if (table.length > 0 && StringUtils.isNotBlank(table[0])) {
            if (shFrom.size() == 0) {
                shFrom.add(table[0]);
            } else {
                shFrom.set(0, table[0]);
            }
        } else if (shFrom.size() == 0) {
            return _getErrorReturn("db_must_set_table", false);
        }

        return true;
    }

    /**
     * 替换语句
     * <p>
     * 从提供的数据生成特定于平台的替换字符串
     *
     * @param table  表名
     * @param keys   插入的键名
     * @param values 插入的键值
     * @return 替换字符串
     */
    protected String _replace(String table, String[] keys, Object[] values) {
        return "REPLACE INTO " + table + " (" + StringUtils.join(keys, ", ") + ") VALUES (" + StringUtils.join(values, ", ") + ")";
    }

    /**
     * 获取要查询的表名
     * <p>
     * 如果需要，可以在FROM子句中对表进行分组，这样就不会混淆操作符的优先级
     * 注意：只能用于 MySQL和CUBRID 数据库
     */
    protected String _fromTables() {
        return StringUtils.join(shFrom, ", ");
    }

    /**
     * 获取更新查询字符串
     * <p>
     * 编译更新查询并返回sql
     *
     * @param table 要更新的表
     * @param reset 是否需要重置查询SH缓存值，TRUE：重置SH值，FALSE:不处理SH值
     * @return 更新SQL
     */
    public String getCompiledUpdate(String table, boolean... reset) {
        // 合并缓存
        _mergeCache();

        if (!_validateUpdate(table)) {
            return "";
        }

        String sql = _update(shFrom.get(0), shSet);

        if ((reset.length > 0) && reset[0]) {
            _resetWrite();
        }

        return sql;
    }

    public String getCompiledUpdate(boolean... reset) {
        return getCompiledUpdate("", reset);
    }

    /**
     * 验证更新操作
     * <p>
     * update() 和 getCompiledUpdate()都使用此方法来验证数据是否实际被设置，以及表是否被选择
     *
     * @param table 要更新数据的表
     * @return 验证结果
     */
    protected boolean _validateUpdate(String table) {
        if (shSet.size() == 0) {
            return _getErrorReturn("db_must_use_set", false);
        }

        if (StringUtils.isNotBlank(table)) {
            shFrom.clear();
            shFrom.add(protectIdentifiers(table, true, false));
        } else if (shFrom.size() == 0) {
            return _getErrorReturn("db_must_set_table", false);
        }

        return true;
    }

    /**
     * 更新语句
     * <p>
     * 从提供的数据生成特定于平台的更新字符串
     *
     * @param table  表名
     * @param values 更新的数据
     * @return 更新字符串
     */
    protected String _update(String table, Map<String, Object> values) {
        StringBuilder sb    = new StringBuilder();
        String        _join = "";
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (StringUtils.isNotBlank(_join)) {
                sb.append(_join);
            }

            sb.append(entry.getKey());
            sb.append(" = ");
            sb.append(entry.getValue());

            if (StringUtils.isBlank(_join)) {
                _join = ", ";
            }
        }

        return "UPDATE " + table + " SET " + sb.toString()
                + _compileWh("sh_where")
                + _compileOrderBy()
                + (shLimit > 0 ? " LIMIT " + shLimit : "");
    }

    /**
     * 批处理更新语句
     * <p>
     * 从提供的数据生成特定于平台的批处理更新字符串
     *
     * @param table   表名
     * @param records 更新数据
     * @param index   WHERE 关键字
     */
    protected String _updateBatch(String table, List<Map<String, Object>> records, String index) {
        List<Object>                   ids       = new ArrayList<>();
        Map<String, ArrayList<String>> finalData = new LinkedHashMap<>();

        for (Map<String, Object> record : records) {
            ids.add(record.get(index));

            for (Map.Entry<String, Object> entry : record.entrySet()) {
                String _field = entry.getKey();
                String _sql   = "WHEN " + index + " = " + record.get(index) + " THEN " + record.get(_field);
                if (!_field.equals(index)) {
                    ArrayList<String> _list = finalData.containsKey(_field) ? finalData.get(_field) : new ArrayList<>();
                    _list.add(_sql);
                    finalData.put(_field, _list);
                }
            }
        }

        StringBuilder cases = new StringBuilder();
        for (Map.Entry<String, ArrayList<String>> entry : finalData.entrySet()) {
            cases.append(entry.getKey()).append(" = CASE \n")
                    .append(StringUtils.join(entry.getValue(), "\n")).append("\n")
                    .append("ELSE").append(entry.getKey()).append(" END, ");
        }

        whereIn(index, ids, false);

        return "UPDATE " + table + " SET " + cases.toString() + _compileWh("sh_where");
    }

    /**
     * 设置 批量更新
     *
     * @param sets   批量更新的数据
     * @param index  WHERE 关键字
     * @param escape 是否需要转义
     * @return SqlHelper
     */
    public T setUpdateBatch(List<Map<String, Object>> sets, String index, boolean... escape) {
        boolean _escape = escape.length > 0 ? escape[0] : _protectIdentifiers;

        boolean indexSet;
        for (Map<String, Object> set : sets) {
            indexSet = false;
            Map<String, Object> clean = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : set.entrySet()) {
                if (entry.getKey().equals(index)) {
                    indexSet = true;
                }
                clean.put(protectIdentifiers(entry.getKey(), false, true, _escape),
                        _escape ? escape(entry.getValue()) : entry.getValue());
            }

            if (!indexSet) {
                throw new IllegalArgumentException("db_batch_missing_index");
            }

            shSetUpdateBatch.add(clean);
        }

        return (T) this;
    }

    /**
     * 清空表操作
     * <p>
     * 从提供的数据生成特定于平台的清空数据表字符串
     * 如果数据库不支持truncate()命令，则该方法映射到“DELETE FROM table”
     *
     * @param table 表名
     * @return 清空表字符串
     */
    protected String _truncate(String table) {
        return "TRUNCATE " + table;
    }

    /**
     * 获取删除查询字符串
     * <p>
     * 编译一个删除查询字符串并返回sql
     */
    public String getCompiledDelete(String table, boolean... reset) {
        // 合并缓存
        _mergeCache();

        if (StringUtils.isBlank(table)) {
            if (shFrom.size() == 0) {
                return _getErrorReturn("db_must_set_table", "");
            }
            table = shFrom.get(0);
        } else {
            table = protectIdentifiers(table, true, false);
        }

        if (shWhere.size() == 0) {
            return _getErrorReturn("db_del_must_use_where", "");
        }

        String sql = _delete(table);
        if (reset.length > 0 && reset[0]) {
            _resetWrite();
        }

        return sql;
    }

    public String getCompiledDelete(boolean... reset) {
        return getCompiledDelete("", reset);
    }

    /**
     * 删除 语句
     * <p>
     * 从提供的数据生成特定于平台的删除字符串
     *
     * @param table 表名
     * @return 删除字符串
     */
    protected String _delete(String table) {
        return "DELETE FROM " + table + _compileWh("qb_where")
                + (shLimit > 0 ? " LIMIT " + shLimit : "");
    }

    /**
     * 设置为调试模式
     */
    public T setDebug(boolean debug) {
        dbDebug = debug;
        return (T) this;
    }

    /**
     * 获取 带有前缀的 数据表名
     *
     * @param table 表名
     * @return 数据表名
     */
    public String dbPrefix(String table) {
        if (StringUtils.isBlank(table)) {
            throw new IllegalArgumentException("db_table_name_required");
        }
        return dbPrefix + table;
    }

    /**
     * 设置 数据表前缀
     * <p>
     * 将表前缀设置为不需要重新连接的新前缀
     *
     * @param prefix 表前置
     * @return SqlHelper
     */
    public T setDbPrefix(String prefix) {
        dbPrefix = prefix;
        return (T) this;
    }

    /**
     * 跟踪别名
     * <p>
     * 用于跟踪使用别名表编写的SQL语句。
     */
    protected void _trackAliases(String table) {
        // 字符串是否包含逗号？如果是这样，我们需要将字符串分割成离散的语句
        if (StringUtils.contains(table, ",")) {
            String[] tables = StringUtils.split(table, ",");
            _trackAliases(Arrays.asList(tables));
            return;
        }

        // 如果使用表别名，我们可以通过空格来识别它
        if (StringUtils.contains(table, " ")) {
            // 如果别名是用AS关键字编写的，则删除它
            table = RegExUtils.replacePattern(table, "(?i)\\s+AS\\s+", " ");

            // 获取别名
            table = StringUtils.substringAfter(table, " ").trim();

            // 如果别名不存在，则保存该别名
            if (!shAliasedTables.contains(table)) {
                shAliasedTables.add(table);
            }
        }
    }

    protected void _trackAliases(List<String> tables) {
        for (String table : tables) {
            _trackAliases(table);
        }
    }

    /**
     * 编译SELECT语句
     * <p>
     * 根据使用的函数生成查询字符串。不直接调用
     *
     * @param selectOverride 直接覆盖的查询语句
     * @return 查询语句
     */
    protected String _compileSelect(String... selectOverride) {
        // 合并缓存
        _mergeCache();

        String sql = "";

        // 编写查询的“选择”部分
        if ((selectOverride.length > 0 && StringUtils.isNotBlank(selectOverride[0]))) {
            sql = selectOverride[0];
        } else {
            sql = shDistinct ? "SELECT DISTINCT " : "SELECT ";
            if (shSelect.size() == 0) {
                sql += "*";
            } else {
                // 循环遍历查询的“select”部分，并且格式化每个列名。
                // 我们之所以在这里而不是在select()函数中保护标识符，
                // 是因为在用户调用from()函数之前，我们不知道是否存在别名
                for (int i = 0; i < shSelect.size(); i++) {
                    boolean no_escape = i >= shNoEscape.size() ? false : shNoEscape.get(i);
                    shSelect.set(i, protectIdentifiers(shSelect.get(i), false, true, no_escape));
                }
                sql += StringUtils.join(shSelect, ", ");
            }
        }

        // 编写查询的“FROM”部分
        if (shFrom.size() > 0) {
            sql += "\nFROM " + _fromTables();
        }

        // 编写查询的“连接”部分
        if (shJoin.size() > 0) {
            sql += "\n" + StringUtils.join(shJoin, "\n");
        }

        sql += _compileWh("sh_where")
                + _compileGroupBy()
                + _compileWh("sh_having")
                + _compileOrderBy()
        ;

        // 数量限制
        if (shLimit > 0) {
            return _limit(sql + "\n");
        }

        return sql;
    }

    /**
     * 编译 WHERE、HAVING 语句
     * <p>
     * 在执行的时候，转义 WHERE 和 HAVING 语句中的 标识符
     * <p>
     * 所以要求我们，无论是否优先于 from()、join() 方法的调用，在调用 where()、
     * orWhere()、having()、orHaving 方法的适当地方，跟踪收集别名，
     * 并且在有需要的地方，添加 dbPrefix 前缀。
     *
     * @param sh_key 'sh_where' or 'sh_having'
     * @return SQL statement
     */
    protected String _compileWh(String sh_key) {
        if (StringUtils.isBlank(sh_key)) {
            return "";
        }

        List<Where> _sh_key = sh_key.equals("sh_having") ? shHaving : shWhere;

        if (_sh_key.size() == 0) {
            return "";
        }

        String op = "";
        for (Where wh : _sh_key) {
            // 条件是否已经编译好了？
            if (wh.getCompile() != null) {
                continue;
            } else if (!wh.isEscape()) {
                wh.setCompile(wh.getCondition());
                continue;
            }

            // 分割多个条件
            String[] conditions = RegexHelper.split(
                    wh.getCondition(),
                    //"(?x)(?i)  (   (?:^|\\s+)  AND  \\s+   |   (?:^|\\s+)   OR   \\s+   )",
                    "(?i)((?:^|\\s+)AND\\s+|(?:^|\\s+)OR\\s+)",
                    -1,
                    RegexHelper.SPLIT_NO_EMPTY | RegexHelper.SPLIT_DELIM_CAPTURE
            );

            for (int i = 0; i < conditions.length; i++) {
                op = _getOperator(conditions[i]);
                if (StringUtils.isBlank(op)) {
                    continue;
                }

                //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                // 正在表达式
                // (?<!exp)：【零宽断言】断言此位置的前面不能匹配表达式exp
                // (?x)：匹配时会忽略（正则表达式里的）空格字符（注：不是指表达式里的"\\s"，而是指表达式里的空格、tab、回车之类）
                // (?i)：忽略大小写
                // 参考文章：
                //      http://php-note.com/article/detail/61d05b64ee3143aa9ddb746d231329b5
                //      http://php-note.com/doc/regex/regex.html#lookaround
                //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                //String  regex = "(?x)(?i)   ^   (\\(?)   (.*)   (" + RegexHelper.quote(op, '/') + ")   \\s*   (.*  (?<!\\))  )?   (\\)?)   $";
                String  regex   = "(?i)^(\\(?)(.*)(" + RegexHelper.quote(op, '/') + ")\\s*(.*(?<!\\)))?(\\)?)$";
                Matcher matcher = Pattern.compile(regex).matcher(conditions[i]);

                if (!matcher.find()) {
                    continue;
                }

                // $matches = array(
                //    0 => '(test <= foo)',     /* the whole thing */
                //    1 => '(',                 /* optional */
                //    2 => 'test',              /* the field name */
                //    3 => ' <= ',              /* $op */
                //    4 => 'foo',               /* optional, if $op is e.g. 'IS NULL' */
                //    5 => ')'                  /* optional */
                // );

                String opt = matcher.group(4);
                if (StringUtils.isNotBlank(opt)) {
                    if (!_isLiteral(opt)) {
                        opt = protectIdentifiers(opt.trim());
                    }
                    opt = " " + opt;
                }

                conditions[i] = matcher.group(1)
                        + protectIdentifiers(matcher.group(2).trim())
                        + " "
                        + matcher.group(3).trim()
                        + StringUtils.defaultString(opt)
                        + StringUtils.defaultString(matcher.group(5));

            }
            wh.setCompile(StringUtils.join(conditions, ""));
        }

        StringBuilder sb = new StringBuilder();
        if (sh_key.equals("sh_having")) {
            sb.append("\nHAVING ");
        } else {
            sb.append("\nWHERE ");
        }

        //【特别注意】
        // 下面的 isNotEmpty()，isEmpty() 不能用 isNotBlank()，isBlank() 来代替，因为 \n 是空白字符
        String _join = "";
        for (Where wh : _sh_key) {
            if (StringUtils.isNotEmpty(_join)) {
                sb.append(_join);
            }
            sb.append(wh.getCompile());
            if (StringUtils.isEmpty(_join)) {
                _join = "\n";
            }
        }

        return sb.toString();
    }

    /**
     * 编译 GROUP BY 语句
     * <p>
     * 在执行的时候，注意 GROUP BY 语句的标识符
     * <p>
     * 所以要求我们，无论是否优先于 from()、join() 方法的调用，在调用 groupBy() 方法的适当地方，
     * 跟踪收集别名，并且在有需要的地方，添加 dbPrefix 前缀。
     */
    protected String _compileGroupBy() {
        if (shGroupby.size() == 0) {
            return "";
        }

        for (Groupby groupby : shGroupby) {
            // 是否已经编译过？
            if (groupby.getCompile() != null) {
                continue;
            }

            if (!groupby.isEscape() || _isLiteral(groupby.getField())) {
                groupby.setCompile(groupby.getField());
            } else {
                groupby.setCompile(protectIdentifiers(groupby.getField()));
            }
        }

        StringBuilder sb    = new StringBuilder("\nGROUP BY ");
        String        _join = "";
        for (Groupby groupby : shGroupby) {
            if (StringUtils.isNotBlank(_join)) {
                sb.append(_join);
            }
            sb.append(groupby.getCompile());
            if (StringUtils.isBlank(_join)) {
                _join = ", ";
            }
        }

        return sb.toString();
    }

    /**
     * 编译 ORDER BY 语句
     * <p>
     * 在执行的时候，转义 ORDER BY 语句中的标识符
     * <p>
     * 所以要求我们，无论是否优先于 from()、join() 方法的调用，在调用 orderBy() 方法的适当地方，
     * 跟踪收集别名，并且在有需要的地方，添加 dbPrefix 前缀。
     */
    protected String _compileOrderBy() {
        if (shOrderby.size() == 0) {
            return "";
        }

        for (Orderby orderby : shOrderby) {
            // 是否已经编译过？
            if (orderby.getCompile() != null) {
                continue;
            }

            if (orderby.isEscape() && !_isLiteral(orderby.getField())) {
                orderby.setField(protectIdentifiers(orderby.getField()));
            }
            orderby.setCompile(orderby.getField() + orderby.getDirection());
        }

        StringBuilder sb    = new StringBuilder("\nORDER BY ");
        String        _join = "";
        for (Orderby orderby : shOrderby) {
            if (StringUtils.isNotBlank(_join)) {
                sb.append(_join);
            }
            sb.append(orderby.getCompile());
            if (StringUtils.isBlank(_join)) {
                _join = ", ";
            }
        }

        return sb.toString();
    }

    /**
     * 开始缓存
     * <p>
     * 开启 SH 缓存标识符
     */
    public T startCache() {
        shCaching = true;
        return (T) this;
    }

    /**
     * 结束缓存
     * <p>
     * 停止 SH 缓存标识符
     */
    public T stopCache() {
        shCaching = false;
        return (T) this;
    }

    /**
     * 清空缓存
     * <p>
     * 清空 SH 的所有缓存对象
     */
    public T flushCache() {
        shCacheSelect.clear();
        shCacheFrom.clear();
        shCacheJoin.clear();
        shCacheWhere.clear();
        shCacheGroupby.clear();
        shCacheHaving.clear();
        shCacheOrderby.clear();
        shCacheSet.clear();
        shCacheExists.clear();
        shCacheNoEscape.clear();
        return (T) this;
    }

    /**
     * 合并缓存
     */
    protected void _mergeCache() {
        if (shCacheExists.size() == 0) {
            return;
        }

        for (Map.Entry<String, Boolean> entry : shCacheExists.entrySet()) {
            switch (entry.getKey()) {
                case "select":
                    for (int i = 0; i < shSelect.size(); i++) {
                        if (!shCacheSelect.contains(shSelect.get(i))) {
                            shCacheSelect.add(shSelect.get(i));
                            shCacheNoEscape.add(shNoEscape.get(i));
                        }
                    }
                    shSelect.clear();
                    shSelect.addAll(shCacheSelect);
                    shNoEscape.clear();
                    shNoEscape.addAll(shCacheNoEscape);
                    break;
                case "from":
                    for (String from : shFrom) {
                        if (!shCacheFrom.contains(from)) {
                            shCacheFrom.add(from);
                        }
                    }
                    shFrom.clear();
                    shFrom.addAll(shCacheFrom);
                    break;
                case "join":
                    for (String join : shJoin) {
                        if (!shCacheJoin.contains(join)) {
                            shCacheJoin.add(join);
                        }
                    }
                    shJoin.clear();
                    shJoin.addAll(shCacheJoin);
                    break;
                case "where":
                    for (Where where : shWhere) {
                        if (!shCacheWhere.contains(where)) {
                            shCacheWhere.add(where);
                        }
                    }
                    shWhere.clear();
                    shWhere.addAll(shCacheWhere);
                    break;
                case "groupby":
                    for (Groupby groupby : shGroupby) {
                        if (!shCacheGroupby.contains(groupby)) {
                            shCacheGroupby.add(groupby);
                        }
                    }
                    shGroupby.clear();
                    shGroupby.addAll(shCacheGroupby);
                    break;
                case "having":
                    for (Where having : shHaving) {
                        if (!shCacheHaving.contains(having)) {
                            shCacheHaving.add(having);
                        }
                    }
                    shHaving.clear();
                    shHaving.addAll(shCacheHaving);
                    break;
                case "orderby":
                    for (Orderby orderby : shOrderby) {
                        if (!shCacheOrderby.contains(orderby)) {
                            shCacheOrderby.add(orderby);
                        }
                    }
                    shOrderby.clear();
                    shOrderby.addAll(shCacheOrderby);
                    break;
            }

            // 如果我们要“保护标识符”，我们需要检查查询的“from”部分，以确定是否存在别名
            if (_protectIdentifiers && shCacheFrom.size() > 0) {
                _trackAliases(shFrom);
            }
        }
    }

    /**
     * 是否是 字面量值
     * <p>
     * 确定字符串是否表示字面值 或 字段名/值
     */
    protected boolean _isLiteral(String str) {
        str = str.trim();
        if (StringUtils.isBlank(str)) {
            return true;
        }

        if (str.toUpperCase().equals("TRUE") || str.toUpperCase().equals("FALSE")) {
            return true;
        }

        if (NumberUtils.isCreatable(str)) {
            return true;
        }

        Set<String> set = new HashSet<>();
        set.add("'");
        if (!_escapeChar.equals("\"")) {
            set.add("\"");
        }

        return set.contains(str.substring(0, 1));
    }

    /**
     * 重置查询生成器值
     * <p>
     * 公开可见的方法 去重置查询生成器值
     */
    public T resetQuery() {
        _resetSelect();
        _resetWrite();
        return (T) this;
    }

    /**
     * 调用 get() 方法时，重置查询生成器值
     */
    protected void _resetSelect() {
        shSelect.clear();
        shFrom.clear();
        shJoin.clear();
        shWhere.clear();
        shGroupby.clear();
        shHaving.clear();
        shOrderby.clear();
        shAliasedTables.clear();
        shNoEscape.clear();
        shDistinct = false;
        shLimit = 0;
        shOffset = 0;
    }

    /**
     * 重置查询生成器的 write() 变量值
     * <p>
     * 在 insert()、update()、insertBatch()、updateBatch() 和 delete() 方法中调用
     */
    protected void _resetWrite() {
        shSet.clear();
        shFrom.clear();
        shJoin.clear();
        shWhere.clear();
        shOrderby.clear();
        shSetInsertKeys.clear();
        shSetInsertBatch.clear();
        shSetUpdateBatch.clear();
        shLimit = 0;
    }

    /**
     * 保护标识符
     * <p>
     * 查询生成器类和该类中的几个函数广泛使用这个函数。
     * 它接受一个列或表名（可以使用别名）并将表前缀插入其中。需要一些逻辑去处理包含路径的列名，
     * 思考下面的查询：
     * <p>
     * SELECT * FROM hostname.database.table.column AS c FROM hostname.database.table
     * <p>
     * 或者是 带别名的查询：
     * <p>
     * SELECT m.member_id, m.member_name FROM members AS m
     * <p>
     * 因为列名最多可以包含四个段（host, db, table, column）
     * 或者也有别名前缀，我们需要做一些工作来解决这个问题，
     * 并在适当的位置插入表前缀（如果它存在），只转义正确的标识符。
     *
     * @param item               被格式化对象
     * @param prefixSingle       当是单个对象时（即不包含点号），是否加前缀（dbPrefix）
     * @param protectIdentifiers 是否保护标识符
     * @param fieldExists        字段是否存在
     * @return 格式化后的 标识符
     */
    protected String protectIdentifiers(String item,
                                        boolean prefixSingle,
                                        boolean fieldExists,
                                        boolean... protectIdentifiers) {
        boolean ___protect_identifiers = protectIdentifiers.length > 0
                ? protectIdentifiers[0]
                : _protectIdentifiers;

        // 这基本上是对使用 MAX、MIN 等的查询的错误修复。
        // 如果找到括号，我们知道不需要转义数据或添加前缀。
        // 也为单引号添加了特殊处理，我们不想改变文字字符串。
        if (StringUtils.containsAny(item, "(", ")", "'")) {
            return item;
        }

        // 将制表符或多个空格转换为单个空格
        StringUtils.replace(item, "\\s+", " ");

        // 如果项有别名声明，则删除它并将其放在一边。
        String alias  = "";
        int    offset = -1;
        if ((offset = StringUtils.indexOfIgnoreCase(item, " AS ")) != -1) {
            alias = ___protect_identifiers
                    ? StringUtils.substring(item, offset, offset + 4) + escapeIdentifiers(StringUtils.substring(item, offset + 4))
                    : StringUtils.substring(item, offset);
            item = StringUtils.substring(item, 0, offset);
        } else if ((offset = StringUtils.indexOfIgnoreCase(item, " ")) != -1) {
            alias = ___protect_identifiers
                    ? " " + escapeIdentifiers(StringUtils.substring(item, offset + 1))
                    : StringUtils.substring(item, offset);
            item = StringUtils.substring(item, 0, offset);
        } else {
            alias = "";
        }

        // 如果字符串包含点号，则将其拆分，然后在正确的位置插入表前缀，假设点号不存在，
        // 表示我们在处理别名。当我们碰到它时，我们需要转义这些组件。
        if (StringUtils.contains(item, ".")) {
            String[] parts = StringUtils.split(item, ",");

            // 分割项的第一个片段是否与前面标识的别名匹配？
            // 如果是，除了转义这个项，我们没有别的事可做。
            //
            // 注意：如果收集到匿名表统计为空，就不进行“转义表名”操作了
            if ((shAliasedTables.size() > 0) && (shAliasedTables.contains(parts[0]))) {
                if (___protect_identifiers) {
                    for (int i = 0; i < parts.length; i++) {
                        if (ArrayUtils.contains(_reservedIdentifiers, parts[i])) {
                            parts[i] = escapeIdentifiers(parts[i]);
                        }
                    }
                    item = StringUtils.join(parts, ".");
                }

                return item + alias;
            }

            // 配置中是否定义了表前缀？如果没有，就不需要做任何事情
            if (StringUtils.isNotBlank(dbPrefix)) {
                int i;
                // 现在，我们根据一些逻辑添加表前缀。
                // 我们是否有4段（hostname.database.table.column）？
                // 如果有，则我们将表前缀添加到第3段列名中
                if (parts.length == 4) {
                    i = 2;
                }
                // 我们是否有3段（database.table.column）？
                // 如果有，则我们将表前缀添加到第2段列名中
                else if (parts.length == 3) {
                    i = 1;
                }
                // 我们是否有2段（table.column）？
                // 如果有，则我们将表前缀添加到第1段列名中
                else {
                    i = 0;
                }

                // 当提供的item不包含字段名时，设置此标志
                // 当从联接调用此函数时，可能会发生这种情况
                if (!fieldExists) {
                    i++;
                }

                // 验证表前缀并在必要时替换
                if (StringUtils.isNotBlank(swapPrefix) && (StringUtils.indexOf(parts[i], swapPrefix) == 0)) {
                    parts[i] = RegExUtils.replacePattern(parts[i], "^" + swapPrefix + "(\\S+?)", dbPrefix + "$1");
                }
                // 我们只在表前缀不存在的情况下添加它
                else if (!StringUtils.startsWith(parts[i], dbPrefix)) {
                    parts[i] = dbPrefix + parts[i];
                }

                // 把这些零件重新组装起来
                item = StringUtils.join(parts, ".");
            }

            if (___protect_identifiers) {
                item = escapeIdentifiers(item);
            }

            return item + alias;
        }

        // 有表前缀吗？如果没有，则不需要插入它
        if (StringUtils.isNotBlank(dbPrefix)) {
            // 验证表前缀并在必要时替换
            if (StringUtils.isNotBlank(swapPrefix) && (StringUtils.startsWith(item, swapPrefix))) {
                item = RegExUtils.replacePattern(item, "^" + swapPrefix + "(\\S+?)", dbPrefix + "$1");
            }
            // 我们是否给没有分段的项加上前缀？
            else if (prefixSingle && !StringUtils.startsWith(item, dbPrefix)) {
                item = dbPrefix + item;
            }
        }

        if (___protect_identifiers && !ArrayUtils.contains(_reservedIdentifiers, item)) {
            item = escapeIdentifiers(item);
        }

        return item + alias;
    }

    protected String protectIdentifiers(String item) {
        return protectIdentifiers(item, false, true);
    }

    /**
     * 转义 标识符
     *
     * @param item 转义对象
     * @return 转义结果字符串
     */
    protected String escapeIdentifiers(String item) {
        if (StringUtils.isBlank(_escapeChar) || StringUtils.isBlank(item) || ArrayUtils.contains(_reservedIdentifiers, item)) {
            return item;
        }

        // 避免在查询中破坏函数和字面量值
        if (NumberUtils.isDigits(item) || StringUtils.substring(item, 0, 1).equals("'")
                || (!_escapeChar.equals("\"") && (StringUtils.substring(item, 0, 1).equals("\"")))
                || StringUtils.contains(item, "(")
        ) {
            return item;
        }

        String[] preg_ec = new String[4];
        preg_ec[0] = preg_ec[1] = RegexHelper.quote(_escapeChar, '/');
        preg_ec[2] = preg_ec[3] = _escapeChar;

        for (String id : _reservedIdentifiers) {
            if (StringUtils.contains(item, "." + id)) {
                // 例如：blog.title 变成 `blog`.title
                return RegExUtils.replacePattern(item, "(?i)" + preg_ec[0] + "?([^" + preg_ec[1] + "\\.]+)" + preg_ec[1] + "?\\.", preg_ec[2] + "$1" + preg_ec[3] + ".");
            }
        }

        // 例如：blog.title 变成 `blog`.`title`
        return RegExUtils.replacePattern(item, "(?i)" + preg_ec[0] + "?([^" + preg_ec[1] + "\\.]+)" + preg_ec[1] + "?(\\.)?", preg_ec[2] + "$1" + preg_ec[3] + "$2");
    }

    /**
     * “智能地”转义字符串
     * <p>
     * 基于类型转义数据
     * 设置布尔型和空类型
     *
     * @param obj 转义对象
     * @return 转义后的结果字符串
     */
    protected Object escape(Object obj) {
        if (obj instanceof String) {
            return "'" + escapeStr((String) obj) + "'";
        } else if (obj instanceof Boolean) {
            return (Boolean) obj ? 1 : 0;
        } else if (obj == null) {
            return "NULL";
        }

        return obj;
    }

    /**
     * 【MySQL】平台，转义字符串
     *
     * @param str  输入串
     * @param like 是否用于 LIKE 条件
     * @return 转义后的字符串
     */
    protected String escapeStr(String str, boolean... like) {
        boolean _like = like.length > 0 && like[0];
        str = _escapeStr(str);

        // 转义 LIKE 条件通配符
        if (_like) {
            str = StringUtils.replaceEach(str,
                    new String[]{
                            _likeEscapeChr,
                            "%",
                            "_"
                    },
                    new String[]{
                            _likeEscapeChr + _likeEscapeChr,
                            _likeEscapeChr + "%",
                            _likeEscapeChr + "_"
                    });
        }

        return str;
    }

    /**
     * 【MySQL】平台，转义字符串
     */
    protected String _escapeStr(String str) {
        return StringUtils.replace(removeInvisibleCharacters(str), "'", "''");
    }

    /**
     * 【MySQL】平台，转义 LIKE 字符串
     * <p>
     * 调用特定于平台的 LIKE条件转义 驱动程序
     *
     * @param str 字符串
     * @return 转义后的字符串
     */
    protected String escapeLikeStr(String str) {
        return escapeStr(str, true);
    }

    /**
     * 【小工具】删除不可见字符
     * <p>
     * 这可以防止在ascii字符(如Java\0script)之间插入空字符。
     *
     * @param str        字符串
     * @param urlEncoded 是否为 URL 字符串
     * @return 处理后的字符串
     */
    protected String removeInvisibleCharacters(String str, boolean... urlEncoded) {
        // 除 换行符（十进制 10），回车符（十进制 13）和 水平制表符（十进制 09）以外的控制字符 

        // 00-08, 11, 12, 14-31, 127
        str = RegExUtils.replaceAll(str, "[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]+", "");

        // 处理 URL编码
        boolean _urlEncoded = urlEncoded.length > 0 && urlEncoded[0];
        if (_urlEncoded) {
            // URL编码 00-08, 11, 12, 14, 15
            str = RegExUtils.replaceAll(str, "%0[0-8bcef]", "");
            // URL编码 16-31
            str = RegExUtils.replaceAll(str, "%1[0-9a-f]", "");
        }

        return str;
    }

    /**
     * 【MySQL】测试 字符串是否含有SQL操作符
     *
     * @param str 字符串
     * @return 测试结果
     */
    protected boolean _hasOperator(String str) {
        // 注意：中间有空格的也算，如“title gt”
        String  regex   = "(?i)(<|>|!|=|\\sIS NULL|\\sIS NOT NULL|\\sEXISTS|\\sBETWEEN|\\sLIKE|\\sIN\\s*\\(|\\s)";
        Matcher matcher = Pattern.compile(regex).matcher(str.trim());
        return matcher.find();
    }

    /**
     * 【MySQL】返回 SQL操作符
     *
     * @param str SQL字符串
     * @return 操作符
     */
    protected String _getOperator(String str) {
        if (_operators[0] == null) {
            String _les = StringUtils.isNotBlank(_likeEscapeStr)
                    ? "\\s+" + RegexHelper.quote(String.format(_likeEscapeStr, _likeEscapeChr).trim(), '/')
                    : "";
            _operators[0] = "\\s*(?:<|>|!)?=\\s*";                      // =, <=, >=, !=
            _operators[1] = "\\s*<>?\\s*";                              // <, <>
            _operators[2] = "\\s*>\\s*";                                // >
            _operators[3] = "\\s+IS NULL";                              // IS NULL
            _operators[4] = "\\s+IS NOT NULL";                          // IS NOT NULL
            _operators[5] = "\\s+EXISTS\\s*\\([^\\)]+\\)";              // EXISTS(sql)
            _operators[6] = "\\s+NOT EXISTS\\s*\\([^\\)]+\\)";          // NOT EXISTS(sql)
            _operators[7] = "\\s+BETWEEN\\s+";                          // BETWEEN value AND value
            _operators[8] = "\\s+IN\\s*\\([^\\)]+\\)";                  // IN(list)
            _operators[9] = "\\s+NOT IN\\s*\\([^\\)]+\\)";              // NOT IN (list)
            _operators[10] = "\\s+LIKE\\s+\\S.*(" + _les + ")?";        // LIKE 'expr'[ ESCAPE '%s']
            _operators[11] = "\\s+NOT LIKE\\s+\\S.*(" + _les + ")?";    // NOT LIKE 'expr'[ ESCAPE '%s']
        }

        Matcher matcher = Pattern.compile(StringUtils.join(_operators, "|"), Pattern.CASE_INSENSITIVE).matcher(str);
        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    protected void _addWhere(Where where) {
        shWhere.add(where);
        if (shCaching) {
            shCacheWhere.add(where);
            shCacheExists.put("where", true);
        }
    }

    /**
     * 返回发生错误后的结果值
     *
     * @param msg 异常提示语
     * @param ret 返回值
     * @return 返回输入的 RET
     */
    protected <E> E _getErrorReturn(String msg, E ret) {
        if (dbDebug) {
            throw new IllegalArgumentException(msg);
        } else {
            logger.error(msg, new Throwable());
            return ret;
        }
    }
}
