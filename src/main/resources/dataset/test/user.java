    package com.legacy;
    public class user {
        private String dbUrl = "mysql://localhost:3306";
        public void save(String user) {
            // Saving user to database...
            if (user == null) return;
            System.out.println("Saved " + user);
        }
    }