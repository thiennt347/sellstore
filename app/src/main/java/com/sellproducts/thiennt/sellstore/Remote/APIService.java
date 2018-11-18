package com.sellproducts.thiennt.sellstore.Remote;

import com.sellproducts.thiennt.sellstore.model.DataMessage;
import com.sellproducts.thiennt.sellstore.model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAvuFdsN4:APA91bEECycSdNjyeCDH8A3gArbUp_3P7oyDEi4u3r0wUYfRHtH4egccl30TVF9TjvBCquXT9j7xWXdO_zp_E4BVt7I3n1qU7Wvz_J3QORSzXj9_B_zMd_-qMVBlJuwFSsrt_ejdSRHq"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendthongbao(@Body DataMessage body);
}