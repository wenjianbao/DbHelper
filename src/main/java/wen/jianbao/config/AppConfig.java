package wen.jianbao.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.*;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import wen.jianbao.model.base._MappingKit;

public class AppConfig extends JFinalConfig {
    @Override
    public void configConstant(Constants me) {
        // 加载少量必要配置，随后可用 Prokit.get(...)获取值
        PropKit.use("config.properties");
        me.setDevMode(PropKit.getBoolean("devMode", false));

        // 最大的提交的body的大小
        me.setMaxPostSize(1024 * 1024 * 1024);
    }

    @Override
    public void configRoute(Routes me) {
        me.add(new RoutesConfig());
    }

    /**
     * 设置 模板引擎
     */
    @Override
    public void configEngine(Engine me) {
        // devMode 配置为 true，将支持模板实时热加载
        me.setDevMode(true);
        me.setBaseTemplatePath(PathKit.getWebRootPath() + "/view/default");
    }

    /**
     * 配置 插件
     */
    @Override
    public void configPlugin(Plugins me) {
        // 配置 druid 数据库连接池插件
        DruidPlugin druidPlugin = createDruidPlugin();

        // 配置 druid 监控
        druidPlugin.addFilter(new StatFilter()); // 监控  /druid/index.html
        WallFilter wall = new WallFilter();      // 防SQL注入
        wall.setDbType("mysql");
        druidPlugin.addFilter(wall);
        druidPlugin.setMaxActive(1200);
        druidPlugin.setTestOnBorrow(true);
        druidPlugin.setTestOnReturn(true);
        me.add(druidPlugin);

        // 配置ActiveRecord插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setShowSql(true);
        _MappingKit.mapping(arp);
        me.add(arp);
    }

    public static DruidPlugin createDruidPlugin() {
        return new DruidPlugin(PropKit.get("db_jdbcUrl"), PropKit.get("db_user"), PropKit.get("db_password").trim());
    }

    /**
     * 配置 全局拦截器
     */
    @Override
    public void configInterceptor(Interceptors me) {
    }

    /**
     * 配置 处理器
     */
    @Override
    public void configHandler(Handlers me) {
    }
}
