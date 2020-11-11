package com.example.cs2102.view.registerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs2102.R;
import com.example.cs2102.model.retrofitApi.Strings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CareTakerSignUpFragment extends Fragment {

    @BindView(R.id.username)
    EditText username;

    @BindView(R.id.password)
    EditText password;

    @BindView(R.id.email)
    EditText email;

    @BindView(R.id.number)
    EditText number;

    @BindView(R.id.address)
    EditText address;

    @BindView(R.id.creditCard)
    EditText creditCard;

    @BindView(R.id.bankAcc)
    EditText bankAcc;

    @BindView(R.id.profile)
    EditText profile;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @BindView(R.id.isFT)
    Switch isFT;

    @BindView(R.id.referral)
    EditText admin;

    @BindView(R.id.register_CT)
    Button register;

    private RegisterCTListener registerListener;

    public interface RegisterCTListener {
        void onExitCareTakerRegister();
        void onRegisterCareTaker(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype, boolean isPT, String admin);
    }

    public void setRegisterListener(RegisterCTListener listener) {
        this.registerListener = listener;
    }

    public static CareTakerSignUpFragment newInstance() {
        return new CareTakerSignUpFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_care_taker_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);

        register.setOnClickListener(currView -> {
            hideKeyboard(getActivity());
            String uname = username.getText().toString();
            String pw = password.getText().toString();
            String mail = email.getText().toString();
            int num = Integer.parseInt(number.getText().toString());
            String add = address.getText().toString();
            int cc = Integer.parseInt(creditCard.getText().toString());
            int bank = Integer.parseInt(bankAcc.getText().toString());
            String acc = Strings.CARE_TAKER;
            String pro = profile.getText().toString();
            boolean isFullTime = !isFT.isChecked();
            String adminUsername = admin.getText().toString();

            registerListener.onRegisterCareTaker(uname,mail,pw,pro,add,num,cc,bank,acc,isFullTime,adminUsername);
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
