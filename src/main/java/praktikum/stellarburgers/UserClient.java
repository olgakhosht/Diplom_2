package praktikum.stellarburgers;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;


public class UserClient extends StellarBurgersRestClient {
    private static final String USER_PATH = "api/auth/";

    @Step("Отправка POST запроса на /api/auth/register")
    public ValidatableResponse createUser(User user) {
        return given()
                .spec(getBaseSpec())
                .body(user)
                .when()
                .post(USER_PATH + "register")
                .then();
    }

    @Step("Отправка POST запроса на /api/auth/login")
    public ValidatableResponse loginUser(UserCredentials credentials) {
        return given()
                .spec(getBaseSpec())
                .body(credentials)
                .when()
                .post(USER_PATH + "login")
                .then();
    }

    @Step("Отправка PATCH запроса на /api/auth/user")
    public ValidatableResponse changeUser(User user, String accessToken) {
        if (accessToken == null) {
            return given()
                    .spec(getBaseSpec())
                    .body(user)
                    .when()
                    .patch(USER_PATH + "user")
                    .then();
        } else {
            return given()
                    .spec(getBaseSpec())
                    .header("Authorization", accessToken)
                    .body(user)
                    .when()
                    .patch(USER_PATH + "user")
                    .then();
        }
    }

    @Step("Отправка DELETE запроса на /api/auth/user")
    public ValidatableResponse deleteUser(String accessToken) {
        if (accessToken == null) {
            return given()
                    .spec(getBaseSpec())
                    .when()
                    .delete(USER_PATH + "user")
                    .then();
        } else {
            return given()
                    .spec(getBaseSpec())
                    .header("Authorization", accessToken)
                    .when()
                    .delete(USER_PATH + "user")
                    .then();
        }
    }
}

