package praktikum.stellarburgers;

import org.apache.commons.lang3.RandomStringUtils;

public class UserGenerator {

    public static User getRandomUser() {
        String userEmail = RandomStringUtils.randomAlphabetic(10) + "@ya.ru";
        String userPassword = RandomStringUtils.randomAlphabetic(10);
        String userName = RandomStringUtils.randomAlphabetic(10);

        return new User(userEmail, userPassword, userName);
    }
}
