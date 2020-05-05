package machine;

import java.util.Scanner;

public class CoffeeMachine {
    public static void main(String[] args) {
        Resources initialResources = new Resources(400, 540,120, 9, 550);
        Engine machine = new Engine(initialResources);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            machine.handleInput(scanner.nextLine());
        }
    }
}

class Engine {
    Mode mode;
    Resources remaining;
    SupplyResourceType fillingModeWaitingFor;

    public Engine(Resources initial) {
        this.remaining = initial;
        this.switchToMainMenu();
    }

    void handleInput(String input) {
        switch (this.mode) {
            case MAIN_MENU:
                this.handleMainMenuAction(input);
                break;
            case BUYING:
                this.handleBuyingModeInput(input);
                break;
            case FILLING:
                this.handleFillingModeInput(input);
                break;
            case EXITED:
                System.out.println("Please restart the coffee machine. It is exited from the main menu.");
                break;
        }
    }

    void handleMainMenuAction(String name) {
        MainMenuAction action = MainMenuAction.findByName(name);
        if (action == null) {
            System.out.println("Unknown action");
            return;
        }
        switch (action) {
            case BUY_COFFEE:
                this.buyCoffee();
                break;
            case FILL_RESOURCES:
                this.fillSupplyResources();
                break;
            case TAKE_MONEY:
                this.takeMoney();
                break;
            case SHOW_REMAINING_RESOURCES:
                this.showRemainingResources();
                break;
            case EXIT:
                this.exit();
                break;
        }

    }

    void handleBuyingModeInput(String input) {
        if ("back".equals(input)) {
            this.switchToMainMenu();
            return;
        }
        CoffeeVariety variety = CoffeeVariety.findById(input);
        if (variety == null) {
            System.out.println("Unknown coffee variety");
            this.switchToMainMenu();
            return;
        }
        this.buyCoffeeVariety(variety);
    }

    void handleFillingModeInput(String input) {
        int amount = Integer.parseInt(input);
        switch (this.fillingModeWaitingFor) {
            case WATER:
                this.remaining.waterMl += amount;
                System.out.println("Write how many ml of milk do you want to add:");
                this.fillingModeWaitingFor = SupplyResourceType.MILK;
                break;
            case MILK:
                this.remaining.milkMl += amount;
                System.out.println("Write how many grams of coffee beans do you want to add:");
                this.fillingModeWaitingFor = SupplyResourceType.COFFEE_BEANS;
                break;
            case COFFEE_BEANS:
                this.remaining.coffeeBeansG += amount;
                System.out.println("Write how many disposable cups of coffee do you want to add:");
                this.fillingModeWaitingFor = SupplyResourceType.DISPOSABLE_CAPS;
                break;
            case DISPOSABLE_CAPS:
                this.remaining.disposableCups += amount;
                this.fillingModeWaitingFor = null;
                this.switchToMainMenu();
        }
    }

    void buyCoffee() {
        this.mode = Mode.BUYING;
        System.out.print("What do you want to buy? ");
        for (CoffeeVariety variety: CoffeeVariety.values()) {
            System.out.print(variety.id + " - " + variety.name + ", ");
        }
        System.out.println("back - to main menu");
    }

    void fillSupplyResources() {
        this.mode = Mode.FILLING;
        System.out.println("Write how many ml of water do you want to add:");
        this.fillingModeWaitingFor = SupplyResourceType.WATER;
    }

    void takeMoney() {
        int moneyUsd = this.remaining.moneyUsd;
        this.remaining.moneyUsd = 0;
        System.out.println("I gave you $" + moneyUsd);
        this.switchToMainMenu();
    }

    void showRemainingResources() {
        System.out.println();
        System.out.println("The coffee machine has:");
        System.out.println(this.remaining.waterMl + " of water");
        System.out.println(this.remaining.milkMl + " of milk");
        System.out.println(this.remaining.coffeeBeansG + " of coffee beans");
        System.out.println(this.remaining.disposableCups + " of disposable cups");
        System.out.println(this.remaining.moneyUsd + " of money");
        System.out.println();
        this.switchToMainMenu();
    }

    void exit() {
        this.mode = Mode.EXITED;
    }

    void buyCoffeeVariety(CoffeeVariety variety) {
        SupplyResourceType lackResourceType= getLackSupplyResourceTypeFor(variety);
        if (lackResourceType != null) {
            System.out.println("Sorry, not enough " + lackResourceType.name);
            switchToMainMenu();
            return;
        }

        System.out.println("I have enough resources, making you a coffee!");

        this.remaining.waterMl -= variety.resources.waterMl;
        this.remaining.milkMl -= variety.resources.milkMl;
        this.remaining.coffeeBeansG -= variety.resources.coffeeBeansG;
        this.remaining.disposableCups -= variety.resources.disposableCups;
        this.remaining.moneyUsd += variety.resources.moneyUsd;

        this.switchToMainMenu();
    }

    SupplyResourceType getLackSupplyResourceTypeFor(CoffeeVariety variety) {
        if (this.remaining.waterMl < variety.resources.waterMl) {
            return SupplyResourceType.WATER;
        }
        if (this.remaining.milkMl < variety.resources.milkMl) {
            return SupplyResourceType.MILK;
        }
        if (this.remaining.coffeeBeansG < variety.resources.coffeeBeansG) {
            return SupplyResourceType.COFFEE_BEANS;
        }
        if (this.remaining.disposableCups < variety.resources.disposableCups) {
            return SupplyResourceType.DISPOSABLE_CAPS;
        }
        return null;
    }

    void switchToMainMenu() {
        this.mode = Mode.MAIN_MENU;
        MainMenuAction[] actions = MainMenuAction.values();

        System.out.print("Write action (");
        for (int i = 0; i < actions.length; ++i) {
            System.out.print(actions[i].name);
            if (i < actions.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("):");
    }
}

enum Mode {
    MAIN_MENU,
    BUYING,
    FILLING,
    EXITED
}

enum MainMenuAction {
    BUY_COFFEE("buy"),
    FILL_RESOURCES("fill"),
    TAKE_MONEY("take"),
    SHOW_REMAINING_RESOURCES("remaining"),
    EXIT("exit");

    final String name;

    MainMenuAction(String name) {
        this.name = name;
    }

    static MainMenuAction findByName(String name) {
        for(MainMenuAction action: MainMenuAction.values()) {
            if (name.equals(action.name)) {
                return action;
            }
        }
        return null;
    }
}

enum SupplyResourceType {
    WATER("water"),
    MILK("milk"),
    COFFEE_BEANS("coffee beans"),
    DISPOSABLE_CAPS("disposable cups");

    final String name;

    SupplyResourceType(String name) {
        this.name = name;
    }


}

class Resources {
    int waterMl;
    int milkMl;
    int coffeeBeansG;
    int disposableCups;
    int moneyUsd;

    Resources(int waterMl, int milkMl, int coffeeBeansG, int disposableCups, int moneyUsd) {
        this.waterMl = waterMl;
        this.milkMl = milkMl;
        this.coffeeBeansG = coffeeBeansG;
        this.disposableCups = disposableCups;
        this.moneyUsd = moneyUsd;
    }
}

enum CoffeeVariety {
    ESPRESSO("1", "espresso", new Resources(250, 0, 16, 1, 4)),
    LATTE("2", "latte", new Resources(350, 75, 20, 1, 7)),
    CAPPUCCINO("3", "cappuccino", new Resources(200, 100, 12, 1, 6));

    final String id;
    final String name;
    final Resources resources;

    CoffeeVariety(String id, String name, Resources resources) {
        this.id = id;
        this.name = name;
        this.resources = resources;
    }

    static CoffeeVariety findById(String id) {
        for (CoffeeVariety item: CoffeeVariety.values()) {
            if (id.equals(item.id)) {
                return item;
            }
        }
        return null;
    }
}

