package com.sellproducts.thiennt.sellstore.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.Product_detail;
import com.sellproducts.thiennt.sellstore.R;
import com.sellproducts.thiennt.sellstore.model.Favorites;
import com.sellproducts.thiennt.sellstore.model.Order;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder>{

    private Context context;
    private List<Favorites> favoriteslist;

    public FavoritesAdapter(Context context, List<Favorites> favoriteslist) {
        this.context = context;
        this.favoriteslist = favoriteslist;
    }



    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.favorites_item, parent, false);
        return new FavoritesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder viewHolder, final int position) {
        viewHolder.product_name.setText(favoriteslist.get(position).getProductName());
        viewHolder.Products_price.setText(favoriteslist.get(position).getProductPrice().toString()+" đ");
        Picasso.with(context)
                .load(favoriteslist.get(position).getProductImage())
                .into(viewHolder.product_image);




        viewHolder.btnCart_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isExits = new Database(context).checkProductExit(favoriteslist.get(position).getProductId(),
                        Common.currentUser.getPhone());
                if(!isExits) {
                    new Database(context).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            favoriteslist.get(position).getProductId(),
                            favoriteslist.get(position).getProductName(),
                            "1",
                            favoriteslist.get(position).getProductPrice(),
                            favoriteslist.get(position).getProductDiscount(),
                            favoriteslist.get(position).getProductImage()

                    ));
                }
                else
                {
                    new Database(context).increaseCart(Common.currentUser.getPhone(), favoriteslist.get(position).getProductId());
                }

                Toast.makeText(context , "Thêm Thành Công", Toast.LENGTH_SHORT).show();
            }
        });

        final Favorites local = favoriteslist.get(position);
        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClik) {
                //Start New Activity
                Intent foodDetail = new Intent(context, Product_detail.class);
                foodDetail.putExtra("ProducId", favoriteslist.get(position).getProductId()); //Send product Id to new activity
                 context.startActivity(foodDetail);

            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteslist.size();
    }

    public  void removeItem(int position)
    {
        favoriteslist.remove(position);
        notifyItemRemoved(position);

    }

    public  void restoreItem(Favorites item,int position)
    {
        favoriteslist.add(position, item);
        notifyItemInserted(position);

    }

    public Favorites getitem(int position)
    {
        return  favoriteslist.get(position);
    }

}
