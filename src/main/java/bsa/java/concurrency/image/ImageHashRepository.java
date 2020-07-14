package bsa.java.concurrency.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageHashRepository extends JpaRepository<Image, UUID> {
}
