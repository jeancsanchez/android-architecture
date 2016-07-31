package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment.REQUEST_EDIT_TASK;
import static com.google.common.base.Preconditions.checkNotNull;

public class TaskDetailNavigator {
    private final Activity mActivity;

    public TaskDetailNavigator(@NonNull Activity activity) {
        mActivity = checkNotNull(activity);
    }

    public void showEditTask(String taskId) {
        Intent intent = new Intent(mActivity, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        mActivity.startActivityForResult(intent, REQUEST_EDIT_TASK);
    }
}
