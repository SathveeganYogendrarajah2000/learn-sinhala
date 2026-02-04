package com.learnsinhala.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.model.Vocabulary;

import java.util.List;

@Repository
public interface VocabularyRepository extends MongoRepository<Vocabulary, String> {

    List<Vocabulary> findByCategory(Category category);

    List<Vocabulary> findByDifficulty(Difficulty difficulty);

    List<Vocabulary> findByCategoryAndDifficulty(Category category, Difficulty difficulty);

    Page<Vocabulary> findByCategory(Category category, Pageable pageable);

    Page<Vocabulary> findByDifficulty(Difficulty difficulty, Pageable pageable);

    Page<Vocabulary> findByCategoryAndDifficulty(Category category, Difficulty difficulty, Pageable pageable);

    List<Vocabulary> findByTagsContaining(String tag);

    long countByCategory(Category category);

    long countByDifficulty(Difficulty difficulty);

    /**
     * Check if vocabulary with given sinhala word exists.
     * Used for duplicate detection during CSV import.
     */
    boolean existsBySinhala(String sinhala);
}
