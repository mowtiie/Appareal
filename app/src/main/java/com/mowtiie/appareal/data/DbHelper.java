package com.mowtiie.appareal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "appareal.db";

    public DbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, "
                + "last_name TEXT, first_name TEXT, middle_initial TEXT, address TEXT, contact TEXT, "
                + "id_type TEXT, id_image TEXT, role TEXT)");
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, description TEXT, "
                + "price INTEGER, stock INTEGER, image TEXT)");
        db.execSQL("CREATE TABLE cart (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, "
                + "product_id INTEGER, qty INTEGER)");
        db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, po_number TEXT, "
                + "order_date TEXT, total INTEGER, payment TEXT, address TEXT, status TEXT, eta TEXT)");
        db.execSQL("CREATE TABLE order_items (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER, "
                + "product_name TEXT, price INTEGER, qty INTEGER)");
        seedDemo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS cart");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS order_items");
        onCreate(db);
    }

    private void seedDemo(SQLiteDatabase db) {
        user(db, "storemngr", "St!apparel01", "Manager", "Store", "", "Appareal Store, Makati City",
                "09170000000", "National ID", "res:ph_id", Data.ROLE_MANAGER);
        user(db, "customer", "demo1234", "Dela Cruz", "Juan", "S", "123 Mabini Street, Quezon City",
                "09171234567", "National ID", "res:ph_id", Data.ROLE_CUSTOMER);

        product(db, "Classic White Tee", "Soft cotton crew neck tee in plain white. Easy everyday basic.", 399, 30, "res:ph_top");
        product(db, "Slim Fit Denim Jeans", "Stretch denim with a slim cut and a classic five-pocket design.", 1299, 15, "res:ph_bottom");
        product(db, "Hoodie Pullover", "Warm fleece-lined pullover hoodie with a kangaroo pocket.", 1099, 12, "res:ph_outer");
        product(db, "Floral Summer Dress", "Light and breezy knee-length dress with a floral print.", 899, 8, "res:ph_dress");
        product(db, "Polo Shirt", "Breathable pique polo with a ribbed collar and two-button placket.", 549, 20, "res:ph_top");
        product(db, "Cargo Shorts", "Relaxed cotton cargo shorts with side utility pockets.", 649, 18, "res:ph_bottom");
        product(db, "Windbreaker Jacket", "Water-resistant lightweight windbreaker with a packable hood.", 1499, 6, "res:ph_outer");
        product(db, "Knit Cardigan", "Cozy button-up knit cardigan for layering.", 999, 5, "res:ph_dress");

        String today = Data.today();
        String eta = Data.daysFromToday(3);
        String address = "123 Mabini Street, Quezon City";
        long o1 = order(db, 2, "PO-0001", today, 1347, address, Data.STATUS_VERIFIED, "");
        item(db, o1, "Classic White Tee", 399, 2);
        item(db, o1, "Polo Shirt", 549, 1);
        long o2 = order(db, 2, "PO-0002", today, 1748, address, Data.STATUS_ON_SHIPMENT, eta);
        item(db, o2, "Hoodie Pullover", 1099, 1);
        item(db, o2, "Cargo Shorts", 649, 1);
    }

    private void user(SQLiteDatabase db, String username, String password, String last, String first, String mi,
                      String address, String contact, String idType, String idImage, String role) {
        ContentValues v = new ContentValues();
        v.put("username", username);
        v.put("password", password);
        v.put("last_name", last);
        v.put("first_name", first);
        v.put("middle_initial", mi);
        v.put("address", address);
        v.put("contact", contact);
        v.put("id_type", idType);
        v.put("id_image", idImage);
        v.put("role", role);
        db.insert("users", null, v);
    }

    private void product(SQLiteDatabase db, String name, String desc, int price, int stock, String image) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", desc);
        v.put("price", price);
        v.put("stock", stock);
        v.put("image", image);
        db.insert("products", null, v);
    }

    private long order(SQLiteDatabase db, int userId, String po, String date, int total, String address,
                       String status, String eta) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("po_number", po);
        v.put("order_date", date);
        v.put("total", total);
        v.put("payment", Data.PAYMENT_COD);
        v.put("address", address);
        v.put("status", status);
        v.put("eta", eta);
        return db.insert("orders", null, v);
    }

    private void item(SQLiteDatabase db, long orderId, String name, int price, int qty) {
        ContentValues v = new ContentValues();
        v.put("order_id", orderId);
        v.put("product_name", name);
        v.put("price", price);
        v.put("qty", qty);
        db.insert("order_items", null, v);
    }
}
