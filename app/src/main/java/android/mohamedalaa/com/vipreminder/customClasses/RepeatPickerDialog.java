package android.mohamedalaa.com.vipreminder.customClasses;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.mohamedalaa.com.vipreminder.R;
import android.mohamedalaa.com.vipreminder.databinding.DialogRepeatPickerBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by Mohamed on 8/6/2018.
 *
 */
public class RepeatPickerDialog extends Dialog implements RadioGroup.OnCheckedChangeListener {

    private DialogRepeatPickerBinding binding;

    private OnRepeatSetListener listener;
    private String stringRepeat;

    public RepeatPickerDialog(@NonNull Context context, OnRepeatSetListener listener, String stringRepeat) {
        super(context);

        this.listener = listener;
        this.stringRepeat = stringRepeat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.dialog_repeat_picker, null, false);
        setContentView(binding.getRoot());

        // setup xml
        setupXml();

        // setup clicks
        setupClicks();
    }

    private void setupXml(){
        for (int i=0; i<binding.radioGroup.getChildCount(); i++){
            RadioButton radioButton = (RadioButton) binding.radioGroup.getChildAt(i);
            if (radioButton.getText().toString().equals(stringRepeat)){
                radioButton.setChecked(true);
                break;
            }
        }
    }

    private void setupClicks() {
        // -- Listeners
        binding.radioGroup.setOnCheckedChangeListener(this);

        // -- Clicks
        binding.okButton.setOnClickListener(view -> {
            if (listener != null){
                listener.onRepeatSet(stringRepeat);
            }

            dismiss();
        });

        binding.cancelButton.setOnClickListener(view
                -> dismiss());
    }

    // ---- Implement RadioGroup.OnCheckedChangeListener

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i=0; i<binding.radioGroup.getChildCount(); i++){
            RadioButton radioButton = (RadioButton) binding.radioGroup.getChildAt(i);
            if (radioButton.isChecked()){
                stringRepeat = radioButton.getText().toString();
                break;
            }
        }
    }

    // ----- Interfaces

    public interface OnRepeatSetListener {
        void onRepeatSet(String stringRepeat);
    }

}
