package project.brianle.securestorage.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateDocumentRequest {
    @NotEmpty(message = "Document Id cannot be empty or null")
    private String documentId;
    @NotEmpty(message = "Name cannot be empty or null")
    private String name;
    @NotEmpty(message = "Description cannot be empty or null")
    private String description;
}
