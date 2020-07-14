package bsa.java.concurrency.mapper;

import bsa.java.concurrency.dto.IncomingImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class Mapper {

    public static IncomingImageDto reqToDto(MultipartFile img) {
        var name = img.getOriginalFilename();
        try {
            assert name != null;
            return new IncomingImageDto(
                    img.getBytes(),
                    name.substring(name.lastIndexOf('.') + 1)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
