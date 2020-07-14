package bsa.java.concurrency.fs;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class FileSystemImpl implements FileSystem {

    private final Path savePath = Paths.get(".\\images");

    public CompletableFuture<String> saveFile(byte[] file) throws IOException {
        var imagePath = savePath.resolve(UUID.randomUUID().toString() + ".jpg");

        if (!Files.exists(savePath)) {
            Files.createDirectories(savePath);
        }

        var outputStream = new BufferedOutputStream(Files.newOutputStream(imagePath));

        ImageIO.write(byteArrayToImage(file), "jpg", outputStream);

        return CompletableFuture.completedFuture(imagePath.toUri().toURL().toString());
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
