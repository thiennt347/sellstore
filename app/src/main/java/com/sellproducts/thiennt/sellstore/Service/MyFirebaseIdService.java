package com.sellproducts.thiennt.sellstore.Service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sellproducts.thiennt.sellstore.Common.Common;
import com.sellproducts.thiennt.sellstore.model.Token;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenfresh = FirebaseInstanceId.getInstance().getToken();
        /////////////////
        if(Common.currentUser != null)
            uptoken(tokenfresh);

    }

    private void uptoken(String tokenfresh) {
        FirebaseDatabase db =FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(tokenfresh,false); //fasle, vi token gui tu clien app
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
