package com.sellproducts.thiennt.sellstore;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.Interface.ItemClickListener;
import com.sellproducts.thiennt.sellstore.ViewHolder.MenuViewHolder;
import com.sellproducts.thiennt.sellstore.model.Banner;
import com.sellproducts.thiennt.sellstore.model.Category;
import com.sellproducts.thiennt.sellstore.model.Token;
import com.sellproducts.thiennt.sellstore.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName, menu_name, txtEmail, txthomeAddress;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout ;

    CounterFab fab;

    HashMap<String, String> image_list;
    SliderLayout mSliderLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
                );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(Common.isConnectedInternet(getBaseContext()))
                {
                    loadMenu();
                }
                else
                {
                    Toast.makeText(Home.this, "Làm ơn Kiểm Tra Kết Nối Mạng", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });




        //default load
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if(Common.isConnectedInternet(getBaseContext()))
                {
                    loadMenu();
                }
                else
                {
                    Toast.makeText(Home.this, "Làm ơn Kiểm Tra Kết Nối Mạng", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        if (Common.currentUser != null){
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");


        Paper.init(this);


        //cart

        fab = (CounterFab) findViewById(R.id.fab);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Common.currentUser == null)
                    {
                        showDialogDangNhap();
                    }
                    else {
                        Intent cartIntent = new Intent(Home.this, Cart.class);
                        startActivity(cartIntent);
                    }
                }
            });

        if (Common.currentUser != null) {
            fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set Name For User
        View headerView = navigationView.getHeaderView(0);
        navigationView.getBackground().setColorFilter(0x80000000, PorterDuff.Mode.MULTIPLY);
        txtFullName = headerView.findViewById(R.id.txtFullName);
        txtEmail = headerView.findViewById(R.id.txtEmail);
        txthomeAddress = headerView.findViewById(R.id.txthomeAdress);



        if (Common.currentUser != null) {
            txtFullName.setText(Common.currentUser.getName());
            txtEmail.setText(Common.currentUser.getEmail());
            txthomeAddress.setText(Common.currentUser.getHomeAddress());
        }

        recycler_menu = findViewById(R.id.recycle_menu);
        recycler_menu.setHasFixedSize(true);
        recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));

        setupSlider();
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false); //fasle, vi token gui tu clien app
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void setupSlider() {
        mSliderLayout = (SliderLayout)findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Banner");
         banners.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                 for(DataSnapshot posSnapshot:dataSnapshot.getChildren())
                 {
                     //we will concat String name and id like
                     // product_01 and we will product for show desciption 01, for product 01
                     Banner banner = posSnapshot.getValue(Banner.class);
                     image_list.put(banner.getName()+"@@@" + banner.getId(),banner.getImage());
                 }
                 for(String key:image_list.keySet())
                 {
                    String[] keySplit =  key.split("@@@");
                    String nameofProduct = keySplit[0];
                    String idofProduct = keySplit[1];

                    //create slider
                     final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                     textSliderView
                             .description(nameofProduct)
                             .image(image_list.get(key))
                             .setScaleType(BaseSliderView.ScaleType.Fit)
                             .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                 @Override
                                 public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this, Product_detail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                 }
                             });

                     textSliderView.bundle(new Bundle());
                     textSliderView.getBundle().putString("ProducId", idofProduct);

                     mSliderLayout.addSlider(textSliderView);
                     banners.removeEventListener(this);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

         mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
         mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
         mSliderLayout.setCustomAnimation(new DescriptionAnimation());
         mSliderLayout.setDuration(4000);
    }

    private void loadMenu() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {
                        //Get CategoryId and Send to new activity
                        Intent intent = new Intent(Home.this, Products_list.class);

                        intent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(intent);
                       // Toast.makeText(Home.this, ""+clickItem.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View iteamView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(iteamView);
            }
        };
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSliderLayout.stopAutoCycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
        if (Common.currentUser != null) {
            fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        }


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nav_search) {
            startActivity(new Intent(Home.this, SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            startActivity(new Intent());
        } else if (id == R.id.nav_HomeAddress) {
            if(Common.currentUser != null) {
                ShowUpdateIfUser();
            }
            else {
                showDialogDangNhap();
            }

        } else if (id == R.id.nav_cart) {

            Intent intentCart = new Intent(Home.this, Cart.class) ;
            startActivity(intentCart);
        } else if (id == R.id.nav_Oder) {


            if(Common.currentUser != null)
            {
                Intent intentOrder = new Intent(Home.this, OrderStatus.class) ;
                startActivity(intentOrder);
            }
            else
            {
                showDialogDangNhap();
            }

        } else if (id == R.id.naw_Log_out) {
            AccountKit.logOut();

            Intent intenLogOut = new Intent(Home.this, HelloActivity.class) ;
            intenLogOut.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Common.currentUser = null;
            startActivity(intenLogOut);

        }
        else if (id == R.id.nav_Favorites) {
            if(Common.currentUser != null)
            {
                Intent intentFavorites = new Intent(Home.this, FavoritesActivity.class);
                startActivity(intentFavorites);
            }
            else
            {
                showDialogDangNhap();
            }

        }
        else if (id == R.id.nav_setting) {

            showSettingDialog();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingDialog() {
        AlertDialog.Builder alerdialog = new AlertDialog.Builder(Home.this);

        alerdialog.setTitle("Cài Đặt");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_setting = inflater.inflate(R.layout.setting_layout, null);

        final CheckBox ckb_sub_new = (CheckBox)layout_setting.findViewById(R.id.ckb_sub_new);
        alerdialog.setView(layout_setting);

        //remember state of checkbox
        Paper.init(this);
        String isSubcribe = Paper.book().read("sub_new");
        if(isSubcribe == null || TextUtils.isEmpty(isSubcribe) || isSubcribe.equals("false")) {
            ckb_sub_new.setChecked(false);
        }
        else {
            ckb_sub_new.setChecked(true);
        }

        alerdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if(ckb_sub_new.isChecked())
                {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);

                    //wirte value
                    Paper.book().write("sub_new", "true");
                }
                else
                {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);

                    //wirte value
                    Paper.book().write("sub_new", "false");
                }
            }
        });
        alerdialog.show();

    }

    private void ShowUpdateIfUser() {
        AlertDialog.Builder alerdialog = new AlertDialog.Builder(Home.this);

        alerdialog.setTitle("Cập Nhật Thông Tin Tài Khoản");
        alerdialog.setMessage("Hãy Điền Đầy Đủ Thông Tin");
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.home_address_layout, null);
        final MaterialEditText edtHomeAddress = (MaterialEditText)layout_home.findViewById(R.id.edtHomeAddress);
        final MaterialEditText edtEmail = (MaterialEditText)layout_home.findViewById(R.id.edtEmail);
        final MaterialEditText edtName = (MaterialEditText)layout_home.findViewById(R.id.edtName);
        alerdialog.setView(layout_home);
        alerdialog.setPositiveButton("Cập Nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //set new home address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());
                Common.currentUser.setName(edtName.getText().toString());
                Common.currentUser.setEmail(edtEmail.getText().toString());
                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Cập Nhật Thông Tin Thành Công", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        alerdialog.show();
    }

    private  void showDialogDangNhap()
    {
        AlertDialog.Builder alerdialog = new AlertDialog.Builder(Home.this);
        alerdialog.setTitle("Vui Lòng Đăng Nhập");

        //set button
        alerdialog.setPositiveButton("Đồng Ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Intent intent = new Intent(Home.this, MainActivity.class);
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
