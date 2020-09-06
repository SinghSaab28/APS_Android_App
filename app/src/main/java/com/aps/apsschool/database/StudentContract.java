package com.aps.apsschool.database;

import android.provider.BaseColumns;

public final class StudentContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private StudentContract() {}

    /* Inner class that defines the table contents */
    public static class StudentEntry implements BaseColumns {
        public static final String TABLE_NAME = "STUDENT";
        public static final String COLUMN_NAME_ROLL_NO = "ROLL_NO";
        public static final String COLUMN_NAME_PASSWORD = "PASSWORD";
        public static final String COLUMN_NAME_MEDIUM = "MEDIUM";
        public static final String COLUMN_NAME_CLASS = "CLASS";
        public static final String COLUMN_NAME_SECTION = "SECTION";
        public static final String COLUMN_NAME_NAME = "NAME";
    }
}
