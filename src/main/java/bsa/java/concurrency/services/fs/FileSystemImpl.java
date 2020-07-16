package bsa.java.concurrency.services.fs;

import bsa.java.concurrency.dto.IncomingImageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileSystemImpl implements FileSystem {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Path savePath = Paths.get('.' + File.separator + "images");

    public CompletableFuture<String> saveFile(IncomingImageDto dto) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return save(dto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    private String save(IncomingImageDto dto) throws IOException {
        var imagePath = savePath.resolve(
                UUID.randomUUID().toString() + '.' + dto.getImgExtension()
        );

        if (!Files.exists(savePath)) {
            Files.createDirectories(savePath);
        }

        try (var outputStream = new BufferedOutputStream(Files.newOutputStream(imagePath))) {
            outputStream.write(dto.getImg());
            outputStream.flush();
        }

        return imagePath.getFileName().toString();
    }

    public void deleteAll() throws IOException {
        Files.walkFileTree(savePath, new SimpleFileVisitor<>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void deleteOne(String fileName) throws IOException {
        Files.deleteIfExists(savePath.resolve(fileName));
    }
}
