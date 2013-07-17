/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2013  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.geopaparazzi.library.database;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class GPLog {

    /**
     * If <code>true</code>, android logging is activated.
     */
    public final static boolean LOG_ANDROID = true;
    /**
     * If <code>true</code>, normal logging is activated.
     */
    public static boolean LOG = true;
    /**
     * If <code>true</code> heavy logging is activated.
     */
    public static boolean LOG_HEAVY = true;

    public static final String ERROR_TAG = "ERROR";

    public static final String TABLE_LOG = "log";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATAORA = "dataora";
    public static final String COLUMN_LOGMSG = "logmsg";

    /**
     * Create the default log table.
     * 
     * @param sqliteDatabase the db into which to create the table.
     * @throws IOException
     */
    public static void createTables( SQLiteDatabase sqliteDatabase ) throws IOException {
        StringBuilder sB = new StringBuilder();

        sB = new StringBuilder();
        sB.append("CREATE TABLE ");
        sB.append(TABLE_LOG);
        sB.append(" (");
        sB.append(COLUMN_ID);
        sB.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sB.append(COLUMN_DATAORA).append(" INTEGER NOT NULL, ");
        sB.append(COLUMN_LOGMSG).append(" TEXT ");
        sB.append(");");
        String CREATE_TABLE = sB.toString();

        sB = new StringBuilder();
        sB.append("CREATE INDEX " + TABLE_LOG + "_" + COLUMN_ID + " ON ");
        sB.append(TABLE_LOG);
        sB.append(" ( ");
        sB.append(COLUMN_ID);
        sB.append(" );");
        String CREATE_INDEX = sB.toString();

        sB = new StringBuilder();
        sB.append("CREATE INDEX " + TABLE_LOG + "_" + COLUMN_DATAORA + " ON ");
        sB.append(TABLE_LOG);
        sB.append(" ( ");
        sB.append(COLUMN_DATAORA);
        sB.append(" );");
        String CREATE_INDEX_DATE = sB.toString();

        sqliteDatabase.beginTransaction();
        try {
            sqliteDatabase.execSQL(CREATE_TABLE);
            sqliteDatabase.execSQL(CREATE_INDEX);
            sqliteDatabase.execSQL(CREATE_INDEX_DATE);
            sqliteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            sqliteDatabase.endTransaction();
        }
    }

    /**
     * Add a new log entry.
     * 
     * @param logMessage the message to insert in the log.
     * @throws IOException
     */
    public static void addLogEntry( String logMessage ) throws IOException {
        SQLiteDatabase sqliteDatabase = ADbHelper.getInstance().getDatabase();
        ContentValues values = new ContentValues();
        long time = new Date().getTime();
        values.put(COLUMN_DATAORA, time);
        values.put(COLUMN_LOGMSG, logMessage);
        insertOrThrow(sqliteDatabase, TABLE_LOG, values);

        if (LOG_ANDROID) {
            StringBuilder sb = new StringBuilder();
            sb.append(time);
            sb.append(": ");
            sb.append(logMessage);
            Log.i("GPLOG", sb.toString());
        }
    }

    /**
     * Add a log entry by concatenating (;) some more info in the message.
     * 
     * @param caller the calling class or tage name.
     * @param user a user name or id. If
     *              <code>null</code>, defaults to UNKNOWN_USER
     * @param tag a tag for the log message. If <code>null</code>, 
     *              defaults to INFO. 
     * @param logMessage the message itself.
     * @throws IOException
     */
    public static void addLogEntry( Object caller, //
            String user, //
            String tag,//
            String logMessage ) throws IOException {

        StringBuilder sb = new StringBuilder();
        if (user == null || user.length() == 0) {
            user = "UNKNOWN_USER";
        }
        sb.append(user).append(";");
        if (tag == null || tag.length() == 0) {
            tag = "INFO";
        }
        sb.append(tag).append(";");

        if (caller != null) {
            String name = toName(caller);
            if (name.length() > 0)
                sb.append(name).append(": ");
        }
        sb.append(logMessage);
        addLogEntry(sb.toString());
    }

    public static void error( Object caller, Throwable t ) throws IOException {
        addLogEntry(caller, null, ERROR_TAG, t.getLocalizedMessage());
        if (LOG_ANDROID) {
            Log.i("GPLOG_ERROR", t.getLocalizedMessage());
        }
        String stackTrace = Log.getStackTraceString(t);
        addLogEntry(caller, null, ERROR_TAG, stackTrace);
        if (LOG_ANDROID) {
            Log.i("GPLOG_ERROR", stackTrace);
        }
    }

    /**
     * Do an insert or throw with the proper error handling.
     * @param table
     * @param values
     * 
     * @return
     * @throws IOException
     */
    private static long insertOrThrow( SQLiteDatabase sqliteDatabase, String table, ContentValues values ) throws IOException {
        long id = sqliteDatabase.insertOrThrow(table, null, values);
        if (id == -1) {
            Set<Entry<String, Object>> valueSet = values.valueSet();
            StringBuilder sb = new StringBuilder();
            sb.append("Insert failed with: \n");
            for( Entry<String, Object> entry : valueSet ) {
                sb.append("(").append(entry.getKey()).append(",");
                sb.append(entry.getValue()).append(")\n");
            }
            String message = sb.toString();
            throw new IOException(message);
        }
        return id;
    }

    /**
     * Clear the log table.
     * 
     * @param db the db to use.
     * @throws Exception
     */
    public static void clearLogTable( SQLiteDatabase db ) throws Exception {
        String deleteLogQuery = "delete from " + TABLE_LOG;
        db.beginTransaction();
        try {
            db.execSQL(deleteLogQuery);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * @return the query to get id,datetimestring,logmsg.
     */
    public static String getLogQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("select _id, datetime(dataora/1000, 'unixepoch', 'localtime') as timestamp, logmsg from log order by dataora desc");
        String query = sb.toString();
        return query;
    }

    private static String toName( Object obj ) {
        if (obj instanceof String) {
            String name = (String) obj;
            return name;
        }
        String simpleName = obj.getClass().getSimpleName();
        return simpleName.toUpperCase();
    }
}