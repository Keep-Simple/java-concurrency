package bsa.java.concurrency.image.dto;

import org.springframework.beans.factory.annotation.Value;


public interface SearchResultDTO {
    String getId();

    @Value("#{target.percent}")
    Double getMatch();

    @Value("#{target.path}")
    String getImage();
}
