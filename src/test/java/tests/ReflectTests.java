package tests;

import org.junit.Test;
import rabbit.common.types.DataRow;
import rabbit.common.utils.ReflectUtil;
import tests.entity.User;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ReflectTests {
    @Test
    public void test1() throws Exception {
        DataRow row = DataRow.fromList(Arrays.asList("chengyuxing", 27, 13), "name", "age", "id");

        User user = row.toEntity(User.class);

        System.out.println(user);

        System.out.println(DataRow.fromEntity(user));
    }

    @Test
    public void beanTest() throws Exception {
        ReflectUtil.getSetMethods(User.class)
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
    public void convert() throws Exception{
        ReflectUtil.getGetMethods(User.class)
                .forEach(m->{
                    System.out.println(m.getReturnType() == Class.class);
                });
    }
}
