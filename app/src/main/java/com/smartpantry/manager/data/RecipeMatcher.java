package com.smartpantry.manager.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Decides which recipes the user can cook from the pantry they have right now.
 * A recipe is suggested only when every required ingredient is present in at least the required amount.
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
    }

    public static List<Recipe> strictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Map<String, Double>> stock = indexPantry(pantry);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getIngredients().isEmpty()) {
                continue;
            }
            if (canMake(recipe, stock)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    private static boolean canMake(Recipe recipe, Map<String, Map<String, Double>> stock) {
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (!hasEnough(stock, ingredient)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasEnough(Map<String, Map<String, Double>> stock, RecipeIngredient ingredient) {
        BaseAmount needed = toBase(ingredient.getQuantity(), ingredient.getUnit());
        if (needed == null) {
            return false;
        }
        Map<String, Double> byFamily = stock.get(normalizeName(ingredient.getName()));
        if (byFamily == null) {
            return false;
        }
        Double have = byFamily.get(needed.family);
        return have != null && have + 0.0001d >= needed.amount;
    }

    /**
     * Adds up pantry rows that share a name and a unit family.
     * "tomato" and "tomatoes" land on the same key. Grams and kilograms land in "mass".
     */
    private static Map<String, Map<String, Double>> indexPantry(List<PantryItem> pantry) {
        Map<String, Map<String, Double>> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            BaseAmount have = toBase(item.getQuantity(), item.getUnit());
            if (have == null) {
                continue;
            }
            String name = normalizeName(item.getName());
            Map<String, Double> byFamily = stock.get(name);
            if (byFamily == null) {
                byFamily = new HashMap<>();
                stock.put(name, byFamily);
            }
            Double current = byFamily.get(have.family);
            byFamily.put(have.family, (current == null ? 0d : current) + have.amount);
        }
        return stock;
    }

    static String normalizeName(String raw) {
        String name = raw == null ? "" : raw.trim().toLowerCase(Locale.US);
        name = name.replaceAll("[^a-z\\s]", "");
        name = name.replaceAll("\\s+", " ").trim();
        if (name.endsWith("oes")) {
            name = name.substring(0, name.length() - 2);
        } else if (name.endsWith("ies")) {
            name = name.substring(0, name.length() - 3) + "y";
        } else if (name.endsWith("s") && !name.endsWith("ss")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    static BaseAmount toBase(double quantity, String unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.trim().toLowerCase(Locale.US)) {
            case "g":
            case "gram":
            case "grams":
                return new BaseAmount("mass", quantity);
            case "kg":
            case "kilogram":
            case "kilograms":
                return new BaseAmount("mass", quantity * 1000d);
            case "ml":
            case "millilitre":
            case "milliliter":
                return new BaseAmount("volume", quantity);
            case "l":
            case "litre":
            case "liter":
                return new BaseAmount("volume", quantity * 1000d);
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return new BaseAmount("volume", quantity * 5d);
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                return new BaseAmount("volume", quantity * 15d);
            case "cup":
            case "cups":
                return new BaseAmount("volume", quantity * 250d);
            case "item":
            case "items":
            case "whole":
                return new BaseAmount("count", quantity);
            default:
                return null;
        }
    }

    static final class BaseAmount {
        final String family;
        final double amount;

        BaseAmount(String family, double amount) {
            this.family = family;
            this.amount = amount;
        }
    }
}
