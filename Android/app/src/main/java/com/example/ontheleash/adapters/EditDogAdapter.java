package com.example.ontheleash.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.ontheleash.Fragments.EditDogsFragment;
import com.example.ontheleash.R;
import com.example.ontheleash.Settings;
import com.example.ontheleash.activities.EditUserActivity;
import com.example.ontheleash.activities.MainActivity;
import com.example.ontheleash.api.ApiClient;
import com.example.ontheleash.api.DogResponse;
import com.example.ontheleash.dataClasses.Dog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * for edit dogs recyclerview
 */
public class EditDogAdapter extends RecyclerView.Adapter<EditDogAdapter.EditDogViewHolder> {

    public List<Dog> items;
    private Context context;
    private Activity activity;
    private EditDogsFragment fragment;

    public EditDogAdapter(List<Dog> items, Activity activity, EditDogsFragment fragment){
        this.items = items;
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == items.size()) ? R.layout.add_dog_after_recycler : R.layout.edit_dog_raw_object;
    }

    @NonNull
    @Override
    public EditDogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(viewType, parent, false);
        return new EditDogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditDogViewHolder holder, int position) {
        // button on the bottom
        if(position == items.size()) {
            holder.add_dog.setOnClickListener(v -> {
                Dog dog = new Dog();
                dog.was_created = true;
                items.add(dog);
                // notify adapter
                notifyItemInserted(items.size() - 1);
            });
            return;
        }

        holder.name.setText(items.get(position).getName());
        holder.breed.setText(items.get(position).getBreed());
        holder.birthday.setText(items.get(position).getBirthday());

        if(items.get(position).getSex() == 0)
            holder.rg.check(R.id.male);
        else
            holder.rg.check(R.id.female);

        if(items.get(position).getCastration() == 1)
            holder.castration.setChecked(true);

        if(items.get(position).getReady_to_mate() == 1)
            holder.ready_to_mate.setChecked(true);

        if(items.get(position).getFor_sale() == 1)
            holder.for_sale.setChecked(true);

        // dialog for deleting dog
        Dialog deleteDogDialog = new Dialog(context);
        deleteDogDialog.setContentView(R.layout.dialog_delete_dog);
        deleteDogDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_bg));
        deleteDogDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView no = deleteDogDialog.findViewById(R.id.no);
        TextView yes = deleteDogDialog.findViewById(R.id.yes);

        no.setOnClickListener(v -> deleteDogDialog.dismiss());

        yes.setOnClickListener(v -> {
            if(items.get(position).getId() != 0)
                deleteDogApi(position, deleteDogDialog);
            else{
                items.remove(position);
                deleteDogDialog.dismiss();
                // notify adapter
                notifyItemRemoved(position);
            }
        });

        holder.delete.setOnClickListener(v -> deleteDogDialog.show());

        // load image
        if(items.get(position).uri != null){
            Glide.with(context)
                    .load(items.get(position).uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.dog_photo_default)
                    .transform(new CenterCrop(),new RoundedCorners(50))
                    .into(holder.image);
        }
        else{
            if(items.get(position).delete_photo == 1){
                Glide.with(context)
                        .load(R.drawable.dog_photo_default)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.dog_photo_default)
                        .transform(new CenterCrop(),new RoundedCorners(50))
                        .into(holder.image);
            }else{
                Glide.with(context)
                        .load(items.get(position).getImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.dog_photo_default)
                        .transform(new CenterCrop(),new RoundedCorners(50))
                        .into(holder.image);
            }
        }

        // dialog on image click
        Dialog image_click_dialog = new Dialog(context);
        image_click_dialog.setContentView(R.layout.dialog_on_image_click);
        image_click_dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_bg));
        image_click_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView upload_photo = image_click_dialog.findViewById(R.id.upload);
        TextView delete_photo = image_click_dialog.findViewById(R.id.delete);

        upload_photo.setOnClickListener(v1 -> {
            image_click_dialog.dismiss();
            setupPermissions(position);
        });

        delete_photo.setOnClickListener(v1 -> {
            deletePhoto(position);
            image_click_dialog.dismiss();}
        );

        holder.image.setOnClickListener(v -> image_click_dialog.show());

        // save dog button
        holder.save.setOnClickListener(v -> {
            if(holder.name.getText().toString().isEmpty()){
                Toast.makeText(context, "A dog has to have a name", Toast.LENGTH_SHORT).show();
                return;
            }
            if(holder.birthday.getText().toString().isEmpty()){
                Toast.makeText(context, "A dog has to have a birthday", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences mSettings;
            mSettings = context.getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
            String jwt = mSettings.getString(Settings.APP_PREFERENCES_TOKEN, "");

            Dog dog = items.get(position);
            dog.setName(holder.name.getText().toString());
            dog.setBreed(holder.breed.getText().toString());
            dog.setBirthday(holder.birthday.getText().toString());

            dog.setCastration(holder.castration.isChecked() ? 1 : 0);
            dog.setFor_sale(holder.for_sale.isChecked() ? 1 : 0);
            dog.setReady_to_mate(holder.ready_to_mate.isChecked() ? 1 : 0);

            dog.setSex(holder.rg.getCheckedRadioButtonId() == R.id.male ? 0 : 1);

            if(dog.was_created)
                createDogApi(dog, jwt);
            else
                saveDogApi(dog, jwt);
        });

        // if castration is checked, ready to mate is unchecked and disabled
        holder.castration.setOnClickListener(v -> {
            if(holder.castration.isChecked()){
                holder.ready_to_mate.setChecked(false);
                holder.ready_to_mate.setEnabled(false);
            }
            else
                holder.ready_to_mate.setEnabled(true);
        });

        // calendar for date picker
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder();
        // only dates in the past
        constraintBuilder.setValidator(DateValidatorPointBackward.now());

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        // without input inside a picker
        builder.setTheme(R.style.Widget_AppTheme_MaterialDatePicker);
        builder.setTitleText("Select date of birth");
        builder.setCalendarConstraints(constraintBuilder.build());
        MaterialDatePicker datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Long>) selection -> {
            // Get the offset from our timezone and UTC.
            TimeZone timeZoneUTC = TimeZone.getDefault();
            // It will be negative, so that's the -1
            int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
            // Create a date format, then a date object with our offset
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = new Date(selection + offsetFromUTC);

            holder.birthday.setText(simpleFormat.format(date));
        });

        holder.birthday.setInputType(InputType.TYPE_NULL);
        holder.birthday.setKeyListener(null);
        holder.birthdayLayout.setEndIconOnClickListener(v -> {
            datePicker.show(((EditUserActivity)context).getSupportFragmentManager(), "DATE_PICKER");
        });

        getBreedsApi(holder);
    }

    /**
     * gets all existing breeds in db for auto complete textview
     */
    private void getBreedsApi(EditDogViewHolder holder){
        ApiClient.getInstance()
                .getApi()
                .getBreeds()
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                        if(response.isSuccessful()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                    android.R.layout.simple_dropdown_item_1line, response.body());
                            holder.breed.setAdapter(adapter);
                        }

                        else{
                            switch (response.code()) {
                                case 500:
                                    Toast.makeText(context, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    /**
     * +1 because we have an add dog button
     */
    @Override
    public int getItemCount() {
        return items.size() + 1;
    }

    class EditDogViewHolder extends RecyclerView.ViewHolder{

        ImageView photo;
        EditText name;
        AppCompatAutoCompleteTextView breed;
        EditText birthday;
        RadioGroup rg;
        CheckBox castration;
        CheckBox ready_to_mate;
        CheckBox for_sale;
        ImageButton add_dog;
        TextView delete;
        TextView save;
        TextInputLayout birthdayLayout;
        ImageView image;

        public EditDogViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.photo);
            name = itemView.findViewById(R.id.editName);
            breed = itemView.findViewById(R.id.autoCompleteBreed);
            birthday = itemView.findViewById(R.id.editBirthday);
            birthdayLayout = itemView.findViewById(R.id.birthdayInputLayout);
            castration = itemView.findViewById(R.id.castration_check);
            ready_to_mate = itemView.findViewById(R.id.ready_to_mate_check);
            for_sale = itemView.findViewById(R.id.for_sale_check);
            image = itemView.findViewById(R.id.photo);

            rg = itemView.findViewById(R.id.radio_group);

            add_dog = itemView.findViewById(R.id.add_dog);

            delete = itemView.findViewById(R.id.delete);
            save = itemView.findViewById(R.id.save);
        }
    }

    private void deleteDogApi(int position, Dialog deleteDogDialog){
        SharedPreferences mSettings;
        mSettings = context.getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        String jwt = mSettings.getString(Settings.APP_PREFERENCES_TOKEN, "");

        ApiClient.getInstance()
                .getApi()
                .deleteDog(items.get(position).getId(), jwt)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {
                            items.remove(position);
                            deleteDogDialog.dismiss();
                            // notify adapter
                            notifyItemRemoved(position);

                            Toast.makeText(context, "Dog was deleted", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    wrong_jwt();
                                    break;
                                case 500:
                                    Toast.makeText(context, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void saveDogApi(Dog dog, String jwt){
        MultipartBody.Part part = null;

        if (dog.uri != null){
            dog.delete_photo = 0;
            File file = new File(getPathFromURI(dog.uri));
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            part = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        }

        ApiClient.getInstance()
                .getApi()
                .updateDog(dog.getId(), jwt, part, dog)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if(response.isSuccessful()) {

                            Toast.makeText(context, "Dog was saved", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    wrong_jwt();
                                    break;
                                case 404:
                                    // somebody deleted dog while we were editing it
                                    createDogApi(dog, jwt);
                                    break;
                                case 500:
                                    Toast.makeText(context, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void createDogApi(Dog dog, String jwt){
        MultipartBody.Part part = null;

        if (dog.uri != null){
            dog.delete_photo = 0;
            File file = new File(getPathFromURI(dog.uri));
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            part = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        }

        ApiClient.getInstance()
                .getApi()
                .createDog(jwt, part, dog)
                .enqueue(new Callback<DogResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DogResponse> call, @NonNull Response<DogResponse> response) {
                        if(response.isSuccessful()) {
                            dog.was_created = false;
                            dog.setId(response.body().getDog_id());
                            Toast.makeText(context, "Dog was created", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            switch (response.code()) {
                                case 401:
                                    wrong_jwt();
                                    break;
                                case 500:
                                    Toast.makeText(context, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(context, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DogResponse> call, @NonNull Throwable t) {
                        Toast.makeText(context,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void wrong_jwt(){
        SharedPreferences mSettings;
        mSettings = context.getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Settings.APP_PREFERENCES_LOGGED_IN, false);
        editor.apply();
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void setupPermissions(int position){
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED)
            makeRequest();
        else
            openGallery(position);
    }

    private void makeRequest(){
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                EditDogsFragment.EXTERNAL_STORAGE_REQUEST_CODE);
    }

    public void openGallery(int position){
        Intent intent = new Intent();
        intent.setType("image/+");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(Intent.createChooser(intent, "Select image from gallery"), position);
    }

    private void deletePhoto(int position){
        items.get(position).delete_photo = 1;
        items.get(position).uri = null;
        notifyDataSetChanged();
    }

    public String getPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
