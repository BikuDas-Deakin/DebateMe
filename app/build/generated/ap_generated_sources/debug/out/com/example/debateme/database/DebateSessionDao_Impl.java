package com.example.debateme.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.debateme.models.DebateSession;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DebateSessionDao_Impl implements DebateSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DebateSession> __insertionAdapterOfDebateSession;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public DebateSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDebateSession = new EntityInsertionAdapter<DebateSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `debate_sessions` (`id`,`topic`,`tone`,`messagesJson`,`timestamp`,`messageCount`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DebateSession entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTopic() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTopic());
        }
        if (entity.getTone() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTone());
        }
        if (entity.getMessagesJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getMessagesJson());
        }
        statement.bindLong(5, entity.getTimestamp());
        statement.bindLong(6, entity.getMessageCount());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM debate_sessions WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM debate_sessions";
        return _query;
      }
    };
  }

  @Override
  public void insert(final DebateSession session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfDebateSession.insert(session);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final int id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteById.release(_stmt);
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<DebateSession>> getAllSessions() {
    final String _sql = "SELECT * FROM debate_sessions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"debate_sessions"}, false, new Callable<List<DebateSession>>() {
      @Override
      @Nullable
      public List<DebateSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTopic = CursorUtil.getColumnIndexOrThrow(_cursor, "topic");
          final int _cursorIndexOfTone = CursorUtil.getColumnIndexOrThrow(_cursor, "tone");
          final int _cursorIndexOfMessagesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "messagesJson");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfMessageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "messageCount");
          final List<DebateSession> _result = new ArrayList<DebateSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DebateSession _item;
            final String _tmpTopic;
            if (_cursor.isNull(_cursorIndexOfTopic)) {
              _tmpTopic = null;
            } else {
              _tmpTopic = _cursor.getString(_cursorIndexOfTopic);
            }
            final String _tmpTone;
            if (_cursor.isNull(_cursorIndexOfTone)) {
              _tmpTone = null;
            } else {
              _tmpTone = _cursor.getString(_cursorIndexOfTone);
            }
            final String _tmpMessagesJson;
            if (_cursor.isNull(_cursorIndexOfMessagesJson)) {
              _tmpMessagesJson = null;
            } else {
              _tmpMessagesJson = _cursor.getString(_cursorIndexOfMessagesJson);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpMessageCount;
            _tmpMessageCount = _cursor.getInt(_cursorIndexOfMessageCount);
            _item = new DebateSession(_tmpTopic,_tmpTone,_tmpMessagesJson,_tmpTimestamp,_tmpMessageCount);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
