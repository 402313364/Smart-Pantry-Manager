# Smart Pantry Manager

Smart Pantry Manager is a Java Android app that helps a person waste less food. It stores the ingredients they already have at home and suggests recipes they can cook from those leftovers. A recipe is shown only when every ingredient it needs is already in the pantry, in at least the required quantity. Nothing in the app depends on a shop, a map, or the user's location.

## Who it is for

The user is someone looking at leftovers in the kitchen and deciding what they can cook tonight without buying more food.

## Planned screens

The app will have five screens, moved between with Intents, and a bottom navigation bar:

1. **Pantry list** — every ingredient currently at home, shown in a RecyclerView.
2. **Add / edit ingredient** — name, quantity, unit, and an optional expiry date, with input validation.
3. **Suggested recipes** — only the recipes the user can make right now.
4. **Recipe detail** — the full ingredient list and the preparation steps for one recipe.
5. **Settings** — a toggle for expiring-soon alerts, and a preference for measurement units.

If no recipe matches the pantry, the suggestions screen will say so instead of showing a blank list.

## Strict-matching rule

This is the main piece of logic in the app.

A recipe is suggested only when every required ingredient is in the pantry and the pantry quantity is at least the quantity the recipe asks for. If a recipe needs five ingredients and the pantry has four, that recipe stays off the suggestions list. Partial matches are not shown there.

Matching will allow for simple differences such as `tomato` and `tomatoes`, and for common unit differences. It will not try to understand free-form sentences.

An optional later extra is a separate "Almost there" list for recipes missing only one ingredient. That list, if it is built, will stay separate from the strict suggestions.

## Planned data model

Two tables in a local SQLite database:

**pantry_items**

| Column | Purpose |
| --- | --- |
| id | Primary key |
| name | Ingredient name |
| quantity | How much the user has |
| unit | g, ml, item, and similar units |
| expiry_date | Optional date |

**recipes** and **recipe_ingredients**

| Stored fact | Purpose |
| --- | --- |
| Recipe name | What the dish is called |
| Required ingredients | Name, quantity, and unit for each line |
| Steps | Short preparation method |

About 15 to 20 recipes will be inserted the first time the database is created. Pantry rows are the user's own data. The user must be able to create, read, update, and delete pantry items, and those rows must still be there after the app is closed and opened again.

## Database choice

The database will be **SQLite**, using `SQLiteOpenHelper`, stored on the device.

SQLite fits this app because the pantry is personal data for one phone. It works with no account and no internet connection. It supports full create, read, update, and delete, and the rows remain after the process is killed. It is also the on-device storage approach covered in this module, so the report and the demonstration can explain one local database from the helper class through to the list on screen.

Firebase and PostgreSQL were considered and set aside. This app does not need cloud sync or a separate server.

The app will not use Google Maps, any mapping SDK, or GPS.

## Technical plan

- Java, in Android Studio. Kotlin is out of scope for this module.
- Separate Activities for the screens above.
- Intents to open a screen and to pass the selected recipe or pantry item.
- A RecyclerView with a custom Adapter for the pantry list and the suggestions list.
- `ConstraintLayout` for the forms and detail screens.
- SharedPreferences for the two settings, because they are preferences rather than pantry records.
- Minimum Android version: 8.0 (API 26). Compile SDK: 34.

## Setup and run

The Android project for this plan is in this folder.

1. Install Android Studio with Android SDK 34.
2. Open this folder in Android Studio. It is the folder that contains `settings.gradle`.
3. Wait for Gradle sync. Android Studio creates `local.properties` for the SDK path. That file stays off Git.
4. Run the `app` configuration on an emulator or a phone with Android 8.0 or higher.

Java 11 source compatibility is set in `app/build.gradle`. Android Studio's bundled JDK is enough.

## Build order

1. Project setup and this plan.
2. SQLite helper and the pantry table, with create, read, update, and delete.
3. Seed the recipe tables on first launch.
4. Pantry list screen and its adapter.
5. Add and edit screen, with validation.
6. Delete, with a confirmation.
7. Suggested recipes and the strict-matching rule.
8. Recipe detail screen.
9. Settings screen.
10. Bottom navigation, the empty-state message, and layout polish.
