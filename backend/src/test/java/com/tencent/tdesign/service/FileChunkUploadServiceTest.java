package com.tencent.tdesign.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.security.AuthContext;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FileChunkUploadServiceTest {
  @Test
  void shouldRejectTraversalLikeUploadId() {
    FileChunkUploadService service = new FileChunkUploadService(
      mock(ObjectStorageService.class),
      new ObjectMapper(),
      mock(AuthContext.class),
      "abcdefghijklmnopqrstuvwxyz123456",
      100
    );

    assertThrows(IllegalArgumentException.class, () -> service.getStatus("../../etc/passwd"));
  }

  @Test
  void openCombinedStreamShouldReadAllChunksSequentially() throws Exception {
    FileChunkUploadService service = new FileChunkUploadService(
      mock(ObjectStorageService.class),
      new ObjectMapper(),
      mock(AuthContext.class),
      "abcdefghijklmnopqrstuvwxyz123456",
      100
    );

    Path dir = Files.createTempDirectory("chunk-stream-test");
    Files.writeString(dir.resolve("chunk-0"), "ab", StandardCharsets.UTF_8);
    Files.writeString(dir.resolve("chunk-1"), "cd", StandardCharsets.UTF_8);
    Files.writeString(dir.resolve("chunk-2"), "ef", StandardCharsets.UTF_8);

    Method method = FileChunkUploadService.class.getDeclaredMethod("openCombinedStream", Path.class, int.class);
    method.setAccessible(true);
    try (InputStream in = (InputStream) method.invoke(service, dir, 3)) {
      String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      assertEquals("abcdef", content);
    }
  }
}
