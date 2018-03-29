package com.icool.reader.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.icool.reader.component.reader.dao.BookMarkBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "BOOK_MARK_BEAN".
*/
public class BookMarkBeanDao extends AbstractDao<BookMarkBean, Long> {

    public static final String TABLENAME = "BOOK_MARK_BEAN";

    /**
     * Properties of entity BookMarkBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property _id = new Property(0, Long.class, "_id", true, "_id");
        public final static Property BookId = new Property(1, String.class, "bookId", false, "BOOK_ID");
        public final static Property Time = new Property(2, Long.class, "time", false, "TIME");
        public final static Property ChapterName = new Property(3, String.class, "chapterName", false, "CHAPTER_NAME");
        public final static Property ChapterId = new Property(4, String.class, "chapterId", false, "CHAPTER_ID");
        public final static Property Progress = new Property(5, Float.class, "progress", false, "PROGRESS");
        public final static Property Content = new Property(6, String.class, "content", false, "CONTENT");
    }


    public BookMarkBeanDao(DaoConfig config) {
        super(config);
    }
    
    public BookMarkBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BOOK_MARK_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: _id
                "\"BOOK_ID\" TEXT NOT NULL ," + // 1: bookId
                "\"TIME\" INTEGER NOT NULL ," + // 2: time
                "\"CHAPTER_NAME\" TEXT NOT NULL ," + // 3: chapterName
                "\"CHAPTER_ID\" TEXT NOT NULL ," + // 4: chapterId
                "\"PROGRESS\" REAL NOT NULL ," + // 5: progress
                "\"CONTENT\" TEXT NOT NULL );"); // 6: content
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BOOK_MARK_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, BookMarkBean entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindString(2, entity.getBookId());
        stmt.bindLong(3, entity.getTime());
        stmt.bindString(4, entity.getChapterName());
        stmt.bindString(5, entity.getChapterId());
        stmt.bindDouble(6, entity.getProgress());
        stmt.bindString(7, entity.getContent());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, BookMarkBean entity) {
        stmt.clearBindings();
 
        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }
        stmt.bindString(2, entity.getBookId());
        stmt.bindLong(3, entity.getTime());
        stmt.bindString(4, entity.getChapterName());
        stmt.bindString(5, entity.getChapterId());
        stmt.bindDouble(6, entity.getProgress());
        stmt.bindString(7, entity.getContent());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public BookMarkBean readEntity(Cursor cursor, int offset) {
        BookMarkBean entity = new BookMarkBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // _id
            cursor.getString(offset + 1), // bookId
            cursor.getLong(offset + 2), // time
            cursor.getString(offset + 3), // chapterName
            cursor.getString(offset + 4), // chapterId
            cursor.getFloat(offset + 5), // progress
            cursor.getString(offset + 6) // content
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, BookMarkBean entity, int offset) {
        entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setBookId(cursor.getString(offset + 1));
        entity.setTime(cursor.getLong(offset + 2));
        entity.setChapterName(cursor.getString(offset + 3));
        entity.setChapterId(cursor.getString(offset + 4));
        entity.setProgress(cursor.getFloat(offset + 5));
        entity.setContent(cursor.getString(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(BookMarkBean entity, long rowId) {
        entity.set_id(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(BookMarkBean entity) {
        if(entity != null) {
            return entity.get_id();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(BookMarkBean entity) {
        return entity.get_id() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
