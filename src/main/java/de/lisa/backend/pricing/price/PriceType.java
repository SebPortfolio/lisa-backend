package de.lisa.backend.pricing.price;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PriceType {
    STANDARD(0, false),
    OFFER(10, false),
    APP_OFFER(20, false),
    APP_COUPON(30, true);

    private final int priority;
    private final boolean singleUsage;
}
