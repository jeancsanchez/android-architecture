package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;

import static com.google.common.base.Preconditions.checkNotNull;


public class TasksNavigator {

    private final Activity mActivity;

    public TasksNavigator(@NonNull Activity activity) {
        mActivity = checkNotNull(activity);
    }

    public void showAddTask() {
        Intent intent = new Intent(mActivity, AddEditTaskActivity.class);
        mActivity.startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK);
    }

    public void showTaskDetailsUi(String taskId) {
        // in its own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        Intent intent = new Intent(mActivity, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        mActivity.startActivity(intent);
    }
}
