package mz.mva.pharmacy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationForm;
import mz.mva.pharmacy.domain.MedicationPriceVersion;
import mz.mva.pharmacy.dto.CreatePriceVersionRequest;
import mz.mva.pharmacy.dto.PriceVersionDto;
import mz.mva.pharmacy.repository.MedicationPriceVersionRepository;
import mz.mva.pharmacy.repository.MedicationRepository;
import org.junit.jupiter.api.Test;

class MedicationServiceTest {

    private final MedicationRepository medicationRepository = mock(MedicationRepository.class);
    private final MedicationPriceVersionRepository priceVersionRepository = mock(MedicationPriceVersionRepository.class);
    private final MedicationService medicationService = new MedicationService(medicationRepository, priceVersionRepository);

    @Test
    void resolveCurrentPriceReturnsNullWhenNoVersionsConfigured() {
        UUID id = UUID.randomUUID();
        when(priceVersionRepository.findByMedicationIdAndPriceListCodeOrderByEffectiveFromDesc(id, "STANDARD"))
                .thenReturn(List.of());

        assertThat(medicationService.resolveCurrentPrice(id, LocalDate.now())).isNull();
    }

    @Test
    void addPriceVersionClosesThePreviousOpenVersionAndRejectsBackdating() {
        UUID id = UUID.randomUUID();
        Medication medication = new Medication(id, "PARA-500", "Paracetamol", MedicationForm.TABLET, "500mg", 100);
        MedicationPriceVersion existingOpen = new MedicationPriceVersion(
                UUID.randomUUID(), medication, BigDecimal.valueOf(5), LocalDate.of(2026, 1, 1), "STANDARD");
        when(medicationRepository.findById(id)).thenReturn(Optional.of(medication));
        when(priceVersionRepository.findByMedicationIdAndPriceListCodeOrderByEffectiveFromDesc(id, "STANDARD"))
                .thenReturn(List.of(existingOpen));
        when(priceVersionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PriceVersionDto created = medicationService.addPriceVersion(
                id, new CreatePriceVersionRequest(BigDecimal.valueOf(6), LocalDate.of(2026, 6, 1), null));

        assertThat(existingOpen.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(created.price()).isEqualByComparingTo(BigDecimal.valueOf(6));
        assertThat(created.effectiveTo()).isNull();

        assertThatThrownBy(() -> medicationService.addPriceVersion(
                        id, new CreatePriceVersionRequest(BigDecimal.valueOf(4), LocalDate.of(2025, 1, 1), null)))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
