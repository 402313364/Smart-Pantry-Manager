package com.smartpantry.manager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * On-device SQLite database for pantry rows and the seeded recipe catalogue.
 * The pantry is the user's data. Recipes are inserted once, when the database file is first created.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

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

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "quantity REAL NOT NULL,"
                + "unit TEXT NOT NULL,"
                + "expiry_date TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "recipe_id INTEGER NOT NULL,"
                + "name TEXT NOT NULL,"
                + "quantity REAL NOT NULL,"
                + "unit TEXT NOT NULL,"
                + "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id))");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 has no migration yet. Leaving this empty keeps pantry rows intact.
    }

    public long insertItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, pantryValues(item));
    }

    public int updateItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, pantryValues(item), "id = ?", new String[]{String.valueOf(item.getId())});
    }

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
            return readPantryItem(cursor);
        } finally {
            cursor.close();
        }
    }

    public List<PantryItem> getPantryItems() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null, "name COLLATE NOCASE ASC");
        List<PantryItem> items = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                items.add(readPantryItem(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public List<Recipe> getRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT r.id, r.name, r.steps, i.id, i.name, i.quantity, i.unit "
                        + "FROM " + TABLE_RECIPES + " r "
                        + "LEFT JOIN " + TABLE_INGREDIENTS + " i ON i.recipe_id = r.id "
                        + "ORDER BY r.name COLLATE NOCASE ASC, i.id ASC",
                null);
        List<Recipe> recipes = new ArrayList<>();
        try {
            Recipe current = null;
            while (cursor.moveToNext()) {
                long recipeId = cursor.getLong(0);
                if (current == null || current.getId() != recipeId) {
                    current = new Recipe();
                    current.setId(recipeId);
                    current.setName(cursor.getString(1));
                    current.setSteps(cursor.getString(2));
                    recipes.add(current);
                }
                if (!cursor.isNull(3)) {
                    RecipeIngredient ingredient = new RecipeIngredient();
                    ingredient.setId(cursor.getLong(3));
                    ingredient.setRecipeId(recipeId);
                    ingredient.setName(cursor.getString(4));
                    ingredient.setQuantity(cursor.getDouble(5));
                    ingredient.setUnit(cursor.getString(6));
                    current.getIngredients().add(ingredient);
                }
            }
        } finally {
            cursor.close();
        }
        return recipes;
    }

    public Recipe getRecipe(long id) {
        for (Recipe recipe : getRecipes()) {
            if (recipe.getId() == id) {
                return recipe;
            }
        }
        return null;
    }

    private ContentValues pantryValues(PantryItem item) {
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

    private PantryItem readPantryItem(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
        int expiryIndex = cursor.getColumnIndexOrThrow("expiry_date");
        item.setExpiryDate(cursor.isNull(expiryIndex) ? null : cursor.getString(expiryIndex));
        return item;
    }

    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Boiled eggs",
                "Cover the eggs with water. Boil for 8 minutes, then cool them under cold water and peel.",
                lines("egg", "2", "item"));
        insertRecipe(db, "Scrambled eggs",
                "Beat the eggs with a pinch of salt. Melt the butter in a pan and stir the eggs over a low heat until just set.",
                lines("egg", "2", "item", "butter", "10", "g", "salt", "1", "g"));
        insertRecipe(db, "Cheese omelette",
                "Beat the eggs. Cook them in butter until the edges set, add the cheese, fold, and slide onto a plate.",
                lines("egg", "2", "item", "cheese", "40", "g", "butter", "10", "g"));
        insertRecipe(db, "Tomato pasta",
                "Boil the pasta. Soften the garlic in oil, add the chopped tomatoes, simmer, then toss with the pasta.",
                lines("pasta", "100", "g", "tomato", "2", "item", "garlic", "1", "item", "oil", "15", "ml"));
        insertRecipe(db, "Garlic bread",
                "Mix the butter with crushed garlic. Spread it on the bread and toast until the edges are golden.",
                lines("bread", "2", "item", "garlic", "1", "item", "butter", "20", "g"));
        insertRecipe(db, "Cheese toast",
                "Lay the cheese on the bread and toast until the cheese melts.",
                lines("bread", "2", "item", "cheese", "30", "g"));
        insertRecipe(db, "Rice and beans",
                "Rinse the rice and simmer it until tender. Warm the beans with onion and oil, then serve them over the rice.",
                lines("rice", "150", "g", "beans", "200", "g", "onion", "1", "item", "oil", "10", "ml"));
        insertRecipe(db, "Egg fried rice",
                "Scramble the egg in oil, add the onion and cooked rice, and season with salt.",
                lines("rice", "150", "g", "egg", "1", "item", "onion", "1", "item", "oil", "10", "ml", "salt", "1", "g"));
        insertRecipe(db, "Chicken and rice",
                "Brown the chicken in oil with the onion. Add the rice and enough water to cover, then simmer until the rice is tender.",
                lines("chicken", "200", "g", "rice", "150", "g", "onion", "1", "item", "oil", "10", "ml"));
        insertRecipe(db, "Tomato soup",
                "Soften the onion and garlic in oil. Add the tomatoes and salt, simmer until soft, then mash or blend.",
                lines("tomato", "3", "item", "onion", "1", "item", "garlic", "1", "item", "oil", "10", "ml", "salt", "1", "g"));
        insertRecipe(db, "Mashed potato",
                "Boil the potatoes until soft. Mash them with butter, milk, and salt.",
                lines("potato", "300", "g", "butter", "20", "g", "milk", "50", "ml", "salt", "1", "g"));
        insertRecipe(db, "Roast potatoes",
                "Toss the potatoes with oil and salt. Roast until the outsides are crisp.",
                lines("potato", "300", "g", "oil", "15", "ml", "salt", "1", "g"));
        insertRecipe(db, "Carrot salad",
                "Grate the carrot. Toss it with lemon juice and oil.",
                lines("carrot", "2", "item", "lemon", "1", "item", "oil", "10", "ml"));
        insertRecipe(db, "Pancakes",
                "Whisk the flour, milk, egg, and sugar into a batter. Fry spoonfuls in a lightly oiled pan until both sides are brown.",
                lines("flour", "100", "g", "milk", "150", "ml", "egg", "1", "item", "sugar", "15", "g"));
        insertRecipe(db, "Butter pasta",
                "Boil the pasta. Toss it with butter, cheese, and salt.",
                lines("pasta", "100", "g", "butter", "20", "g", "cheese", "30", "g", "salt", "1", "g"));
        insertRecipe(db, "Chicken stir fry",
                "Slice the chicken and pepper. Stir-fry them with the onion in oil until the chicken is cooked through.",
                lines("chicken", "200", "g", "onion", "1", "item", "pepper", "1", "item", "oil", "15", "ml"));
        insertRecipe(db, "Bean salad",
                "Rinse the beans. Toss them with chopped onion, lemon juice, and oil.",
                lines("beans", "200", "g", "onion", "1", "item", "lemon", "1", "item", "oil", "10", "ml"));
        insertRecipe(db, "French toast",
                "Beat the egg with milk and sugar. Dip the bread and fry until both sides are golden.",
                lines("bread", "2", "item", "egg", "1", "item", "milk", "40", "ml", "sugar", "10", "g"));
    }

    private void insertRecipe(SQLiteDatabase db, String name, String steps, String[][] ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("steps", steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipe);
        for (String[] line : ingredients) {
            ContentValues row = new ContentValues();
            row.put("recipe_id", recipeId);
            row.put("name", line[0]);
            row.put("quantity", Double.parseDouble(line[1]));
            row.put("unit", line[2]);
            db.insert(TABLE_INGREDIENTS, null, row);
        }
    }

    private String[][] lines(String... values) {
        String[][] rows = new String[values.length / 3][3];
        for (int i = 0; i < rows.length; i++) {
            rows[i][0] = values[i * 3];
            rows[i][1] = values[i * 3 + 1];
            rows[i][2] = values[i * 3 + 2];
        }
        return rows;
    }
}
