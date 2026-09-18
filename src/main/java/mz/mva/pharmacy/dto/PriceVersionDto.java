package mz.mva.pharmacy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import mz.mva.pharmacy.domain.MedicationPriceVersion;

public record PriceVersionDto(UUID id, BigDecimal price, LocalDate effectiveFrom, LocalDate effectiveTo, String priceListCode) {

    public static PriceVersionDto from(MedicationPriceVersion version) {
        return new PriceVersionDto(
                version.getId(), version.getPrice(), version.getEffectiveFrom(), version.getEffectiveTo(),
                version.getPriceListCode());
    }
}
