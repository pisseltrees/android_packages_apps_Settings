/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.notification;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;

import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.accounts.AccountRestrictionHelper;
import com.android.settings.core.PreferenceController;
import com.android.settingslib.RestrictedPreference;

/**
 * Base class for preference controller that handles preference that enforce adjust volume
 * restriction
 */
public class EmergencyBroadcastPreferenceController extends PreferenceController {

    private static final String KEY_CELL_BROADCAST_SETTINGS = "cell_broadcast_settings";

    private AccountRestrictionHelper mHelper;
    private UserManager mUserManager;
    private PackageManager mPm;
    private boolean mCellBroadcastAppLinkEnabled;

    public EmergencyBroadcastPreferenceController(Context context) {
        this(context, new AccountRestrictionHelper(context));
    }

    @VisibleForTesting
    EmergencyBroadcastPreferenceController(Context context, AccountRestrictionHelper helper) {
        super(context);
        mHelper = helper;
        mUserManager = UserManager.get(context);
        mPm = mContext.getPackageManager();
        // Enable link to CMAS app settings depending on the value in config.xml.
        mCellBroadcastAppLinkEnabled = isCellBroadcastAppLinkEnabled();
    }

    @Override
    public void updateState(Preference preference) {
        if (!(preference instanceof RestrictedPreference)) {
            return;
        }
        ((RestrictedPreference) preference).checkRestrictionAndSetDisabled(
            UserManager.DISALLOW_CONFIG_CELL_BROADCASTS);
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CELL_BROADCAST_SETTINGS;
    }

    @Override
    public boolean isAvailable() {
        return mUserManager.isAdminUser() && mCellBroadcastAppLinkEnabled
            && !mHelper.hasBaseUserRestriction(
                UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, UserHandle.myUserId());
    }

    private boolean isCellBroadcastAppLinkEnabled() {
        boolean enabled = mContext.getResources().getBoolean(
            com.android.internal.R.bool.config_cellBroadcastAppLinks);
        if (enabled) {
            try {
                if (mPm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver")
                    == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    enabled = false;  // CMAS app disabled
                }
            } catch (IllegalArgumentException ignored) {
                enabled = false;  // CMAS app not installed
            }
        }
        return enabled;
    }

}