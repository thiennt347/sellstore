package com.sellproducts.thiennt.sellstore.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.R;

public class  CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener{

    public TextView txt_cart_name, txt_price;
    public ElegantNumberButton btnNumberCart;
    public ImageView cart_image;

    public RelativeLayout view_background;
    public LinearLayout view_foreGround;

    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(View itemView) {
        super(itemView);
        txt_cart_name = itemView.findViewById(R.id.cart_item_name);
        txt_price = itemView.findViewById(R.id.cart_item_price);
        btnNumberCart = itemView.findViewById(R.id.btnNumberCart);
        cart_image = itemView.findViewById(R.id.cart_image);
        view_background = itemView.findViewById(R.id.view_background);
        view_foreGround = itemView.findViewById(R.id.view_foreGround);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Mời Chọn");
        menu.add(0, 0, getAdapterPosition(), Common.DELETE);

    }
}