    package com.example.recipebook;

    public class User {
        private String uid;
        private String name;
        private String email;
        private String country;
        private String profileImageUrl;

        public User() {}

        public User(String uid, String name, String email, String country, String profileImageUrl) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.country = country;
            this.profileImageUrl = profileImageUrl;
        }


        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    }