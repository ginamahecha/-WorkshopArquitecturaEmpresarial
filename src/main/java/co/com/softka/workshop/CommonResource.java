package co.com.softka.workshop;

import co.com.softka.workshop.data.FormDataRegister;
import co.com.softka.workshop.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static co.com.softka.workshop.Constants.*;
import static co.com.softka.workshop.event.DomainEvent.DOMAIN_EVENT_WRITER;


public abstract class CommonResource {

    @ConfigProperty(name = "bucket.name")
    private String bucketName;

    @ConfigProperty(name = "tabla.name")
    private String tablaName;

    @ConfigProperty(name = "queue.url")
    private String queueUrl;

    protected PutObjectRequest buildPutRequest(FormDataRegister formDataRegister) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(formDataRegister.getId())
                .contentType(formDataRegister.getMimeType())
                .build();
    }

    protected SendMessageRequest buildSendMessage(FormDataRegister formDataRegister) throws JsonProcessingException {
        String message = DOMAIN_EVENT_WRITER.writeValueAsString(new DomainEvent(
                formDataRegister.getId(),
                formDataRegister.getUrl(),
                formDataRegister.getSelector()
        ));
        return SendMessageRequest
                .builder()
                .messageBody(message)
                .queueUrl(queueUrl)
                // .delaySeconds(5)
                .build();
    }

    protected GetItemRequest getRequest(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(DOCUMENT_KEY_COL, AttributeValue.builder().s(id).build());

        return GetItemRequest.builder()
                .tableName(tablaName)
                .key(key)
                .attributesToGet(DOCUMENT_KEY_COL, DOCUMENT_STATUS_COL, DOCUMENT_URL_COL, DOCUMENT_METADATA_COL)
                .build();
    }

    protected PutItemRequest putRequest(FormDataRegister formDataRegister) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(DOCUMENT_KEY_COL, AttributeValue.builder().s(formDataRegister.getId()).build());
        item.put(DOCUMENT_STATUS_COL, AttributeValue.builder().s("IN_PROGRESS").build());
        item.put(DOCUMENT_URL_COL, AttributeValue.builder().s(formDataRegister.getUrl()).build());
        item.put(DOCUMENT_METADATA_COL, AttributeValue.builder().s("").build());
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