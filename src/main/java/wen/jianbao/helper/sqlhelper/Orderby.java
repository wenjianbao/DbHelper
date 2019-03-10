package wen.jianbao.helper.sqlhelper;

public class Orderby {
    private String  field     = "";
    private String  direction = "";
    private boolean escape    = false;
    private String  compile   = null;       // 已编译的结果

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
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
