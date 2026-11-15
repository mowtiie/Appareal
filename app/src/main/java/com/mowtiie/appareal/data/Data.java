package com.mowtiie.appareal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Data {

    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String STATUS_PENDING = "STATUS_PENDING";
    public static final String STATUS_VERIFIED = "STATUS_VERIFIED";
    public static final String STATUS_PACKAGED = "STATUS_PACKAGED";
    public static final String STATUS_ON_SHIPMENT = "STATUS_ON_SHIPMENT";
    public static final String PAYMENT_COD = "PAYMENT_COD";

    public static final String[] ID_TYPES = {"SSS", "PhilHealth", "Driver's License", "PRC License", "National ID"};

    public static int USER_ID;
    public static String USER_USERNAME = "";
    public static String USER_ROLE = "";
    public static String USER_LAST_NAME = "";
    public static String USER_FIRST_NAME = "";
    public static String USER_MIDDLE_INITIAL = "";
    public static String USER_ADDRESS = "";
    public static String USER_CONTACT = "";
    public static String USER_ID_TYPE = "";
    public static String USER_ID_IMAGE = "";

    private static DbHelper helper;

    public static SQLiteDatabase db(Context ctx) {
        if (helper == null) helper = new DbHelper(ctx.getApplicationContext());
        return helper.getWritableDatabase();
    }

    public static boolean usernameTaken(Context ctx, String username) {
        try (Cursor c = db(ctx).rawQuery("SELECT id FROM users WHERE username = ? COLLATE NOCASE",
                new String[]{username})) {
            return c.moveToFirst();
        }
    }

    public static void insertUser(Context ctx, String username, String password, String last, String first,
                                  String mi, String address, String contact, String idType, String idImage) {
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
        v.put("role", ROLE_CUSTOMER);
        db(ctx).insert("users", null, v);
    }

    // Copies the matching account into USER_*; returns false on wrong credentials.
    public static boolean login(Context ctx, String username, String password) {
        try (Cursor c = db(ctx).rawQuery("SELECT id, username, role, last_name, first_name, middle_initial, "
                        + "address, contact, id_type, id_image FROM users WHERE username = ? COLLATE NOCASE AND password = ?",
                new String[]{username, password})) {
            if (!c.moveToFirst()) return false;
            USER_ID = c.getInt(0);
            USER_USERNAME = c.getString(1);
            USER_ROLE = c.getString(2);
            USER_LAST_NAME = c.getString(3);
            USER_FIRST_NAME = c.getString(4);
            USER_MIDDLE_INITIAL = c.getString(5);
            USER_ADDRESS = c.getString(6);
            USER_CONTACT = c.getString(7);
            USER_ID_TYPE = c.getString(8);
            USER_ID_IMAGE = c.getString(9);
            return true;
        }
    }

    public static String money(int amount) {
        return "₱" + NumberFormat.getIntegerInstance(Locale.US).format(amount);
    }

    public static String today() {
        return daysFromToday(0);
    }

    public static String daysFromToday(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(cal.getTime());
    }

    public static String fullName() {
        String mi = USER_MIDDLE_INITIAL.isEmpty() ? "" : USER_MIDDLE_INITIAL + ". ";
        return USER_FIRST_NAME + " " + mi + USER_LAST_NAME;
    }

    private static java.util.ArrayList<String[]> rows(Cursor c, int n) {
        java.util.ArrayList<String[]> list = new java.util.ArrayList<>();
        while (c.moveToNext()) {
            String[] r = new String[n];
            for (int i = 0; i < n; i++) r[i] = c.getString(i);
            list.add(r);
        }
        c.close();
        return list;
    }

    // Product row: {id, name, description, price, stock, image}
    public static java.util.ArrayList<String[]> searchProducts(Context ctx, String keyword) {
        String like = "%" + keyword.trim() + "%";
        return rows(db(ctx).rawQuery("SELECT id, name, description, price, stock, image FROM products "
                + "WHERE name LIKE ? OR description LIKE ? ORDER BY id", new String[]{like, like}), 6);
    }

    public static String[] getProduct(Context ctx, String id) {
        java.util.ArrayList<String[]> r = rows(db(ctx).rawQuery(
                "SELECT id, name, description, price, stock, image FROM products WHERE id = ?",
                new String[]{id}), 6);
        return r.isEmpty() ? null : r.get(0);
    }

    // Returns false when the cart already holds all the stock (or stock is 0).
    public static boolean addToCart(Context ctx, String productId) {
        SQLiteDatabase d = db(ctx);
        String[] p = getProduct(ctx, productId);
        int stock = Integer.parseInt(p[4]);
        try (Cursor c = d.rawQuery("SELECT id, qty FROM cart WHERE user_id = ? AND product_id = ?",
                new String[]{String.valueOf(USER_ID), productId})) {
            if (c.moveToFirst()) {
                int qty = c.getInt(1);
                if (qty + 1 > stock) return false;
                ContentValues v = new ContentValues();
                v.put("qty", qty + 1);
                d.update("cart", v, "id = ?", new String[]{c.getString(0)});
                return true;
            }
        }
        if (stock < 1) return false;
        restoreCart(ctx, productId, 1);
        return true;
    }

    public static void restoreCart(Context ctx, String productId, int qty) {
        ContentValues v = new ContentValues();
        v.put("user_id", USER_ID);
        v.put("product_id", productId);
        v.put("qty", qty);
        db(ctx).insert("cart", null, v);
    }

    // Cart row: {cartId, productId, name, price, qty, stock, image}
    public static java.util.ArrayList<String[]> cartRows(Context ctx) {
        return rows(db(ctx).rawQuery("SELECT c.id, p.id, p.name, p.price, c.qty, p.stock, p.image "
                + "FROM cart c JOIN products p ON p.id = c.product_id WHERE c.user_id = ? ORDER BY c.id",
                new String[]{String.valueOf(USER_ID)}), 7);
    }

    public static void setCartQty(Context ctx, String cartId, int qty) {
        ContentValues v = new ContentValues();
        v.put("qty", qty);
        db(ctx).update("cart", v, "id = ?", new String[]{cartId});
    }

    public static void deleteCart(Context ctx, String cartId) {
        db(ctx).delete("cart", "id = ?", new String[]{cartId});
    }

    public static int cartTotal(Context ctx) {
        try (Cursor c = db(ctx).rawQuery("SELECT COALESCE(SUM(c.qty * p.price), 0) FROM cart c "
                + "JOIN products p ON p.id = c.product_id WHERE c.user_id = ?", new String[]{String.valueOf(USER_ID)})) {
            c.moveToFirst();
            return c.getInt(0);
        }
    }

    public static int cartCount(Context ctx) {
        try (Cursor c = db(ctx).rawQuery("SELECT COALESCE(SUM(qty), 0) FROM cart WHERE user_id = ?",
                new String[]{String.valueOf(USER_ID)})) {
            c.moveToFirst();
            return c.getInt(0);
        }
    }

    // Creates the order and its items, decrements stock, clears the cart; returns the order id.
    public static long placeOrder(Context ctx, String address) {
        SQLiteDatabase d = db(ctx);
        java.util.ArrayList<String[]> cart = cartRows(ctx);
        int total = cartTotal(ctx);
        d.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("user_id", USER_ID);
            v.put("po_number", "");
            v.put("order_date", today());
            v.put("total", total);
            v.put("payment", PAYMENT_COD);
            v.put("address", address);
            v.put("status", STATUS_PENDING);
            v.put("eta", "");
            long id = d.insert("orders", null, v);
            ContentValues po = new ContentValues();
            po.put("po_number", String.format(Locale.US, "PO-%04d", id));
            d.update("orders", po, "id = ?", new String[]{String.valueOf(id)});
            for (String[] r : cart) {
                ContentValues i = new ContentValues();
                i.put("order_id", id);
                i.put("product_name", r[2]);
                i.put("price", Integer.parseInt(r[3]));
                i.put("qty", Integer.parseInt(r[4]));
                d.insert("order_items", null, i);
                d.execSQL("UPDATE products SET stock = MAX(stock - ?, 0) WHERE id = ?", new Object[]{r[4], r[1]});
            }
            d.delete("cart", "user_id = ?", new String[]{String.valueOf(USER_ID)});
            d.setTransactionSuccessful();
            return id;
        } finally {
            d.endTransaction();
        }
    }

    // Order row: {id, po_number, order_date, total, payment, address, status, eta}
    public static String[] getOrder(Context ctx, String id) {
        java.util.ArrayList<String[]> r = rows(db(ctx).rawQuery("SELECT id, po_number, order_date, total, "
                + "payment, address, status, eta FROM orders WHERE id = ?", new String[]{id}), 8);
        return r.get(0);
    }

    // Item row: {product_name, price, qty}
    public static java.util.ArrayList<String[]> orderItems(Context ctx, String orderId) {
        return rows(db(ctx).rawQuery("SELECT product_name, price, qty FROM order_items WHERE order_id = ? "
                + "ORDER BY id", new String[]{orderId}), 3);
    }

    public static String statusLabel(String status) {
        switch (status) {
            case STATUS_VERIFIED:
                return "Verified";
            case STATUS_PACKAGED:
                return "Packaged";
            case STATUS_ON_SHIPMENT:
                return "On shipment";
            default:
                return "Order placed, waiting for verification";
        }
    }

    // Order list row: {id, po_number, order_date, total, status, eta}
    public static java.util.ArrayList<String[]> ordersForUser(Context ctx) {
        return rows(db(ctx).rawQuery("SELECT id, po_number, order_date, total, status, eta FROM orders "
                + "WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(USER_ID)}), 6);
    }

    // Manager list row: {id, po_number, order_date, total, status, customer name}
    public static java.util.ArrayList<String[]> allOrders(Context ctx) {
        return rows(db(ctx).rawQuery("SELECT o.id, o.po_number, o.order_date, o.total, o.status, "
                + "u.first_name || ' ' || u.last_name FROM orders o LEFT JOIN users u ON u.id = o.user_id "
                + "ORDER BY o.id DESC", null), 6);
    }

    public static void setOrderStatus(Context ctx, String orderId, String status, String eta) {
        ContentValues v = new ContentValues();
        v.put("status", status);
        v.put("eta", STATUS_ON_SHIPMENT.equals(status) ? eta : "");
        db(ctx).update("orders", v, "id = ?", new String[]{orderId});
    }

    public static java.util.ArrayList<String[]> allProducts(Context ctx) {
        return searchProducts(ctx, "");
    }

    public static void insertProduct(Context ctx, String name, String desc, int price, int stock, String image) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", desc);
        v.put("price", price);
        v.put("stock", stock);
        v.put("image", image);
        db(ctx).insert("products", null, v);
    }

    public static void updateProduct(Context ctx, String id, String name, String desc, int price, int stock,
                                     String image) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", desc);
        v.put("price", price);
        v.put("stock", stock);
        v.put("image", image);
        db(ctx).update("products", v, "id = ?", new String[]{id});
    }

    public static void logout() {
        USER_ID = 0;
        USER_USERNAME = "";
        USER_ROLE = "";
        USER_LAST_NAME = "";
        USER_FIRST_NAME = "";
        USER_MIDDLE_INITIAL = "";
        USER_ADDRESS = "";
        USER_CONTACT = "";
        USER_ID_TYPE = "";
        USER_ID_IMAGE = "";
    }

    // Decodes with inSampleSize (longest side ~800px) and writes a JPEG into filesDir; returns the path.
    public static String saveImage(Context ctx, Uri uri) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, o);
            }
            int sample = 1;
            while (Math.max(o.outWidth, o.outHeight) / (sample * 2) >= 800) sample *= 2;
            o = new BitmapFactory.Options();
            o.inSampleSize = sample;
            Bitmap bmp;
            try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                bmp = BitmapFactory.decodeStream(in, null, o);
            }
            File f = new File(ctx.getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(f)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    // "res:<drawable>" is a bundled drawable, anything else is a file path; falls back to ph_id.
    public static void loadImage(ImageView iv, String ref) {
        Context ctx = iv.getContext();
        if (ref != null && ref.startsWith("res:")) {
            int id = ctx.getResources().getIdentifier(ref.substring(4), "drawable", ctx.getPackageName());
            if (id != 0) {
                iv.setImageResource(id);
                return;
            }
        } else if (ref != null && !ref.isEmpty() && new File(ref).exists()) {
            iv.setImageURI(Uri.fromFile(new File(ref)));
            return;
        }
        iv.setImageResource(ctx.getResources().getIdentifier("ph_id", "drawable", ctx.getPackageName()));
    }
}
