package com.bocahkoding.igame.utils;

import android.view.View;

public class ViewAnimation {

    public static void hideBottomBar(View view) {
        int moveY = 2 * view.getHeight();
        view.animate()
                .translationY(moveY)
                .setDuration(300)
                .start();
    }

    public static void showBottomBar(View view) {
        view.animate()
                .translationY(0)
                .setDuration(300)
                .start();
    }

    public static void hideToolbar(View view) {
        int moveY = 2 * view.getHeight();
        view.animate()
                .translationY(-moveY)
                .setDuration(300)
                .start();
    }

    public static void showToolbar(View view) {
        view.animate()
                .translationY(0)
                .setDuration(300)
                .start();
    }
}
