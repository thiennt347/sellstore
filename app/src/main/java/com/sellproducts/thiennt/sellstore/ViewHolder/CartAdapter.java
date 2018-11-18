package com.sellproducts.thiennt.sellstore.ViewHolder;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.sellproducts.thiennt.sellstore.Cart;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.R;
import com.sellproducts.thiennt.sellstore.model.Order;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {
    private List<Order> listData = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView = inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {

        Picasso.with(cart.getBaseContext())
                .load(listData.get(position).getImage())
                .resize(70, 70)
                .centerCrop()
                .into(holder.cart_image);

        holder.btnNumberCart.setNumber(listData.get(position).getQuantity());
        holder.btnNumberCart.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //update total
                // total
                int total = 0;
                List<Order> orders = new Database(cart).getCarts(Common.currentUser.getPhone());
                for(Order item:orders)
                {
                    total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(item.getQuantity()));
                }
                Locale locale = new Locale("vi","VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                cart.txtTotalPrice.setText(fmt.format(total));
            }
        });

        Locale locale = new Locale("vi","VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        int price = (Integer.parseInt(listData.get(position).getPrice())) * (Integer.parseInt(listData.get(position).getQuantity()));
        holder.txt_price.setText(fmt.format(price));

        holder.txt_cart_name.setText(listData.get(position).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }


    public Order getItem(int position)
    {
        return listData.get(position);
    }

    public  void removeItem(int position)
    {
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public  void restoreItem(Order item,int position)
    {
        listData.add(position, item);
        notifyItemInserted(position);
    }
}