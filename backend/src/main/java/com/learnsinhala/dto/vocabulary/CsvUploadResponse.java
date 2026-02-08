package com.learnsinhala.dto.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for CSV vocabulary upload operation.
 * Contains summary of upload results and detailed error information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvUploadResponse {

    /**
     * Number of vocabulary items successfully imported
     */
    private int successCount;

    /**
     * Number of rows that failed validation or import
     */
    private int errorCount;

    /**
     * Total number of rows processed (excluding header)
     */
    private int totalRows;

    /**
     * List of IDs of successfully created vocabulary items
     */
    @Builder.Default
    private List<String> createdIds = new ArrayList<>();

    /**
     * Detailed error information for failed rows
     */
    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    /**
     * Individual row error details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        /**
         * Row number in CSV (1-indexed, excluding header)
         */
        private int rowNumber;

        /**
         * Field name that caused the error (null if general row error)
         */
        private String field;

        /**
         * Error message
         */
        private String message;

        /**
         * Raw row data that failed (optional, for debugging)
         */
        private String rawData;
    }
}
