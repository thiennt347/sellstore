package com.sellproducts.thiennt.sellstore.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.R;

public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView product_name, Products_price;
    public ImageView product_image,btnCart_item;

    private ItemClickListener itemClickListener;

    public RelativeLayout view_background;
    public LinearLayout view_foreGround;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FavoritesViewHolder(View itemView) {
        super(itemView);

        product_name = (TextView) itemView.findViewById(R.id.Products_name);
        Products_price = (TextView) itemView.findViewById(R.id.Products_price);
        product_image = (ImageView)itemView.findViewById(R.id.Products_image);
        btnCart_item = (ImageView)itemView.findViewById(R.id.btnCart_item);

        view_background = itemView.findViewById(R.id.view_background);
        view_foreGround = itemView.findViewById(R.id.view_foreGround);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}

