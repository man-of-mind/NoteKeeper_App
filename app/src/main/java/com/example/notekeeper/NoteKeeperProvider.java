package com.example.notekeeper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.NoteKeeperProviderContract.CourseIdColumns;
import com.example.notekeeper.NoteKeeperProviderContract.Courses;
import com.example.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteKeeperProvider extends ContentProvider {

    private NoteKeeperOpenHelper mDbOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;

    public static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);
                //content://com.example.notekeeper.provider/notes/1
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                //throw exception saying this is a read only table
                break;
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs
                ,null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs
                        ,null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null,
                        null, null);
                break;
        }
        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];
        for (int idx = 0; idx < projection.length; idx++){
            columns[idx] = projection[idx].equals(BaseColumns._ID)  || projection[idx].equals(CourseIdColumns.COLUMN_COURSE_ID) ?
                    NoteInfoEntry.getQName(projection[idx]) : projection[idx];
        }

        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON "
                + NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " + CourseInfoEntry.getQName(
                        CourseInfoEntry.COLUMN_COURSE_ID);

        return db.query(tablesWithJoin, columns, selection, selectionArgs, null,
                null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
