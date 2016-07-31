package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.content.Intent;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment.REQUEST_EDIT_TASK;

public class TaskDetailNavigator {

    private final TaskDetailFragment mTaskDetailFragment;

    public TaskDetailNavigator(TaskDetailFragment mTaskDetailFragment) {
        this.mTaskDetailFragment = mTaskDetailFragment;
    }

    public void showEditTask(String taskId) {
        Intent intent = new Intent(mTaskDetailFragment.getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        mTaskDetailFragment.startActivityForResult(intent, REQUEST_EDIT_TASK);
    }
}
