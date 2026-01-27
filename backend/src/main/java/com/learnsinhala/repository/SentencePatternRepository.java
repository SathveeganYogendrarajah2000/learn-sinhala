package com.learnsinhala.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.model.SentencePattern;

import java.util.List;

@Repository
public interface SentencePatternRepository extends MongoRepository<SentencePattern, String> {

    List<SentencePattern> findByCategory(Category category);

    List<SentencePattern> findByDifficulty(Difficulty difficulty);

    List<SentencePattern> findByCategoryAndDifficulty(Category category, Difficulty difficulty);

    Page<SentencePattern> findByCategory(Category category, Pageable pageable);

    Page<SentencePattern> findByDifficulty(Difficulty difficulty, Pageable pageable);

    Page<SentencePattern> findByCategoryAndDifficulty(Category category, Difficulty difficulty, Pageable pageable);

    long countByCategory(Category category);

    long countByDifficulty(Difficulty difficulty);
}
