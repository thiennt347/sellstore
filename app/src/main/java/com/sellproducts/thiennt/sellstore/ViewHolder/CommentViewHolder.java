package com.sellproducts.thiennt.sellstore.ViewHolder;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.sellproducts.thiennt.sellstore.R;

public class CommentViewHolder extends RecyclerView.ViewHolder{

    public TextView txtUserPhone, txtComment, txdate;
    public RatingBar ratingBar;
    public CommentViewHolder(View itemView) {
        super(itemView);
        txtUserPhone = (TextView) itemView.findViewById(R.id.txtUserPhone);
        txtComment = (TextView) itemView.findViewById(R.id.txtComment);
        ratingBar = (RatingBar) itemView.findViewById(R.id.rangtingBar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
    }
}
