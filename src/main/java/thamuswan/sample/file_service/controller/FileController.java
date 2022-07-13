package thamuswan.sample.file_service.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import thamuswan.sample.file_service.model.FileContent;
import thamuswan.sample.file_service.model.GenerateFileRequest;

@Slf4j
@RestController
@RequestMapping("file-service")
public class FileController {

  private Map<String, FileContent> generatedFileContents = new ConcurrentHashMap<>();

  @PostMapping("generate-file")
  public HttpEntity<String> generateFile(@RequestBody GenerateFileRequest request) {

    try (//
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(os); //
    ) {

      String uuid = UUID.randomUUID().toString();
      new Thread(() -> {
        try {
          Thread.sleep(20000); // Dummy process
          FileContent fileContent = FileContent.builder().fileName(request.getFileName())
              .content(os.toByteArray()).build();
          generatedFileContents.put(uuid, fileContent);
        } catch (InterruptedException e) {
          log.error(e.getMessage(), e);
        }
      }).start();

      for (String message : request.getContent()) {
        pw.println(message);
      }
      pw.flush();


      return ResponseEntity.ok().body(uuid);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }

  }

  @SneakyThrows
  @GetMapping("download/{fileId}")
  public HttpEntity<?> downloadFile(@PathVariable String fileId) {
    if (generatedFileContents.containsKey(fileId)) {
      FileContent generatedFileContent = generatedFileContents.get(fileId);
      String baseFileName = FilenameUtils.getBaseName(generatedFileContent.getFileName());

      HttpHeaders headers = new HttpHeaders();
      headers.setContentDisposition(ContentDisposition.attachment()
          .filename(baseFileName + ".txt", StandardCharsets.UTF_8).build());
      headers.setContentType(new MediaType("text", "plain"));
      headers.setCacheControl(CacheControl.noStore());
      headers.setExpires(Instant.now());

      ByteArrayResource resource = new ByteArrayResource(generatedFileContent.getContent());

      generatedFileContents.remove(fileId);
      return ResponseEntity.ok().headers(headers).body(resource);
    } else {
      return ResponseEntity.noContent().build();
    }
  }

}
