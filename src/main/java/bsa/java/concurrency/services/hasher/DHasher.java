package bsa.java.concurrency.services.hasher;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Service
public class DHasher {

    private static BufferedImage preprocessImage(BufferedImage image) {
        var result = image.getScaledInstance(9, 9, Image.SCALE_SMOOTH);
        var output = new BufferedImage(9, 9, BufferedImage.TYPE_BYTE_GRAY);
        output.getGraphics().drawImage(result, 0, 0, null);

        return output;
    }

    private static int brightnessScore(int rgb) {
        return rgb & 0b11111111;
    }

    public static long calculateDHash(BufferedImage processedImage) {
        long hash = 0;
        for (var row = 1; row < 9; row++) {
            for (var col = 1; col < 9; col++) {
                var prev = brightnessScore(processedImage.getRGB(col - 1, row - 1));
                var current = brightnessScore(processedImage.getRGB(col, row));
                hash |= current > prev ? 1 : 0;
                hash <<= 1;
            }
        }

        return hash;
    }

    public long calculateHash(byte[] image) throws Exception {
        var img = ImageIO.read(new ByteArrayInputStream(image));
        return calculateDHash(preprocessImage(img));
    }
}

