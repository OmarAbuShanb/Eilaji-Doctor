package dev.anonymous.eilaji.doctor.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

//  apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

//    apiService.sendNotification(sender)
//            .enqueue(new Callback<MyResponse>() {
//    @Override
//    public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
//            if (response.code() == 200) {
//                if (response.body().success != 1) {
//                 Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
//                  Log.e("TAGOMAR", "onResponse: " + response.body() );
//            }
//            }
//            }
//
//    @Override
//    public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
//
//            }
//            });

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAwvIEHJc:APA91bGKysFBaOuoltl3Dv9gITBZxDhe8SLpgTC-Nb0oGHtPFVuh2EQU7RZK4_1eumM6bZ8Z3YVF6tGG4gbyhmrSkQ8SHJRCINrR1u7tFX_RYVP88vmnAXMu__q0R4bblm4sdGg1DpDB"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
