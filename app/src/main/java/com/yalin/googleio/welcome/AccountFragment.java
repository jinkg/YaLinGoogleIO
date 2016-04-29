package com.yalin.googleio.welcome;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.yalin.googleio.R;
import com.yalin.googleio.util.AccountUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class AccountFragment extends WelcomeFragment implements WelcomeActivity.WelcomeActivityContent,
        RadioGroup.OnCheckedChangeListener {
    private static final String TAG = makeLogTag(AccountFragment.class);

    private AccountManager mAccountManager;

    private List<Account> mAccounts;
    private String mSelectedAccount;

    @Override
    public boolean shouldDisplay(Context context) {
        Account account = AccountUtils.getActiveAccount(context);
        return account == null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAccountManager = AccountManager.get(activity);
//        mAccounts = new ArrayList<>(Arrays.asList(mAccountManager.getAccounts()));
        Account account = new Account("YaLin", "Google");
        mAccounts = new ArrayList<>(Arrays.asList(new Account[]{account}));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAccountManager = null;
        mAccounts = null;
        mSelectedAccount = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View layout = inflater.inflate(R.layout.welcome_account_fragment, container, false);
        if (mAccounts == null) {
            LOGD(TAG, "No accounts to display.");
            return null;
        }

        if (getActivity() instanceof WelcomeFragmentContainer) {
            ((WelcomeFragmentContainer) getActivity()).setPositiveButtonEnabled(false);
        }

        RadioGroup accountsContainer = (RadioGroup) layout.findViewById(R.id.welcome_account_list);
        accountsContainer.removeAllViews();
        accountsContainer.setOnCheckedChangeListener(this);
        for (Account account : mAccounts) {
            LOGD(TAG, "Account: " + account.name);
            RadioButton button = new RadioButton(getActivity());
            button.setText(account.name);
            accountsContainer.addView(button);
        }
        return layout;
    }

    @Override
    protected String getPositiveText() {
        return getResourceString(R.string.ok);
    }

    @Override
    protected String getNegativeText() {
        return getResourceString(R.string.cancel);
    }

    @Override
    protected View.OnClickListener getPositiveListener() {
        return new WelcomeFragmentOnClickListener(getActivity()) {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "Marking attending flag.");
                AccountUtils.setActiveAccount(mActivity, mSelectedAccount);
                doNext();
            }
        };
    }

    @Override
    protected View.OnClickListener getNegativeListener() {
        return new WelcomeFragmentOnClickListener(getActivity()) {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "User needs to select an account.");
                doFinish();
            }
        };
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton rb = (RadioButton) group.findViewById(checkedId);
        mSelectedAccount = rb.getText().toString();
        LOGD(TAG, "Checked: " + mSelectedAccount);
        if (getActivity() instanceof WelcomeFragmentContainer) {
            ((WelcomeFragmentContainer) getActivity()).setPositiveButtonEnabled(true);
        }
    }
}
