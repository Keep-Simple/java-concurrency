package bsa.java.concurrency.services.fs;

import bsa.java.concurrency.dto.IncomingImageDto;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

// Если вам интересно, почему файловая система предоставляет асинхронный интерфейс, хотя зачастую мы используем блокирующие вызовы к файловой системе, то ответ весьма прост:
// этот интерфейс расчитан на то, что в будущем мы будем использовать CDN для хранения файлов и использовать асинхронный API для выполнения HTTP запросов при работе с ним.
// При работе с файловой системой вы можете использовать блокирующие вызовы, просто оберните результат в CompletableFuture
public interface FileSystem {
    CompletableFuture<String> saveFile(IncomingImageDto dto);

    void deleteAll() throws IOException;

    void deleteOne(String id) throws IOException;
}
