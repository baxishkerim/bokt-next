package az.bokt.fileimport.web;

import az.bokt.fileimport.dto.ImportResultResponse;
import az.bokt.fileimport.service.FileImportService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/credit-files")
public class FileImportController {

    private final FileImportService fileImportService;

    public FileImportController(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    /** Загрузка CSV с кредитами (pin;cardPan;amount;currencyId;description). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('FILE_IMPORT')")
    public ImportResultResponse upload(@RequestParam("file") MultipartFile file) {
        return ImportResultResponse.from(fileImportService.importCredits(file));
    }
}
