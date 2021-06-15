package co.com.softka.workshop.event;

public class DomainEvent {
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
}
