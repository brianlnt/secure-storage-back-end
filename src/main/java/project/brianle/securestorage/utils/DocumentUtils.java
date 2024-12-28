package project.brianle.securestorage.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import project.brianle.securestorage.dto.response.DocumentResponse;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.entity.DocumentEntity;

public class DocumentUtils {

    public static DocumentResponse fromDocumentEntity(DocumentEntity documentEntity, UserResponse createdBy, UserResponse updatedBy) {
        var documentResponse = new DocumentResponse();
        BeanUtils.copyProperties(documentEntity, documentResponse);
        documentResponse.setOwnerName(createdBy.getFirstName() + " " + createdBy.getLastName());
        documentResponse.setOwnerEmail(createdBy.getEmail());
        documentResponse.setOwnerPhone(createdBy.getPhone());
        documentResponse.setOwnerLastLogin(createdBy.getLastLogin());
        documentResponse.setUpdaterName(updatedBy.getFirstName() + " " + updatedBy.getLastName());
        return documentResponse;
    }

    public static String getDocumentUri(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(String.format("/documents/%s", filename)).toUriString();
    }

    public static String setIcon(String fileExtension){
        String extension = StringUtils.trimAllWhitespace(fileExtension);
        if(extension.equalsIgnoreCase("doc") || extension.equalsIgnoreCase("docx")){
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/word-icon.svg";
        }
        if(extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx")){
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/excel-icon.svg";
        }
        if(extension.equalsIgnoreCase("pdf")){
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/pdf-icon.svg";
        } else {
            return "https://htmlstream.com/preview/front-dashboard-v2.1.1/assets/svg/brands/word-icon.svg";
        }
    }
}
