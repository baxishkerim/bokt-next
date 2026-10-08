package az.bokt.fileimport.dto;

import az.bokt.fileimport.domain.CreditFile;

public record ImportResultResponse(
        Long fileId,
        String fileName,
        int totalRows,
        int importedRows,
        int failedRows,
        String status,
        String errors
) {
    public static ImportResultResponse from(CreditFile f) {
        return new ImportResultResponse(f.getId(), f.getFileName(), f.getTotalRows(),
                f.getImportedRows(), f.getFailedRows(), f.getStatus().name(), f.getErrors());
    }
}
