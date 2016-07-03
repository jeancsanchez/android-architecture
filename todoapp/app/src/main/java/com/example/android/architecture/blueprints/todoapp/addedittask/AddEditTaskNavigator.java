package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;

public class AddEditTaskNavigator implements AddEditTaskContract.Navigator {
    private final Activity mActivity;

    public AddEditTaskNavigator(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void showTasksList() {
        mActivity.setResult(Activity.RESULT_OK);
        mActivity.finish();
    }
}
