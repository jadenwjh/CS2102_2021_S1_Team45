package com.example.cs2102.model.retrofitApi;

public class Strings {

    public static final String PROFILE = "UserProfile";
    public static final String LEAVES = "Leaves";
    public static final String LEAVES_AVAILABILITY = "CT Dates";
    public static final String BIDS = "Bids";
    public static final String PRICES = "Prices";
    public static final String LISTINGS = "Listings";
    public static final String PETS = "Pets";
    public static final String REVIEW = "Review";
    public static final String MOD_BASE_PRICE = "BasePrices";
    public static final String SALARY = "CTSalary";
    public static final String RATING = "CTRating";

    public static final String ADMIN = "admin";
    public static final String PET_OWNER = "petowner";
    public static final String CARE_TAKER = "caretaker";
    public static final String FULL_TIME = "fulltimer";
    public static final String PART_TIME = "parttimer";

    public static final String PET_OWNER_SIGN_UP = "PetOwnerSignUp";
    public static final String CARE_TAKER_SIGN_UP = "CareTakerSignUp";
    public static final String BOTH_SIGN_UP = "BothSignUp";
    public static final String DELETE_USER = "Users/delete";

    public static final String BASE_URL = "https://shielded-oasis-35437.herokuapp.com/";

    public static final String LOGIN = "Users/login";
    public static final String REGISTER = "Users/register";

    public static final String CARE_TAKERS_CONTRACT = "caretaker/contract";
    public static final String CARE_TAKERS = "CareTaker/:caretaker";
    public static final String UPDATE_PRICE_CT = "caretaker/pricing";
    public static final String PETS_THE_CARE_TAKER_CAN_TAKE_CARE = "caretaker/abletocare";
    public static final String CT_FULL_TIME_LEAVE = "caretaker/leaves";
    public static final String CT_BIDS = "caretaker/bids";
    public static final String CT_SUCCESSFUL_BIDS = "caretaker/bids/accepted";
    public static final String CT_PART_TIME_FREE = "caretaker/available";
    public static final String CT_SALARY = "caretaker/summary";
    public static final String CT_REVIEWS = "petowner/ratingsreviews";
    public static final String CARETAKER_STATS = "caretaker/summary";

    public static final String PO_GET_LISTINGS = "petowner/findCareTaker";
    public static final String PETS_PETOWNER = "petowner/pets";
    public static final String SEND_BID_REQUEST = "PetOwner/Bids";
    public static final String ALL_PET_TYPES = "Admin/PetTypes";
    public static final String ADD_PETOWNER_PET = "PetOwner/Pets";
    public static final String ONGOING_BIDS = "petowner/bids";
    public static final String EXPIRED_BIDS = "PetOwner/Bids";
    public static final String LEAVE_RATING = "PetOwner/RatingsReviews";

    public static final String GET_RATINGS = "Admin/summary";
    public static final String STATS = "Admin/petstats";
    public static final String GET_CTINFO = "Admin/ctsummary";
    public static final String FINANCES = "Admin/finances";

    public static String convertDate(String old) {
        String[] dates = old.split("-");
        int m = Integer.parseInt(dates[1]) + 1;
        if (m == 13) {
            m = 1;
        }
        return String.format("%s-%d-%s", dates[0], m, dates[2]);
    }
}
