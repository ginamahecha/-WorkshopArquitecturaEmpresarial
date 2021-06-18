package co.com.softka.workshop.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class DomainEvent {
    public static final ObjectReader DOMAIN_EVENT_READER = new ObjectMapper().readerFor(DomainEvent.class);
    public static final ObjectWriter DOMAIN_EVENT_WRITER = new ObjectMapper().writerFor(DomainEvent.class);

    private String id;
    private String url;
    private String selector;

    public DomainEvent(String id, String url, String selector) {
        this.id = id;
        this.url = url;
        this.selector = selector;
    }

    public DomainEvent() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", selector='" + selector + '\'' +
                '}';
    }
}
