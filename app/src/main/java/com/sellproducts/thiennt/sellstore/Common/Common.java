package com.sellproducts.thiennt.sellstore.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import com.sellproducts.thiennt.sellstore.Remote.APIService;
import com.sellproducts.thiennt.sellstore.Remote.RetrofitClient;
import com.sellproducts.thiennt.sellstore.model.User;


public class Common {
    public  static String topicName = "News";
    public  static User currentUser;
    public static final String DELETE = "Xoá";

    public static final String INTEN_PRODUCT_ID = "productId";
    private static final String BASE_URL = "https://fcm.googleapis.com/";


    public static APIService getFCMservice()
    {
        return RetrofitClient.getclient(BASE_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String code){
        if (code.equals("0"))
            return "Đã Đặt";
        else if (code.equals("1"))
            return "Trên đường Đến";
        else if (code.equals("2"))
            return "Đang Lấy hàng";
        else
            return "Đã Giao Hàng";
    }

    public static  boolean isConnectedInternet(Context context )
    {
        ConnectivityManager connectivityManager  = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
        {
            NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();

            if(infos != null)
            {
                for(int i = 0 ; i < infos.length; i++)
                {
                    if(infos[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
