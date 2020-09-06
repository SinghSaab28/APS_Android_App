package com.aps.apsschool.database;

import android.provider.BaseColumns;

public final class SubjectsContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SubjectsContract() {}

    /* Inner class that defines the table contents */
    public static class SubjectEntry implements BaseColumns {
        public static final String TABLE_NAME = "SUBJECTS";
        public static final String COLUMN_NAME_SUBJECT_NAME = "SUBJECT_NAME";
    }
}
