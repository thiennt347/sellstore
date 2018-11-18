package com.sellproducts.thiennt.sellstore;

import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.model.Order;
import com.sellproducts.thiennt.sellstore.model.Product;
import com.sellproducts.thiennt.sellstore.model.Rating;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import info.hoang8f.widget.FButton;

public class Product_detail extends AppCompatActivity implements RatingDialogListener {

    TextView product_name, product_price, product_description;
    ImageView product_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    ElegantNumberButton numberButton;
    CounterFab btnCart;

    RatingBar rangtingBar;

    String productId="";

    FirebaseDatabase database;
    DatabaseReference product;
    DatabaseReference ratingtbl;
    Product currentProduct;

    FButton btnShowComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        //Firbase
        database = FirebaseDatabase.getInstance();
        product = database.getReference("Products");
        ratingtbl = database.getReference("Rating");

         //Init view
        numberButton =(ElegantNumberButton) findViewById(R.id.number_button);
        btnCart =(CounterFab) findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton) findViewById(R.id.btnRating);
        btnShowComment = findViewById(R.id.btnShowComment);
        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Product_detail.this, ShowCommemt.class);
                intent.putExtra(Common.INTEN_PRODUCT_ID, productId);
                startActivity(intent);
            }
        });
        rangtingBar = (RatingBar) findViewById(R.id.rangtingBar);
        LayerDrawable stars = (LayerDrawable) rangtingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });


            btnCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(Common.currentUser == null)
                    {
                        showDialogDangNhap();
                    }
                    else
                    {
                        new Database(getBaseContext()).addToCart(new Order(
                                Common.currentUser.getPhone(),
                                productId,
                                currentProduct.getName(),
                                numberButton.getNumber(),
                                currentProduct.getPrice(),
                                currentProduct.getDiscount(),
                                currentProduct.getImage()

                        ));

                        Toast.makeText(Product_detail.this, "Thêm Thành Công", Toast.LENGTH_SHORT).show();
                    }

                }
            });


        if (Common.currentUser != null) {
            btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        }


        product_description = (TextView)findViewById(R.id.product_description);
        product_name = (TextView)findViewById(R.id.product_name);
        product_price =(TextView) findViewById(R.id.product_price);
        product_image = (ImageView) findViewById(R.id.img_product);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapseAppbar);

        //Get Food Id From Internet
        if (getIntent() != null)
            productId = getIntent().getStringExtra("ProducId");
        if (!productId.isEmpty()){

            //check connect
            if(Common.isConnectedInternet(getBaseContext()))
            {
                getDetailFood(productId);
                getRating(productId);
            }
            else
            {
                Toast.makeText(Product_detail.this, "Hãy Kiểm Tra Kết Nối Mạng", Toast.LENGTH_SHORT).show();
                return;
            }

        }

    }

    private void getRating(String productId) {
       Query productRating  = ratingtbl.orderByChild("productId").equalTo(productId);

       productRating.addValueEventListener(new ValueEventListener() {

           int count = 0, sum = 0;
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot snapshot:dataSnapshot.getChildren())
               {
                   Rating item = snapshot.getValue(Rating.class);
                   sum+=Integer.parseInt(item.getRateValue());
                   count++;
               }

               if(count != 0)
               {
                   float average  = sum / count;
                   rangtingBar.setRating(average);
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Xác Nhận")
                .setNegativeButtonText("Huỷ Bỏ")
                .setNoteDescriptions(Arrays.asList("Rất Tệ", "Không Tốt", "Tạm Được", "Rất Tốt", "Tuyệt Vời"))
                .setDefaultRating(1)
                .setTitle("Đánh Giá Sản Phẩm Này")
                .setDescription("Hãy Chọn Một Số Ngôi Sao Để Cung Cấp Phản Hồi Của Bạn")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Hãy Viết Đánh Giá Của Bạn Ở Đây..")
                .setHintTextColor(R.color.colorPrimary)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RangtingDialogFadeAnim)
                .create(Product_detail.this)
                .show();

    }

    private void getDetailFood(String productId) {
        product.child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentProduct = dataSnapshot.getValue(Product.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentProduct.getImage())
                        .into(product_image);

                collapsingToolbarLayout.setTitle(currentProduct.getName());

                int gia = Integer.parseInt(currentProduct.getPrice());
                Locale locale = new Locale("vi","VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                product_price.setText(fmt.format(gia));

                product_name.setText(currentProduct.getName());

                product_description.setText(currentProduct.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int values, String commets) {

        //get rating and upload to firebase
        if (Common.currentUser == null) {
            showDialogDangNhap();
        }
        else
        {
            final Rating rating = new Rating(Common.currentUser.getPhone(), productId, String.valueOf(values), commets);
            //user rangting mutiline
            ratingtbl.push()
                    .setValue(rating)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Product_detail.this, "Cảm Ơn Bạn Đã Đánh Giá Sản Phẩm", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Common.currentUser != null) {
            btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        }

    }

    private  void showDialogDangNhap()
    {
        AlertDialog.Builder alerdialog = new AlertDialog.Builder(Product_detail.this);
        alerdialog.setTitle("Vui Lòng Đăng Nhập");

        //set button
        alerdialog.setPositiveButton("Đồng Ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Intent intent = new Intent(Product_detail.this, MainActivity.class);
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
