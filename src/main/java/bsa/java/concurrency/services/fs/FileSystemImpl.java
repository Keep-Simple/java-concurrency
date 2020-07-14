package bsa.java.concurrency.services.fs;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileSystemImpl implements FileSystem {

    private final ExecutorService executor = Executors.newFixedThreadPool(6);

    private final Path savePath = Paths.get(".\\images");

    public CompletableFuture<String> saveFile(byte[] file) {
        return CompletableFuture.supplyAsync(()-> {
            try {
                return save(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    private String save(byte[] file) throws Exception{
            var imagePath = savePath.resolve(UUID.randomUUID().toString() + ".jpg");

            if (!Files.exists(savePath)) {
                Files.createDirectories(savePath);
            }

            var outputStream = new BufferedOutputStream(Files.newOutputStream(imagePath));

            ImageIO.write(byteArrayToImage(file), "jpg", outputStream);

            return imagePath.toUri().toURL().toString();
    }

    private static RenderedImage byteArrayToImage(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {

            return ImageIO.read(in);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAll() throws IOException {
        Files.walkFileTree(savePath, new SimpleFileVisitor<>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void deleteOne(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path.substring(6)));
    }
}
