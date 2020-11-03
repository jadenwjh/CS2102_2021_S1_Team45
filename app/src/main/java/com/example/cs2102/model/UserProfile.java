package com.example.cs2102.model;

import com.example.cs2102.model.retrofitApi.DataApiService;

public class UserProfile {

    private static UserProfile userProfile;

    public void setUserProfile(String name, String pw, String mail, String pro, String add, String num, String acc) {
        this.username=  name;
        this.password = pw;
        this.email = mail;
        this.address = add;
        this.phoneNum = num;
        this.accType = acc;
        this.profile = pro;
    }

    public static UserProfile getInstance() {
        if (userProfile == null) {
            userProfile = new UserProfile();
        }
        return userProfile;
    }

    private UserProfile() {}

    public String username;
    public String password;
    public String email;
    public String profile;
    public String address;
    public String phoneNum;
    public String accType;
}
