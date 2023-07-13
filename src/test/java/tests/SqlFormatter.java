package tests;

import com.github.chengyuxing.common.StringFormatter;

public class SqlFormatter extends StringFormatter {
    /**
     * 占位符键名称的特殊前缀，可用来对特殊的对象类型值做一些特别的处理，默认为 ':'
     *
     * @param specialPrefix 特殊前缀
     */
    public SqlFormatter(char specialPrefix) {
        super(specialPrefix);
    }
}
