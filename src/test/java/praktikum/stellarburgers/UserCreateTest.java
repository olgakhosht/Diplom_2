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

@Feature("Создание пользователя")
public class UserCreateTest {
    UserClient userClient;
    User user;
    String accessToken;

    @Before
    @Step("ДО теста: генерация данных пользователя")
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
    }

    @After
    @Step("ПОСЛЕ теста: удаление пользователя, если пользователь был создан")
    public void tearDownUser() {
        if (accessToken != null) {
           userClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Успешное создание пользователя с корректными данными")
    public void userCreateWithValidCredentials() {
        ValidatableResponse createResponse = userClient.createUser(user);

        int statusCode = createResponse.extract().statusCode();
        assertThat("При создании пользователя со всеми корректными данными не получен ожидаемый статус кода 200",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = createResponse.extract().path("success");
        assertThat("При создании пользователя с корректными данными в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(true));

        //email в ответе приходит с заменой заглавных букв на строчные, использовано сравнение без учета регистра
        String emailCreateResponse = createResponse.extract().path("user.email");
        assertThat("При создании пользователя с корректными данными в теле ответа не получен ожидаемый email",
                emailCreateResponse, equalToIgnoringCase(user.getEmail()));

        String nameCreateResponse = createResponse.extract().path("user.name");
        assertThat("При создании пользователя с корректными данными в теле ответа не получено ожидаемое name",
                nameCreateResponse, equalTo(user.getName()));

        accessToken = createResponse.extract().path("accessToken");
        assertThat("При создании пользователя с корректными данными в теле ответа у accessToken нет в начале 'Bearer '",
                accessToken.startsWith("Bearer "), equalTo(true));
        assertThat("При создании пользователя с корректными данными в теле ответа получен некорректный accessToken",
                accessToken.length(), greaterThan(7));

        String refreshToken = createResponse.extract().path("refreshToken");
        assertThat("При создании пользователя с корректными данными в теле ответа получен некорректный refreshToken",
                refreshToken.length(), greaterThanOrEqualTo(1));
//        assertThat("При создании пользователя с корректными данными в теле ответа получен незаполненный refreshToken", !refreshToken.isEmpty(), equalTo(true));
//        assertThat("При создании пользователя с корректными данными в теле ответа refreshToken null", refreshToken!=null, equalTo(true));
    }

    @Test
    @DisplayName("Невозможность создания пользователя с существующим email")
    public void userNotCreateWithReplayEmail() {
        ValidatableResponse createResponse = userClient.createUser(user);
        // accessToken пользователя нужен для удаления пользователя в @After
        accessToken = createResponse.extract().path("accessToken");

        ValidatableResponse createResponse2 = userClient.createUser(user);
        int statusCode = createResponse2.extract().statusCode();
        assertThat("При создании пользователя с существующим email не получен ожидаемый статус кода 403",
                statusCode, equalTo(SC_FORBIDDEN));

        Boolean actualSuccess = createResponse2.extract().path("success");
        assertThat("При создании пользователя с существующим email в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = createResponse2.extract().path("message");
        assertThat("При создании пользователя с существующим email в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("User already exists"));
    }

    @Test
    @DisplayName("Невозможность создания пользователя без email")
    public void userNotCreateWithoutEmail() {
        ValidatableResponse createResponse = userClient.createUser(new User(null, user.getPassword(), user.getName()));

        int statusCode = createResponse.extract().statusCode();
        assertThat("При создании пользователя без email не получен ожидаемый статус кода 403",
                statusCode, equalTo(SC_FORBIDDEN));

        Boolean actualSuccess = createResponse.extract().path("success");
        assertThat("При создании пользователя без email в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = createResponse.extract().path("message");
        assertThat("При создании пользователя без email в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Невозможность создания пользователя без пароля")
    public void userNotCreateWithoutPassword() {
        ValidatableResponse createResponse = userClient.createUser(new User(user.getEmail(), null, user.getName()));

        int statusCode = createResponse.extract().statusCode();
        assertThat("При создании пользователя без пароля не получен ожидаемый статус кода 403",
                statusCode, equalTo(SC_FORBIDDEN));

        Boolean actualSuccess = createResponse.extract().path("success");
        assertThat("При создании пользователя без пароля в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = createResponse.extract().path("message");
        assertThat("При создании пользователя без пароля в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Невозможность создания пользователя без имени")
    public void userNotCreateWithoutName() {

        ValidatableResponse createResponse = userClient.createUser(new User(user.getEmail(), user.getPassword(), null));

        int statusCode = createResponse.extract().statusCode();
        assertThat("При создании пользователя без name не получен ожидаемый статус кода 403",
                statusCode, equalTo(SC_FORBIDDEN));

        Boolean actualSuccess = createResponse.extract().path("success");
        assertThat("При создании пользователя без name в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = createResponse.extract().path("message");
        assertThat("При создании пользователя без name в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("Email, password and name are required fields"));
    }
}
