import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientIntegrationTest {
  @BeforeAll
  static void setUp(){
    RestAssured.baseURI = "http://localhost:4004";
  }

  @Test
  public void shouldReturnPatientsWithValidToken () {
    String token = getToken();

    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/api/patients")
        .then()
        .statusCode(200)
        .body("patients", notNullValue());
  }

  @Test
  public void shouldEnforceRateLimit() {
    String token = getToken();

    int rateLimitThreshold = 10; // adjust based on your API's actual rate limit
    int status429Count = 0;

    for (int i = 0; i < rateLimitThreshold + 5; i++) {
      Response response = given()
              .header("Authorization", "Bearer " + token)
              .when()
              .get("/api/patients");

      int statusCode = response.getStatusCode();

      // Print each request number, status code, and message (optional)
      System.out.println("Request " + (i + 1) + " - Status Code: " + statusCode);

      if (statusCode == 429) {
        System.out.println("Rate limit triggered on request " + (i + 1));
        status429Count++;
      }
    }

    System.out.println("Total 429 responses: " + status429Count);
    assertTrue(status429Count > 0, "Expected at least one 429 Too Many Requests response");
  }


  private static String getToken() {
    String loginPayload = """
          {
            "email": "testuser@test.com",
            "password": "password123"
          }
        """;

    String token = given()
        .contentType("application/json")
        .body(loginPayload)
        .when()
        .post("/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .get("token");
    return token;
  }
}
