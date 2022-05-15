package praktikum.stellarburgers;

import java.util.List;

public class Order {
    private List<String> ingredients;

    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Order() {
    }

    public List<String> getIngredients() {
        return ingredients;
    }
}
