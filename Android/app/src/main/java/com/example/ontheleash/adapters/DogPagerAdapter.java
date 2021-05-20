package com.example.ontheleash.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.ontheleash.R;
import com.example.ontheleash.dataClasses.Dog;

import java.util.List;

/**
 * For dog carousel in profile
 */
public class DogPagerAdapter extends RecyclerView.Adapter<DogPagerAdapter.DogPagerViewHolder> {

    private final List<Dog> items;
    private Context context;

    public DogPagerAdapter(List<Dog> items){
        this.items = items;
    }

    @NonNull
    @Override
    public DogPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
            .inflate(R.layout.dog_raw_object, parent, false);
        return new DogPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogPagerViewHolder holder, int position) {
        holder.dogName.setText(items.get(position).getName());
        holder.date.setText(holder.date.getText() + items.get(position).getBirthday());
        holder.breed.setText(holder.breed.getText() + items.get(position).getBreed());

        holder.sex.setImageResource(items.get(position).getSex() == 0 ? R.drawable.male_icon_orange : R.drawable.female_icon_orange);
        holder.castration.setImageResource(items.get(position).getCastration() == 0 ? R.drawable.unchecked_icon_orange : R.drawable.checked_icon_orange);
        holder.ready_to_mate.setImageResource(items.get(position).getReady_to_mate() == 0 ? R.drawable.unchecked_icon_orange : R.drawable.checked_icon_orange);
        holder.for_sale.setImageResource(items.get(position).getFor_sale() == 0 ? R.drawable.unchecked_icon_orange : R.drawable.checked_icon_orange);

        Glide.with(context)
                .load(items.get(position).getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CenterCrop(),new RoundedCorners(50))
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.dog_photo_default)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class DogPagerViewHolder extends RecyclerView.ViewHolder{

        TextView dogName;
        TextView date;
        TextView breed;
        ImageView sex;
        ImageView castration;
        ImageView ready_to_mate;
        ImageView for_sale;
        ImageView image;

        public DogPagerViewHolder(@NonNull View itemView) {
            super(itemView);

            dogName = itemView.findViewById(R.id.dog_name);
            date = itemView.findViewById(R.id.birthday);
            breed = itemView.findViewById(R.id.breed);
            sex = itemView.findViewById(R.id.sex);
            castration = itemView.findViewById(R.id.castration);
            ready_to_mate = itemView.findViewById(R.id.ready_to_mate);
            for_sale = itemView.findViewById(R.id.for_sale);
            image = itemView.findViewById(R.id.image);
        }
    }
}
