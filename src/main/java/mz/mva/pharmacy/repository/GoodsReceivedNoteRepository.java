package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.GoodsReceivedNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, UUID> {

    List<GoodsReceivedNote> findAllByOrderByCreatedAtDesc();
}
