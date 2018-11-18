package com.sellproducts.thiennt.sellstore;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.Heper.RecyclerItemTouchHelper;
import com.sellproducts.thiennt.sellstore.Interface.RecyclerItemTouchHelperListener;
import com.sellproducts.thiennt.sellstore.Remote.APIService;
import com.sellproducts.thiennt.sellstore.ViewHolder.CartAdapter;
import com.sellproducts.thiennt.sellstore.ViewHolder.CartViewHolder;
import com.sellproducts.thiennt.sellstore.model.DataMessage;
import com.sellproducts.thiennt.sellstore.model.MyResponse;
import com.sellproducts.thiennt.sellstore.model.Report;
import com.sellproducts.thiennt.sellstore.model.Request;
import com.sellproducts.thiennt.sellstore.model.Order;
import com.sellproducts.thiennt.sellstore.model.Token;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements  RecyclerItemTouchHelperListener {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;
    DatabaseReference reports;

    public TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    Place shipAdress;

    RelativeLayout rootLayout;

    private LocationRequest mlocationRequest;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FATEST_INTERVAL = 5000;
    private static final int DISPACEMENT = 5000;
    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;



    String address;

    //
    APIService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init service
        mService = Common.getFCMservice();
        setContentView(R.layout.activity_cart);



        //runtime permisstion
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },LOCATION_REQUEST_CODE);
        }
        else
        {
            if(checkPlayService())//neu co play service tren thiet bi
            {
                createLocationRequest();
            }
        }
        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        reports = database.getReference("Reports");

        //init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTuchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTuchHelperCallback).attachToRecyclerView(recyclerView);


        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cart.size() > 0)
                {
                    showAlertDialog();
                }
                else
                {
                    Toast.makeText(Cart.this, "Giỏ Hàng Của Bạn Rỗng!!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        loadListProduct();
    }

    private void createLocationRequest() {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FATEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPACEMENT);
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            }
            else
            {
                Toast.makeText(this, "Thiết Bị Này Không Hỗ Trợ", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }



    private void  showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Thêm Một Bước Nữa !");
        alertDialog.setMessage("Nhập Địa Chỉ Của Bạn: ");

        LayoutInflater inflater  = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_commet, null);



       // final MaterialEditText edtAddress = (MaterialEditText) order_address_comment.findViewById(R.id.edtAddress);

        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        //set hint for autocomplete edt

        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Nhập Địa Chỉ Của Bạn");
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        final RadioButton rdbHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdbHomeAddress);
        final RadioButton rbdCOD = (RadioButton) order_address_comment.findViewById(R.id.rbdCOD);

        //home address
        rdbHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    if(!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) || Common.currentUser.getHomeAddress() != null)
                    {

                        address = Common.currentUser.getHomeAddress();
                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);
                    }
                    else
                    {
                        Toast.makeText(Cart.this, "Hãy Cập Nhật Địa Chỉ Nhà của Bạn", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        //get address from place autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shipAdress = place;

            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR", status.getStatusMessage());
            }
        });

        final MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);



        alertDialog.setView(order_address_comment);

        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Đồng Ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                //add check condition
                // if user select address from fragment, just use it
                //if user select home address, get home address and use it
                if(shipAdress != null)
                {
                    address = shipAdress.getAddress().toString();
                }
                else {
                    Toast.makeText(Cart.this, "Hãy Nhập địa chỉ Hoặc Tuỳ Chọn Địa Chỉ Của Bạn", Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;
                }
                if (TextUtils.isEmpty(address))
                {
                    Toast.makeText(Cart.this, "Hãy Nhập địa Chỉ ", Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;
                }


                if(!rbdCOD.isChecked())
                {
                    Toast.makeText(Cart.this, "Hãy Chọn Phương Thức Thanh Toán", Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;
                }
                else if(rbdCOD.isChecked())
                {
                    // Create new Request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",//status
                            edtComment.getText().toString(),
                            "Nhận Hàng Tại Nhà",
                            "Chưa Thanh Toán",
                            String.format("%s, %s", shipAdress.getLatLng().latitude, shipAdress.getLatLng().longitude),
                            cart
                    );
                    Report report = new Report(Common.currentUser.getPhone(),
                            address,
                            txtTotalPrice.getText().toString());



                    // Submit ke Firebase
                    // We Will using System.CurrentMilli to Key
                    String order_number = String.valueOf(System.currentTimeMillis());
                    reports.child(order_number)
                            .setValue(report);

                    requests.child(order_number)
                            .setValue(request);
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    senNotificationOrder(order_number);
                    
                }
            }
        });

        alertDialog.setNegativeButton("Huỷ Bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDialog.show();
    }

    private void senNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        final Query data= tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Token svtoken = postSnapShot.getValue(Token.class);
                    //
                    Map<String, String> dataSend = new HashMap<>();

                    dataSend.put("tilte", "SellStore");
                    dataSend.put("message", "Bạn có Đơn Hàng Mới"+ order_number+"");
                    DataMessage dataMessage = new DataMessage(svtoken.getToken(), dataSend);

                    String test = new Gson().toJson(dataMessage);
                    Log.d("Content", test);

                    mService.sendthongbao(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "ĐƠN HÀNG ĐÃ ĐƯỢC ĐẶT", Toast.LENGTH_SHORT).show();
                                            finish();

                                        } else {
                                            Toast.makeText(Cart.this, "Không Thành Công", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });
    }

    private void loadListProduct() {
        if(Common.currentUser == null)
        {
            Toast.makeText(this, "Hãy Đăng Nhập Để Sử Dụng Chức Năng Này", Toast.LENGTH_SHORT).show();
        }
        else
        {
            cart = new Database(this).getCarts(Common.currentUser.getPhone());
            adapter = new CartAdapter(cart, this);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);

            // total
            int total = 0;
            for(Order order:cart)
                total+= (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
            Locale locale = new Locale("vi","VN");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
        {
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int order) {
        cart.remove(order);

        new Database(this).cleanCart(Common.currentUser.getPhone());

        for (Order item:cart)
        {
            new Database(this).addToCart(item);
        }
        loadListProduct();

    }




    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if(viewHolder instanceof CartViewHolder)
        {
           String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

           final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());

           final int deleteIndex = viewHolder.getAdapterPosition();

           adapter.removeItem(deleteIndex);
           new Database(getBaseContext()).DeleteCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            //update total
            // total
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for(Order item:orders)
            {
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            }
            Locale locale = new Locale("vi","VN");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));


            //make snakebar

            Snackbar snackbar = Snackbar.make(rootLayout, name+" Đã Xoá ", Snackbar.LENGTH_SHORT);
            snackbar.setAction("Hoàn Tác", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //update total
                    // total
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for(Order item:orders)
                    {
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    }
                    Locale locale = new Locale("vi","VN");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText(fmt.format(total));
                }
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
