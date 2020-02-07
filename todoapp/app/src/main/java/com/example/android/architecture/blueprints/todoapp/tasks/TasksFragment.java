/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.tasks;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.ACTION_BATTERY_CHANGED;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment implements TasksContract.View {

    private TasksContract.Presenter mPresenter;

    private TasksAdapter mListAdapter;

    private View mNoTasksView;

    private ImageView mNoTaskIcon;

    private TextView mNoTaskMainView;

    private TextView mNoTaskAddView;

    private static LinearLayout mTasksView;

    private static TextView mFilteringLabelView;

    private static FloatingActionButton fab;

    private Activity activity;

    private View root;
    private Handler mHandle = new Handler();
    private static SwipeRefreshLayout srl;
    private boolean isAdded;

    private Intent batteryStatus;
    private float inicialBateria;
    private float finalBateria;
    private String startTempo;
    private String finalTempo;
    private int count = 0;
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_BATTERY_CHANGED)) {
                batteryStatus = intent;
                float currentBattery = getBatteryPct();

                if (inicialBateria == currentBattery) {
                    return;
                }

                finalBateria = currentBattery;
                finalTempo = format.format(new Date());


                String data = inicialBateria + "," + finalBateria + "," + count + "," + startTempo + "," + finalTempo + "\n";
                writeToFile(data, context);

                count = 0;
                inicialBateria = finalBateria;
                startTempo = finalTempo;
                mPresenter.clearAllTasks();
            }
        }
    };


    public TasksFragment() {
        // Requires empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new TasksAdapter(new ArrayList<Task>(0), mItemListener);
        activity = getActivity();
    }


    @Override
    public void setPresenter(@NonNull TasksContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.tasks_frag, container, false);

        // Set up tasks view
        ListView listView = root.findViewById(R.id.tasks_list);
        listView.setAdapter(mListAdapter);
        mFilteringLabelView = root.findViewById(R.id.filteringLabel);
        mTasksView = root.findViewById(R.id.tasksLL);

        // Set up  no tasks view
        mNoTasksView = root.findViewById(R.id.noTasks);
        mNoTaskIcon = root.findViewById(R.id.noTasksIcon);
        mNoTaskMainView = root.findViewById(R.id.noTasksMain);
        mNoTaskAddView = root.findViewById(R.id.noTasksAdd);
        mNoTaskAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTask();
            }
        });

        // Set up floating action button
        fab = getActivity().findViewById(R.id.fab_add_task);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.addNewTask();
            }
        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadTasks(false);
            }
        });

        setHasOptionsMenu(true);
        activity = getActivity();

        return root;
    }


    private static void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("results.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mPresenter.clearCompletedTasks();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mPresenter.loadTasks(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mPresenter.setFiltering(TasksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.completed:
                        mPresenter.setFiltering(TasksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        mPresenter.setFiltering(TasksFilterType.ALL_TASKS);
                        break;
                }
                mPresenter.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    /**
     * Listener for clicks on tasks in the ListView.
     */
    TaskItemListener mItemListener = new TaskItemListener() {
        @Override
        public void onTaskClick(Task clickedTask) {
            mPresenter.openTaskDetails(clickedTask);
        }

        @Override
        public void onCompleteTaskClick(Task completedTask) {
            mPresenter.completeTask(completedTask);
        }

        @Override
        public void onActivateTaskClick(Task activatedTask) {
            mPresenter.activateTask(activatedTask);
        }
    };

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }

        srl = getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        mHandle.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showTasks(List<Task> tasks) {
        mListAdapter.replaceData(tasks);

        mTasksView.setVisibility(View.VISIBLE);
        mNoTasksView.setVisibility(View.GONE);
    }

    @Override
    public void showNoActiveTasks() {
        mNoTaskMainView.setText(getResources().getString(R.string.no_tasks_active));
        showNoTasksViews(R.drawable.ic_check_circle_24dp, false);
    }

    @Override
    public void showNoTasks() {
        mNoTaskMainView.setText(getResources().getString(R.string.no_tasks_all));
        showNoTasksViews(R.drawable.ic_assignment_turned_in_24dp, false);
    }

    @Override
    public void showNoCompletedTasks() {
        mNoTaskMainView.setText(getResources().getString(R.string.no_tasks_completed));
        showNoTasksViews(R.drawable.ic_verified_user_24dp, false);
    }

    @Override
    public void showSuccessfullySavedMessage() {
        if (activity != null) {
            Snackbar.make(getView(), getString(R.string.successfully_saved_task_message), Snackbar.LENGTH_LONG).show();
        }
    }

    private void showNoTasksViews(int iconRes, boolean showAddView) {
        mTasksView.setVisibility(View.GONE);
        mNoTasksView.setVisibility(View.VISIBLE);

        mNoTaskIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoTaskAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showActiveFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    @Override
    public void showCompletedFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_completed));
    }

    @Override
    public void showAllFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_all));
    }

    @Override
    public void showAddTask() {
        if (activity != null) {
            Intent intent = new Intent(activity, AddEditTaskActivity.class);
            startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK);
        }
    }

    @Override
    public void showTaskDetailsUi(String taskId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        if (activity != null) {
            Intent intent = new Intent(activity, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
            startActivity(intent);
        }
    }

    @Override
    public void showTaskMarkedComplete() {
        if (activity != null) {
            Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showTaskMarkedActive() {
        if (activity != null) {
            Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showCompletedTasksCleared() {
        if (activity != null) {
            Snackbar.make(getView(), getString(R.string.completed_tasks_cleared), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showLoadingTasksError() {
        if (activity != null) {
            Snackbar.make(getView(), getString(R.string.loading_tasks_error), Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean isActive() {
        return isAdded;
    }

    private static class TasksAdapter extends BaseAdapter {

        private List<Task> mTasks;
        private TaskItemListener mItemListener;

        public TasksAdapter(List<Task> tasks, TaskItemListener itemListener) {
            mTasks = tasks;
            mItemListener = itemListener;
        }

        public void replaceData(List<Task> tasks) {
            mTasks = tasks;
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return mTasks.size();
        }

        @Override
        public Task getItem(int i) {
            return mTasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.task_item, viewGroup, false);
            }

            final Task task = getItem(i);

            TextView titleTV = rowView.findViewById(R.id.title);
            titleTV.setText(task.getTitleForList());

            CheckBox completeCB = rowView.findViewById(R.id.complete);

            // Active/completed task UI
            completeCB.setChecked(task.isCompleted());
            if (task.isCompleted()) {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.list_completed_touch_feedback));
            } else {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.touch_feedback));
            }

            completeCB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!task.isCompleted()) {
                        mItemListener.onCompleteTaskClick(task);
                    } else {
                        mItemListener.onActivateTaskClick(task);
                    }
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemListener.onTaskClick(task);
                }
            });

            return rowView;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAdded = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        isAdded = true;
        startTests();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        batteryStatus = getActivity().registerReceiver(batteryReceiver, new IntentFilter(ACTION_BATTERY_CHANGED));
        count = 0;
        inicialBateria = 100;
        startTempo = format.format(new Date());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) getContext(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
    }

    private void startTests() {
        count += 1;
        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.performClick();
            }
        }, 1000);
    }

    private float getBatteryPct() {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level / (float) scale) * 100;
    }

    public interface TaskItemListener {

        void onTaskClick(Task clickedTask);

        void onCompleteTaskClick(Task completedTask);

        void onActivateTaskClick(Task activatedTask);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandle.removeCallbacksAndMessages(null);
    }
}
