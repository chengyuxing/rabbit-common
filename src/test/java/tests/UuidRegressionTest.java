package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.util.ValueUtils;
import org.junit.Test;

import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;

public class UuidRegressionTest {
    @Test
    public void standardAndCompactStringsMapToTheSameUuid() {
        for (String text : new String[]{"550e8400-e29b-41d4-a716-446655440000",
                "00000000-0000-0000-0000-000000000000", "ffffffff-ffff-ffff-ffff-ffffffffffff"}) {
            UUID expected = UUID.fromString(text);
            for (String input : new String[]{text, text.replace("-", ""),
                    text.toUpperCase(Locale.ROOT), text.replace("-", "").toUpperCase(Locale.ROOT)}) {
                assertEquals(expected, ValueUtils.adaptValue(UUID.class, input));
                assertEquals(expected, DataRow.of("id", input).toEntity(UuidRow.class).getId());
            }
        }
    }

    @Test
    public void nativeUuidAndNullValuesArePreserved() {
        UUID uuid = UUID.randomUUID();
        assertSame(uuid, ValueUtils.adaptValue(UUID.class, uuid));
        assertSame(uuid, DataRow.of("id", uuid).toEntity(UuidRow.class).getId());
        assertNull(ValueUtils.adaptValue(UUID.class, null));
        assertNull(DataRow.of("id", null).toEntity(UuidRow.class).getId());
    }

    @Test
    public void invalidAndAbbreviatedUuidStringsAreRejected() {
        for (String input : new String[]{"", "1-1-1-1-1", "550e8400-e29b-41d4-a716-44665544000",
                "550e8400-e29b-41d4-a716-4466554400000", "550e8400e29b41d4a7164466554400000",
                "550e8400e29b41d4a716446655440000000", "550e8400e29b41d4a71644665544000g",
                "550e8400-e29b-41d4-a716-44665544000g", "550e84000-e29b-41d4-a716-446655440000",
                " 550e8400-e29b-41d4-a716-446655440000", "550e8400-e29b-41d4-a716-446655440000 "}) {
            try {
                ValueUtils.adaptValue(UUID.class, input);
                fail("Accepted invalid UUID: " + input);
            } catch (IllegalArgumentException expected) {
                // Invalid text must never be silently normalized to a different UUID.
            }
        }
    }

    public static class UuidRow {
        private UUID id;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }
    }
}
