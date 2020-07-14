package bsa.java.concurrency.image.dto;

import java.util.UUID;

public interface SearchResultDTO {
    UUID getId();
    Double getMatchPercent();
    String getPath();
}
