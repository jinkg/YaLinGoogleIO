package com.yalin.googleio.framework;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public interface UpdatableView<M> {
    void displayData(M model, QueryEnum query);

    void displayErrorMessage(QueryEnum query);

    Uri getDataUri(QueryEnum query);

    Context getContext();

    void addListener(UserActionListener listener);

    interface UserActionListener {
        void onUserAction(UserActionEnum actionEnum, @Nullable Bundle args);
    }
}
