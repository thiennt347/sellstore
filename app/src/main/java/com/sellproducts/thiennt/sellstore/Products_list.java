package com.sellproducts.thiennt.sellstore;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.sellproducts.thiennt.sellstore.ViewHolder.MenuViewHolder;
import com.sellproducts.thiennt.sellstore.ViewHolder.ProductsViewHolder;
import com.sellproducts.thiennt.sellstore.model.Category;
import com.sellproducts.thiennt.sellstore.model.Favorites;
import com.sellproducts.thiennt.sellstore.model.Order;
import com.sellproducts.thiennt.sellstore.model.Product;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Products_list extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference productlist;

    String categoryId = "";
    //favorite
    Database localDB;

    //facebook share
    CallbackManager callbackManager ;
    ShareDialog shareDialog ;

    //Swipe

    SwipeRefreshLayout swipeRefreshLayout ;

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

    FirebaseRecyclerAdapter<Product,ProductsViewHolder> adapter;

   //Serach Functionality
    FirebaseRecyclerAdapter<Product,ProductsViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);

        //intit facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Firebase Init
        database = FirebaseDatabase.getInstance();
        productlist = database.getReference("Products");

        //local db
        localDB = new Database(this);

        recyclerView = findViewById(R.id.recycler_products);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        //Swipe

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //Get Intent Here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null){

                    //check connect
                    if(Common.isConnectedInternet(getBaseContext())) {
                        loadListProduct(categoryId);
                    }
                    else
                    {
                        Toast.makeText(Products_list.this, "Hãy Kiểm Tra Kết Nối Mạng", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });


        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //Get Intent Here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null){

                    //check connect
                    if(Common.isConnectedInternet(getBaseContext())) {
                        loadListProduct(categoryId);
                    }
                    else
                    {
                        Toast.makeText(Products_list.this, "Hãy Kiểm Tra Kết Nối Mạng", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

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
            }
        });



    }

    private void loadListProduct(String categoryId) {
        Query searchByname = productlist.orderByChild("menuId").equalTo(categoryId);
        FirebaseRecyclerOptions<Product> ProductOptions = new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(searchByname, Product.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Product, ProductsViewHolder>(ProductOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ProductsViewHolder viewHolder, final int position, @NonNull final Product model) {

                viewHolder.product_name.setText(model.getName());
                int gia = Integer.parseInt(model.getPrice());
                Locale locale = new Locale("vi","VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                 viewHolder.Products_price.setText(fmt.format(gia));
                 Picasso.with(getBaseContext())
                         .load(model.getImage())
                         .into(viewHolder.product_image);

                 //add Favories


                if (Common.currentUser != null) {
                    if(localDB.selectFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    {
                        viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_black_24dp);
                    }
                }

                 //click to quick cart
                    viewHolder.btnCart_item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(Common.currentUser == null)
                            {
                                showDialogDangNhap();
                            }
                            else
                            {
                                boolean isExits = new Database(getBaseContext()).checkProductExit(adapter.getRef(position).getKey(),
                                        Common.currentUser.getPhone());
                                if(!isExits) {
                                    new Database(getBaseContext()).addToCart(new Order(
                                            Common.currentUser.getPhone(),
                                            adapter.getRef(position).getKey(),
                                            model.getName(),
                                            "1",
                                            model.getPrice(),
                                            model.getDiscount(),
                                            model.getImage()

                                    ));
                                }
                                else
                                {
                                    new  Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());
                                }

                                Toast.makeText(Products_list.this, "Thêm Thành Công", Toast.LENGTH_SHORT).show();
                            }


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

                if (Common.currentUser != null) {
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
                                    Toast.makeText(Products_list.this, ""+model.getName() + "Đã Được Thêm Vào Mục Yêu Thích", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    localDB.DeletrFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                                    viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                    Toast.makeText(Products_list.this, ""+model.getName() + "Đã Xoá Khỏi Mục Yêu Thích", Toast.LENGTH_SHORT).show();
                                }
                            }

                    });
                }


                 final  Product local = model;
                 viewHolder.setItemClickListener(new ItemClickListener() {
                     @Override
                     public void onClick(View view, int position, boolean isLongClik) {
                         //Start New Activity
                         Intent foodDetail = new Intent(Products_list.this, Product_detail.class);
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
         swipeRefreshLayout.setRefreshing(false);
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
                Log.d("TAG", ""+adapter.getItemCount());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.product_image);

                final Product local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Start New Activity
                        Intent foodDetail = new Intent(Products_list.this, Product_detail.class);
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

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        //searchAdapter.stopListening();
    }

    private void loadSuggest() {
        productlist.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
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
    protected void onResume() {
        super.onResume();
        if(adapter != null)
        {
            adapter.startListening();
        }


    }

    private  void showDialogDangNhap()
    {
        AlertDialog.Builder alerdialog = new AlertDialog.Builder(Products_list.this);
        alerdialog.setTitle("Vui Lòng Đăng Nhập");

        //set button
        alerdialog.setPositiveButton("Đồng Ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Intent intent = new Intent(Products_list.this, MainActivity.class);
                startActivity(intent);
            }
        });

        alerdialog.setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alerdialog.show();
    }
}
