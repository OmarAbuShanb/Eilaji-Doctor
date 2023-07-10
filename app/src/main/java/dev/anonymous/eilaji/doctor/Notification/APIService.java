package dev.anonymous.eilaji.doctor.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAwvIEHJc:APA91bGKysFBaOuoltl3Dv9gITBZxDhe8SLpgTC-Nb0oGHtPFVuh2EQU7RZK4_1eumM6bZ8Z3YVF6tGG4gbyhmrSkQ8SHJRCINrR1u7tFX_RYVP88vmnAXMu__q0R4bblm4sdGg1DpDB"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
