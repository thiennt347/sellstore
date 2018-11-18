package com.sellproducts.thiennt.sellstore;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.Heper.RecyclerItemTouchHelper;
import com.sellproducts.thiennt.sellstore.Interface.RecyclerItemTouchHelperListener;
import com.sellproducts.thiennt.sellstore.ViewHolder.FavoritesAdapter;
import com.sellproducts.thiennt.sellstore.ViewHolder.FavoritesViewHolder;
import com.sellproducts.thiennt.sellstore.model.Favorites;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FavoritesAdapter adapter;
    RelativeLayout root_Layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        root_Layout = (RelativeLayout)findViewById(R.id.root_Layout);
        recyclerView = findViewById(R.id.recycler_Favorites);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback itemTuchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTuchHelperCallback).attachToRecyclerView(recyclerView);

        loadFavorites();
    }

    private void loadFavorites() {
            adapter = new FavoritesAdapter(this, new Database(this).getFavorites(Common.currentUser.getPhone()));
            recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof FavoritesViewHolder)
        {
            String name = ((FavoritesAdapter)recyclerView.getAdapter()).getitem(position).getProductName();
            final Favorites deleteItem = ((FavoritesAdapter)recyclerView.getAdapter()).getitem(viewHolder.getAdapterPosition());
            final int deleteIndex =  viewHolder.getAdapterPosition();
            adapter.removeItem(viewHolder.getAdapterPosition());

            new Database(getBaseContext()).DeletrFavorites(deleteItem.getProductId(), Common.currentUser.getPhone());

            Snackbar snackbar = Snackbar.make(root_Layout, name+"Đã Xoá", Snackbar.LENGTH_SHORT);
            snackbar.setAction("Hoàn Tác", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addFavorites(deleteItem);

                }
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
