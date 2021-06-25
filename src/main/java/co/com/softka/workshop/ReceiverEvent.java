package co.com.softka.workshop;

import co.com.softka.workshop.data.FormDataUpdate;
import co.com.softka.workshop.event.DomainEvent;
import io.quarkus.scheduler.Scheduled;
import org.jsoup.Jsoup;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReceiverEvent extends CommonReceiverEvent {
    @Inject
    private SqsClient sqs;
    @Inject
    private S3Client s3;
    @Inject
    private DynamoDbClient dynamoDB;

    @Scheduled(every = "5s")
    public void listener() {
        sqs.receiveMessage(builderConsumer()).messages()
                .stream()
                .peek(message -> sqs.deleteMessage(buildRequestDelete(message)))
                .map(Message::body)
                .map(this::toDomainEvent)
                .collect(Collectors.toList())
                .forEach((DomainEvent event) -> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //5. Obtener el objeto del bucket, por medio del id
                    s3.getObject(
                            buildGetRequest(event.getId()),
                            ResponseTransformer.toOutputStream(baos)
                    );
                    //6. Processo de scraping
                    var html = baos.toString();
                    var metadata = Jsoup.parse(html)
                            .body()
                            .select(event.getSelector());
                    var dataUpdated = new FormDataUpdate();
                    dataUpdated.setId(event.getId());
                    dataUpdated.setHtml(metadata.html());

                    //7. Cambiar el estado o actualizar los datos de ese registro de la tabla
                    dynamoDB.putItem(putRequest(dataUpdated));
                });
    }
}
