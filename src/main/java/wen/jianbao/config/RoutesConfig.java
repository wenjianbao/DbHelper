package wen.jianbao.config;

import com.jfinal.config.Routes;
import wen.jianbao.controller.home.HomeController;

public class RoutesConfig extends Routes {
    @Override
    public void config() {
        add("/", HomeController.class, "home/Home");
    }
}
