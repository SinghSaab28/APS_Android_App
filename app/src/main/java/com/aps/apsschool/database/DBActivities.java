package com.aps.apsschool.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.beans.Student;

import java.util.ArrayList;
import java.util.List;

public class DBActivities extends AppCompatActivity {

    private Context context;

    public DBActivities(Context c){
        this.context=c;
    }

    public Boolean verifyStudent(String rollNo){
        Cursor cursor = getStudentDBCursor();
        while(cursor.moveToNext()) {
            if(cursor.getString(0).equalsIgnoreCase("") || cursor.getString(0)==null){
                return false;
            }
            return true;
        }
        cursor.close();
        return false;
    }

    private Cursor getStudentDBCursor(){
        StudentDbHelper dbHelper = new StudentDbHelper(this.context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                StudentContract.StudentEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        return cursor;
    }

    private Cursor getSubjectDBCursor(){
        SubjectsDbHelper dbHelper = new SubjectsDbHelper(this.context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                SubjectsContract.SubjectEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        return cursor;
    }

    private Cursor getCourseDBCursor(){
        CourseDbHelper dbHelper = new CourseDbHelper(this.context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                CourseContract.CourseEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        return cursor;
    }



    public String addStudent(Context context, Student student) {
        StudentDbHelper dbHelper = new StudentDbHelper(context);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(StudentContract.StudentEntry.TABLE_NAME, null, null);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StudentContract.StudentEntry.COLUMN_NAME_ROLL_NO, student.ROLLNUMBER);
        values.put(StudentContract.StudentEntry.COLUMN_NAME_PASSWORD, student.PASSWORD);
        values.put(StudentContract.StudentEntry.COLUMN_NAME_MEDIUM, student.MEDIUM);
        values.put(StudentContract.StudentEntry.COLUMN_NAME_CLASS, student.CLASS);
        values.put(StudentContract.StudentEntry.COLUMN_NAME_SECTION, student.SECTION);
        values.put(StudentContract.StudentEntry.COLUMN_NAME_NAME, student.NAME);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(StudentContract.StudentEntry.TABLE_NAME, null, values);
        return student.ROLLNUMBER;
    }

    public Integer addSubjects(Context context, List<String> subjects) {
        SubjectsDbHelper dbHelper = new SubjectsDbHelper(context);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        for(int i=0; i<subjects.size(); i++) {
            values.put(SubjectsContract.SubjectEntry.COLUMN_NAME_SUBJECT_NAME, subjects.get(i));
            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(SubjectsContract.SubjectEntry.TABLE_NAME, null, values);
        }
        return subjects.size();
    }

    public List<String> getSubjectList() {
        List<String> subjects = new ArrayList<>();
        Cursor cursor = getSubjectDBCursor();
        while(cursor.moveToNext()) {
            if(cursor.getString(0).equalsIgnoreCase("") || cursor.getString(0)==null){
                break;
            }
            subjects.add(cursor.getString(0));
        }
        cursor.close();
        return subjects;
    }

    public String getStudentClass() {
        String division=null;
        Cursor cursor = getStudentDBCursor();
        while(cursor.moveToNext()) {
            if(cursor.getString(3).equalsIgnoreCase("") || cursor.getString(3)==null){
                break;
            }
            division=cursor.getString(3);
        }
        cursor.close();
        return division;
    }

    public String getStudentMedium() {
        String medium=null;
        Cursor cursor = getStudentDBCursor();
        while(cursor.moveToNext()) {
            if(cursor.getString(2)==null || cursor.getString(2).equalsIgnoreCase("")){
                break;
            }
            medium=cursor.getString(2);
        }
        cursor.close();
        return medium;
    }

    public String getStudentRollNo() {
        String rollNo=null;
        Cursor cursor = getStudentDBCursor();
        while(cursor.moveToNext()) {
            if(cursor.getString(0)==null || cursor.getString(0).equalsIgnoreCase("")){
                break;
            }
            rollNo=cursor.getString(0);
        }
        cursor.close();
        return rollNo;
    }

    public void deleteStudentTable(Context ctx){
        StudentDbHelper dbHelper = new StudentDbHelper(this.context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.delete(StudentContract.StudentEntry.TABLE_NAME, null, null);
        dbHelper.onDowngrade(db,0,0);
    }

    public void deleteSubjectTable(Context ctx){
        SubjectsDbHelper dbHelper = new SubjectsDbHelper(this.context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onDowngrade(db,0,0);
    }

    public void setCoursesProgress(Context context, String courseURI, int currentPosition) {
        CourseDbHelper dbHelper = new CourseDbHelper(context);

        ArrayList<String> array_list = dbHelper.getCoursesProgress(courseURI);
        if(array_list.size()>0 && array_list.get(0)!=null && !array_list.get(0).equalsIgnoreCase("")) {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(CourseContract.CourseEntry.COLUMN_NAME_WATCHED_TIME, currentPosition);
            String where=CourseContract.CourseEntry.COLUMN_NAME_COURSE_URI+" = ?";
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            int v = 0;
            v = db.update(CourseContract.CourseEntry.TABLE_NAME,updatedValues, where, new String[]{courseURI});
        }else {
            // Gets the data repository in write mode
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(CourseContract.CourseEntry.COLUMN_NAME_COURSE_URI, courseURI);
            values.put(CourseContract.CourseEntry.COLUMN_NAME_WATCHED_TIME, currentPosition);
            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(CourseContract.CourseEntry.TABLE_NAME, null, values);
        }
    }

    public String getCoursesProgress(Context applicationContext, String videoSelected) {
        CourseDbHelper dbHelper = new CourseDbHelper(this.context);
        ArrayList<String> array_list = dbHelper.getCoursesProgress(videoSelected);
        if(array_list.size()>0 && array_list.get(0)!=null && !array_list.get(0).equalsIgnoreCase("")) {
            return array_list.get(0);
        }
        return null;
    }
}
