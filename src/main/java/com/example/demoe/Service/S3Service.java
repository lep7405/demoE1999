package com.example.demoe.Service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class S3Service {
    private static final String bucketName = "alle2001";
    private static final Region region = Region.US_EAST_1;
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    public String uploadToS3(MultipartFile multipartFile, String filename) throws IOException {
        try (S3Client s3Client = S3Client.builder().region(region).build()) {
            String filename1 = filename+ multipartFile.getOriginalFilename();
            InputStream fileInputStream = multipartFile.getInputStream();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename1)
                    .build();
            // Upload đối tượng lên S3
            PutObjectResponse response = s3Client.putObject(request, RequestBody.fromInputStream(fileInputStream, fileInputStream.available()));

            return filename1;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getPresignedUrl(String filename) {
        String bucketName = "alle2001";
//        String objectKey = "6.webp";
        String accessKey = "AKIAWLBNLEWGDRMEMV7K";
        String secretKey = "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";
        Region region = Region.US_EAST_1;
        S3Presigner presigner = S3Presigner.create();
        // Thời gian tồn tại của URL, ví dụ 1 phút
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(1000))  // The URL will expire in 10 minutes.
                .getObjectRequest(objectRequest)
                .build();



        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);


        return presignedRequest.url().toString();

    }

    private static ByteBuffer getRandomByteBuffer(int size) throws IOException {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }
    public List<String> addtoS3(MultipartFile[] multipartFiles,String text) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        try ( S3Client s3Client = S3Client.builder()
                .region(region)
                .build();) {

            for (MultipartFile multipartFile : multipartFiles) {
                String filename = text+"/"+ multipartFile.getOriginalFilename();
                InputStream fileInputStream = multipartFile.getInputStream();

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename)
                        .build();
                PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, fileInputStream.available()));
//                String url=getPresignedUrl(filename);
                imageUrls.add(filename);
            }
            return imageUrls;
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }
    public void dele(String filename){
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        s3.deleteObject(deleteObjectRequest);
    }
}

