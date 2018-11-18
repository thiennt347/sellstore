package com.sellproducts.thiennt.sellstore.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.R;

public class ProductsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView product_name, Products_price;
    public ImageView product_image, imgFav, btnShare, btnCart_item;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ProductsViewHolder(View itemView) {
        super(itemView);

        product_name = (TextView) itemView.findViewById(R.id.Products_name);
        Products_price = (TextView) itemView.findViewById(R.id.Products_price);
        product_image = (ImageView)itemView.findViewById(R.id.Products_image);
        imgFav = (ImageView) itemView.findViewById(R.id.imgFav);
        btnShare = (ImageView)itemView.findViewById(R.id.btnShare);
        btnCart_item = (ImageView)itemView.findViewById(R.id.btnCart_item);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
