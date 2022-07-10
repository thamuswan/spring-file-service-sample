package thamuswan.sample.file_service.model;

import lombok.Data;

@Data
public class GenerateFileRequest {

	private String fileName;

	private String[] content;

}
