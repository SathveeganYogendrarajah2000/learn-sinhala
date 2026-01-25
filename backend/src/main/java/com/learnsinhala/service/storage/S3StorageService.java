package com.learnsinhala.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * AWS S3 storage implementation (placeholder for future).
 *
 * To enable:
 * 1. Add AWS SDK dependency to pom.xml:
 *    <dependency>
 *        <groupId>software.amazon.awssdk</groupId>
 *        <artifactId>s3</artifactId>
 *    </dependency>
 *
 * 2. Configure in application.yml:
 *    app:
 *      storage:
 *        type: s3
 *        s3:
 *          bucket: your-bucket
 *          region: us-east-1
 *
 * 3. Uncomment @Service and implement methods.
 *
 * 4. Use @ConditionalOnProperty to switch implementations:
 *    @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
 */
// @Service
// @ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    // @Value("${app.storage.s3.bucket}")
    // private String bucket;

    // private S3Client s3Client;

    @Override
    public String store(MultipartFile file, String category, String filename) {
        // TODO: Implement S3 upload
        // String key = category + "/" + filename;
        // s3Client.putObject(PutObjectRequest.builder()
        //     .bucket(bucket)
        //     .key(key)
        //     .build(),
        //     RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        // return key;
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public String store(byte[] bytes, String category, String filename) {
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public Resource load(String path) {
        // TODO: Return S3 resource or redirect to S3 URL
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public boolean delete(String path) {
        // TODO: Delete from S3
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public boolean exists(String path) {
        // TODO: Check S3 object exists
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public String getPublicUrl(String path) {
        // TODO: Return CloudFront URL or presigned S3 URL
        // return "https://cdn.example.com/" + path;
        // Or: return s3Client.utilities().getUrl(GetUrlRequest.builder()
        //     .bucket(bucket).key(path).build()).toString();
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }
}
