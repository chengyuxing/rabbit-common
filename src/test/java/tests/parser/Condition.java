package tests.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Condition {
    private Map<String, Object> args;
    private String expression;
    private List<String> content = new ArrayList<>();
    private List<String> prefix = new ArrayList<>();
    private List<String> suffix = new ArrayList<>();
    private List<Condition> child = new ArrayList<>();

    public Condition(Map<String, Object> args, String expression) {
        this.args = args;
        this.expression = expression;
    }

    public List<Condition> getChild() {
        return child;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setChild(List<Condition> child) {
        this.child = child;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public List<String> getContent() {
        return content;
    }

    public String getExpression() {
        return expression;
    }

    public List<String> getSuffix() {
        return suffix;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public void setSuffix(List<String> suffix) {
        this.suffix = suffix;
    }

    public List<String> getPrefix() {
        return prefix;
    }

    public void setPrefix(List<String> prefix) {
        this.prefix = prefix;
    }
}
