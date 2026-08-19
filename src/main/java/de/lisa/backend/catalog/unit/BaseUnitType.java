package de.lisa.backend.catalog.unit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BaseUnitType {
    KILOGRAM("kg", true),
    LITRE("l", false),
    METER("m", false),
    PIECE("", false),
    SHEETS("", false);

    private final String abbreviation;
    private final boolean weightBased; // kontinuierlich messbar wie Bananen oder nur in festen abgepackten Mengen?
}
