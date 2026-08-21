package de.lisa.backend.common.datetimeperiod;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Abstrakte Basisklasse für Entitäten, die einen Zeitraum mit Start- und
 * Endzeitpunkt repräsentieren.
 * Enthält die Felder {@code startAt} und {@code endAt} sowie eine Methode
 * {@code isActive} zur Überprüfung, ob die Entität zum aktuellen Zeitpunkt
 * aktiv ist.
 */
@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class DateTimePeriod {

    @NotNull(message = "Start timestamp must not be null")
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt = OffsetDateTime.now();

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    /**
     * Überprüft, ob der Zeitraum aktuell aktiv ist (d.h. das aktuelle Datum liegt
     * zwischen startAt und endAt). Gleicher Start- und Endzeitpunkt sind immer
     * nicht aktiv.
     * <p>
     * Der Start wird inklusive betrachtet, während das Ende exklusive betrachtet
     * wird.
     *
     * @return true, wenn der Zeitraum aktiv ist, sonst false
     */
    public boolean isActive() {
        OffsetDateTime now = OffsetDateTime.now();

        boolean isWithinRange = (startAt.isBefore(now) || startAt.isEqual(now))
                && (endAt == null || endAt.isAfter(now));
        boolean isNotInstantaneous = endAt == null || !startAt.isEqual(endAt);

        return isWithinRange && isNotInstantaneous;
    }

    @AssertTrue(message = "endAt must be after or equal to startAt")
    private boolean isChronological() {
        if (startAt == null || endAt == null) {
            return true; // Überlässt die Prüfung der @NotNull Annotation
        }
        return !endAt.isBefore(startAt);
    }

}
