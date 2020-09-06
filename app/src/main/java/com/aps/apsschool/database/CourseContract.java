package com.aps.apsschool.database;

import android.provider.BaseColumns;

public class CourseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private CourseContract() {}

    /* Inner class that defines the table contents */
    public static class CourseEntry implements BaseColumns {
        public static final String TABLE_NAME = "COURSES";
        public static final String COLUMN_NAME_COURSE_URI = "COURSE_URI";
        public static final String COLUMN_NAME_WATCHED_TIME = "WATCHED_TIME";
    }
}
