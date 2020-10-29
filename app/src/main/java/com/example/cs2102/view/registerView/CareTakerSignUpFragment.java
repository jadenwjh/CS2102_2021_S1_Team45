package com.example.cs2102.view.registerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs2102.R;

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

    @BindView(R.id.contract)
    EditText contract;

    @BindView(R.id.register)
    Button register;

    private RegisterCTListener registerListener;

    public interface RegisterCTListener {
        void onExitCareTakerRegister();
        void onRegisterCareTaker(String username, String password, String email, String number, String address, String contract);
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
            String uname = username.getText().toString();
            String pw = password.getText().toString();
            String mail = email.getText().toString();
            String num = number.getText().toString();
            String add = address.getText().toString();
            String con = contract.getText().toString();

            registerListener.onRegisterCareTaker(uname, pw, mail, num, add, con);
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RegisterCTListener) {
            registerListener = (RegisterCTListener) context;
        } else {
            throw new ClassCastException("RegisterCTListener not implemented");
        }
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
}
