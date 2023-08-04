package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.utils.ReflectUtil;
import org.junit.Test;
import tests.entity.Coord;
import tests.entity.Location;
import tests.entity.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class ReflectTests {

    @Test
    public void r2e() throws Exception {
        int x = 10, y = 18;
        DataRow row = DataRow.of("x", x, "y", y, "name", "昆明市");
        System.out.println(row);
        System.out.println(row.toEntity(Coord.class, row.get("x"), row.get("y")));
    }

    @Test
    public void reflect() throws Exception {
        Constructor<?>[] constructors = Location.class.getDeclaredConstructors();
        Stream.of(constructors).forEach(c -> {
            System.out.println(Arrays.toString(c.getGenericParameterTypes()));
        });
        System.out.println(int.class.getSimpleName());
    }

    @Test
    public void classTest() throws Exception {
        System.out.println(Collection.class.isAssignableFrom(ArrayList.class));
    }

    @Test
    public void beanTest() throws Exception {
        ReflectUtil.getRWMethods(User.class).getItem2()
                .forEach(method -> {
                    // set方法的第一个参数类型
                    Type pType = method.getGenericParameterTypes()[0];
                    // 如果类型是泛型类型
                    if (pType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) pType;
                        System.out.println(pt);
                        Type[] argType = pt.getActualTypeArguments();
                        System.out.println(argType[0]);
//                        System.out.println(Arrays.toString(pt.getActualTypeArguments()));
                    } else {
//                        System.out.println(pType);
                    }
                });
    }

    @Test
    public void convert() throws Exception {
        ReflectUtil.getRWMethods(User.class).getItem1()
                .forEach(m -> {
                    System.out.println(m.getReturnType() == Class.class);
                });
    }
}
