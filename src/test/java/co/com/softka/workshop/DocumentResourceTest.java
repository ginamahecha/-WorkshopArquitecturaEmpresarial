package co.com.softka.workshop;

import co.com.softka.workshop.data.RequestData;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentResourceTest {

    private static String responseId;

    @Test
    @Order(1)
    void extractData()  {
        RequestData requestData = new RequestData();
        requestData.setName("sofka.com.co");
        requestData.setSelector("website");
        requestData.setSelector("body > main > div > div.elementor.elementor-8 > div > div > section.elementor-section.elementor-top-section.elementor-element.elementor-element-5425d3a.elementor-section-boxed.elementor-section-height-default.elementor-section-height-default > div > div");
        requestData.setUrl("https://www.sofka.com.co/es/inicio");

        var response = given()
                .body(requestData)
                .header("Content-Type","application/json" )
                .header("Accept","application/json" )
                .when()
                    .post("/document/extract")
                .then()
                    .statusCode(201)
                .extract()
                .response();

         responseId = response.jsonPath().get("id");
    }

    @Test
    @Order(2)
    void getData() throws InterruptedException {
        Thread.sleep(10000);
        var response = given()
                .header("Content-Type","application/json" )
                .header("Accept","application/json" )
                .when()
                .get("/document/{id}", responseId)
                .then()
                .statusCode(201)
                .extract()
                .response();

        String html = response.jsonPath().get("html");
        System.out.println(html);

    }



}