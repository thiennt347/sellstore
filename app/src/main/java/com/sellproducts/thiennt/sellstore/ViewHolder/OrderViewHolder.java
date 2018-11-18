package com.sellproducts.thiennt.sellstore.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{
    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddres;
    private ItemClickListener itemClickListener;
    public ImageView btn_delete;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderAddres = (TextView) itemView.findViewById(R.id.order_addres);
        txtOrderId = (TextView) itemView.findViewById(R.id.order_id);
        txtOrderStatus = (TextView) itemView.findViewById(R.id.order_status);
        txtOrderPhone = (TextView) itemView.findViewById(R.id.order_phone);
        btn_delete = (ImageView) itemView.findViewById(R.id.btn_delete);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
