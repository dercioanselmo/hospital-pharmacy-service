package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/** {@code priceListCode} defaults to "STANDARD" when null/blank — most catalogs only ever use the one list. */
public record CreatePriceVersionRequest(
        @NotNull @Positive BigDecimal price, @NotNull LocalDate effectiveFrom, String priceListCode) {

    public String resolvedPriceListCode() {
        return priceListCode == null || priceListCode.isBlank() ? "STANDARD" : priceListCode;
    }
}
