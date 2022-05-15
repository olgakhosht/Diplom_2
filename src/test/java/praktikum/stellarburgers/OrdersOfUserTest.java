package praktikum.stellarburgers;

import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@Feature("Получение заказов конкретного пользователя")
public class OrdersOfUserTest {
    UserClient userClient;
    User user;
    OrderClient orderClient;
    Order order;
    Order order2;
    List<String> ingredients1;
    List<String> ingredients2;
    List<String> idsAllIngredients;
    String accessToken;

    @Before
    @Step("ДО теста: получение данных о всех ингредиентах, из них формирование ингредиентов для заказов")
    public void setUp() {
        orderClient = new OrderClient();
        ingredients1 = new ArrayList<>();
        ingredients2 = new ArrayList<>();

        ValidatableResponse dataAllIngredientsResponse = orderClient.getIDataAllIngredients();
        idsAllIngredients = dataAllIngredientsResponse.extract().jsonPath().getList("data._id");

        ingredients1.add(idsAllIngredients.get(0));
        ingredients1.add(idsAllIngredients.get(1));
        ingredients1.add(idsAllIngredients.get(2));
        order = new Order(ingredients1);

        ingredients2.add(idsAllIngredients.get(0));
        ingredients2.add(idsAllIngredients.get(1));
        ingredients2.add(idsAllIngredients.get(2));
        ingredients2.add(idsAllIngredients.get(3));
        order2 = new Order(ingredients2);

        /*
          Как альтернативный вариант,
          можно было бы ДОБАВИТЬ НОВЫЕ ингредиенты в общий список ВСЕХ ингредиентов и из них сформировать заказ.
          Но как добавить новый ингредиент документация не содержит, поэтому данный вариант не использован.
          */
    }

    // в @After нет удаления заказа, т.к. в документации отсутствует информация, как удалить заказ
    @After
    @Step("ПОСЛЕ теста: удаление пользователя, если пользователь был создан")
    public void tearDownUser() {
        if (accessToken != null) {
            userClient.deleteUser(accessToken);
        }
    }


    @Test
    @DisplayName("Авторизованный пользователь: успешное получение его заказов")
    public void authorizedUserGetHisOrders() throws InterruptedException {
        userClient = new UserClient();
        user = UserGenerator.getRandomUser();
        ValidatableResponse createResponse = userClient.createUser(user);
        accessToken = createResponse.extract().path("accessToken");

        // Создается первый заказ
        orderClient.createOrder(order, accessToken);

        // Приостановка на 2 секунды. Чтобы в теле ответа у первого и второго заказов отличались createdAt и updatedAt
        Thread.sleep(2000);

        // Создается второй заказ
        orderClient.createOrder(order2, accessToken);

        ValidatableResponse getOrdersOfUserResponse = orderClient.getOrdersOfUser(accessToken);

        // В документации не указан статус кода при успешном ответе, предположила "200"
        int statusCode = getOrdersOfUserResponse.extract().statusCode();
        assertThat("Авториз.пользователь: при получении своих заказов не получен ожидаемый статус кода",
                statusCode, equalTo(SC_OK));

        Boolean actualSuccess = getOrdersOfUserResponse.extract().path("success");
        assertThat("Авториз.пользователь: при получении своих заказов в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(true));

        List ingredientsOrders = getOrdersOfUserResponse.extract().jsonPath().getList("orders.ingredients");
        assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у 1-го заказа некорректные хеши ингредиентов",
                ingredientsOrders.get(0), equalTo(order.getIngredients()));
        assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у 2-го заказа некорректные хеши ингредиентов",
                ingredientsOrders.get(1), equalTo(order2.getIngredients()));

        List<String> idsOrders = getOrdersOfUserResponse.extract().jsonPath().getList("orders._id");
        for (int i = 0; i < idsOrders.size(); i++) {
            assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у заказа не заполнен '_id'",
                    !idsOrders.get(i).isEmpty(), equalTo(true));
        }

        List<String> statusesOrders = getOrdersOfUserResponse.extract().jsonPath().getList("orders.status");
        for (int i = 0; i < statusesOrders.size(); i++) {
            assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у заказа не заполнен status",
                    !statusesOrders.get(i).isEmpty(), equalTo(true));
        }

        List numbersOrders = getOrdersOfUserResponse.extract().jsonPath().getList("orders.number");
        assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у 1-го заказа некорректный number",
                (int) numbersOrders.get(0), greaterThanOrEqualTo(1));
        assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у 2-го заказа некорректный number (не больше, чем у первого)",
                (int) numbersOrders.get(1), greaterThan((int) numbersOrders.get(0)));


        List createdAtOrders = getOrdersOfUserResponse.extract().jsonPath().getList("orders.createdAt");
        ZonedDateTime actualCreatedAt1 = ZonedDateTime.parse(createdAtOrders.get(0).toString());
        ZonedDateTime actualCreatedAt2 = ZonedDateTime.parse(createdAtOrders.get(1).toString());
        assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у 2-го заказа некорректное createdAt",
                actualCreatedAt2.toLocalDateTime(), greaterThan(actualCreatedAt1.toLocalDateTime()));


        List updatedAtOrders = getOrdersOfUserResponse.extract().jsonPath().getList("orders.updatedAt");
        ZonedDateTime actualupdatedAt1 = ZonedDateTime.parse(updatedAtOrders.get(0).toString());
        ZonedDateTime actualupdatedAt2 = ZonedDateTime.parse(updatedAtOrders.get(1).toString());
        assertThat("Авториз.пользователь: при получении своих заказов в теле ответа у 2-го заказа некорректное updatedAt",
                actualupdatedAt2.toLocalDateTime(), greaterThan(actualupdatedAt1.toLocalDateTime()));

        int actualTotal = getOrdersOfUserResponse.extract().path("total");
        assertThat("Общее количество заказов пользователя в теле ответа неправильное",
                actualTotal, equalTo(2));

        int actualTotalToday = getOrdersOfUserResponse.extract().path("totalToday");
        assertThat("Количество заказов пользователя СЕГОДНЯ в теле ответа неправильное",
                actualTotalToday, equalTo(2));
    }

    @Test
    @DisplayName("Неавторизованный пользователь: невозможность получения его заказов")
    public void unauthorizedUserGetNotHisOrders() {
        orderClient.createOrder(order, null);
        //orderClient.createOrder(order2, null);

        ValidatableResponse getOrdersOfUserResponse = orderClient.getOrdersOfUser(null);

        int statusCode = getOrdersOfUserResponse.extract().statusCode();
        assertThat("Неавториз.пользователь: при получении заказов не получен ожидаемый статус кода 401",
                statusCode, equalTo(SC_UNAUTHORIZED));

        Boolean actualSuccess = getOrdersOfUserResponse.extract().path("success");
        assertThat("Неавториз.пользователь: при получении заказов в теле ответа не получен ожидаемый success",
                actualSuccess, equalTo(false));

        String actualMessage = getOrdersOfUserResponse.extract().path("message");
        assertThat("Неавториз.пользователь: при получении заказов в теле ответа сообщение не соответствует ожидаемому",
                actualMessage, equalTo("You should be authorised"));
    }
}
