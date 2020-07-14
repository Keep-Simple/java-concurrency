package bsa.java.concurrency.image;

import bsa.java.concurrency.image.dto.SearchResultDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ImageHashRepository extends JpaRepository<Image, UUID> {

    @Query(nativeQuery = true,
    value = "select Cast(id as varchar), " +
            "path, " +
            "match_percent(:imgHash, hash) as percent " +
            "from images " +
            "where match_percent(:imgHash, hash) >= :accuracy")
    List<SearchResultDTO> getAllMatches(long imgHash, double accuracy);
}
