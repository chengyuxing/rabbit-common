package tests.entity;

import com.github.chengyuxing.common.DataRow;

import java.util.Map;
import java.util.Set;

public class Your {
    private Map<String, Object> map;

    public Your(Map<String, Object> map) {
        this.map = map;
    }

    public Your(String param) {
        this.map = DataRow.of("param", param);
    }

    private Your() {
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return "Your{" +
                "map=" + map +
                '}';
    }
}
