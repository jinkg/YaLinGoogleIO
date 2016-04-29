package com.yalin.googleio.framework;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yalin.googleio.util.ThrottledContentObserver;

import java.util.HashMap;
import java.util.Map;

import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class PresenterFragmentImpl extends Fragment implements Presenter, UpdatableView.UserActionListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = makeLogTag(PresenterFragmentImpl.class);
    public static final String KEY_RUN_QUERY_ID = "RUN_QUERY_ID";

    private Model mModel;

    private UpdatableView<Model> mUpdatableView;

    private QueryEnum[] mInitQueriesToLoad;

    private UserActionEnum[] mValidUserActions;

    private HashMap<Uri, ThrottledContentObserver> mContentObservers;

    private LoaderIdlingResource mLoaderIdlingResource;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContentObservers != null) {
            for (Map.Entry<Uri, ThrottledContentObserver> entry : mContentObservers.entrySet()) {
                activity.getContentResolver().registerContentObserver(entry.getKey(), true, entry.getValue());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cleanUp();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLoaderIdlingResource =
                new LoaderIdlingResource(getClass().getName() + "/" + getId(), getLoaderManager());

        if (mInitQueriesToLoad != null && mInitQueriesToLoad.length > 0) {
            LoaderManager manager = getLoaderManager();
            for (QueryEnum query : mInitQueriesToLoad) {
                manager.initLoader(query.getId(), null, this);
            }
        } else {
            mUpdatableView.displayData(mModel, null);
        }
    }

    @Override
    public void setModel(Model model) {
        mModel = model;
    }

    @Override
    public void setUpdatableView(UpdatableView view) {
        mUpdatableView = view;
        mUpdatableView.addListener(this);
    }

    @Override
    public void setInitialQueriesToLoad(QueryEnum[] queries) {
        mInitQueriesToLoad = queries;
    }

    @Override
    public void setValidUserActions(UserActionEnum[] actions) {
        mValidUserActions = actions;
    }

    @Override
    public void cleanUp() {
        mUpdatableView = null;
        mModel = null;
        if (mContentObservers != null) {
            for (ThrottledContentObserver observer : mContentObservers.values()) {
                getActivity().getContentResolver().unregisterContentObserver(observer);
            }
        }
    }

    @Override
    public Context getContext() {
        return mUpdatableView.getContext();
    }

    @Override
    public void onUserAction(UserActionEnum action, @Nullable Bundle args) {
        boolean isValid = false;
        if (mValidUserActions != null && mValidUserActions.length > 0 && action != null) {
            for (UserActionEnum actionEnum : mValidUserActions) {
                if (actionEnum.getId() == action.getId()) {
                    isValid = true;
                    break;
                }
            }
        }

        if (isValid) {
            if (args != null && args.containsKey(KEY_RUN_QUERY_ID)) {
                Object queryId = args.get(KEY_RUN_QUERY_ID);
                if (queryId instanceof Integer) {
                    LoaderManager manager = getLoaderManager();
                    manager.restartLoader((Integer) queryId, args, this);
                } else {
                    LOGE(TAG, "onUserAction called with a bundle containing KEY_RUN_QUERY_ID but "
                            + "the value is not an Integer so it's not a valid query id!");
                }
            }
            boolean success = mModel.requestModelUpdate(action, args);
            if (!success) {
                LOGE(TAG, "Model doesn't implement user action " + action.getId() + ". Have you "
                        + "forgotten to implement this UserActionEnum in you model, or have you "
                        + "called setValidUserActions on your presenter with a UserActionEnum that "
                        + "it shouldn't support?");
            }
        } else {
            LOGE(TAG, "Invalid user action " + action.getId() + ". Have you called "
                    + "setValidUserActions on your presenter, with all the UserActionEnum you want "
                    + "to support?");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> cursorLoader = createLoader(id, args);
        mLoaderIdlingResource.onLoaderStarted(cursorLoader);
        return cursorLoader;
    }

    protected Loader<Cursor> createLoader(int id, Bundle args) {
        Uri uri = mUpdatableView.getDataUri(QueryEnumHelper.getQueryForId(id, mModel.getQueries()));
        return mModel.createCursorLoader(id, uri, args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        processData(loader, data);
        mLoaderIdlingResource.onLoaderFinished(loader);
    }

    protected void processData(Loader<Cursor> loader, Cursor data) {
        QueryEnum query = QueryEnumHelper.getQueryForId(loader.getId(), mModel.getQueries());
        boolean successfulDataRead = mModel.readDataFromCursor(data, query);
        if (successfulDataRead) {
            mUpdatableView.displayData(mModel, query);
        } else {
            mUpdatableView.displayErrorMessage(query);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLoaderIdlingResource.onLoaderFinished(loader);
    }
}
