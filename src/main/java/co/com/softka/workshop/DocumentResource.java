package co.com.softka.workshop;

import co.com.softka.workshop.data.FormDataRegister;
import co.com.softka.workshop.data.RequestData;
import co.com.softka.workshop.data.ResponseData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jsoup.Jsoup;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static co.com.softka.workshop.Constants.DOCUMENT_KEY_COL;
import static co.com.softka.workshop.Constants.DOCUMENT_METADATA_COL;

@Path("/document")
public class DocumentResource extends CommonResource {
    @Inject
    private S3Client s3;

    @Inject
    private DynamoDbClient dynamoDB;

    @Inject
    private SqsClient sqs;

    @POST
    @Path("extract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extract(RequestData requestData) throws IOException {
        var doc = Jsoup.connect(requestData.getUrl()).get();
        var formData = new FormDataRegister();

        formData.setData(doc.outerHtml());
        formData.setMimeType("text/html");
        formData.setId(UUID.randomUUID().toString());
        formData.setUrl(requestData.getUrl());
        formData.setSelector(requestData.getSelector());

        return generateResponse(formData);
    }

    @GET
    @Path("{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("key") String key) {
        var item = dynamoDB.getItem(getRequest(key)).item();
        ResponseData responseData = new ResponseData();
        if (item != null && !item.isEmpty()) {
            responseData.setId(item.get(DOCUMENT_KEY_COL).s());
            responseData.setHtml(item.get(DOCUMENT_METADATA_COL).s());
        }
        return Response.ok(responseData).status(Response.Status.CREATED).build();
    }

    private Response generateResponse(FormDataRegister formData) throws JsonProcessingException {
        var putS3Response = s3.putObject(
                buildPutRequest(formData),
                RequestBody.fromFile(uploadToTemp(formData.getData()))
        );
        var putDbResponse = dynamoDB.putItem(putRequest(formData));
        var sendResponse = sqs.sendMessage(buildSendMessage(formData));

        if (putS3Response != null && putDbResponse != null && sendResponse != null) {
            var response = new ResponseData();
            response.setId(formData.getId());
            return Response.ok(response).status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }
}