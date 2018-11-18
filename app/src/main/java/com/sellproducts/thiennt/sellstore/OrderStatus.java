package com.sellproducts.thiennt.sellstore;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.ViewHolder.MenuViewHolder;
import com.sellproducts.thiennt.sellstore.ViewHolder.OrderViewHolder;
import com.sellproducts.thiennt.sellstore.model.Category;
import com.sellproducts.thiennt.sellstore.model.Order;
import com.sellproducts.thiennt.sellstore.model.Request;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public  RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");


        recyclerView = (RecyclerView) findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent() == null)
        {
            loadOrder(Common.currentUser.getPhone());
        }

        else
        {
            loadOrder(getIntent().getStringExtra("userphone"));
        }
        //loadOrder(Common.currentUser.getPhone());


    }

    private void loadOrder(String phone) {

        if(Common.currentUser != null) {
              Query getorderByUser = requests.orderByChild("phone").equalTo(Common.currentUser.getPhone());
            FirebaseRecyclerOptions<Request> Oderoptions = new FirebaseRecyclerOptions.Builder<Request>()
                    .setQuery(getorderByUser, Request.class)
                    .build();

            adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(Oderoptions) {
                @Override
                protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull Request model) {
                    viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                    viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                    viewHolder.txtOrderAddres.setText(model.getAddress());
                    viewHolder.txtOrderPhone.setText(model.getPhone());
                    viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(adapter.getItem(position).getStatus().equals("1") ||
                                    adapter.getItem(position).getStatus().equals("0") || adapter.getItem(position).getStatus().equals("2"))
                            {
                                Toast.makeText(OrderStatus.this, "Không Thể Xoá", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                deleteOrder(adapter.getRef(position).getKey());
                            }
                        }
                    });
                }

                @NonNull
                @Override
                public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View iteamView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.order_layout, parent, false);
                    return new OrderViewHolder(iteamView);
                }
            };
            adapter.startListening();
            recyclerView.setAdapter(adapter);
        }
   }

    private void deleteOrder(final String key) {
        requests.child(key)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(OrderStatus.this, new StringBuilder("Đơn Hàng ")
                        .append(key)
                        .append(" Đã Được Xoá"), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OrderStatus.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
