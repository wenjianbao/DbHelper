# sqlHelper：SQL查询构造器 
搞了几年的PHP开发，接触到好些PHP框架，数据库模型这块，发现CodeIgniter框架的语法非常 通俗易懂，也容易记。

最近在学习Java，接触到JFinal框架，使用起来也非常简单，有点脚本语言的味道，但是模型这块，打算让它更好玩一些。

于是，想到把CodeIgniter框架的这套模型语法搬到JFinal框架上来，这样就可以节省手写SQL字符串的时间。  

##核心文件
主要用到2个类文件：  

(1) SqlHelper.java : 专门用来拼接SQL字符串的，最终输出完整的SQL语句；

(2) DbHelper.java : 继承了SqlHelper类，简单封装了JFinal框架执行SQL语句的API，让自己有执行SQL语句的能力；  

## 语法如下
- [查询](#jump_select)
- [搜索](#jump_where)
- [模糊搜索](#jump_like)
- [排序](#jump_orderby)
- [分页与计数](#jump_limit)
- [查询条件组](#jump_groupby)
- [插入数据](#jump_insert)
- [更新数据](#jump_update)
- [删除数据](#jump_delete)
- [链式方法](#jump_chain)
- [查询构造器缓存](#jump_cache)
- [重置查询构造器](#jump_reset)
- [对 JFinal DB API 简单封装](#jump_jfinal)
- [对 JFinal Model API 简单封装](#jump_jfinal_model)

###<span id="jump_select">查询</span>

下面的方法用来构建 SELECT 语句。

**select("title,content,date")**

该方法用于编写查询语句中的 SELECT 子句：

```
List<Record> list = DbHelper.create()
        .select("title,content,date")
        .findAll("mytable");
// 执行：SELECT `title`, `content`, `date`
// FROM `mytable`
```
>注意：如果你要查询表的所有列，可以不用写这个函数，SqlHelper 会自动查询所有列（SELECT *）。

select() 方法的第二个参数可选，如果设置为 FALSE，SqlHelper 将不保护你的 表名和字段名，这在当你编写复合查询语句时很有用，不会破坏你编写的语句。

```
List<Record> list = DbHelper.create()
        .select("(SELECT SUM(payments.amount) FROM payments WHERE payments.invoice_id=4) AS amount_paid", false)
        .findAll("mytable");
// 执行：SELECT (SELECT SUM(payments.amount) FROM payments 
// WHERE payments.invoice_id=4) AS amount_paid
// FROM `mytable`
```

**selectMax()**

该方法用于编写查询语句中的 SELECT MAX(field) 部分，你可以使用第二个参数（可选）重命名结果字段。

  ```    
List<Record> list = DbHelper.create()
        .selectMax("age")
        .findAll("members");
// 执行：SELECT MAX(`age`) AS `age`
// FROM `members`

List<Record> list = DbHelper.create()
        .selectMax("age", "member_age")
        .findAll("members");
// 执行：SELECT MAX(`age`) AS `member_age`
// FROM `members`
```

**selectMin()**

该方法用于编写查询语句中的 SELECT MIN(field) 部分，和 selectMax() 方法一样， 你可以使用第二个参数（可选）重命名结果字段。
```
List<Record> list = DbHelper.create()
        .selectMin("age")
        .findAll("members");
// 执行：SELECT MIN(`age`) AS `age`
// FROM `members`
```

**selectAvg()**

该方法用于编写查询语句中的 SELECT AVG(field) 部分，和 selectMax() 方法一样， 你可以使用第二个参数（可选）重命名结果字段。

```
List<Record> list = DbHelper.create()
        .selectAvg("age")
        .findAll("members");
// 执行：SELECT MIN(`age`) AS `age`
// FROM `members`
```

**selectSum()**

该方法用于编写查询语句中的 SELECT SUM(field) 部分，和 selectMax() 方法一样， 你可以使用第二个参数（可选）重命名结果字段。

```
List<Record> list = DbHelper.create()
        .selectSum("age")
        .findAll("members");
// 执行：SELECT SUM(`age`) AS `age`
// FROM `members`
```

**from()**

该方法用于编写查询语句中的 FROM 子句：

```
List<Record> list = DbHelper.create()
        .select("title, content, date")
        .from("mytable")
        .findAll();
// 执行：SELECT `title`, `content`, `date`
// FROM `mytable`
```

>注意：查询中的 FROM 部分可以在方法 findAll() 中指定，所以，你可以 选择任意一种你喜欢的方式。

**join()**

该方法用于编写查询语句中的 JOIN 子句：

```
List<Record> list = DbHelper.create()
        .select("*")
        .from("blogs")
        .join("comments", "comments.id=blogs.id")
        .findAll();
// 执行：SELECT *
// FROM blogs
// JOIN comments ON comments.id=blogs.id
```
如果你的查询中有多个连接，你可以多次调用这个方法。

你可以传入第三个参数指定连接的类型，有这样几种选择：left，right，outer，inner，left outer 和 right outer 。

```
List<Record> list = DbHelper.create()
        .select("*")
        .from("blogs")
        .join("comments", "comments.id=blogs.id", "left")
        .findAll();
// 执行：SELECT *
// FROM `blogs`
// LEFT JOIN `comments` ON `comments`.`id`=`blogs`.`id`
```

###<span id="jump_where">搜索</span>

**where()**

该方法提供了4中方式让你编写查询语句中的 WHERE 子句：

>注意：所有的数据将会自动转义，生成安全的查询语句。

1. **简单的 key/value 方式**：
 ```
List<Record> list = DbHelper.create()
        .where("name", "温建宝")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `name` = '温建宝'
```

注意自动为你加上了等号。

如果你多次调用该方法，那么多个 WHERE 条件将会使用 AND 连接起来：

```
List<Record> list = DbHelper.create()
        .where("name", "温建宝")
        .where("title", "Java入门教程")
        .where("status", 1)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `name` = '温建宝'
// AND `title` = 'Java入门教程'
// AND `status` = 1
```

2. **自定义 key/value 方式**：

为了控制比较，你可以在第一个参数中包含一个比较运算符：
```
List<Record> list = DbHelper.create()
        .where("name !=", "温建宝")
        .where("id <", 18)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `name` != '温建宝'
// AND `id` < 18
```

3. **关联Map方式**：

```
Map<String, Object> where = new LinkedHashMap<>();
where.put("name", "温建宝");
where.put("id", 18);
where.put("status", 2);

List<Record> list = DbHelper.create()
        .where(where)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `name` = '温建宝'
// AND `id` = 18
// AND `status` = 2
```

你也可以在这个方法里包含你自己的比较运算符：

```
Map<String, Object> where = new LinkedHashMap<>();
where.put("name !=", "温建宝");
where.put("id <", 18);
where.put("date >", "2019-03-09 12:45:03");

List<Record> list = DbHelper.create()
        .where(where)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `name` != '温建宝'
// AND `id` < 18
// AND `date` > '2019-03-09 12:45:03'
```

4. **自定义字符串**：

你可以完全手工编写 WHERE 子句：

```
String where = "name='温建宝' AND status=2 OR status=3";
List<Record> list = DbHelper.create()
        .where(where)
        .findAll("mytable");
// 执行：
// SELECT *
// FROM `mytable`
// WHERE `name` = '温建宝' AND `status` = 2 OR `status` = 3
```
>注意：where() 方法有一个可选的第三个参数，如果设置为 false，sqlHelper 将不保护你的表名和字段名。

```
List<Record> list = DbHelper.create()
        .where("mytable.name", "温建宝", false)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE mytable.name = 温建宝
```

**orWhere()**

这个方法和上面的方法一样，只是多个 WHERE 条件之间使用 OR 进行连接：

```
List<Record> list = DbHelper.create()
        .where("name !=", "温建宝")
        .orWhere("id >", 18)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `name` != '温建宝'
// OR `id` > 18
```

**whereIn()**

该方法用于生成 WHERE IN 子句，多个子句之间使用 AND 连接

```
List<Object> names = new ArrayList<>();
names.add("Frank");
names.add("Todd");
names.add("James");

List<Record> list = DbHelper.create()
        .whereIn("userName", names)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `userName` IN('Frank', 'Todd', 'James')
```

**orWhereIn()**

该方法用于生成 WHERE IN 子句，多个子句之间使用 OR 连接

```
List<Object> names = new ArrayList<>();
names.add("Frank");
names.add("Todd");
names.add("James");

List<Record> list = DbHelper.create()
        .where("id>", 18)
        .orWhereIn("userName", names)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `id` > 18
// OR `userName` IN('Frank', 'Todd', 'James')
```

**whereNotIn()**

```
List<Object> names = new ArrayList<>();
names.add("Frank");
names.add("Todd");
names.add("James");

List<Record> list = DbHelper.create()
        .where("id>", 18)
        .whereNotIn("userName", names)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `id` > 18
// AND `userName` NOT IN('Frank', 'Todd', 'James')
```

**orWhereNotIn()**

该方法用于生成 WHERE NOT IN 子句，多个子句之间使用 OR 连接

```
List<Object> names = new ArrayList<>();
names.add("Frank");
names.add("Todd");
names.add("James");

List<Record> list = DbHelper.create()
        .where("id>", 18)
        .orWhereNotIn("userName", names)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `id` > 18
// OR `userName` NOT IN('Frank', 'Todd', 'James')
```

###<span id="jump_like">模糊搜索</span>

**like()**

该方法用于生成 LIKE 子句，在进行搜索时非常有用。

>注意：所有数据将会自动被转义。

1. **简单 key/value 方式**：

```
List<Record> list = DbHelper.create()
        .like("title", "match")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE '%match%' ESCAPE '!'
```
如果你多次调用该方法，那么多个 WHERE 条件将会使用 AND 连接起来：

```
List<Record> list = DbHelper.create()
        .like("title", "match")
        .like("body", "match")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE '%match%' ESCAPE '!'
// AND  `body` LIKE '%match%' ESCAPE '!'
```

可以传入第三个可选的参数来控制 LIKE 通配符（%）的位置，可用选项有：'before'，'after' 和 'both'（默认为 'both'）。

```
List<Record> list = DbHelper.create()
        .like("title", "match", "before")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE '%match' ESCAPE '!'

List<Record> list = DbHelper.create()
        .like("title", "match", "after")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE 'match%' ESCAPE '!'

List<Record> list = DbHelper.create()
        .like("title", "match", "both")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE '%match%' ESCAPE '!'
```

2. **关联Map方式**：

```
Map<String, String> likes = new LinkedHashMap<>();
likes.put("title", "match");
likes.put("page1", "match");
likes.put("page2", "match");

List<Record> list = DbHelper.create()
        .like(likes)
        .findAll("mytable");
    }
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE '%match%' ESCAPE '!'
// AND  `page1` LIKE '%match%' ESCAPE '!'
// AND  `page2` LIKE '%match%' ESCAPE '!'
```

**orLike()**

这个方法和上面的方法一样，只是多个 WHERE 条件之间使用 OR 进行连接：

```
List<Record> list = DbHelper.create()
        .like("title", "match")
        .orLike("body", "match")
        .findAll("mytable");
    }
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE '%match%' ESCAPE '!'
// OR  `body` LIKE '%match%' ESCAPE '!'
```

**notLike()**

这个方法和 like() 方法一样，只是生成 NOT LIKE 子句：

```
List<Record> list = DbHelper.create()
        .notLike("title", "match")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` NOT LIKE '%match%' ESCAPE '!'
```

**orNotLike()**

这个方法和 notLike() 方法一样，只是多个 WHERE 条件之间使用 OR 进行连接：

```
List<Record> list = DbHelper.create()
        .like("title", "match")
        .orNotLike("body", "match")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// WHERE `title` LIKE '%match%' ESCAPE '!'
// OR  `body` NOT LIKE '%match%' ESCAPE '!'
```

**groupBy()**

该方法用于生成 GROUP BY 子句：

```
List<Record> list = DbHelper.create()
        .groupBy("title")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// GROUP BY `title`
```

你也可以通过一个数组传入多个值：

```
List<Record> list = DbHelper.create()
        .groupBy(new String[]{"title", "date"})
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// GROUP BY `title`, `date`
```

**distinct()**

该方法用于向查询中添加 DISTINCT 关键字：

```
List<Record> list = DbHelper.create()
        .distinct()
        .findAll("mytable");
// 执行：SELECT DISTINCT *
// FROM `mytable`
```

**having()**

该方法用于生成 HAVING 子句，有下面两种不同的语法：

```
List<Record> list = DbHelper.create()
        .having("userId = 45")
        .findAll("mytable");
// 执行：
// SELECT *
// FROM `mytable`
// HAVING `userId` = 45

List<Record> list = DbHelper.create()
        .having("userId", 45)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// HAVING `userId` = 45
```

你也可以通过一个Map传入多个值：

```
Map<String, Object> havings = new LinkedHashMap<>();
havings.put("title", "My Title");
havings.put("id <", 18);

List<Record> list = DbHelper.create()
        .having(havings)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// HAVING `title` = 'My Title'
// AND `id` < 18
```

如果 sqlHelper 自动转义你的查询，为了避免转义，你可以将第三个参数设置为 false 。

```
List<Record> list = DbHelper.create()
        .having("userId", 45)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// HAVING `userId` = 45

List<Record> list = DbHelper.create()
        .having("userId", 45, false)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// HAVING userId = 45
```

**orHaving()**

该方法和 having() 方法一样，只是多个条件之间使用 OR 进行连接。

###<span id="jump_orderby">排序</span>

**orderBy()**

该方法用于生成 ORDER BY 子句。

第一个参数为你想要排序的字段名，第二个参数用于设置排序的方向， 可选项有： ASC（升序），DESC（降序）和 RANDOM （随机）。

```
List<Record> list = DbHelper.create()
        .orderBy("title", "desc")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// ORDER BY `title` DESC
```

第一个参数也可以是你自己的排序字符串：

```
List<Record> list = DbHelper.create()
        .orderBy("title DESC, name ASC")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// ORDER BY `title` DESC, `name` ASC
```

如果需要根据多个字段进行排序，可以多次调用该方法。

```
List<Record> list = DbHelper.create()
        .orderBy("title", "DESC")
        .orderBy("name", "ASC")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// ORDER BY `title` DESC, `name` ASC
```

如果你选择了 RANDOM （随机排序），第一个参数会被忽略，但是你可以传入一个 数字值，作为随机数的 seed。

```
List<Record> list = DbHelper.create()
        .orderBy("title", "RANDOM")
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// ORDER BY RAND()

List<Record> list = DbHelper.create()
        .orderBy(45, "RANDOM")
        .findAll("mytable");
    }
// 执行：SELECT *
// FROM `mytable`
// ORDER BY RAND(45)
```

>Oracle 暂时还不支持随机排序，会默认使用升序

###<span id="jump_limit">分页与计数</span>

**limit()**

该方法用于限制你的查询返回结果的数量：

```
List<Record> list = DbHelper.create()
        .limit(10)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// LIMIT 10
```

输入二个参数时，第一个参数表示偏移，第二个参数表示返回数量限制。

```
List<Record> list = DbHelper.create()
        .limit(8, 10)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// LIMIT 8, 10
```

**offset()**

跟 limit() 配合使用，设置偏移量

```
List<Record> list = DbHelper.create()
        .limit(10)
        .offset(8)
        .findAll("mytable");
// 执行：SELECT *
// FROM `mytable`
// LIMIT 8, 10

```

**findCount()**

该方法用于获取特定查询返回结果的数量，也可以使用查询构造器的这些方法： where()，orWhere()，like()，orLike() 等等。举例：

```
int count = DbHelper.create()
        .where("age >", 18)
        .like("name", "match")
        .findCount("mytable");
// 执行：SELECT COUNT(*) AS `numrows`
// FROM `mytable`
// WHERE `age` > 18
// AND  `name` LIKE '%match%' ESCAPE '!'
```

但是，这个方法会重置你在 select() 方法里设置的所有值，如果你希望保留它们，可以将 第二个参数设置为 false。

```
int count = DbHelper.create()
        .where("age >", 18)
        .like("name", "match")
        .findCount("mytable", false);
```

###<span id="jump_groupby">查询条件组</span>

查询条件组可以让你生成用括号括起来的一组 WHERE 条件，这能创造出非常复杂的 WHERE 子句， 支持嵌套的条件组。例如：

```
List<Record> list = DbHelper.create()
        .select("*").from("my_table")
            .groupStart()
                .where("aa", "11")
                .orGroupStart()
                    .where("bb", "22")
                    .where("cc", "33")
                .groupEnd()
            .groupEnd()
            .where("dd", "44")
        .findAll();
// 执行：SELECT *
// FROM `my_table`
// WHERE (
// `aa` = '11'
// OR  (
//         `bb` = '22'
//         AND `cc` = '33'
//     )
// )
// AND `dd` = '44'
```

> 注意：条件组必须要配对，确保每个 groupStart() 方法都有一个 groupEnd() 方法与之配对。

**groupStart()**

开始一个新的条件组，为查询中的 WHERE 条件添加一个左括号。

**orGroupStart()**

开始一个新的条件组，为查询中的 WHERE 条件添加一个左括号，并在前面加上 OR 。

**notGroupStart()**

开始一个新的条件组，为查询中的 WHERE 条件添加一个左括号，并在前面加上 NOT 。

**orNotGroupStart()**

开始一个新的条件组，为查询中的 WHERE 条件添加一个左括号，并在前面加上 OR NOT 。

**groupEnd()**

结束当前的条件组，为查询中的 WHERE 条件添加一个右括号。

###<span id="jump_insert">插入数据</span>

**insert()**

该方法根据你提供的数据生成一条 INSERT 语句并执行，它的参数是一个 Map对象，举例：

```
Map<String, Object> data = new LinkedHashMap<>();
data.put("title", "My Title");
data.put("name", "My Name");
data.put("date", "My Date");

DbHelper.create().insert("mytable", data);
// 执行：
// INSERT INTO `mytable` (`title`, `name`, `date`) VALUES ('My Title', 'My Name', 'My Date')
```

第一个参数为要插入的表名，第二个参数为要插入的数据，是个Map对象。

> 注意：所有数据会被自动转义，生成安全的查询语句。

**getCompiledInsert()**

该方法和 insert() 方法一样根据你提供的数据生成一条 INSERT 语句，但是并不执行，举例：

```
Map<String, Object> data = new LinkedHashMap<>();
data.put("title", "My Title");
data.put("name", "My Name");
data.put("date", "My Date");

String sql = DbHelper.create().set(data).getCompiledInsert("mytable");
System.out.println(sql);
// 输出：
// INSERT INTO `mytable` (`title`, `name`, `date`) VALUES ('My Title', 'My Name', 'My Date')
```

第二个参数用于设置是否重置查询（默认情况下会重置，正如 insert() 方法一样）：

```
DbHelper dbHelper = DbHelper.create();

String sql = dbHelper.set("title", "My Title").getCompiledInsert("mytable", false);
System.out.println(sql);
// 输出：
// INSERT INTO `mytable` (`title`) VALUES ('My Title')

String sql2 = dbHelper.set("content", "My Content").getCompiledInsert();
System.out.println(sql2);
// 输出：
// INSERT INTO `mytable` (`title`, `content`) VALUES ('My Title', 'My Content')
```

上面的例子中，最值得注意的是，第二个查询并没有用到 from() 方法， 也没有为查询指定表名参数，但是它生成的 SQL 语句中有 INTO mytable 子句。 这是因为查询并没有被重置（使用 insert() 方法会被执行并被重置， 使用 resetQuery() 方法直接重置）。

> 注意：这个方法不支持批量插入。

**insertBatch()**

该方法根据你提供的数据生成一条 INSERT 语句并执行，它的参数是一个 数组，举例：

```
List<Map<String, Object>> data = new ArrayList<>();

Map<String, Object> item = new HashMap<>();
item.put("title", "My title");
item.put("name", "My Name");
item.put("date", "My date");
data.add(item);

Map<String, Object> item2 = new HashMap<>();
item2.put("title", "Another title");
item2.put("name", "Another Name");
item2.put("date", "Another date");
data.add(item2);

DbHelper.create().insertBatch("mytable", data);

// 执行：
// INSERT INTO `mytable` (`date`, `name`, `title`) VALUES ('My date','My Name','My title'), ('Another date','Another Name','Another title')
```

第一个参数为要插入的表名，第二个参数为要插入的数据。

> 所有数据会被自动转义，生成安全的查询语句。

###<span id="jump_update">更新数据</span>

**replace()**

该方法用于执行一条 REPLACE 语句，REPLACE 语句根据表的【主键】和【唯一索引】来执行，类似于标准的 DELETE + INSERT 。 使用这个方法，你不用再手工去实现 select()，update()，delete() 以及 insert() 这些方法的不同组合，为你节约大量时间。

例如：

```
Map<String, Object> data = new HashMap<>();
data.put("title", "My title");
data.put("name", "My Name");
data.put("date", "My date");

DbHelper.create().replace("mytable", data);
// 执行：
// REPLACE INTO `mytable` (`date`, `name`, `title`) VALUES ('My date', 'My Name', 'My title')
```

上面的例子中，我们假设 title 字段是我们的主键，那么如果我们数据库里有一行 的 title 列的值为 'My title'，这一行将会被删除并被我们的新数据所取代。

也可以使用 set() 方法，而且所有字段都被自动转义，正如 insert() 方法一样。

**set()**

该方法用于设置新增或更新的数据。

**该方法可以取代直接传递数据Map到 insert() 或 update() 方法：**

```
DbHelper.create()
        .set("name", "温建宝")
        .insert("mytable");
// 执行：
// INSERT INTO `mytable` (`name`) VALUES ('温建宝')
```

如果你多次调用该方法，它会正确组装出 INSERT 或 UPDATE 语句来：

```
DbHelper.create()
        .set("name", "温建宝")
        .set("title", "Java入门教程")
        .set("status", 2)
        .insert("mytable");
// 执行：
// INSERT INTO `mytable` (`name`, `title`, `status`) VALUES ('温建宝', 'Java入门教程', 2)
```

set() 方法也接受可选的第三个参数（escape），如果设置为 false，数据将不会自动转义。为了说明两者之间的区别，这里有一个带转义的 set() 方法和不带转义的例子。

```
DbHelper.create()
        .set("field", "field+1", false)
        .set("id", 9)
        .insert("mytable");
// 执行：
// INSERT INTO `mytable` (field, `id`) VALUES (field+1, 9)

DbHelper.create()
        .set("field", "field+1")
        .set("id", 9)
        .insert("mytable");
// 执行：
// INSERT INTO `mytable` (`field`, `id`) VALUES ('field+1', 9)
```

你也可以传一个关联Map对象作为参数：

```
Map<String, Object> data = new LinkedHashMap<>();
data.put("name", "温建宝");
data.put("title", "Java入门教程");
data.put("status", 9);

DbHelper.create()
        .set(data)
        .insert("mytable");
// 执行：
// INSERT INTO `mytable` (`name`, `title`, `status`) VALUES ('温建宝', 'Java入门教程', 9)
```

**update()**

该方法根据你提供的数据生成一条 UPDATE 语句并执行，它的参数是一个 Map对象 ，举例：

```
Map<String, Object> data = new LinkedHashMap<>();
data.put("name", "温建宝");
data.put("title", "Java入门教程");
data.put("date", "2019-03-10");

DbHelper.create()
        .where("id", 20)
        .update("mytable", data);
// 执行：
// UPDATE `mytable` SET `name` = '温建宝', `title` = 'Java入门教程', `date` = '2019-03-10'
// WHERE `id` = 20
```

>所有数据会被自动转义，生成安全的查询语句。

你应该注意到 where() 方法的使用，它可以为你设置 WHERE 子句。 你也可以直接使用字符串形式设置 WHERE 子句：

```
Map<String, Object> data = new LinkedHashMap<>();
data.put("name", "温建宝");
data.put("title", "Java入门教程");
data.put("date", "2019-03-10");

DbHelper.create()
        .update("mytable", data, "id=20");
// 执行：
// UPDATE `mytable` SET `name` = '温建宝', `title` = 'Java入门教程', `date` = '2019-03-10'
// WHERE `id` = 20
```

或者使用一个Map对象：

```
Map<String, Object> data = new LinkedHashMap<>();
data.put("name", "温建宝");
data.put("title", "Java入门教程");
data.put("date", "2019-03-10");

Map<String, Object> where = new LinkedHashMap<>();
where.put("id", 4);

DbHelper.create()
        .update("mytable", data, where);
// 执行：
// UPDATE `mytable` SET `name` = '温建宝', `title` = 'Java入门教程', `date` = '2019-03-10'
// WHERE `id` = 4
```

当执行 UPDATE 操作时，你还可以使用上面介绍的 set() 方法。

**updateBatch()**

该方法根据你提供的数据生成一条 UPDATE 语句并执行，它的参数是一个数组，举例：

```
List<Map<String, Object>> data = new ArrayList<>();

Map<String, Object> set = new HashMap<>();
set.put("title", "My Title");
set.put("name", "My Name");
set.put("date", "My Date");
data.add(set);

Map<String, Object> set2 = new HashMap<>();
set2.put("title", "My Title2");
set2.put("name", "My Name2");
set2.put("date", "My Date2");
data.add(set2);

DbHelper.create()
        .updateBatch("mytable", data, "title");
// 执行：
// UPDATE `mytable` SET `date` = CASE 
// WHEN `title` = 'My Title' THEN 'My Date'
// WHEN `title` = 'My Title2' THEN 'My Date2'
// ELSE`date` END, `name` = CASE 
// WHEN `title` = 'My Title' THEN 'My Name'
// WHEN `title` = 'My Title2' THEN 'My Name2'
// ELSE`name` END, 
// WHERE `title` IN('My Title', 'My Title2')
```

第一个参数为要更新的表名，第二个参数为要更新的数据，是个Map对象数组，第三个 参数是 WHERE 语句的键。

> 所有数据会被自动转义，生成安全的查询语句。

**getCompiledUpdate()**

该方法和 getCompiledInsert() 方法完全一样，除了生成的 SQL 语句是 UPDATE 而不是 INSERT。

> 该方法不支持批量更新。

###<span id="jump_delete">删除数据</span>

**delete()**

该方法生成 DELETE 语句并执行。

```
Map<String, Object> where = new HashMap<>();
where.put("id", 9);

DbHelper.create()
        .delete("mytable", where);
// 执行：
// DELETE FROM `mytable`
// WHERE `id` = 9
```

第一个参数为表名，第二个参数为 WHERE 条件。你也可以不用第二个参数， 使用 where() 或者 orWhere() 函数来替代它：

```
DbHelper.create()
        .where("id", 9)
        .delete("mytable");
// 执行：
// DELETE FROM `mytable`
// WHERE `id` = 9
```

如果你想要删除一个表中的所有数据，可以使用 truncate() 或 emptyTable() 方法。

**emptyTable()**

该方法生成 DELETE 语句并执行：

```
DbHelper.create()
        .emptyTable("mytable");
// 执行：
// DELETE FROM `mytable`
```

**truncate()**

该方法生成 TRUNCATE 语句并执行。

```
DbHelper.create()
        .truncate("mytable");
// 或
DbHelper.create()
        .from("mytable")
        .truncate();

// 执行：
// TRUNCATE `mytable`
```

**getCompiledDelete()**

该方法和 getCompiledInsert() 方法完全一样，除了生成的 SQL 语句是 DELETE 而不是 INSERT。

###<span id="jump_chain">链式方法</span>

通过将多个方法连接在一起，链式方法可以大大的简化你的语法。感受一下这个例子：

```
DbHelper.create()
        .select("title")
        .where("id", 20)
        .findAll("mytable");
```

###<span id="jump_cache">查询构造器缓存</span>

尽管不是 "真正的" 缓存，查询构造器允许你将查询的某个特定部分保存（或 "缓存"）起来， 以便在你的脚本执行之后重用。一般情况下，当查询构造器的一次调用结束后，所有已存储的信息 都会被重置，以便下一次调用。如果开启缓存，你就可以使信息避免被重置，方便你进行重用。

缓存调用是累加的。如果你调用了两次有缓存的 select()，然后再调用两次没有缓存的 select()， 这会导致 select() 被调用4次。

有三个可用的缓存方法方法：

**startCache()**

如需开启缓存必须先调用此方法，所有支持的查询类型（见下文）都会被存储起来供以后使用。

**stopCache()**

此方法用于停止缓存。

**flushCache()**

此方法用于清空缓存。

这里是一个使用缓存的例子：

```
DbHelper dbHelper = DbHelper.create();

dbHelper.startCache()
.select("field1")
.stopCache()
.findAll("mytable");
// 执行：
// SELECT `field1`
// FROM `mytable`

dbHelper.select("field2").findAll("mytable");
// 执行：
// SELECT `field1`, `field2` FROM (`mytable`)

dbHelper.flushCache()
        .select("field2")
        .findAll("mytable");
// 执行：
// SELECT `field2` FROM (`mytable`)
```

> 支持缓存的语句有: select, from, join, where, like, groupBy, having, orderBy

###<span id="jump_reset">重置查询构造器</span>

**resetQuery()**

该方法无需执行就能重置查询构造器中的查询，findAll() 和 insert() 方法也可以用于重置查询，但是必须要先执行它。和这两个方法一样，使用`查询构造器缓存`_ 缓存下来的查询不会被重置。

当你在使用查询构造器生成 SQL 语句（如：getCompiledSelect()），之后再执行它。这种情况下，不重置查询缓存将非常有用：

```
DbHelper dbHelper = DbHelper.create();

dbHelper.select("field1, field2")
        .where("field3", 5)
        .getCompiledSelect("mytable", false);

List<Record> list = dbHelper.findAll();
// 执行：
// SELECT field1, field1 from mytable where field3 = 5;
```

> 如果你正在使用查询构造器缓存功能，连续两次调用 getCompiledSelect() 方法 并且不重置你的查询，这将会导致缓存被合并两次。举例来说，例如你正在缓存 select() 方法，那么会查询两个相同的字段。

###<span id="jump_jfinal">对 JFinal DB API 简单封装</span>

DbHelper.java : 继承了SqlHelper类，拥有构造SQL字符串的能力，同时简单封装了JFinal框架执行SQL语句的API，让自己又有执行SQL语句的能力。

**create()**

返回 DbHelper 新实例，同时可传入 configName  参数，自由切换 数据库的连接，举例：

```
DbHelper dbHelper = DbHelper.create("mysql_pro");
```

**findAll()**

返回 所有的 查询记录，举例：

```
List<Record> list = DbHelper.create()
        .from("blog")
        .where("userName", "温建宝")
        .findAll();
// 执行：
// SELECT *
// FROM `blog`
// WHERE `userName` = '温建宝'
```

**findFirst()**

返回 一条 查询记录，举例：

```
Record record = DbHelper.create()
        .from("blog")
        .where("userName", "温建宝")
        .findFirst();
// 执行：
// SELECT *
// FROM `blog`
// WHERE `userName` = '温建宝'
// LIMIT 1
```

**findCount()**

返回 满足查询条件的记录数，举例：

```
int count = DbHelper.create()
        .from("blog")
        .where("userName", "温建宝")
        .findCount();
// 执行：
// SELECT COUNT(*) AS `numrows`
// FROM `blog`
// WHERE `userName` = '温建宝'
```

**findPage()**

查询分页

```
Page<Record> page = DbHelper.create()
        .from("blog")
        .where("userName", "温建宝")
        .findPage(2, 10);
```

###<span id="jump_jfinal_model">对 JFinal Model API 简单封装</span>


ModelHelper.java : 继承了SqlHelper类，拥有构造SQL字符串的能力，同时传入 模型实例，让自己又有执行SQL语句的能力。

```
/**
 * 模型 助手类
 *
 * @author php-note.com
 */
@SuppressWarnings({"serial"})
public class ModelHelper<M extends Model<M>> extends SqlHelper<ModelHelper<M>> {
    private M dao;    // 模型实例，专门用来执行SQL的

    /**
     * 构造方法
     *
     * @param dao 模型实例
     */
    public ModelHelper(M dao) {
        this.dao = dao;
    }
    
    // 省略代码。。。
}
```

假如有一个模型：Blog.java

```
/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Blog extends BaseBlog<Blog> {
    public static       String tableName = "blog";
    public static final Blog   dao       = new Blog().dao();

    // 省略代码。。。
}
```

**findAll()**

返回 所有的 查询记录，举例：

```
List<Blog> blogList = new ModelHelper<>(Blog.dao)
        .from(Blog.tableName)
        .where("blogId >", 2)
        .findAll();
```

**findFirst()**

返回 一条 查询记录，举例：

```
Blog blog = new ModelHelper<>(Blog.dao)
        .from(Blog.tableName)
        .where("blogId", 2)
        .findFirst();
```

**findPage()**

查询分页

```
Page<Blog> page = new ModelHelper<>(Blog.dao)
        .from(Blog.tableName)
        .where("blogId >", 2)
        .findPage(1, 20);
```
