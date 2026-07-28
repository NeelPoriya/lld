package in.neelporiya.restaurant.repository;

import in.neelporiya.restaurant.Menu;

/** // DESIGN PATTERN: Repository — facade code depends on this boundary, not map details. */
public class MenuRepository {
    private final Menu menu = new Menu();

    public Menu menu() {
        return menu;
    }
}
