package bsa.java.concurrency.services.fs;

import bsa.java.concurrency.dto.IncomingImageDto;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
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

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

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

    private String save(IncomingImageDto dto) throws Exception {
        var imagePath = savePath.resolve(
                UUID.randomUUID().toString() + '.' + dto.getImgExtension()
        );

        if (!Files.exists(savePath)) {
            Files.createDirectories(savePath);
        }

        var outputStream = new BufferedOutputStream(Files.newOutputStream(imagePath));

        ImageIO.write(byteArrayToImage(dto.getImg()), dto.getImgExtension(), outputStream);

        return imagePath.toUri().toURL().toString();
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

    private static RenderedImage byteArrayToImage(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
