package ru.egor_d.instarating.api;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import ru.egor_d.instarating.model.InstagramMedia;
import ru.egor_d.instarating.model.InstagramResponse;
import ru.egor_d.instarating.model.InstagramUser;
import rx.Observable;

public interface IInstagramService {
    @GET("search")
    Observable<InstagramResponse<List<InstagramUser>>> getUserId(
            @Query("q") String username,
            @Query("client_id") String client_id,
            @Query("access_token") String access_token
    );

    @GET("{user_id}/media/recent")
    Observable<InstagramResponse<List<InstagramMedia>>> getPhotosList(
            @Path("user_id") String user_id,
            @Query("max_id") String max_id,
            @Query("count") int count,
            @Query("client_id") String client_id,
            @Query("access_token") String access_token
    );

    @GET("self")
    Observable<InstagramResponse<InstagramUser>> getMe(
            @Query("access_token") String access_token
    );

    @GET("{user_id}")
    Observable<InstagramResponse<InstagramUser>> getUser(
            @Path("user_id") String user_id,
            @Query("access_token") String access_token
    );
}
