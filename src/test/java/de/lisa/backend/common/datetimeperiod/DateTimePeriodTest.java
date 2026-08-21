package de.lisa.backend.common.datetimeperiod;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateTimePeriodTest {
    private static class TestPeriod extends DateTimePeriod {
    }

    // TODO: allgemeiner Clock-Service
    /*
     * private static final OffsetDateTime DEFAULT_START = OffsetDateTime.of(2026,
     * 5, 1, 0, 0, 0, 0,
     * OffsetDateTime.now().getOffset());
     * private static final OffsetDateTime DEFAULT_END = OffsetDateTime.of(2026, 5,
     * 31, 0, 0, 0, 0,
     * OffsetDateTime.now().getOffset());
     */

    @Test
    @DisplayName("Should be active when now is within the period")
    void isActive_withinInterval_returnsTrue() {
        TestPeriod period = new TestPeriod();
        OffsetDateTime now = OffsetDateTime.now();
        period.setStartAt(now.minusDays(1));
        period.setEndAt(now.plusDays(1));

        assertThat(period.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should be active when now is equal to start date")
    void isActive_atExactStart_returnsTrue() {
        TestPeriod period = new TestPeriod();
        OffsetDateTime now = OffsetDateTime.now();
        period.setStartAt(now);
        period.setEndAt(now.plusDays(1));

        assertThat(period.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should not be active when now is equal to end date")
    void isActive_atExactEnd_returnsFalse() {
        TestPeriod period = new TestPeriod();
        OffsetDateTime now = OffsetDateTime.now();
        period.setStartAt(now.minusDays(1));
        period.setEndAt(now);

        assertThat(period.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should be active when no end date is set")
    void isActive_openEnded_returnsTrue() {
        TestPeriod period = new TestPeriod();
        period.setStartAt(OffsetDateTime.now().minusDays(1));
        period.setEndAt(null);

        assertThat(period.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should not be active when no end date is equal to start date")
    void isActive_emptyInterval_returnsFalse() {
        TestPeriod period = new TestPeriod();
        period.setStartAt(OffsetDateTime.now().minusDays(1));
        period.setEndAt(OffsetDateTime.now().minusDays(1));

        assertThat(period.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should not be active when period is in the future")
    void isActive_futureStart_returnsFalse() {
        TestPeriod period = new TestPeriod();
        period.setStartAt(OffsetDateTime.now().plusDays(1));

        assertThat(period.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should not be active when period is in the past")
    void isActive_pastEnd_returnsFalse() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.minusDays(10);
        OffsetDateTime end = now.minusDays(1);

        TestPeriod period = new TestPeriod();
        period.setStartAt(start);
        period.setEndAt(end);

        assertThat(period.isActive()).isFalse();
    }
}
