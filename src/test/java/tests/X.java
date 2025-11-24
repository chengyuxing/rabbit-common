package tests;

import com.github.chengyuxing.common.script.pipe.IPipe;

public class X implements IPipe<Integer> {
    @Override
    public Integer transform(Object value, Object... params) {
        if (params.length == 1 && params[0] instanceof Integer && value instanceof Integer) {
            return (Integer) params[0] * (Integer) value;
        }
        return 0;
    }
}
