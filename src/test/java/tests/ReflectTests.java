package tests;

import org.junit.Test;
import rabbit.common.types.DataRow;
import rabbit.common.utils.ReflectUtil;
import tests.entity.User;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ReflectTests {


    @Test
    public void classTest() throws Exception{
        System.out.println(Collection.class.isAssignableFrom(ArrayList.class));
    }

    @Test
    public void beanTest() throws Exception {
        ReflectUtil.getWriteMethods(User.class)
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
        ReflectUtil.getReadMethods(User.class)
                .forEach(m -> {
                    System.out.println(m.getReturnType() == Class.class);
                });
    }
}
