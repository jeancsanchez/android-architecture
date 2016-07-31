package com.example.android.architecture.blueprints.todoapp.tasks;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;

import static com.google.common.base.Preconditions.checkNotNull;


public class TasksNavigator {

    private final TasksFragment mTasksFragment;

    public TasksNavigator(@NonNull TasksFragment tasksFragment) {
        mTasksFragment = checkNotNull(tasksFragment, "tasksFragment cannot be null");
    }

    public void showAddTask() {
        Intent intent = new Intent(mTasksFragment.getContext(), AddEditTaskActivity.class);
        mTasksFragment.startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK);
    }

    public void showTaskDetailsUi(String taskId) {
        // in its own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        Intent intent = new Intent(mTasksFragment.getContext(), TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        mTasksFragment.startActivity(intent);
    }
}
