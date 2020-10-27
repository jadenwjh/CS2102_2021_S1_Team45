package com.example.cs2102.view;

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

import java.util.Objects;

public class CareTakerSignUpFragment extends Fragment {

    private RegisterCTListener registerListener;

    public interface RegisterCTListener {
        void onExitCareTakerRegister();
        boolean onCheckCareTakerUsernameTaken(String username);
        void onRegisterCareTaker(String username, String password, String email, String number, String address, String contract);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_care_taker_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText username = Objects.requireNonNull(getView()).findViewById(R.id.username);
        EditText password = Objects.requireNonNull(getView()).findViewById(R.id.password);
        EditText email = Objects.requireNonNull(getView()).findViewById(R.id.email);
        EditText number = Objects.requireNonNull(getView()).findViewById(R.id.number);
        EditText address = Objects.requireNonNull(getView()).findViewById(R.id.address);
        EditText contract = Objects.requireNonNull(getView()).findViewById(R.id.contract);
        Button register = Objects.requireNonNull(getView()).findViewById(R.id.register);

        register.setOnClickListener(currView -> {
            String uname = username.getText().toString();
            String pw = password.getText().toString();
            String mail = email.getText().toString();
            String num = number.getText().toString();
            String add = address.getText().toString();
            String con = contract.getText().toString();

            if (!registerListener.onCheckCareTakerUsernameTaken(uname)) {
                registerListener.onRegisterCareTaker(uname, pw, mail, num, add, con);
                registerListener.onExitCareTakerRegister();
            }
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
