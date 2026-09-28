package com.smartpantry.manager.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    // A recipe only counts if every ingredient is in the pantry, with enough quantity.
    // "tomato" and "tomatoes" are treated as the same thing, and kg is converted to g (same idea for ml / L).
    public static List<Recipe> strictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> stock = new HashMap<>();

        for (PantryItem item : pantry) {
            String key = stockKey(item.getName(), item.getUnit());
            if (key == null) {
                continue;
            }
            double amount = toBaseAmount(item.getQuantity(), item.getUnit());
            if (stock.containsKey(key)) {
                stock.put(key, stock.get(key) + amount);
            } else {
                stock.put(key, amount);
            }
        }

        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            if (canCook(recipe, stock)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    private static boolean canCook(Recipe recipe, Map<String, Double> stock) {
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            String key = stockKey(ingredient.getName(), ingredient.getUnit());
            if (key == null || !stock.containsKey(key)) {
                return false;
            }
            double need = toBaseAmount(ingredient.getQuantity(), ingredient.getUnit());
            // small fudge so 100 and 100.0001 still count as enough
            if (stock.get(key) + 0.001 < need) {
                return false;
            }
        }
        return true;
    }

    // key is the cleaned name plus the unit group, so 500g and 0.5kg land in the same bucket
    private static String stockKey(String name, String unit) {
        String group = unitGroup(unit);
        if (group == null) {
            return null;
        }
        return cleanName(name) + "|" + group;
    }

    static String cleanName(String raw) {
        if (raw == null) {
            return "";
        }
        String name = raw.trim().toLowerCase(Locale.US);
        name = name.replaceAll("[^a-z ]", "");
        name = name.replaceAll(" +", " ").trim();

        if (name.endsWith("ies") && name.length() > 3) {
            name = name.substring(0, name.length() - 3) + "y";
        } else if (name.endsWith("oes") && name.length() > 3) {
            // tomatoes -> tomato
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
