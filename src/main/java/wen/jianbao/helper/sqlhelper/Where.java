package wen.jianbao.helper.sqlhelper;

public class Where {
    private String  condition = "";
    private boolean escape    = false;      // 是否转义
    private String  compile   = null;       // 已编译的结果

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isEscape() {
        return escape;
    }

    public void setEscape(boolean escape) {
        this.escape = escape;
    }

    public String getCompile() {
        return compile;
    }

    public void setCompile(String compile) {
        this.compile = compile;
    }
}
