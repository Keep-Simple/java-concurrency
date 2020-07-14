package bsa.java.concurrency.image;

import bsa.java.concurrency.fs.FileSystem;
import bsa.java.concurrency.hasher.DHasher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

    public void upload(MultipartFile[] files) throws IOException, ExecutionException, InterruptedException {
        for (MultipartFile i : files) {
            var bytes = i.getBytes();

            String path = fs.saveFile(bytes).get();

            long hash = hasher.calculateHash(bytes);

            var entity = Image
                    .builder()
                    .hash(hash)
                    .path(path)
                    .build();

            repository.save(entity);
        }
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
}
