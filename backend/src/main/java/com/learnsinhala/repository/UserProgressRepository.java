package com.learnsinhala.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.learnsinhala.model.LearningStatus;
import com.learnsinhala.model.UserProgress;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends MongoRepository<UserProgress, String> {

    Optional<UserProgress> findByUserIdAndVocabularyId(String userId, String vocabularyId);

    List<UserProgress> findByUserId(String userId);

    List<UserProgress> findByUserIdAndStatus(String userId, LearningStatus status);

    List<UserProgress> findByUserIdAndNextReviewAtBefore(String userId, Instant time);

    long countByUserIdAndStatus(String userId, LearningStatus status);

    List<UserProgress> findByUserIdAndVocabularyIdIn(String userId, List<String> vocabularyIds);
}
