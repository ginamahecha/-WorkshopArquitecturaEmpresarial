package co.com.softka.workshop;

import co.com.softka.workshop.data.FormData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;


public abstract class CommonResource {

    public static final  String DOCUMENT_KEY_COL = "documentKey";
    public static final  String DOCUMENT_STATUS_COL = "documentStatus";
    public static final  String DOCUMENT_URL_COL = "documentURL";

    @ConfigProperty(name = "bucket.name")
    private String bucketName;

    @ConfigProperty(name = "tabla.name")
    private String tablaName;

    protected PutObjectRequest buildPutRequest(FormData formData) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(formData.getId())
                .contentType(formData.getMimeType())
                .build();
    }


    protected PutItemRequest putRequest(FormData formData) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(DOCUMENT_KEY_COL, AttributeValue.builder().s(formData.getId()).build());
        item.put(DOCUMENT_STATUS_COL, AttributeValue.builder().s("IN_PROGRESS").build());
        item.put(DOCUMENT_URL_COL, AttributeValue.builder().s(formData.getUrl()).build());
        return PutItemRequest.builder()
                .tableName(tablaName)
                .item(item)
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