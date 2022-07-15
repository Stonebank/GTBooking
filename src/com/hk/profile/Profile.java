package com.hk.profile;

import org.slf4j.Logger;

public enum Profile {

    YOUR_PROFILE("YOUR PHONE NUMBER"),

    ;

    private final String phone_number;

    Profile(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPhone_number() {
        return phone_number;
    }

    @Override
    public String toString() {
        return name();
    }

    public static Profile getProfile(Logger logger, String input) {
        for (Profile profile : Profile.values()) {
            if (input.equalsIgnoreCase(profile.toString())) {
                logger.info("SELECTED PROFILE: " + profile);
                return profile;
            }
        }
        logger.error("ERROR! Profile " + input + " does not exist. (TODO: ADD A NEW PROFILE FEATURE)");
        return null;
    }

}
