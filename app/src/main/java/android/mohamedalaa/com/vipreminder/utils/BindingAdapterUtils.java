package android.mohamedalaa.com.vipreminder.utils;

import android.databinding.BindingAdapter;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Created by Mohamed on 8/6/2018.
 *
 */
public class BindingAdapterUtils {

    @BindingAdapter("setOnCheckedChangeListener")
    public static void setOnCheckedChangeListener(CompoundButton compoundButton,
                                                  CompoundButton.OnCheckedChangeListener listener){
        compoundButton.setOnCheckedChangeListener(listener);
    }

    @BindingAdapter("setTextWatcher")
    public static void setTextWatcher(EditText editText, TextWatcher textWatcher){
        editText.addTextChangedListener(textWatcher);
    }

}
