# Smart Pantry Manager

Android app that keeps a list of food I already have and shows recipes I can actually cook from it.

A recipe only counts as "you can cook now" when every ingredient is in the pantry, and I have at least as much as the recipe needs. If one thing is missing, it stays off that list. "Tomato" and "tomatoes" count as the same ingredient, and units like g/kg or ml/L get converted before they're compared.

Recipes that are missing exactly one ingredient show under Almost there. That is separate from the strict list.

Nothing in here uses a shop, a map, or location.

## Screens

Moved between with Intents. The pantry toolbar has Recipes and Settings.

1. Pantry list — RecyclerView of what's at home. Tap a row to edit it, or the bin icon to delete (it asks first).
2. Add / edit ingredient — name, quantity, unit, optional expiry. It checks the name and that the quantity is more than 0. New items start on the default unit from Settings.
3. Suggested recipes — recipes I can make right now, Almost there (one ingredient short), and all recipes with Have / Need.
4. Recipe detail — ingredients and the method. Opened with the recipe id on the Intent.
5. Settings — expiry alerts toggle and default unit, saved in SharedPreferences (not in the pantry table).

## Database

SQLite, through `SQLiteOpenHelper`, on the phone. File is `smart_pantry.db`.

Pantry rows are my data (create, read, update, delete) and they stay after the app is closed. Recipes are inserted once, when the database is first created — 18 of them, with their ingredients in a second table.

I looked at Firebase and a server database and didn't use them. This is one person's pantry and it has to work with no account and no internet. No Google Maps or GPS either.

## Run it

Java, Android Studio. Min Android 8.0 (API 26), compile SDK 34. Java 11 in `app/build.gradle`.

1. Install Android Studio with SDK 34.
2. Open the folder that has `settings.gradle`.
3. Wait for Gradle sync. Android Studio writes `local.properties` for the SDK path — that file stays off git.
4. Run `app` on an emulator or a phone on Android 8 or higher.
