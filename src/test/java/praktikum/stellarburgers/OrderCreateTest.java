package praktikum.stellarburgers;

import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Feature("Создание заказа")
public class OrderCreateTest {
    UserClient userClient;
    User user;
    OrderClient orderClient;
    List<String> ingredients;
    List<String> idsAllIngredients;
    String accessToken;

    @Before
    @Step("ДО теста: создание пользователя и получение данных о всех ингредиентах")
    public void setUp() {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
        ValidatableResponse createResponse = userClient.createUser(user);
        accessToken = createResponse.extract().path("accessToken");

        orderClient = new OrderClient();
        ingredients = new ArrayList<>();

       ValidatableResponse dataAllIngredientsResponse= orderClient.getIDataAllIngredients();
       idsAllIngredients = dataAllIngredientsResponse.extract().jsonPath().getList("data._id");
    }

    // в @After нет удаления заказа, т.к. в документации отсутствует информация, как удалить заказ
    @After
    @Step("ПОСЛЕ теста: удаление пользователя")
    public void tearDownUser() {
        userClient.deleteUser(accessToken);
    }


    /*
    В документации по созданию заказа нет информации о различиях ответов с авторизацией или без авторизации,
    поэтому исхожу из того, что ответы должны быть одинаковыми.
    Но по факту при создании заказа с авторизацией с правильными хешами ингредиентов в ответе информации больше,
    чем указано в документации
     */
    @Test
    @DisplayName("Авторизованный пользователь: успешное создание заказа с корректными хешами ингредиентов")
    public void authorizedUserCanCreateOrderWithCorrectIngredients() {
        ingredients.add(idsAllIngredients.get(0));
        ingredients.add(idsAllIngredients.get(1));
        ingredients.add(idsAllIngredients.get(2));

        Order order = new Order(ingredients);

        ValidatableResponse createOrderResponse = orderClient.createOrder(order, accessToken);

        int statusCode = createOrderResponse.extract().statusCode();
        assertThat("При создании авториз. пользователем заказа с корректными ингредиентами не получен ожидаемый статус кода 200",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = createOrderResponse.extract().path("success");
        assertThat("При создании авториз. пользователем заказа с корректными ингредиентами в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(true));

        String actualNameBurger = createOrderResponse.extract().path("name");
        assertThat("При создании авториз. пользователем заказа с корректными ингредиентами в теле ответа название бургера не заполнено",
                !actualNameBurger.isEmpty(), equalTo(true));

        int actualNumber = createOrderResponse.extract().path("order.number");
        assertThat("При создании авториз. пользователем заказа с корректными ингредиентами в теле ответа номер заказа некорректный",
                actualNumber, greaterThanOrEqualTo(1) );
    }

    @Test
    @DisplayName("Авторизованный пользователь: невозможность создания заказа без ингредиентов")
    public void authorizedUserCanNotCreateOrderWithoutIngredients() {
        ValidatableResponse createOrderResponse = orderClient.createOrder(null, accessToken);

        int statusCode = createOrderResponse.extract().statusCode();
        assertThat("При создании авториз. пользователем заказа без ингредиентов не получен ожидаемый статус кода 400",
                statusCode, equalTo(SC_BAD_REQUEST));

        Boolean actualSuccess = createOrderResponse.extract().path("success");
        assertThat("При создании авториз. пользователем заказа без ингредиентов в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = createOrderResponse.extract().path("message");
        assertThat("При создании неавториз. пользователем заказа без ингредиентов в теле ответа сообщение не соответствует ожидаемому",
                actualMessage, equalTo("Ingredient ids must be provided"));
    }
    @Test
    @DisplayName("Авторизованный пользователь: невозможность создания заказа со всеми невалидными хешами ингредиентов")
    public void authorizedUserCanNotCreateOrderWithAllInvalidIngredients() {
        ingredients.add("w"+idsAllIngredients.get(0).substring(1));
        ingredients.add("w"+idsAllIngredients.get(1).substring(1));
        ingredients.add("w"+idsAllIngredients.get(2).substring(1));

        Order order = new Order(ingredients);

        ValidatableResponse orderClientOrder = orderClient.createOrder(order, accessToken);

        int statusCode = orderClientOrder.extract().statusCode();
        assertThat("При создании авториз.пользователем заказа со всеми невалидными хешами ингредиентов не получен ожидаемый статус кода 500",
                statusCode, equalTo(SC_INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("Авторизованный пользователь: невозможность создания заказа с невалидным хешем и правильными хешами ингредиентов")
    public void authorizedUserCanNotCreateOrderWithInvalidAndCorrectIngredients() {
        // Первый ингредиент - невалидный хеш, второй и третий ингредиенты правильные
        ingredients.add("w"+idsAllIngredients.get(0).substring(1));
        ingredients.add(idsAllIngredients.get(1));
        ingredients.add(idsAllIngredients.get(2));

        Order order = new Order(ingredients);

        ValidatableResponse orderClientOrder = orderClient.createOrder(order, accessToken);

        int statusCode = orderClientOrder.extract().statusCode();
        assertThat("При создании авториз.пользователем заказа c невалидным хешем и правильными хешами ингредиентов не получен ожидаемый статус кода 500",
                statusCode, equalTo(SC_INTERNAL_SERVER_ERROR));
    }

    /*
    Считаю, что нужны еще тесты на проверку невозможности создания заказа не только с невалидным хешем ингредиента,
    но также с хешем ингредиента, СТРУКТУРА которого ПРАВИЛЬНАЯ, но ингредиент с такими хешем ОТСУТСТВУЕТ:
    один тест - в заказе ВСЕ такие ингредиенты (с правильной структурой хеша, но отсутствующие),
    другой тест - ОДИН такой ингредиент + правильные ингредиенты.
    С авторизацией и без авторизации.

    ! Но документация не содержит ожидаемого поведения для этих случаев. Поэтому написать тесты не могу.

    Опытным путем определила, что:
        при создании заказа со ВСЕМИ хешами ингредиентов, структура которых правильная, но ингредиенты ОТСУТСТВУЮТ:
            статус кода 400,
            "success": false,
            "message": "One or more ids provided are incorrect".

        при создании заказа с ОДНИМ хешем ОТСУТСТВУЮЩЕГО ингредиента с правильной структурой и другими правильными хешами ингредиентов:
            статус кода 200,
            "success": true,
            указано название бургера и номер заказа.
     */


    @Test
    @DisplayName("Неавторизованный пользователь: успешное создание заказа с корректными хешами ингредиентов")
    public void unauthorizedUserCanCreateOrderWithCorrectIngredients() {
        ingredients.add(idsAllIngredients.get(0));
        ingredients.add(idsAllIngredients.get(1));
        ingredients.add(idsAllIngredients.get(2));

        Order order = new Order(ingredients);

        ValidatableResponse createOrderResponse = orderClient.createOrder(order, null);

        int statusCode = createOrderResponse.extract().statusCode();
        assertThat("При создании неавториз. пользователем заказа с корректными ингредиентами не получен ожидаемый статус кода 200",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = createOrderResponse.extract().path("success");
        assertThat("При создании неавториз. пользователем заказа с корректными ингредиентами в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(true));

        String actualNameBurger = createOrderResponse.extract().path("name");
        assertThat("При создании неавториз. пользователем заказа с корректными ингредиентами в теле ответа название бургера не заполнено",
                !actualNameBurger.isEmpty(), equalTo(true));

        int actualNumber = createOrderResponse.extract().path("order.number");
        assertThat("При создании неавториз. пользователем заказа с корректными ингредиентами в теле ответа номер заказа некорректный",
                actualNumber, greaterThanOrEqualTo(1) );
    }

    @Test
    @DisplayName("Неавторизованный пользователь: невозможность создания заказа без ингредиентов")
    public void unauthorizedUserCanNotCreateOrderWithoutIngredients() {
        ValidatableResponse createOrderResponse = orderClient.createOrder(null, null);

        int statusCode = createOrderResponse.extract().statusCode();
        assertThat("При создании неавториз. пользователем заказа без ингредиентов не получен ожидаемый статус кода 400",
                statusCode, equalTo(SC_BAD_REQUEST));

        Boolean actualSuccess = createOrderResponse.extract().path("success");
        assertThat("При создании неавториз. пользователем заказа без ингредиентов в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = createOrderResponse.extract().path("message");
        assertThat("При создании неавториз. пользователем заказа без ингредиентов в теле ответа сообщение не соответствует ожидаемому",
                actualMessage, equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Неавторизованный пользователь: невозможность создания заказа со всеми невалидными хешами ингредиентов")
    public void unauthorizedUserCanNotCreateOrderWithAllInvalidIngredients() {
        ingredients.add("w"+idsAllIngredients.get(0).substring(1));
        ingredients.add("w"+idsAllIngredients.get(1).substring(1));
        ingredients.add("w"+idsAllIngredients.get(2).substring(1));

        Order order = new Order(ingredients);

        ValidatableResponse createOrderResponse = orderClient.createOrder(order, null);

        int statusCode = createOrderResponse.extract().statusCode();
        assertThat("При создании неавториз. пользователем заказа cо всеми невалидными хешами ингредиентов не получен ожидаемый статус кода 500",
                statusCode, equalTo(SC_INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("Неавторизованный пользователь: невозможность создания заказа с невалидным хешем и правильными хешами ингредиентов")
    public void unauthorizedUserCanNotCreateOrderWithInvalidAndCorrectIngredients() {
        // Первый ингредиент - невалидный хеш, второй и третий ингредиенты правильные
        ingredients.add("w"+idsAllIngredients.get(0).substring(1));
        ingredients.add(idsAllIngredients.get(1));
        ingredients.add(idsAllIngredients.get(2));

        Order order = new Order(ingredients);

        ValidatableResponse createOrderResponse = orderClient.createOrder(order, null);

        int statusCode = createOrderResponse.extract().statusCode();
        assertThat("При создании неавториз. пользователем заказа с невалидным хешем и правильными хешами ингредиентов не получен ожидаемый статус кода 500",
                statusCode, equalTo(SC_INTERNAL_SERVER_ERROR));
    }
}
