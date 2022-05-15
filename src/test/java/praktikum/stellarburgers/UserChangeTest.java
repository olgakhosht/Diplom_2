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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;

@Feature("Изменение данных пользователя")
public class UserChangeTest {
    UserClient userClient;
    User user;
    User userWithChange;
    String accessToken;
    String accessToken2;

    @Before
    @Step("ДО теста: создание пользователя и генерация измененных данных пользователя")
    public void setUpUser() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
        ValidatableResponse createResponse = userClient.createUser(user);
        accessToken = createResponse.extract().path("accessToken");
        userWithChange = UserGenerator.getRandomUser();
    }

    @After
    @Step("ПОСЛЕ теста: удаление пользователя/ей")
    public void tearDownUser() {
        userClient.deleteUser(accessToken);
        if (accessToken2!=null) {
            userClient.deleteUser(accessToken2);
        }
    }

    @Test
    @DisplayName("Авторизованный пользователь: успешное изменение email")
    public void authorizedUserCanChangeEmail() {
        ValidatableResponse changeResponse = userClient.changeUser(new User(userWithChange.getEmail(), user.getPassword(), user.getName()), accessToken);

        int statusCode = changeResponse.extract().statusCode();
        assertThat("При изменении email авторизованным пользователем не получен ожидаемый статус кода 200",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = changeResponse.extract().path("success");
        assertThat("При изменении email авторизованным пользователем в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(true));

        //email в ответе приходит с заменой заглавных букв на строчные, использовано сравнение без учета регистра
        String emailChangeResponse = changeResponse.extract().path("user.email");
        assertThat("При изменении email авторизованным пользователем в теле ответа не получен ожидаемый измененный email",
                emailChangeResponse, equalToIgnoringCase(userWithChange.getEmail()));

        String nameChangeResponse = changeResponse.extract().path("user.name");
        assertThat("При изменении email авторизованным пользователем в теле ответа не получено ожидаемое name",
                nameChangeResponse, equalTo(user.getName()));
    }

    @Test
    @DisplayName("Авторизованный пользователь: успешное изменение пароля")
    public void authorizedUserCanChangePassword() {
        ValidatableResponse changeResponse = userClient.changeUser(new User(user.getEmail(), userWithChange.getPassword(), user.getName()), accessToken);

        int statusCode = changeResponse.extract().statusCode();
        assertThat("При изменении пароля авторизованным пользователем не получен ожидаемый статус кода 200",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = changeResponse.extract().path("success");
        assertThat("При изменении пароля авторизованным пользователем в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(true));

        //email в ответе приходит с заменой заглавных букв на строчные, использовано сравнение без учета регистра
        String emailChangeResponse = changeResponse.extract().path("user.email");
        assertThat("При изменении пароля авторизованным пользователем в теле ответа не получен ожидаемый email",
                emailChangeResponse, equalToIgnoringCase(user.getEmail()));

        String nameChangeResponse = changeResponse.extract().path("user.name");
        assertThat("При изменении пароля авторизованным пользователем в теле ответа не получено ожидаемое name",
                nameChangeResponse, equalTo(user.getName()));
    }

    @Test
    @DisplayName("Авторизованный пользователь: успешное изменение имени")
    public void authorizedUserCanChangeName() {
        ValidatableResponse changeResponse = userClient.changeUser(new User(user.getEmail(), user.getPassword(), userWithChange.getName()), accessToken);

        int statusCode = changeResponse.extract().statusCode();
        assertThat("При изменении name авторизованным пользователем не получен ожидаемый статус кода 200",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = changeResponse.extract().path("success");
        assertThat("При изменении name авторизованным пользователем в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(true));

        //email в ответе приходит с заменой заглавных букв на строчные, использовано сравнение без учета регистра
        String emailChangeResponse = changeResponse.extract().path("user.email");
        assertThat("При изменении name авторизованным пользователем в теле ответа не получен ожидаемый email",
                emailChangeResponse, equalToIgnoringCase(user.getEmail()));

        String nameChangeResponse = changeResponse.extract().path("user.name");
        assertThat("При изменении name авторизованным пользователем в теле ответа не получено ожидаемое измененное name",
                nameChangeResponse, equalTo(userWithChange.getName()));
    }

    @Test
    @DisplayName("Авторизованный пользователь: невозможность изменения email на уже используемый")
    public void authorizedUserCanNotChangeEmailOnUsedEmail() {
        ValidatableResponse createResponse2 = userClient.createUser(userWithChange);
        accessToken2 = createResponse2.extract().path("accessToken");

        ValidatableResponse changeResponse = userClient.changeUser(new User(userWithChange.getEmail(), user.getPassword(), user.getName()), accessToken);

        int statusCode = changeResponse.extract().statusCode();
        assertThat("При изменении email авторизованным пользователем на уже используемый email не получен ожидаемый статус кода 403",
                statusCode, equalTo(SC_FORBIDDEN));

        Boolean actualSuccess = changeResponse.extract().path("success");
        assertThat("При изменении email авторизованным пользователем на уже используемый email в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = changeResponse.extract().path("message");
        assertThat("При изменении email авторизованным пользователем на уже используемый email в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("User with such email already exists"));
    }

    @Test
    @DisplayName("Неавторизованный пользователь: невозможность изменения email")
    public void unauthorizedUserCanNotChangeEmail() {
        ValidatableResponse changeResponse = userClient.changeUser(new User(userWithChange.getEmail(), user.getPassword(), user.getName()), null);

        int statusCode = changeResponse.extract().statusCode();
        assertThat("При изменении email неавторизованным пользователем не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = changeResponse.extract().path("success");
        assertThat("При изменении email неавторизованным пользователем в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = changeResponse.extract().path("message");
        assertThat("При изменении email неавторизованным пользователем в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Неавторизованный пользователь: невозможность изменения пароля")
    public void unauthorizedUserCanNotChangePassword() {
        ValidatableResponse changeResponse = userClient.changeUser(new User(user.getEmail(), userWithChange.getPassword(), user.getName()), null);

        int statusCode = changeResponse.extract().statusCode();
        assertThat("При изменении пароля неавторизованным пользователем не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = changeResponse.extract().path("success");
        assertThat("При изменении пароля неавторизованным пользователем в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = changeResponse.extract().path("message");
        assertThat("При изменении пароля неавторизованным пользователем в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Неавторизованный пользователь: невозможность изменения имени")
    public void unauthorizedUserCanNotChangeName() {
        ValidatableResponse changeResponse = userClient.changeUser(new User(user.getEmail(), user.getPassword(), userWithChange.getName()), null);

        int statusCode = changeResponse.extract().statusCode();
        assertThat("При изменении name неавторизованным пользователем не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = changeResponse.extract().path("success");
        assertThat("При изменении name неавторизованным пользователем в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = changeResponse.extract().path("message");
        assertThat("При изменении name неавторизованным пользователем в теле ответа не получен ожидаемый message",
                actualMessage, equalTo("You should be authorised"));
    }
}
