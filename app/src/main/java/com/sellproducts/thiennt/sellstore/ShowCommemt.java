package com.sellproducts.thiennt.sellstore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.Database.Database;
import com.sellproducts.thiennt.sellstore.ViewHolder.CommentViewHolder;
import com.sellproducts.thiennt.sellstore.model.Rating;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowCommemt extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager ;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwiprefreshLayout;

    FirebaseRecyclerAdapter<Rating,CommentViewHolder> adapter;
    String productId = "";

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null)
        {
            adapter.stopListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_commemt);

        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = (RecyclerView) findViewById(R.id.recycle_Comment);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //swipe layout

        mSwiprefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwiprefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent() != null)
                {
                    productId = getIntent().getStringExtra(Common.INTEN_PRODUCT_ID);
                }
                if(!productId.isEmpty() && productId != null)
                {
                    //create request query
                    Query query = ratingTbl.orderByChild("productId").equalTo(productId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, CommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rating model) {
                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComMent());
                            holder.txtUserPhone.setText(model.getUserPhone());
                        }

                        @NonNull
                        @Override
                        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.comment_item, parent, false);
                            return new CommentViewHolder(view);
                        }
                    };

                    LoadComment(productId);

                }
            }
        });

        //thread to load comment on first launch
        mSwiprefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwiprefreshLayout.setRefreshing(true);

                if(getIntent() != null)
                {
                    productId = getIntent().getStringExtra(Common.INTEN_PRODUCT_ID);
                }
                if(!productId.isEmpty() && productId != null)
                {
                    //create request query
                    Query query = ratingTbl.orderByChild("productId").equalTo(productId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, CommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rating model) {
                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComMent());
                            holder.txtUserPhone.setText(model.getUserPhone());
                        }

                        @NonNull
                        @Override
                        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.comment_item, parent, false);
                            return new CommentViewHolder(view);
                        }
                    };

                    LoadComment(productId);

                }
            }
        });
    }

    private void LoadComment(String productId) {
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        mSwiprefreshLayout.setRefreshing(false);
    }
}
