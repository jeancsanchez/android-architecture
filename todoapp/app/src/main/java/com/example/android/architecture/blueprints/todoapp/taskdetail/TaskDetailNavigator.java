package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.content.Intent;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment.REQUEST_EDIT_TASK;
import static com.google.common.base.Preconditions.checkNotNull;

public class TaskDetailNavigator {

    private final TaskDetailFragment mTaskDetailFragment;

    public TaskDetailNavigator(TaskDetailFragment taskDetailFragment) {
        this.mTaskDetailFragment = checkNotNull(taskDetailFragment, "taskDetailFragment cannot be null");
    }

    public void showEditTask(String taskId) {
        Intent intent = new Intent(mTaskDetailFragment.getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        mTaskDetailFragment.startActivityForResult(intent, REQUEST_EDIT_TASK);
    }
}
