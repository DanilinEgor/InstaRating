package ru.egor_d.instarating;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.egor_d.instarating.model.InstagramPhoto;

public class InstagramPhotosAdapter extends RecyclerView.Adapter<InstagramPhotosAdapter.CardViewHolder> {
    private List<InstagramPhoto> photos = new ArrayList<>();
    private Context context;

    @Inject
    Picasso picasso;

    public InstagramPhotosAdapter(Context context) {
        this.context = context;
        App.getInstance().component().inject(this);
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_photo, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CardViewHolder cardViewHolder, final int i) {
        final InstagramPhoto photo = photos.get(i);
        cardViewHolder.likes.setText(String.format("%s%s", context.getString(R.string.heart), String.valueOf(photo.likes)));
        cardViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(photo.link));
                context.startActivity(intent);
            }
        });
        picasso.load(photo.thumbnail).into(cardViewHolder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void addPhotos(final List<InstagramPhoto> newPhotos) {
//        for (InstagramPhoto newPhoto : newPhotos) {
//            boolean added = false;
//            if (photos.isEmpty() || newPhoto.likes > photos.get(0).likes) {
//                add(newPhoto, 0);
//                added = true;
//            } else {
//                for (int i = 0; i < photos.size() - 1; i++) {
//                    if (newPhoto.likes < photos.get(i).likes
//                            && newPhoto.likes >= photos.get(i + 1).likes) {
//                        add(newPhoto, i);
//                        added = true;
//                        break;
//                    }
//                }
//            }
//            if (!added) {
//                add(newPhoto, Math.max(photos.size() - 1, 0));
//            }
//        }
        photos.addAll(newPhotos);
        Collections.sort(photos, new Comparator<InstagramPhoto>() {
            @Override
            public int compare(final InstagramPhoto lhs, final InstagramPhoto rhs) {
                return rhs.likes - lhs.likes;
            }
        });
        notifyDataSetChanged();
    }

    public void add(InstagramPhoto photo, int position) {
        photos.add(position, photo);
        notifyItemInserted(position);
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_photo_likes_text_view)
        TextView likes;
        @Bind(R.id.item_photo_image)
        ImageView thumbnail;
        @Bind(R.id.item_photo_card)
        CardView cardView;

        public CardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
