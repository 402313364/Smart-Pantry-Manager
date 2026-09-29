package com.smartpantry.manager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// local SQLite database on the phone
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_INGREDIENTS = "recipe_ingredients";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // first time the app runs - create the tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id))");

        seedRecipes(db); // 18 recipes so matching has data straight away
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // keep the pantry, reload recipes so matching uses the latest ingredients
        db.execSQL("DELETE FROM " + TABLE_INGREDIENTS);
        db.execSQL("DELETE FROM " + TABLE_RECIPES);
        seedRecipes(db);
    }

    // Create
    public long insertItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, toValues(item));
    }

    // Update
    public int updateItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, toValues(item), "id = ?", new String[]{String.valueOf(item.getId())});
    }

    // Delete
    public int deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return readItem(cursor);
        } finally {
            cursor.close();
        }
    }

    // Read - all pantry items, sorted by name
    public List<PantryItem> getPantryItems() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null, "name COLLATE NOCASE ASC");
        List<PantryItem> items = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                items.add(readItem(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public List<Recipe> getRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null, "name COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                Recipe recipe = readRecipe(cursor);
                loadIngredients(db, recipe);
                recipes.add(recipe);
            }
        } finally {
            cursor.close();
        }
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            Recipe recipe = readRecipe(cursor);
            loadIngredients(db, recipe);
            return recipe;
        } finally {
            cursor.close();
        }
    }

    private Recipe readRecipe(Cursor cursor) {
        Recipe recipe = new Recipe();
        recipe.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        recipe.setSteps(cursor.getString(cursor.getColumnIndexOrThrow("steps")));
        return recipe;
    }

    private void loadIngredients(SQLiteDatabase db, Recipe recipe) {
        Cursor cursor = db.query(
                TABLE_INGREDIENTS,
                null,
                "recipe_id = ?",
                new String[]{String.valueOf(recipe.getId())},
                null,
                null,
                "id ASC");
        try {
            while (cursor.moveToNext()) {
                RecipeIngredient ingredient = new RecipeIngredient();
                ingredient.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                ingredient.setRecipeId(recipe.getId());
                ingredient.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                ingredient.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")));
                ingredient.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
                recipe.getIngredients().add(ingredient);
            }
        } finally {
            cursor.close();
        }
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put("name", item.getName().trim());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
            values.putNull("expiry_date");
        } else {
            values.put("expiry_date", item.getExpiryDate());
        }
        return values;
    }

    private PantryItem readItem(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
        int expiryCol = cursor.getColumnIndexOrThrow("expiry_date");
        if (cursor.isNull(expiryCol)) {
            item.setExpiryDate(null);
        } else {
            item.setExpiryDate(cursor.getString(expiryCol));
        }
        return item;
    }

    private void seedRecipes(SQLiteDatabase db) {
        long id;

        id = insertRecipe(db, "Boiled eggs",
                "Cover the eggs with water. Boil for 8 minutes, then cool them under cold water and peel.");
        addIngredient(db, id, "egg", 2, "item");

        id = insertRecipe(db, "Scrambled eggs",
                "Beat the eggs with a pinch of salt. Melt the butter in a pan and stir the eggs over a low heat until just set.");
        addIngredient(db, id, "egg", 2, "item");
        addIngredient(db, id, "butter", 10, "g");
        addIngredient(db, id, "salt", 1, "g");

        id = insertRecipe(db, "Cheese omelette",
                "Beat the eggs. Cook them in butter until the edges set, add the cheese, fold, and slide onto a plate.");
        addIngredient(db, id, "egg", 2, "item");
        addIngredient(db, id, "cheese", 40, "g");
        addIngredient(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Tomato pasta",
                "Boil the pasta. Soften the garlic in oil, add the chopped tomatoes, simmer, then toss with the pasta.");
        addIngredient(db, id, "pasta", 100, "g");
        addIngredient(db, id, "tomato", 2, "item");
        addIngredient(db, id, "garlic", 1, "item");
        addIngredient(db, id, "oil", 15, "ml");

        id = insertRecipe(db, "Garlic bread",
                "Mix the butter with crushed garlic. Spread it on the bread and toast until the edges are golden.");
        addIngredient(db, id, "bread", 2, "item");
        addIngredient(db, id, "garlic", 1, "item");
        addIngredient(db, id, "butter", 20, "g");

        id = insertRecipe(db, "Cheese toast",
                "Lay the cheese on the bread and toast until the cheese melts.");
        addIngredient(db, id, "bread", 2, "item");
        addIngredient(db, id, "cheese", 30, "g");

        id = insertRecipe(db, "Rice and beans",
                "Rinse the rice and simmer it until tender. Warm the beans with onion and oil, then serve them over the rice.");
        addIngredient(db, id, "rice", 150, "g");
        addIngredient(db, id, "beans", 200, "g");
        addIngredient(db, id, "onion", 1, "item");
        addIngredient(db, id, "oil", 10, "ml");

        id = insertRecipe(db, "Egg fried rice",
                "Scramble the egg in oil, add the onion and cooked rice, and season with salt.");
        addIngredient(db, id, "rice", 150, "g");
        addIngredient(db, id, "egg", 1, "item");
        addIngredient(db, id, "onion", 1, "item");
        addIngredient(db, id, "oil", 10, "ml");
        addIngredient(db, id, "salt", 1, "g");

        id = insertRecipe(db, "Chicken and rice",
                "Brown the chicken in oil with the onion. Add the rice and enough water to cover, then simmer until the rice is tender.");
        addIngredient(db, id, "chicken", 200, "g");
        addIngredient(db, id, "rice", 150, "g");
        addIngredient(db, id, "onion", 1, "item");
        addIngredient(db, id, "oil", 10, "ml");

        id = insertRecipe(db, "Tomato soup",
                "Soften the onion and garlic in oil. Add the tomatoes and salt, simmer until soft, then mash or blend.");
        addIngredient(db, id, "tomato", 3, "item");
        addIngredient(db, id, "onion", 1, "item");
        addIngredient(db, id, "garlic", 1, "item");
        addIngredient(db, id, "oil", 10, "ml");
        addIngredient(db, id, "salt", 1, "g");

        id = insertRecipe(db, "Mashed potato",
                "Boil the potatoes until soft. Mash them with butter, milk, and salt.");
        addIngredient(db, id, "potato", 300, "g");
        addIngredient(db, id, "butter", 20, "g");
        addIngredient(db, id, "milk", 50, "ml");
        addIngredient(db, id, "salt", 1, "g");

        id = insertRecipe(db, "Roast potatoes",
                "Toss the potatoes with oil and salt. Roast until the outsides are crisp.");
        addIngredient(db, id, "potato", 300, "g");
        addIngredient(db, id, "oil", 15, "ml");
        addIngredient(db, id, "salt", 1, "g");

        id = insertRecipe(db, "Carrot salad",
                "Grate the carrot. Toss it with lemon juice and oil.");
        addIngredient(db, id, "carrot", 2, "item");
        addIngredient(db, id, "lemon", 1, "item");
        addIngredient(db, id, "oil", 10, "ml");

        id = insertRecipe(db, "Pancakes",
                "Whisk the flour, milk, egg, and sugar into a batter. Fry spoonfuls in a lightly oiled pan until both sides are brown.");
        addIngredient(db, id, "flour", 100, "g");
        addIngredient(db, id, "milk", 150, "ml");
        addIngredient(db, id, "egg", 1, "item");
        addIngredient(db, id, "sugar", 15, "g");

        id = insertRecipe(db, "Butter pasta",
                "Boil the pasta. Toss it with butter, cheese, and salt.");
        addIngredient(db, id, "pasta", 100, "g");
        addIngredient(db, id, "butter", 20, "g");
        addIngredient(db, id, "cheese", 30, "g");
        addIngredient(db, id, "salt", 1, "g");

        id = insertRecipe(db, "Chicken stir fry",
                "Slice the chicken and pepper. Stir-fry them with the onion in oil until the chicken is cooked through.");
        addIngredient(db, id, "chicken", 200, "g");
        addIngredient(db, id, "onion", 1, "item");
        addIngredient(db, id, "pepper", 1, "item");
        addIngredient(db, id, "oil", 15, "ml");

        id = insertRecipe(db, "Bean salad",
                "Rinse the beans. Toss them with chopped onion, lemon juice, and oil.");
        addIngredient(db, id, "beans", 10, "g");
        addIngredient(db, id, "onion", 1, "item");
        addIngredient(db, id, "lemon", 1, "item");
        addIngredient(db, id, "oil", 10, "ml");

        id = insertRecipe(db, "French toast",
                "Beat the egg with milk and sugar. Dip the bread and fry until both sides are golden.");
        addIngredient(db, id, "bread", 2, "item");
        addIngredient(db, id, "egg", 1, "item");
        addIngredient(db, id, "milk", 40, "ml");
        addIngredient(db, id, "sugar", 10, "g");
    }

    private long insertRecipe(SQLiteDatabase db, String name, String steps) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("steps", steps);
        return db.insert(TABLE_RECIPES, null, values);
    }

    private void addIngredient(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues values = new ContentValues();
        values.put("recipe_id", recipeId);
        values.put("name", name);
        values.put("quantity", qty);
        values.put("unit", unit);
        db.insert(TABLE_INGREDIENTS, null, values);
    }
}
