package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;
import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTaskNavigator {
    private final Activity mActivity;

    public AddEditTaskNavigator(@NonNull Activity activity) {
        mActivity = checkNotNull(activity);
    }

    public void showTasksList() {
        mActivity.setResult(Activity.RESULT_OK);
        mActivity.finish();
    }
}
