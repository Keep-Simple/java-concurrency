package bsa.java.concurrency.services.hasher;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class DHasher {

    public long calculateHash(byte[] image) {
        BufferedImage img;

        try {
            img = ImageIO.read(new ByteArrayInputStream(image));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return calculateDHash(preprocessImage(img));
    }

    private static long calculateDHash(BufferedImage processedImage) {
        long hash = 0;

        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                int prev = brightnessScore(processedImage.getRGB(col - 1, row - 1));
                int current = brightnessScore(processedImage.getRGB(col, row));
                hash |= current > prev ? 1 : 0;
                hash <<= 1;
            }
        }

        return hash;
    }

    private static int brightnessScore(int rgb) {
        return rgb & 0b11111111;
    }

    private static BufferedImage preprocessImage(BufferedImage image) {
        var result = image.getScaledInstance(9, 9, Image.SCALE_SMOOTH);
        var output = new BufferedImage(9, 9, BufferedImage.TYPE_BYTE_GRAY);
        output.getGraphics().drawImage(result, 0, 0, null);

        return output;
    }

}

