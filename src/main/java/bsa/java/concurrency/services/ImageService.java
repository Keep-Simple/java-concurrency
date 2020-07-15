package bsa.java.concurrency.services;

import bsa.java.concurrency.dto.IncomingImageDto;
import bsa.java.concurrency.dto.SearchResultDTO;
import bsa.java.concurrency.entity.Image;
import bsa.java.concurrency.repository.ImageHashRepository;
import bsa.java.concurrency.services.fs.FileSystem;
import bsa.java.concurrency.services.hasher.DHasher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageService {
    private final ImageHashRepository repository;
    private final FileSystem fs;
    private final DHasher hasher;

    public ImageService(ImageHashRepository repository, FileSystem fs, DHasher hasher) {
        this.repository = repository;
        this.fs = fs;
        this.hasher = hasher;
    }

    public List<SearchResultDTO> searchMatches(IncomingImageDto dto, double accuracy) {
        long imgHash = hasher.calculateHash(dto.getImg());

        List<SearchResultDTO> result = repository.getAllMatches(imgHash, accuracy);

        if (result.isEmpty()) {
            fs.saveFile(dto)
                    .thenAccept(
                            p -> repository.save(Image.builder()
                                    .hash(imgHash)
                                    .path(p)
                                    .build()
                            )
                    );
        }
        return result;
    }

    public CompletableFuture<Void> upload(List<IncomingImageDto> files) {
        return CompletableFuture.allOf(files
                .parallelStream()
                .map(this::processImage)
                .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> processImage(IncomingImageDto dto) {
        var futurePath = fs.saveFile(dto);
        var hash = hasher.calculateHash(dto.getImg());

        return futurePath
                .thenApply(path -> Image.builder().hash(hash).path(path).build())
                .thenAccept(repository::save);
    }

    public void deleteImage(UUID id) throws IOException {

        var entity = repository.findById(id);

        if (entity.isPresent()) {
            repository.deleteById(id);
            fs.deleteOne(entity.get().getPath());
        }
    }

    public void fullWipe() throws IOException {
        repository.deleteAll();
        fs.deleteAll();
    }

//    public CompletableFuture<Long> getHash(byte[] img) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                return hasher.calculateHash(img);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }, executor);
//    }
}
