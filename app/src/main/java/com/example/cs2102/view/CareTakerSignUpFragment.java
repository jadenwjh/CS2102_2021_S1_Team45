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

    private EditText username;
    private EditText password;
    private EditText email;
    private EditText number;
    private EditText address;
    private Button register;

    private RegisterCTListener registerListener;

    public interface RegisterCTListener {
        void onExitRegister();

        //move these as a separate interface
        void onCheckCareTakerUsernameTaken(String username);
        void onRegisterCareTaker(String username, String password, String email, String number, String address);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_care_taker_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        username = (EditText) Objects.requireNonNull(getView()).findViewById(R.id.username);
        password = (EditText) Objects.requireNonNull(getView()).findViewById(R.id.password);
        email = (EditText) Objects.requireNonNull(getView()).findViewById(R.id.email);
        number = (EditText) Objects.requireNonNull(getView()).findViewById(R.id.number);
        address = (EditText) Objects.requireNonNull(getView()).findViewById(R.id.address);
        register = (Button) Objects.requireNonNull(getView()).findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uname = username.getText().toString();
                String pw = password.getText().toString();
                String mail = email.getText().toString();
                String num = number.getText().toString();
                String add = address.getText().toString();

                registerListener.onCheckCareTakerUsernameTaken(uname);
                registerListener.onRegisterCareTaker(uname, pw, mail, num, add);
                registerListener.onExitRegister();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RegisterCTListener) {
            registerListener = (RegisterCTListener) context;
        } else {
            throw new ClassCastException("ExitRegisterListener not implemented");
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
