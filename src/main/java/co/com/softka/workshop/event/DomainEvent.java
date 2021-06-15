package co.com.softka.workshop.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class DomainEvent {
    public static final  ObjectReader DOMAIN_EVENT_READER = new ObjectMapper().readerFor(DomainEvent.class);
    public static final  ObjectWriter DOMAIN_EVENT_WRITER = new ObjectMapper().writerFor(DomainEvent.class);

    private  String id;
    private  String url;

    public DomainEvent(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public DomainEvent(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
