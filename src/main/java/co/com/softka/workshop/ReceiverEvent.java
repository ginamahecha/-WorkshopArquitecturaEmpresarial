package co.com.softka.workshop;

import co.com.softka.workshop.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static co.com.softka.workshop.event.DomainEvent.DOMAIN_EVENT_READER;

@ApplicationScoped
public class ReceiverEvent {
    @Inject
    private SqsClient sqs;

    @ConfigProperty(name = "queue.url")
    private String queueUrl;

    @Scheduled(every="5s")
    public void listener() {
        List<Message> messages = sqs.receiveMessage(
                m -> m.maxNumberOfMessages(10)
                        .queueUrl(queueUrl)
        ).messages();

        var eventList =  messages.stream()
                .map(Message::body)
                .map(this::toDomainEvent)
                .collect(Collectors.toList());

        for (Message m : messages) {
            sqs.deleteMessage(DeleteMessageRequest
                    .builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(m.receiptHandle())
                    .build());
        }
        eventList.forEach(System.out::println);
    }

    private DomainEvent toDomainEvent(String message) {
        try {
            return DOMAIN_EVENT_READER.readValue(message);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
