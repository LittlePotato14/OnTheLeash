package com.example.ontheleash.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.util.Pair;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.ontheleash.MapSettings;
import com.example.ontheleash.MyClusteringItem;
import com.example.ontheleash.R;
import com.example.ontheleash.api.ApiClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFiltersActivity extends AppCompatActivity {

    AppCompatAutoCompleteTextView breedTextView;
    ChipGroup breedChips;
    TextInputEditText birthday;
    List<String> breeds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove splash screen
        setTheme(R.style.Theme_OnTheLeash);
        setContentView(R.layout.activity_map_filters);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        breedTextView = findViewById(R.id.autoCompleteBreed);
        breedChips = findViewById(R.id.breed_chips);

        birthday = findViewById(R.id.birthday);

        setFilters();

        getBreedsApi();

        // make expandable marker filters
        GridLayout expandableFilter = findViewById(R.id.expandable_marker_filters);
        ImageButton expandButton = findViewById(R.id.expand_filters_button);

        expandButton.setOnClickListener(v -> {
            if(expandableFilter.getVisibility() == View.GONE){
                expandableFilter.setVisibility(View.VISIBLE);
                expandButton.setImageResource(R.drawable.up_icon_green);
            }else{
                expandableFilter.setVisibility(View.GONE);
                expandButton.setImageResource(R.drawable.down_icon_green);
            }
        });

        // make expandable dog filters
        LinearLayout expandableDogs = findViewById(R.id.expandable_dogs);
        ImageButton expandDogsButton = findViewById(R.id.expand_dogs_button);

        expandDogsButton.setOnClickListener(v -> {
            if(expandableDogs.getVisibility() == View.GONE){
                expandableDogs.setVisibility(View.VISIBLE);
                expandDogsButton.setImageResource(R.drawable.up_icon_green);
            }else{
                expandableDogs.setVisibility(View.GONE);
                expandDogsButton.setImageResource(R.drawable.down_icon_green);
            }
        });

        CheckBox castration = findViewById(R.id.castration);
        CheckBox ready_to_mate = findViewById(R.id.ready_to_mate);

        // if castration is checked, ready to mate is unchecked and disabled
        castration.setOnClickListener(v -> {
            if(castration.isChecked()){
                ready_to_mate.setChecked(false);
                ready_to_mate.setEnabled(false);
            }
            else
                ready_to_mate.setEnabled(true);
        });

        TextInputLayout birthdayLayout = findViewById(R.id.birthdayInputLayout);

        // calendar for date picker
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder();
        // only dates in the past
        constraintBuilder.setValidator(DateValidatorPointBackward.now());

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.dateRangePicker();
        // without input inside a picker
        builder.setTheme(R.style.Widget_AppTheme_MaterialDatePicker);
        builder.setTitleText("Select range of birth");
        builder.setCalendarConstraints(constraintBuilder.build());

        MapSettings mapSettings = MapSettings.getInstance(this);

        if(!mapSettings.birthday.isEmpty()) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            TimeZone timeZoneUTC = TimeZone.getDefault();
            int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
            try {
                Date date1 = formatter.parse(mapSettings.birthday.split(" : ")[0]);
                Date date2 = formatter.parse(mapSettings.birthday.split(" : ")[1]);

                date1 = new Date(date1.getTime() - offsetFromUTC);
                date2 = new Date(date2.getTime() - offsetFromUTC);

                builder.setSelection(new Pair<>(date1.getTime(), date2.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        MaterialDatePicker datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long,Long>>) selection -> {
            // Get the offset from our timezone and UTC.
            TimeZone timeZoneUTC = TimeZone.getDefault();
            // It will be negative, so that's the -1
            int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
            // Create a date format, then a date object with our offset
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date1 = new Date(selection.first + offsetFromUTC);
            Date date2 = new Date(selection.second + offsetFromUTC);

            birthday.setText(simpleFormat.format(date1) + " : " + simpleFormat.format(date2));
        });

        datePicker.addOnNegativeButtonClickListener(v -> birthday.setText(""));

        birthday.setInputType(InputType.TYPE_NULL);
        birthday.setKeyListener(null);
        birthdayLayout.setEndIconOnClickListener(v -> {
            datePicker.show(MapFiltersActivity.this.getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    // set all chosen filters at start
    private void setFilters(){
        MapSettings mapSettings = MapSettings.getInstance(this);

        // map style
        RadioGroup radioGroup = findViewById(R.id.radio_group);
        if (mapSettings.mapStyle == MapSettings.MapStyle.NORMAL)
            radioGroup.check(R.id.normal);
        else if (mapSettings.mapStyle == MapSettings.MapStyle.SATELLITE)
            radioGroup.check(R.id.satellite);
        else
            radioGroup.check(R.id.hybrid);

        // marker types
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.DOG))
            ((CheckBox)findViewById(R.id.dogs)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.DOG_PARK))
            ((CheckBox)findViewById(R.id.dog_parks)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.DOG_FRIENDLY))
            ((CheckBox)findViewById(R.id.dog_friendly)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.HOSPITAL))
            ((CheckBox)findViewById(R.id.hospitals)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.SHELTER))
            ((CheckBox)findViewById(R.id.shelters)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.STORE))
            ((CheckBox)findViewById(R.id.stores)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.HOTEL))
            ((CheckBox)findViewById(R.id.hotels)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.TRAINING))
            ((CheckBox)findViewById(R.id.clubs)).setChecked(true);
        if(mapSettings.markerTypeList.contains(MyClusteringItem.MarkerType.BREEDING))
            ((CheckBox)findViewById(R.id.breeding)).setChecked(true);

        ((Chip)findViewById(R.id.male)).setChecked(mapSettings.male);
        ((Chip)findViewById(R.id.female)).setChecked(mapSettings.female);

        if(!mapSettings.birthday.equals(""))
            birthday.setText(mapSettings.birthday);

        if(mapSettings.castration){
            ((CheckBox)findViewById(R.id.castration)).setChecked(true);
            ((CheckBox)findViewById(R.id.ready_to_mate)).setEnabled(false);
        }
        ((CheckBox)findViewById(R.id.ready_to_mate)).setChecked(mapSettings.ready_to_mate);
        ((CheckBox)findViewById(R.id.for_sale)).setChecked(mapSettings.for_sale);

        for(String i: mapSettings.breeds){
            breeds.add(i);

            Chip chip = new Chip(MapFiltersActivity.this);
            chip.setText(i);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                breeds.remove(chip.getText().toString());
                breedChips.removeView(chip);
            });

            breedChips.addView(chip);
        }
    }

    /**
     * gets all existing breeds in db for auto complete
     */
    private void getBreedsApi(){
        ApiClient.getInstance()
                .getApi()
                .getBreeds()
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                        if(response.isSuccessful()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapFiltersActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, response.body());
                            breedTextView.setAdapter(adapter);
                            breedTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String breed = parent.getItemAtPosition(position).toString();

                                    if(!breeds.contains(breed)) {
                                        breeds.add(breed);

                                        Chip chip = new Chip(MapFiltersActivity.this);
                                        chip.setText(breed);
                                        chip.setCloseIconVisible(true);
                                        chip.setOnCloseIconClickListener(v -> {
                                            breeds.remove(chip.getText().toString());
                                            breedChips.removeView(chip);
                                        });

                                        breedChips.addView(chip);
                                    }

                                    // hide keyboard
                                    InputMethodManager imm = (InputMethodManager) (MapFiltersActivity.this).getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(breedChips.getApplicationWindowToken(), 0);

                                    breedTextView.setText("");
                                    breedTextView.clearFocus();
                                }
                            });
                        }

                        else{
                            switch (response.code()) {
                                case 500:
                                    Toast.makeText(MapFiltersActivity.this, "Server broken", Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(MapFiltersActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                        Toast.makeText(MapFiltersActivity.this,
                                "Something went wrong... Please, check the Internet connection", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    // saving data
    @Override
    public void onBackPressed() {
        MapSettings mapSettings = MapSettings.getInstance(this);

        // map style
        int radioId = ((RadioGroup)findViewById(R.id.radio_group)).getCheckedRadioButtonId();
        if (radioId == R.id.normal)
            mapSettings.mapStyle = MapSettings.MapStyle.NORMAL;
        else if (radioId == R.id.satellite)
            mapSettings.mapStyle = MapSettings.MapStyle.SATELLITE;
        else
            mapSettings.mapStyle = MapSettings.MapStyle.HYBRID;

        // marker types
        mapSettings.markerTypeList.clear();
        if(((CheckBox)findViewById(R.id.dogs)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.DOG);
        if(((CheckBox)findViewById(R.id.dog_parks)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.DOG_PARK);
        if(((CheckBox)findViewById(R.id.dog_friendly)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.DOG_FRIENDLY);
        if(((CheckBox)findViewById(R.id.hospitals)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.HOSPITAL);
        if(((CheckBox)findViewById(R.id.shelters)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.SHELTER);
        if(((CheckBox)findViewById(R.id.stores)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.STORE);
        if(((CheckBox)findViewById(R.id.hotels)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.HOTEL);
        if(((CheckBox)findViewById(R.id.clubs)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.TRAINING);
        if(((CheckBox)findViewById(R.id.breeding)).isChecked())
            mapSettings.markerTypeList.add(MyClusteringItem.MarkerType.BREEDING);

        mapSettings.male = ((Chip)findViewById(R.id.male)).isChecked();
        mapSettings.female = ((Chip)findViewById(R.id.female)).isChecked();

        if(birthday.getText() != null && !birthday.getText().toString().isEmpty())
            mapSettings.birthday = birthday.getText().toString();
        else
            mapSettings.birthday = "";

        mapSettings.castration = ((CheckBox)findViewById(R.id.castration)).isChecked();
        mapSettings.ready_to_mate = ((CheckBox)findViewById(R.id.ready_to_mate)).isChecked();
        mapSettings.for_sale = ((CheckBox)findViewById(R.id.for_sale)).isChecked();
        mapSettings.breeds = breeds;

        mapSettings.update(this);

        Toast.makeText(MapFiltersActivity.this, "Map settings saved", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }
}