package bsa.java.concurrency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomingImageDto {

    private byte[] img;

    private String imgExtension;
}
