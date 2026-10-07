package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.util.ReflectUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReflectRegressionTest {
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
}
