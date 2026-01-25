package com.learnsinhala.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.learnsinhala.repository")
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB auto-configuration handles connection via application.yml
    // Auditing enabled for @CreatedDate and @LastModifiedDate
}
