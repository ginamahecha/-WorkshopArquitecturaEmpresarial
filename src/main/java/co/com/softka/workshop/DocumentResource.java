package co.com.softka.workshop;

import co.com.softka.workshop.data.FormData;
import co.com.softka.workshop.data.RequestData;
import org.jsoup.Jsoup;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/document")
public class DocumentResource extends CommonResource {
    @Inject
    private S3Client s3;

    @POST
    @Path("extract")
    public Response uploadFile(RequestData requestData) throws IOException {
        var doc = Jsoup.connect(requestData.getUrl()).get();
        var formData = new FormData();

        formData.setData(doc.outerHtml());
        formData.setMimeType("text/html");
        formData.setFileName(requestData.getName());

        PutObjectResponse putResponse = s3.putObject(
                buildPutRequest(formData),
                RequestBody.fromFile(uploadToTemp(formData.getData()))
        );
        if (putResponse != null) {
            return Response.ok().status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }
}