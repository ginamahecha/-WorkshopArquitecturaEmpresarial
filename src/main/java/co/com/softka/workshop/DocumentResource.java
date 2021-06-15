package co.com.softka.workshop;

import co.com.softka.workshop.data.FormData;
import co.com.softka.workshop.data.RequestData;
import org.jsoup.Jsoup;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

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
    public Response uploadFile(RequestData requestData) throws IOException {
        var doc = Jsoup.connect(requestData.getUrl()).get();
        var formData = new FormData();

        formData.setData(doc.outerHtml());
        formData.setMimeType("text/html");
        formData.setId(UUID.randomUUID().toString());
        formData.setUrl(requestData.getUrl());

        PutObjectResponse putS3Response = s3.putObject(
                buildPutRequest(formData),
                RequestBody.fromFile(uploadToTemp(formData.getData()))
        );
        PutItemResponse putDbResponse = dynamoDB.putItem( putRequest(formData));
        SendMessageResponse sendResponse = sqs.sendMessage(buildSendMessage(formData));

        if (putS3Response != null && putDbResponse != null && sendResponse != null) {
            return Response.ok(formData.getId())
                    .status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }
}