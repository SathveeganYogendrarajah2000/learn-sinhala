package com.learnsinhala.dto.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyListResponse {

    private List<VocabularyDto> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;

    public static VocabularyListResponse of(List<VocabularyDto> items, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return VocabularyListResponse.builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(total)
                .totalPages(totalPages)
                .build();
    }
}
