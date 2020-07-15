package bsa.java.concurrency.repository;

import bsa.java.concurrency.dto.SearchResultDTO;
import bsa.java.concurrency.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ImageHashRepository extends JpaRepository<Image, UUID> {

    @Query(nativeQuery = true,
            value = "select cast(id as varchar), " +
                    "path, " +
                    "match_percent(:imgHash, hash) * 100 as percent " +
                    "from images " +
                    "where match_percent(:imgHash, hash) >= :accuracy " +
                    "order by percent desc")
    List<SearchResultDTO> getAllMatches(long imgHash, double accuracy);
}
