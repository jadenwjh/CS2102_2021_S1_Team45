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

import java.util.Objects;

public class PetOwnerSignUpFragment extends Fragment {

    private RegisterPOListener registerListener;

    public interface RegisterPOListener {
        void onExitPetOwnerRegister();
        boolean onCheckPetOwnerUsernameTaken(String username);
        void onRegisterPetOwner(String username, String password, String email, String number, String address, String petName, String petType);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pet_owner_sign_up, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText username = Objects.requireNonNull(getView()).findViewById(R.id.username);
        EditText password = Objects.requireNonNull(getView()).findViewById(R.id.password);
        EditText email = Objects.requireNonNull(getView()).findViewById(R.id.email);
        EditText number = Objects.requireNonNull(getView()).findViewById(R.id.number);
        EditText address = Objects.requireNonNull(getView()).findViewById(R.id.address);
        EditText petName = Objects.requireNonNull(getView()).findViewById(R.id.pet_name);
        EditText petType = Objects.requireNonNull(getView()).findViewById(R.id.pet_type);
        Button register = Objects.requireNonNull(getView()).findViewById(R.id.register);

        register.setOnClickListener(currView -> {
            String uname = username.getText().toString();
            String pw = password.getText().toString();
            String mail = email.getText().toString();
            String num = number.getText().toString();
            String add = address.getText().toString();
            String pn = petName.getText().toString();
            String pt = petType.getText().toString();

            if (!registerListener.onCheckPetOwnerUsernameTaken(uname)) {
                registerListener.onRegisterPetOwner(uname, pw, mail, num, add, pn, pt);
                registerListener.onExitPetOwnerRegister();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RegisterPOListener) {
            registerListener = (RegisterPOListener) context;
        } else {
            throw new ClassCastException("RegisterPOListener not implemented");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}