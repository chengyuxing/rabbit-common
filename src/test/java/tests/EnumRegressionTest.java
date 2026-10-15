package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.util.ValueUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class EnumRegressionTest {
    public enum State {
        ACTIVE { @Override public String toString() { return "display label"; } },
        BLOCKED
    }

    @Test
    public void namesAndExistingConstantsArePreserved() {
        assertSame(State.ACTIVE, ValueUtils.adaptValue(State.class, "ACTIVE"));
        assertSame(State.ACTIVE, ValueUtils.adaptValue(State.class, State.ACTIVE));
        assertNull(ValueUtils.adaptValue(State.class, null));
        for (Object invalid : new Object[]{"active", " ACTIVE ", "display label", "1", "", Thread.State.NEW, true}) {
            assertThrows(IllegalArgumentException.class, () -> ValueUtils.adaptValue(State.class, invalid));
        }
    }

    @Test
    public void numericOrdinalsMustBeExactAndInRange() {
        for (Number value : new Number[]{(byte) 1, (short) 1, 1, 1L, 1.0, new BigDecimal("1.00"), BigInteger.ONE}) {
            assertSame(State.BLOCKED, ValueUtils.adaptValue(State.class, value));
        }
        assertSame(State.ACTIVE, ValueUtils.adaptValue(State.class, 0));
        for (Number invalid : new Number[]{-1, 2, 1.5, new BigDecimal("0.1"), Long.MAX_VALUE, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> ValueUtils.adaptValue(State.class, invalid));
        }
    }

    @Test
    public void dataRowMappingSupportsEnumFieldsAndPreservesDiagnosticContext() {
        assertSame(State.ACTIVE, DataRow.of("state", "ACTIVE").toEntity(Row.class).getState());
        assertSame(State.BLOCKED, DataRow.of("state", 1).toEntity(Row.class).getState());
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> DataRow.of("state", "UNKNOWN").toEntity(Row.class));
        assertTrue(error.getMessage().contains("state"));
        assertTrue(error.getMessage().contains(Row.class.getName()));
        assertTrue(error.getCause() instanceof IllegalArgumentException);
    }

    public static class Row {
        private State state;
        public State getState() { return state; }
        public void setState(State state) { this.state = state; }
    }
}
