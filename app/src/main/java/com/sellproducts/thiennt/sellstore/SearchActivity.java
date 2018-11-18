package com.sellproducts.thiennt.sellstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.ViewHolder.ProductsViewHolder;
import com.sellproducts.thiennt.sellstore.model.Favorites;
import com.sellproducts.thiennt.sellstore.model.Order;
import com.sellproducts.thiennt.sellstore.model.Product;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    //Serach Functionality
    FirebaseRecyclerAdapter<Product,ProductsViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference productlist;

    FirebaseRecyclerAdapter<Product,ProductsViewHolder> adapter;

    //favorite
    Database localDB;

    //facebook share
    CallbackManager callbackManager ;
    ShareDialog shareDialog ;

    //create target from picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //create photo from bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            if(ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //intit facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Firebase Init
        database = FirebaseDatabase.getInstance();
        productlist = database.getReference("Products");

        recyclerView = findViewById(R.id.recycler_seach);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //local db
        localDB = new Database(this);


        //boi vi chuc nang tim kiem can category
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Nhập Sản Phẩm..");
        loadSuggest();// funtion load suggest from firebase
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //khi nguoi dung thay doi  text, chung ta se thay doi suggest
                List<String> suggest = new ArrayList<String>();
                for (String search:suggestList)
                {  // looping di suggestLIst
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                    {
                        suggest.add(search);
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // khi nguoi dung khong tim kim nua
                //restore no
                if (!enabled)
                {
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //khi nguoi dung tim kiem ket thuc
                //show cac product
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        loadAllproduct();
    }

    private void startSearch(CharSequence text) {
        Query searchByname  = productlist.orderByChild("name").equalTo(text.toString());

        FirebaseRecyclerOptions<Product> SearchOptions = new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(searchByname, Product.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Product, ProductsViewHolder>(SearchOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ProductsViewHolder viewHolder, int position, @NonNull Product model) {

                viewHolder.product_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.product_image);

                final Product local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Start New Activity
                        Intent foodDetail = new Intent(SearchActivity.this, Product_detail.class);
                        foodDetail.putExtra("ProducId", searchAdapter.getRef(position).getKey()); //Send product Id to new activity
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View iteamView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.products_item, parent, false);
                return new ProductsViewHolder(iteamView);
            }
        };
        searchAdapter.startListening();
        searchAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadAllproduct() {
        Query searchByname = productlist;
        FirebaseRecyclerOptions<Product> ProductOptions = new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(searchByname, Product.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Product, ProductsViewHolder>(ProductOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ProductsViewHolder viewHolder, final int position, @NonNull final Product model) {

                viewHolder.product_name.setText(model.getName());
                viewHolder.Products_price.setText(model.getPrice().toString()+" đ");
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.product_image);

                //add Favories
                if(localDB.selectFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                {
                    viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_black_24dp);
                }

                //click to quick cart
                viewHolder.btnCart_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Database(getBaseContext()).addToCart(new Order(
                                Common.currentUser.getPhone(),
                                adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()

                        ));

                        Toast.makeText(SearchActivity.this, "Thêm Thành Công", Toast.LENGTH_SHORT).show();
                    }
                });

                //click to share facebook

                viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });
                //click change status Favorites
                viewHolder.imgFav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setProductId(adapter.getRef(position).getKey());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setProductPrice(model.getPrice());
                        favorites.setProductMenuId(model.getMenuId());
                        favorites.setProductImage(model.getImage());
                        favorites.setProductName(model.getName());
                        favorites.setProductDiscount(model.getDiscount());
                        favorites.setProductDescription(model.getDescription());

                        if(!localDB.selectFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                        {
                            localDB.addFavorites(favorites);
                            viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(SearchActivity.this, ""+model.getName() + "Đã Được Thêm Vào Mục Yêu Thích", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            localDB.DeletrFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(SearchActivity.this, ""+model.getName() + "Đã Xoá Khỏi Mục Yêu Thích", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                final  Product local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Start New Activity
                        Intent foodDetail = new Intent(SearchActivity.this, Product_detail.class);
                        foodDetail.putExtra("ProducId", adapter.getRef(position).getKey()); //Send product Id to new activity
                        startActivity(foodDetail);

                    }
                });
            }

            @NonNull
            @Override
            public ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View iteamView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.products_item, parent, false);
                return new ProductsViewHolder(iteamView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void loadSuggest() {
        productlist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Product item = postSnapshot.getValue(Product.class);
                    suggestList.add(item.getName());
                }
                materialSearchBar.setLastSuggestions(suggestList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        if(adapter != null)
        {
            adapter.stopListening();
        }
        if(searchAdapter != null)
        {
            searchAdapter.stopListening();
        }
        super.onStop();
    }
}

