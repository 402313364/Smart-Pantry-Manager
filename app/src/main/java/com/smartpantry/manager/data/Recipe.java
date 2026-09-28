package com.smartpantry.manager.data;

import java.util.ArrayList;
import java.util.List;

/** A seeded recipe: a name, the method, and every ingredient it requires. */
public class Recipe {

    private long id;
    private String name;
    private String steps;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }
}
