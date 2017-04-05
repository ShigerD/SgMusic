package com.tiger.sgmusic;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

import android.text.TextUtils;

import java.io.File;


public class ID3Info {
    /**
     * mp3 info  using
     */

    public  Cursor mCursor;
    private boolean mRequestToken;
    private Context mContext;
    public ID3Info(Context context) {
        mContext=context;
    }
    public Cursor getCursor() {
        return  mCursor;
    }

    String[] mCursorCols = new String[] {
            "audio._id AS _id", // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.BOOKMARK
    };
    private void tryToGetPlayingId3Info(String path ) {
        if (mCursor == null && !TextUtils.isEmpty(path)) {
            ContentResolver resolver = mContext.getContentResolver();
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
    //
    public String getArtistName(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return null;
                }
            }
            return mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }
    }
    //
    public long getArtistId(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return -1;
                }
            }
            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        }
    }
    //
    public String getAlbumName(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return null;
                }
            }
            return mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        }
    }
    //
    public long getAlbumId(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return -1;
                }
            }
            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }
    //
    public String getTrackName(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    if (!TextUtils.isEmpty(mFilePath)) {
                        return new File(mFilePath).getName();
                    }
                    return null;
                }
            }
            return mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
    }
    /*

    */

}

//    private Cursor mCursor;
//    private boolean mRequestToken;
//    String[] mCursorCols = new String[] {
//            "audio._id AS _id", // index must match IDCOLIDX below
//            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
//            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
//            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
//            MediaStore.Audio.Media.ARTIST_ID,
//            MediaStore.Audio.Media.BOOKMARK
//    };
//
//    private void tryToGetPlayingId3Info(String path) {
//        if (mCursor == null && !TextUtils.isEmpty(path)) {
//            ContentResolver resolver = getContentResolver();
//            Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
//            String where = MediaStore.Audio.Media.DATA + "=?";
//            String[] selectionArgs = new String[] {
//                    path
//            };
//            try {
//                mCursor = resolver.query(uri, mCursorCols, where,
//                        selectionArgs, null);
//                if (mCursor != null) {
//                    if (mCursor.getCount() != 1) {
//                        mCursor.close();
//                        mCursor = null;
//                    } else {
//                        mCursor.moveToNext();
//                    }
//                }
//            } catch (UnsupportedOperationException ex) {
//            } catch (IllegalStateException ex) {
//                ex.printStackTrace();
//            }
//        }
//    }




