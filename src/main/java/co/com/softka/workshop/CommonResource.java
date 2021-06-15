package co.com.softka.workshop;

import co.com.softka.workshop.data.FormData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;



public abstract class CommonResource {

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    protected PutObjectRequest buildPutRequest(FormData formData) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(formData.getFileName())
                .contentType(formData.getMimeType())
                .build();
    }


    protected File uploadToTemp(String toWrite) {
        File tempPath;
        try {
            tempPath = File.createTempFile("uploadS3Tmp", ".tmp");
            FileWriter writer = new FileWriter(tempPath);
            writer.write(toWrite);
            writer.close();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
        return tempPath;
    }
}