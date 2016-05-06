package com.yalin.googleio.explore;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.yalin.googleio.R;
import com.yalin.googleio.explore.ExploreModel.ExploreUserActionEnum;
import com.yalin.googleio.explore.data.ItemGroup;
import com.yalin.googleio.explore.data.LiveStreamData;
import com.yalin.googleio.explore.data.SessionData;
import com.yalin.googleio.framework.QueryEnum;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.session.SessionDetailActivity;
import com.yalin.googleio.ui.BaseActivity;
import com.yalin.googleio.ui.widget.CollectionView;
import com.yalin.googleio.ui.widget.DrawShadowFrameLayout;
import com.yalin.googleio.util.UIUtils;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

public class ExploreIOActivity extends BaseActivity {
    private static final String TAG = makeLogTag(ExploreIOActivity.class);

    public static Intent getOpenIntent(Activity activity) {
        return new Intent(activity, ExploreIOActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_io);
        addPresenterFragment(R.id.explore_library_frag,
                new ExploreModel(getApplicationContext()),
                new QueryEnum[]{ExploreModel.ExploreQueryEnum.SESSIONS,
                        ExploreModel.ExploreQueryEnum.TAGS},
                new ExploreUserActionEnum[]{ExploreUserActionEnum.RELOAD});
        registerHideableHeaderView(findViewById(R.id.header_bar));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        enableActionBarAuthHide((CollectionView) findViewById(R.id.explore_collection_view));
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        DrawShadowFrameLayout frame = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        frame.setShadowVisible(shown, shown);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAV_DRAWER_ITEM_EXPLORE;
    }

    public void cardHeaderClicked(View view) {
        LOGD(TAG, "clicked: " + view + " " +
                ((view != null) ? view.getTag() : ""));
        View moreButton = view.findViewById(android.R.id.button1);
        Object tag = moreButton != null ? moreButton.getTag() : null;
        if (tag instanceof LiveStreamData) {
            Uri sessionUri = ScheduleContract.Sessions.buildSessionsAfterUri(UIUtils.getCurrentTime(this));
            ExploreSessionsActivity.openLiveStream(this, sessionUri);
        } else if (tag instanceof ItemGroup) {
            ExploreSessionsActivity.openItemGroup(this, ((ItemGroup) tag).getId());
        }
    }

    public void sessionDetailItemClicked(View view) {
        LOGD(TAG, "clicked: " + view + " " +
                ((view != null) ? view.getTag() : ""));
        Object tag = null;
        if (view != null) {
            tag = view.getTag();
        }

        if (tag instanceof SessionData) {
            SessionData sessionData = (SessionData) view.getTag();
            if (!TextUtils.isEmpty(sessionData.getSessionId())) {
                Uri sessionUri = ScheduleContract.Sessions.buildSessionUri(sessionData.getSessionId());
                SessionDetailActivity.open(this, sessionUri);
            } else {
                LOGE(TAG, "Theme item clicked but session data was null:" + sessionData);
                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
