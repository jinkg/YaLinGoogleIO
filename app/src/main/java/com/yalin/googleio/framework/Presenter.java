package com.yalin.googleio.framework;

import android.content.Context;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public interface Presenter {
    void setModel(Model model);

    void setUpdatableView(UpdatableView view);

    void setInitialQueriesToLoad(QueryEnum[] queries);

    void setValidUserActions(UserActionEnum[] actions);

    void cleanUp();

    Context getContext();
}
