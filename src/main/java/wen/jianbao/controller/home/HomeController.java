package wen.jianbao.controller.home;

import com.jfinal.core.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认控制器
 */
public class HomeController extends Controller {
    private Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * 首页
     */
    public String index() {
        return "home/index.html";
    }
}
