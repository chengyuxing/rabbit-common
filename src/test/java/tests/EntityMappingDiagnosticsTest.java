package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.util.ValueUtils;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class EntityMappingDiagnosticsTest {
    @Test
    public void invalidUuidIdentifiesInheritedPropertyAndMappedColumn() {
        IllegalStateException ex = failure(() -> DataRow.of("db_id", "invalid-uuid")
                .toEntity(UuidChild.class, field -> "db_" + field.getName(), null));
        assertContext(ex, UuidChild.class, "db_id", "id", String.class.getName(), UUID.class.getName());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void invalidDateIdentifiesItsFieldAndOriginalConversionReason() {
        IllegalStateException ex = failure(() -> DataRow.of("created_at", "not-a-date")
                .toEntity(DateRow.class, field -> "created_at", null));
        assertContext(ex, DateRow.class, "created_at", "createdAt", String.class.getName(), Date.class.getName());
        assertNotNull(ex.getCause());
        assertTrue(ex.getMessage().contains(ex.getCause().getMessage()));
    }

    @Test
    public void nullAssignedToPrimitiveShowsNullAndPrimitiveTarget() {
        IllegalStateException ex = failure(() -> DataRow.of("count", null).toEntity(CountRow.class));
        assertContext(ex, CountRow.class, "count", "count", "source type: null", "target type: int");
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void customValueAdaptorFailureKeepsTheOriginalCause() {
        RuntimeException original = new IllegalArgumentException("custom conversion rejected");
        IllegalStateException ex = failure(() -> ValueUtils.mapToEntity(DataRow.of("id", 42), UuidBase.class,
                field -> "id", (field, value) -> { throw original; }));
        assertContext(ex, UuidBase.class, "id", "id", Integer.class.getName(), UUID.class.getName());
        assertSame(original, ex.getCause());
    }

    @Test
    public void incompatibleCustomAdaptorResultIdentifiesTheSetter() {
        IllegalStateException ex = failure(() -> ValueUtils.mapToEntity(DataRow.of("id", "valid"), UuidBase.class,
                field -> "id", (field, value) -> 42));
        assertContext(ex, UuidBase.class, "id", "id", String.class.getName(), UUID.class.getName());
        assertTrue(ex.getMessage().contains("setId"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void setterExceptionIsUnwrappedAndKeepsItsIdentity() {
        IllegalStateException ex = failure(() -> DataRow.of("name", "value").toEntity(RejectingRow.class));
        assertContext(ex, RejectingRow.class, "name", "name", String.class.getName(), String.class.getName());
        assertSame(RejectingRow.REJECTION, ex.getCause());
        assertTrue(ex.getMessage().contains("setter rejected"));
    }

    @Test
    public void validNullAndUnmappedValuesKeepExistingBehavior() {
        assertNull(ValueUtils.mapToEntity(null, CountRow.class));
        assertEquals(0, DataRow.of("unknown", "unused").toEntity(CountRow.class).getCount());
        assertEquals(7, DataRow.of("count", "7").toEntity(CountRow.class).getCount());
        assertNull(DataRow.of("id", null).toEntity(UuidBase.class).getId());
    }

    @Test
    public void entityToMapGetterFailureIdentifiesThePropertyAndMappedColumn() {
        IllegalStateException ex = failure(() -> DataRow.ofEntity(new RejectingGetter(), field -> "display_name"));
        assertTrue(ex.getMessage().contains(RejectingGetter.class.getName()));
        assertTrue(ex.getMessage().contains("property 'name'"));
        assertTrue(ex.getMessage().contains("column 'display_name'"));
        assertTrue(ex.getMessage().contains("getName"));
        assertSame(RejectingGetter.REJECTION, ex.getCause());
    }

    private IllegalStateException failure(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected mapping failure");
        } catch (IllegalStateException ex) {
            return ex;
        }
    }

    private void assertContext(IllegalStateException ex, Class<?> entity, String column,
                               String property, String sourceType, String targetType) {
        for (String expected : new String[]{entity.getName(), "column '" + column + "'",
                "property '" + property + "'", sourceType, targetType}) {
            assertTrue("Missing " + expected + " in " + ex.getMessage(), ex.getMessage().contains(expected));
        }
    }

    public static class UuidBase {
        private UUID id;
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
    }

    public static class UuidChild extends UuidBase { }

    public static class DateRow {
        private Date createdAt;
        public Date getCreatedAt() { return createdAt; }
        public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    }

    public static class CountRow {
        private int count;
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class RejectingRow {
        static final RuntimeException REJECTION = new IllegalArgumentException("setter rejected");
        private String name;
        public String getName() { return name; }
        public void setName(String name) { throw REJECTION; }
    }

    public static class RejectingGetter {
        static final RuntimeException REJECTION = new IllegalArgumentException("getter rejected");
        private String name;
        public String getName() { throw REJECTION; }
        public void setName(String name) { this.name = name; }
    }
}
