package com.smartpantry.manager.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeMatcher {

    // strict matching: every ingredient must be in the pantry, enough quantity
    public static List<Recipe> strictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            if (canCook(recipe, pantry)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    public static List<String> haveNames(Recipe recipe, List<PantryItem> pantry) {
        List<String> names = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (hasEnough(ingredient, pantry)) {
                names.add(ingredient.getName());
            }
        }
        return names;
    }

    public static List<String> needNames(Recipe recipe, List<PantryItem> pantry) {
        List<String> names = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (!hasEnough(ingredient, pantry)) {
                names.add(needLabel(ingredient, pantry));
            }
        }
        return names;
    }

    // bonus: missing only 1 ingredient - not shown in the strict list
    public static List<Recipe> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        List<Recipe> almost = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            if (canCook(recipe, pantry)) {
                continue;
            }
            if (missingCount(recipe, pantry) == 1) {
                almost.add(recipe);
            }
        }
        return almost;
    }

    private static int missingCount(Recipe recipe, List<PantryItem> pantry) {
        int missing = 0;
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (!hasEnough(ingredient, pantry)) {
                missing++;
            }
        }
        return missing;
    }

    public static String joinNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(names.get(i));
        }
        return builder.toString();
    }

    private static boolean canCook(Recipe recipe, List<PantryItem> pantry) {
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (!hasEnough(ingredient, pantry)) {
                return false;
            }
        }
        return true;
    }

    // same ingredient name counts even if the unit is different (g vs item vs ml)
    // if the units convert (g/kg, ml/L) we still check there is enough
    private static boolean hasEnough(RecipeIngredient ingredient, List<PantryItem> pantry) {
        String want = cleanName(ingredient.getName());
        if (want.isEmpty()) {
            return false;
        }
        double haveSameUnit = 0;
        double haveAny = 0;
        boolean foundName = false;
        String recipeGroup = unitGroup(ingredient.getUnit());

        for (PantryItem item : pantry) {
            if (!want.equals(cleanName(item.getName()))) {
                continue;
            }
            foundName = true;
            haveAny += item.getQuantity();
            String itemGroup = unitGroup(item.getUnit());
            if (recipeGroup != null && recipeGroup.equals(itemGroup)) {
                haveSameUnit += toBaseAmount(item.getQuantity(), item.getUnit());
            }
        }

        if (!foundName) {
            return false;
        }

        double need = toBaseAmount(ingredient.getQuantity(), ingredient.getUnit());
        if (haveSameUnit + 0.001 >= need) {
            return true;
        }
        // leftover in g but recipe says item / ml - still count it if the amount is enough
        return haveAny + 0.001 >= ingredient.getQuantity();
    }

    private static String needLabel(RecipeIngredient ingredient, List<PantryItem> pantry) {
        String onHand = onHandText(ingredient.getName(), pantry);
        if (onHand == null) {
            return ingredient.getName() + " (none in pantry)";
        }
        return ingredient.getName() + " (need "
                + PantryItem.formatQuantity(ingredient.getQuantity()) + " "
                + ingredient.getUnit() + ", you have " + onHand + ")";
    }

    private static String onHandText(String name, List<PantryItem> pantry) {
        String want = cleanName(name);
        for (PantryItem item : pantry) {
            if (want.equals(cleanName(item.getName()))) {
                return PantryItem.formatQuantity(item.getQuantity()) + " " + item.getUnit();
            }
        }
        return null;
    }

    static String cleanName(String raw) {
        if (raw == null) {
            return "";
        }
        String name = raw.trim().toLowerCase(Locale.US);
        name = name.replaceAll("[^a-z ]", "");
        name = name.replaceAll(" +", " ").trim();

        // tomatoes -> tomato, berries -> berry, eggs -> egg
        if (name.endsWith("ies") && name.length() > 3) {
            name = name.substring(0, name.length() - 3) + "y";
        } else if (name.endsWith("oes") && name.length() > 3) {
            name = name.substring(0, name.length() - 2);
        } else if (name.endsWith("s") && !name.endsWith("ss") && name.length() > 1) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private static String unitGroup(String unit) {
        if (unit == null) {
            return null;
        }
        String u = unit.trim().toLowerCase(Locale.US);
        if (u.equals("g") || u.equals("kg") || u.equals("gram") || u.equals("grams")) {
            return "weight";
        }
        if (u.equals("ml") || u.equals("l") || u.equals("tsp") || u.equals("tbsp") || u.equals("cup")) {
            return "volume";
        }
        if (u.equals("item") || u.equals("items")) {
            return "count";
        }
        return null;
    }

    private static double toBaseAmount(double quantity, String unit) {
        String u = unit.trim().toLowerCase(Locale.US);
        if (u.equals("kg")) {
            return quantity * 1000;
        }
        if (u.equals("l")) {
            return quantity * 1000;
        }
        if (u.equals("tsp")) {
            return quantity * 5;
        }
        if (u.equals("tbsp")) {
            return quantity * 15;
        }
        if (u.equals("cup")) {
            return quantity * 250;
        }
        return quantity;
    }
}
