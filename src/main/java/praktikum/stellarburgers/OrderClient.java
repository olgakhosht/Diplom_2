package praktikum.stellarburgers;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class OrderClient extends StellarBurgersRestClient {
    private static final String ORDERS_PATH = "api/";

    @Step("Отправка POST запроса на /api/orders")
    public ValidatableResponse createOrder(Order order, String accessToken) {
        if (order == null && accessToken == null) {
            return given()
                    .spec(getBaseSpec())
                    .when()
                    .post(ORDERS_PATH + "orders")
                    .then();
        } else if (order == null && accessToken != null) {
            return given()
                    .spec(getBaseSpec())
                    .header("Authorization", accessToken)
                    .when()
                    .post(ORDERS_PATH + "orders")
                    .then();
        } else if (order != null && accessToken == null) {
            return given()
                    .spec(getBaseSpec())
                    .body(order)
                    .when()
                    .post(ORDERS_PATH + "orders")
                    .then();
        } else {
            return given()
                    .spec(getBaseSpec())
                    .header("Authorization", accessToken)
                    .body(order)
                    .when()
                    .post(ORDERS_PATH + "orders")
                    .then();
        }
    }


    @Step("Отправка GET запроса на /api/orders")
    public ValidatableResponse getOrdersOfUser(String accessToken) {
        if (accessToken == null) {
            return given()
                    .spec(getBaseSpec())
                    .when()
                    .get(ORDERS_PATH + "orders")
                    .then();
        } else {
            return given()
                    .spec(getBaseSpec())
                    .header("Authorization", accessToken)
                    .when()
                    .get(ORDERS_PATH + "orders")
                    .then();
        }
    }

    @Step("Отправка GET запроса на /api/ingredients")
    public ValidatableResponse getIDataAllIngredients() {
            return given()
                    .spec(getBaseSpec())
                    .when()
                    .get(ORDERS_PATH + "ingredients")
                    .then();
    }
}
