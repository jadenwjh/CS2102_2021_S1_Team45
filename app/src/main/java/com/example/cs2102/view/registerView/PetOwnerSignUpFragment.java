package com.example.cs2102.view.registerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs2102.R;
import com.example.cs2102.model.retrofitApi.Strings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PetOwnerSignUpFragment extends Fragment {

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

    @BindView(R.id.register_PO)
    Button register;

    public static PetOwnerSignUpFragment newInstance() {
        return new PetOwnerSignUpFragment();
    }

    private RegisterPOListener registerListener;

    public interface RegisterPOListener {
        void onExitPetOwnerRegister();
        void onRegisterPetOwner(String username, String email, String password, String profile, String address, int phoneNum, int creditCard, int bankAcc, String acctype);
    }

    public void setRegisterListener(RegisterPOListener listener) {
        this.registerListener = listener;
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
        ButterKnife.bind(this,view);

        if (registerListener != null) {
            Log.e("POSignUp", "registerListener is impl");
        }

        register.setOnClickListener(currView -> {
            Log.e("POSignUp", "Register clicked");
            String uname = username.getText().toString();
            String pw = password.getText().toString();
            String mail = email.getText().toString();
            int num = Integer.parseInt(number.getText().toString());
            String add = address.getText().toString();
            int cc = Integer.parseInt(creditCard.getText().toString());
            int bank = Integer.parseInt(bankAcc.getText().toString());
            String acc = Strings.PET_OWNER;
            String pro = profile.getText().toString();

            registerListener.onRegisterPetOwner(uname,mail,pw,pro,add,num,cc,bank,acc);
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}