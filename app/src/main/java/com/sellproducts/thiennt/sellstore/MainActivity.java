package com.sellproducts.thiennt.sellstore;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.model.User;

import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 7171;
    Button btnSignUp;
    TextView txtSologan;

    FirebaseDatabase database;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initfacebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(this);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        txtSologan = (TextView)findViewById(R.id.txtSologan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        txtSologan.setTypeface(face);
        txtSologan.setTextSize(30f);
        //init paper



        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartLoginSystem();
            }
        });

        //check section AccountKit

        if(AccountKit.getCurrentAccessToken() != null)
        {
            //show dialog
            final AlertDialog waitingDialog = new SpotsDialog(this);
            waitingDialog.show();
            waitingDialog.setMessage("Chờ Trong Giây Lát");
            waitingDialog.setCancelable(false);
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    //login user
                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User localUser = dataSnapshot.getValue(User.class);
                                    Intent intent = new Intent(MainActivity.this, Home.class);
                                    Common.currentUser = localUser;
                                    startActivity(intent);
                                    waitingDialog.dismiss();
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }


    }


    private void StartLoginSystem() {

        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE)
        {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(result.getError() != null)
            {
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            else if(result.wasCancelled())
            {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                if(result.getAccessToken() != null)
                {
                    //show dialog
                    final AlertDialog waitingDialog = new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Chờ Trong Giây Lát");
                    waitingDialog.setCancelable(false);

                    //get curenPhone

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String Userphone = account.getPhoneNumber().toString();

                            //check neu ton tai

                            users.orderByKey().equalTo(Userphone).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.child(Userphone).exists())//neu chua ton tai
                                    {
                                        User newUser = new User();
                                        newUser.setPhone(Userphone);
                                        newUser.setName("");

                                        //add user to firebase
                                        users.child(Userphone)
                                                .setValue(newUser)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            Toast.makeText(MainActivity.this, "Đăng Ký Thành Công", Toast.LENGTH_SHORT).show();
                                                        }

                                                        //login
                                                        users.child(Userphone)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        User localUser = dataSnapshot.getValue(User.class);
                                                                        Intent intent = new Intent(MainActivity.this, Home.class);
                                                                        Common.currentUser = localUser;
                                                                        startActivity(intent);
                                                                        waitingDialog.dismiss();
                                                                        finish();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                    else //user ton tai
                                    {
                                        //login user
                                        users.child(Userphone)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        User localUser = dataSnapshot.getValue(User.class);
                                                        Intent intent = new Intent(MainActivity.this, Home.class);
                                                        Common.currentUser = localUser;
                                                        startActivity(intent);
                                                        waitingDialog.dismiss();
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }
    }

}
