package lk.avn.irenttechs.custom;

import com.google.gson.GsonBuilder;

import lk.avn.irenttechs.service.Service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
        private static final String BASE_URL = "http://10.0.2.2:8080/irenttechs/";

        private static Retrofit retrofit;

        private static Retrofit getRetrofitInstance() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                        .build();
            }
            return retrofit;
        }

        public static Service getApiService() {
            return getRetrofitInstance().create(Service.class);
        }

}
