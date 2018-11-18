package com.sellproducts.thiennt.sellstore.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.sellproducts.thiennt.sellstore.model.Favorites;
import com.sellproducts.thiennt.sellstore.model.Order;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {
    private static final String DB_NAME="SellAnythingDb.db";
    private static final int DB_VER=2;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public List<Order> getCarts(String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone","ProductName", "ProductId","Quantity", "Price", "Discount", "Image"};
        String sqlTable = "Orderdetail";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db,sqlSelect, "UserPhone=?",new String[]{userPhone},null,null,null);

        final  List<Order> result = new ArrayList<>();
        if (c.moveToFirst()){
            do {
                result.add(new Order(
                        c.getString(c.getColumnIndex("UserPhone")),
                        c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantity")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Discount")),
                        c.getString(c.getColumnIndex("Image"))
                ));
            }while (c.moveToNext());
        }
        return result;
    }

    public List<Favorites> getFavorites(String userPhone)   {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ProductId", "ProductName", "ProductPrice","ProductMenuId", "ProductImage", "ProductDiscount", "ProductDescription", "UserPhone"};
        String sqlTable = "Favorites";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db,sqlSelect, "UserPhone=?",new String[]{userPhone},null,null,null);

        final  List<Favorites> result = new ArrayList<>();
        if (c.moveToFirst()){
            do {
                result.add(new Favorites(
                        c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("UserPhone")),
                        c.getString(c.getColumnIndex("ProductPrice")),
                        c.getString(c.getColumnIndex("ProductMenuId")),
                        c.getString(c.getColumnIndex("ProductImage")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("ProductDiscount")),
                        c.getString(c.getColumnIndex("ProductDescription"))
                ));

            }while (c.moveToNext());
        }
        return result;
    }


    public  Boolean checkProductExit(String ProductId, String UserPhone)
    {
        boolean flag = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQL = String.format("SELECT * FROM Orderdetail WHERE UserPhone = '%s' AND ProductId = '%s' ", UserPhone, ProductId);
        cursor = db.rawQuery(SQL, null);
        if(cursor.getCount() > 0)
        {
           flag = true;
        }
        else
        {
            flag = false;
        }
        cursor.close();
        return  flag;
    }

    public void addToCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT OR REPLACE INTO Orderdetail(UserPhone, ProductId,ProductName,Quantity,Price,Discount, Image) VALUES ('%s','%s','%s','%s','%s','%s', '%s');",
                order.getUserPhone(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage());
        db.execSQL(query);
    }

    public void cleanCart(String UserPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE UserPhone = '%s'", UserPhone);
        db.execSQL(query);
    }


    public void DeleteCart(String ProductId, String UserPhone) {

        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE ProductId = '%s' AND UserPhone = '%S'", ProductId, UserPhone);
        db.execSQL(query);
    }

    public int getCountCart(String UserPhone) {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT COUNT(*) FROM OrderDetail WHERE UserPhone = '%s'", UserPhone);
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst())
        {
            do {
                count = cursor.getInt(0);

            }while (cursor.moveToNext());
        }
        return count;
    }

    public void updateCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity = '%s' WHERE UserPhone = '%s' AND ProductId = '%s'", order.getQuantity(), order.getUserPhone(), order.getProductId());
        db.execSQL(query);
    }

    public void increaseCart(String UserPhone, String ProductId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity = Quantity+1 WHERE UserPhone = '%s' AND ProductId = '%s'",UserPhone, ProductId);
    }

    public void addFavorites(Favorites product)
    {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(ProductId, UserPhone, ProductPrice,ProductMenuId, ProductImage,ProductName, ProductDiscount, ProductDescription) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                product.getProductId(), product.getUserPhone(), product.getProductPrice(), product.getProductMenuId(), product.getProductImage(), product.getProductName(), product.getProductDiscount(),product.getProductDescription());
        db.execSQL(query);

    }
    public void DeletrFavorites(String productid, String UserPhone)
    {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE ProductId='%s' and UserPhone = '%s';", productid, UserPhone );
        db.execSQL(query);

    }
    public boolean selectFavorites(String productid, String UserPhone)
    {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE ProductId='%s' and UserPhone = '%s';", productid , UserPhone);
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0)
        {
            cursor.close();
            return false;
        }
        cursor.close();
        return  true;

    }
}

