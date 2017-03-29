package com.tiger.sgmusic;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

import android.text.TextUtils;

/**
 * Created by yangcanhu on 3/21/17.
 */

public class ID3Info extends Activity{
    private Cursor mCursor;
    private boolean mRequestToken;
    String[] mCursorCols = new String[] {
            "audio._id AS _id", // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.BOOKMARK
    };

    private void tryToGetPlayingId3Info(String path) {
        if (mCursor == null && !TextUtils.isEmpty(path)) {
            ContentResolver resolver = getContentResolver();
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
            String where = MediaStore.Audio.Media.DATA + "=?";
            String[] selectionArgs = new String[] {
                    path
            };
            try {
                mCursor = resolver.query(uri, mCursorCols, where,
                        selectionArgs, null);
                if (mCursor != null) {
                    if (mCursor.getCount() != 1) {
                        mCursor.close();
                        mCursor = null;
                    } else {
                        mCursor.moveToNext();
                    }
                }
            } catch (UnsupportedOperationException ex) {
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
    }



}
