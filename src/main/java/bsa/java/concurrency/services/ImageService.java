package bsa.java.concurrency.services;

import bsa.java.concurrency.dto.IncomingImageDto;
import bsa.java.concurrency.dto.SearchResultDTO;
import bsa.java.concurrency.entity.Image;
import bsa.java.concurrency.repository.ImageHashRepository;
import bsa.java.concurrency.services.fs.FileSystem;
import bsa.java.concurrency.services.hasher.DHasher;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${domain.path}")
    private String domainPath;

    public ImageService(ImageHashRepository repository, FileSystem fs, DHasher hasher) {
        this.repository = repository;
        this.fs = fs;
        this.hasher = hasher;
    }

    public CompletableFuture<Void> upload(List<IncomingImageDto> files) {
        return CompletableFuture.allOf(files
                .parallelStream()
                .map(this::processImage)
                .toArray(CompletableFuture[]::new));
    }

    public List<SearchResultDTO> searchMatches(IncomingImageDto dto, double accuracy) {
        long imgHash = hasher.calculateHash(dto.getImg());

        List<SearchResultDTO> result = repository.getAllMatches(imgHash, accuracy);

        if (result.isEmpty()) {
            saveImageToFsAndDb(fs.saveFile(dto), imgHash);
        }
        return result;
    }


    private CompletableFuture<Void> processImage(IncomingImageDto dto) {
        var futureImgName = fs.saveFile(dto);
        var hash = hasher.calculateHash(dto.getImg());

        return saveImageToFsAndDb(futureImgName, hash);
    }

    private CompletableFuture<Void> saveImageToFsAndDb(CompletableFuture<String> futureImgName, long hash) {
        return futureImgName
                .thenApply(path -> Image.builder().hash(hash).path(domainPath.concat(path)).build())
                .thenAccept(repository::save);
    }

    public void deleteImage(UUID id) throws IOException {
        var entity = repository.findById(id);

        if (entity.isPresent()) {
            repository.deleteById(id);
            fs.deleteOne(entity.get().getPath().substring(domainPath.length()));
        }
    }

    public void fullWipe() throws IOException {
        repository.deleteAll();
        fs.deleteAll();
    }

}
