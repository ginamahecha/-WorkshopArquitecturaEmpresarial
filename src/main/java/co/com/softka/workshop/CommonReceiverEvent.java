package co.com.softka.workshop;

import co.com.softka.workshop.data.FormDataUpdate;
import co.com.softka.workshop.event.DomainEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static co.com.softka.workshop.Constants.*;
import static co.com.softka.workshop.event.DomainEvent.DOMAIN_EVENT_READER;

public class CommonReceiverEvent {
    @ConfigProperty(name = "bucket.name")
    private String bucketName;

    @ConfigProperty(name = "tabla.name")
    private String tablaName;

    @ConfigProperty(name = "queue.url")
    private String queueUrl;

    protected PutItemRequest putRequest(FormDataUpdate formDataUpdate) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(DOCUMENT_KEY_COL, AttributeValue.builder().s(formDataUpdate.getId()).build());
        item.put(DOCUMENT_STATUS_COL, AttributeValue.builder().s("COMPLETED").build());
        item.put(DOCUMENT_METADATA_COL, AttributeValue.builder().s(formDataUpdate.getHtml()).build());
        return PutItemRequest.builder()
                .tableName(tablaName)
                .item(item)
                .build();
    }

    protected GetObjectRequest buildGetRequest(String objectKey) {
        return GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
    }

    protected DeleteMessageRequest buildRequestDelete(Message m) {
        return DeleteMessageRequest
                .builder()
                .queueUrl(queueUrl)
                .receiptHandle(m.receiptHandle())
                .build();
    }

    protected DomainEvent toDomainEvent(String message) {
        try {
            return DOMAIN_EVENT_READER.readValue(message);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected Consumer<ReceiveMessageRequest.Builder> builderConsumer() {
        return m -> m.maxNumberOfMessages(10)
                .queueUrl(queueUrl);

    }


}