package com.yalin.googleio.framework;

import android.app.LoaderManager;
import android.content.Loader;
import android.support.test.espresso.IdlingResource;

import java.util.HashSet;
import java.util.Set;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class LoaderIdlingResource implements IdlingResource {
    private ResourceCallback mResourceCallback;

    private Set<Integer> mLoadersLoading = new HashSet<>();
    private final String mName;
    private final LoaderManager mLoaderManager;

    public LoaderIdlingResource(String name, LoaderManager loaderManager) {
        mName = name;
        mLoaderManager = loaderManager;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean isIdleNow() {
        cleanupLoaders();
        return mLoadersLoading.isEmpty();
    }

    private void cleanupLoaders() {
        for (int loaderId : mLoadersLoading) {
            Loader loader = mLoaderManager.getLoader(loaderId);
            if (loader == null) {
                mLoadersLoading.remove(loaderId);
            }
        }
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mResourceCallback = callback;
    }

    public void onLoaderStarted(Loader loader) {
        mLoadersLoading.add(loader.getId());
    }

    public void onLoaderFinished(Loader loader) {
        mLoadersLoading.remove(loader.getId());
        if (isIdleNow() && mResourceCallback != null) {
            mResourceCallback.onTransitionToIdle();
        }
    }
}
