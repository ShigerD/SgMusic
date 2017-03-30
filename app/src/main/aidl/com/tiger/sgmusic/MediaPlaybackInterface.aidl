// MediaPlaybackInterface.aidl
package com.tiger.sgmusic;

// Declare any non-default types here with import statements

interface MediaPlaybackInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void play(in int index);
    void seekTo(in int position);
    void pause();
    void start();
    void next();
    void previous();
    boolean isPlaying();
    void getLatestInfo();
    int getMediaDuration();
    int getMediaCurDuration();
}
