package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.util.ReflectUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class ReflectRegressionTest {
    @Test
    public void scalarAndContainerSubclassesAreNotBeanBindingCandidates() {
        for (Class<?> type : new Class<?>[]{String.class, int.class, String[].class, Runnable.class,
                BusinessNumber.class, BusinessText.class, BusinessDate.class, BusinessCalendar.class,
                BusinessMap.class, BusinessList.class}) {
            assertFalse(type.getName(), ReflectUtils.isBindableObject(type));
        }
    }

    @Test
    public void dynamicProxiesAndEnumConstantBodiesAreNotBeanBindingCandidates() {
        Object proxy = java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Runnable.class}, (instance, method, args) -> null);
        assertFalse(ReflectUtils.isBindableObject(proxy.getClass()));
        assertFalse(ReflectUtils.isBindableObject(EnumWithBody.class));
        assertFalse(ReflectUtils.isBindableObject(EnumWithBody.VALUE.getClass()));
    }

    @Test
    public void existingArgumentBeansDoNotNeedADefaultConstructor() {
        assertTrue(ReflectUtils.isBindableObject(ConstructorOnly.class));
        assertTrue(ReflectUtils.isBindableObject(Child.class));
        assertEquals(Long.valueOf(42), DataRow.ofEntity(new ConstructorOnly(42L)).get("id"));
    }

    @Test
    public void resultMappingStillReportsUninstantiableBeanTypes() {
        for (Class<?> type : new Class<?>[]{ConstructorOnly.class, AbstractBean.class}) {
            try {
                DataRow.of("id", 42L).toEntity(type);
                fail("Expected instantiation failure for " + type.getName());
            } catch (IllegalStateException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().contains(type.getName()));
                assertTrue(ex.getCause() instanceof NoSuchMethodException
                        || ex.getCause() instanceof InstantiationException);
            }
        }
    }

    @Test
    public void returnGenericTypesUseClassesDirectlyIncludingNestedTypesAndArrays() throws Exception {
        assertSame(LoadedEntity.class, ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("entities"), 0));
        assertSame(LoadedEntity.class, ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("optional"), 0));
        assertSame(Map.class, ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("maps"), 0));
        assertSame(String[].class, ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("arrays"), 0));
        assertSame(int[].class, ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("primitiveArrays"), 0));
    }

    @Test
    public void returnTypesWithoutGenericArgumentsReturnNull() throws Exception {
        assertNull(ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("plain"), 0));
        assertNull(ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("raw"), 0));
    }

    @Test
    public void multipleReturnArgumentsCanBeSelectedByIndex() throws Exception {
        Method map = GenericReturns.class.getMethod("map");
        assertSame(String.class, ReflectUtils.getReturnGenericType(map, 0));
        assertSame(Object.class, ReflectUtils.getReturnGenericType(map, 1));
        assertSame(List.class, ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("nestedMap"), 1));
        assertSame(String.class, ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("partlyUnresolved"), 0));
        try {
            ReflectUtils.getReturnGenericType(GenericReturns.class.getMethod("partlyUnresolved"), 1);
            fail("Expected an unresolved second argument error");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("index 1"));
        }
    }

    @Test
    public void returnGenericArgumentIndexesAreChecked() throws Exception {
        Method map = GenericReturns.class.getMethod("map");
        for (int index : new int[]{-1, 2}) {
            try {
                ReflectUtils.getReturnGenericType(map, index);
                fail("Expected an invalid argument index error");
            } catch (IndexOutOfBoundsException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().contains(String.valueOf(index)));
            }
        }
    }

    @Test
    public void unresolvedReturnArgumentsIdentifyTheMethodInsteadOfGuessingAClass() throws Exception {
        for (String name : new String[]{"wildcard", "upperBound", "lowerBound", "variable", "genericArray"}) {
            Method method = GenericReturns.class.getMethod(name);
            try {
                ReflectUtils.getReturnGenericType(method, 0);
                fail("Expected an unresolved argument error for " + name);
            } catch (IllegalArgumentException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().contains(name));
                assertTrue(ex.getMessage(), ex.getMessage().contains("concrete return generic type"));
            }
        }
    }

    @Test
    public void returnGenericTypePreservesTheDeclaringLoaderWithoutInitializingTheEntity() throws Exception {
        InitializationTracker.initialized = false;
        ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.equals(GenericReturns.class.getName()) && !name.equals(LoadedEntity.class.getName())) {
                    return super.loadClass(name, resolve);
                }
                Class<?> type = findLoadedClass(name);
                if (type == null) {
                    String resource = name.replace('.', '/') + ".class";
                    try (InputStream in = getParent().getResourceAsStream(resource)) {
                        if (in == null) {
                            throw new ClassNotFoundException(name);
                        }
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int size;
                        while ((size = in.read(buffer)) != -1) {
                            bytes.write(buffer, 0, size);
                        }
                        byte[] content = bytes.toByteArray();
                        type = defineClass(name, content, 0, content.length);
                    } catch (IOException ex) {
                        throw new ClassNotFoundException(name, ex);
                    }
                }
                if (resolve) {
                    resolveClass(type);
                }
                return type;
            }
        };
        Method method = loader.loadClass(GenericReturns.class.getName()).getMethod("entities");
        Class<?> result = ReflectUtils.getReturnGenericType(method, 0);
        assertSame(loader, result.getClassLoader());
        assertSame(loader.loadClass(LoadedEntity.class.getName()), result);
        assertNotSame(LoadedEntity.class, result);
        assertFalse(InitializationTracker.initialized);
    }

    @Test
    public void overriddenGettersKeepFieldsFromMultipleParentLevels() {
        assertEquals(Base.class, ReflectUtils.getBeanPropertyMetas(Child.class)
                .get("id").getField().getDeclaringClass());
    }

    @Test
    public void shadowedFieldsUseTheConcreteClass() {
        assertEquals(Shadow.class, ReflectUtils.getBeanPropertyMetas(Shadow.class)
                .get("id").getField().getDeclaringClass());
    }

    @Test
    public void inheritedFieldsAreResolvedForSetterOnlyProperties() {
        assertEquals(WriteOnly.class, ReflectUtils.getBeanPropertyMetas(WriteOnlyChild.class)
                .get("value").getField().getDeclaringClass());
    }

    @Test
    public void overriddenGettersPreserveCustomColumnMappingInBothDirections() {
        Child child = new Child();
        child.setId(42L);
        DataRow row = DataRow.ofEntity(child, field -> "db_" + field.getName());
        assertEquals(Long.valueOf(42), row.get("db_id"));
        assertFalse(row.containsKey("id"));
        Child restored = row.toEntity(Child.class, field -> "db_" + field.getName(), null);
        assertEquals(Long.valueOf(42), restored.getId());
    }

    @Test
    public void booleanGetterMethodReferencesUseJavaBeanNames() {
        assertEquals("active", ReflectUtils.getFieldName(Flags::isActive));
        assertEquals("URL", ReflectUtils.getFieldName(Flags::isURL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isPrefixedNonBooleanMethodsAreRejected() {
        ReflectUtils.getFieldName(Flags::isLabel);
    }

    public static class Base {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class Middle extends Base {
    }

    public static class Child extends Middle {
        @Override public Long getId() { return super.getId(); }
    }

    public static class Shadow extends Base {
        private Long id;
        @Override public Long getId() { return id; }
        @Override public void setId(Long id) { this.id = id; }
    }

    public static class WriteOnly {
        private String value;
        public void setValue(String value) { this.value = value; }
    }

    public static class WriteOnlyChild extends WriteOnly {
        @Override public void setValue(String value) { super.setValue(value); }
    }

    public static class Flags {
        public boolean isActive() { return true; }
        public boolean isURL() { return true; }
        public String isLabel() { return "label"; }
    }

    public interface GenericReturns<T> {
        List<LoadedEntity> entities();
        Optional<LoadedEntity> optional();
        List<Map<String, Object>> maps();
        List<String[]> arrays();
        List<int[]> primitiveArrays();
        String plain();
        List raw();
        Map<String, Object> map();
        Map<String, List<LoadedEntity>> nestedMap();
        Map<String, T> partlyUnresolved();
        List<?> wildcard();
        List<? extends Number> upperBound();
        List<? super Integer> lowerBound();
        List<T> variable();
        List<T[]> genericArray();
    }

    public static class LoadedEntity {
        static { InitializationTracker.initialized = true; }
    }

    public static class InitializationTracker {
        public static boolean initialized;
    }

    public static class BusinessNumber extends Number {
        public int intValue() { return 1; }
        public long longValue() { return 1; }
        public float floatValue() { return 1; }
        public double doubleValue() { return 1; }
    }

    public static class BusinessText implements CharSequence {
        public int length() { return 0; }
        public char charAt(int index) { throw new IndexOutOfBoundsException(); }
        public CharSequence subSequence(int start, int end) { return ""; }
    }

    public static class BusinessDate extends java.util.Date {}
    public static class BusinessCalendar extends java.util.GregorianCalendar {}
    public static class BusinessMap extends java.util.HashMap<String, Object> {}
    public static class BusinessList extends java.util.ArrayList<Object> {}
    public enum EnumWithBody { VALUE {} }
    public abstract static class AbstractBean {}

    public static class ConstructorOnly {
        private final Long id;
        public ConstructorOnly(Long id) { this.id = id; }
        public Long getId() { return id; }
    }
}
