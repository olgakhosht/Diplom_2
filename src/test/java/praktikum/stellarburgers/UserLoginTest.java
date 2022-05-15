package praktikum.stellarburgers;

import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Feature("Авторизация пользователя")
public class UserLoginTest {

    UserClient userClient;
    User user;
    String accessToken;

    @Before
    @Step("ДО теста: создание пользователя")
    public void setUpUser() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
        ValidatableResponse createResponse = userClient.createUser(user);
        accessToken = createResponse.extract().path("accessToken");
    }

    @After
    @Step("ПОСЛЕ теста: удаление пользователя")
    public void tearDownUser() {
        userClient.deleteUser(accessToken);
    }


    @Test
    @DisplayName("Успешная авторизация пользователя с корректными данными")
    public void userCanLoginWithCorrectCredentials() {
        ValidatableResponse loginResponse = userClient.loginUser(new UserCredentials(user.getEmail(), user.getPassword()));

        int statusCode = loginResponse.extract().statusCode();
        assertThat("При авторизации пользователя с корректными данными не получен ожидаемый статус кода 200",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = loginResponse.extract().path("success");
        assertThat("При авторизации пользователя с корректными данными не получен ожидаемый success",
                actualSuccess, equalTo(true));

        String accessTokenLoginResponse = loginResponse.extract().path("accessToken");
        assertThat("При авторизации пользователя с корректными данными в теле ответа у accessToken нет в начале 'Bearer '",
                accessTokenLoginResponse.startsWith("Bearer "), equalTo(true));
        assertThat("При авторизации пользователя с корректными данными в теле ответа получен некорректный accessToken",
                accessTokenLoginResponse.length(), greaterThan(7));


        String refreshTokenLoginResponse = loginResponse.extract().path("refreshToken");
        assertThat("При авторизации пользователя с корректными данными в теле ответа получен некорректный refreshToken",
                refreshTokenLoginResponse.length(), greaterThanOrEqualTo(1));
//        assertThat("При авторизации пользователя с корректными данными в теле ответа получен незаполненный refreshToken", !refreshTokenLoginResponse.isEmpty(), equalTo(true));
//        assertThat("При авторизации пользователя с корректными данными в теле ответа refreshToken null", refreshTokenLoginResponse!=null, equalTo(true));

        //email в ответе приходит с заменой заглавных букв на строчные, использовано сравнение без учета регистра
        String emailLoginResponse = loginResponse.extract().path("user.email");
        assertThat("При авторизации пользователя с корректными данными не получен ожидаемый email",
                emailLoginResponse, equalToIgnoringCase(user.getEmail()));

        String nameLoginResponse = loginResponse.extract().path("user.name");
        assertThat("При авторизации пользователя с корректными данными не получено ожидаемое name",
                nameLoginResponse, equalTo(user.getName()));
    }


    @Test
    @DisplayName("Невозможность авторизации пользователя с неверным email")
    public void userCannotLoginWithIncorrectEmail() {
        ValidatableResponse loginResponse = userClient.loginUser(new UserCredentials("email-email", user.getPassword()));

        int statusCode = loginResponse.extract().statusCode();
        assertThat("При авторизации пользователя c неправильным email не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = loginResponse.extract().path("success");
        assertThat("При авторизации пользователя с неправильным email в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = loginResponse.extract().path("message");
        assertThat("При авторизации пользователя с неправильным email в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Невозможность авторизации пользователя с неверным паролем")
    public void userCannotLoginWithIncorrectPassword() {
        ValidatableResponse loginResponse = userClient.loginUser(new UserCredentials(user.getEmail(), "parolparol"));

        int statusCode = loginResponse.extract().statusCode();
        assertThat("При авторизации пользователя с неправильным паролем не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = loginResponse.extract().path("success");
        assertThat("При авторизации пользователя с неправильным паролем в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = loginResponse.extract().path("message");
        assertThat("При авторизации пользователя с неправильным паролем в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Невозможность авторизации пользователя без email")
    public void userCannotLoginWithoutEmail() {
        ValidatableResponse loginResponse = userClient.loginUser(new UserCredentials(null, user.getPassword()));

        int statusCode = loginResponse.extract().statusCode();
        assertThat("При авторизации пользователя без email не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = loginResponse.extract().path("success");
        assertThat("При авторизации пользователя без email в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = loginResponse.extract().path("message");
        assertThat("При авторизации пользователя без email в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Невозможность авторизации пользователя без пароля")
    public void userCannotLoginWithoutPassword() {
        ValidatableResponse loginResponse = userClient.loginUser(new UserCredentials(user.getEmail(), null));

        int statusCode = loginResponse.extract().statusCode();
        assertThat("При авторизации пользователя без пароля не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = loginResponse.extract().path("success");
        assertThat("При авторизации пользователя без пароля в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = loginResponse.extract().path("message");
        assertThat("При авторизации пользователя без пароля в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("email or password are incorrect"));
    }
}
