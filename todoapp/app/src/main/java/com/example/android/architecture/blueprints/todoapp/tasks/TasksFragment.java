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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.android.architecture.blueprints.todoapp.Event;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.databinding.TasksFragBinding;
import com.example.android.architecture.blueprints.todoapp.util.SnackbarUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Intent.ACTION_BATTERY_CHANGED;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment {

    private TasksViewModel mTasksViewModel;

    private TasksFragBinding mTasksFragBinding;

    private final Handler mHandle = new Handler();

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
                mTasksViewModel.clearAllTasks();
            }
        }
    };
    private FloatingActionButton fab;

    public TasksFragment() {
        // Requires empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTasksFragBinding = TasksFragBinding.inflate(inflater, container, false);

        mTasksViewModel = TasksActivity.obtainViewModel(getActivity());

        mTasksFragBinding.setViewmodel(mTasksViewModel);
        mTasksFragBinding.setLifecycleOwner(getActivity());

        setHasOptionsMenu(true);

        return mTasksFragBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) getContext(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mTasksViewModel.clearCompletedTasks();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mTasksViewModel.loadTasks(true);
                break;
        }
        return true;
    }

    private static void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("results_mvvm.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSnackbar() {
        mTasksViewModel.getSnackbarMessage().observe(this, new Observer<Event<Integer>>() {
            @Override
            public void onChanged(Event<Integer> event) {
                Integer msg = event.getContentIfNotHandled();
                if (msg != null) {
                    SnackbarUtils.showSnackbar(getView(), getString(msg));
                }
            }
        });
    }

    private void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.completed:
                        mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);
                        break;
                }
                mTasksViewModel.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    private void setupFab() {
        fab = getActivity().findViewById(R.id.fab_add_task);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTasksViewModel.addNewTask();
            }
        });
    }

    private void setupListAdapter() {
        ListView listView = mTasksFragBinding.tasksList;

        TasksAdapter mListAdapter = new TasksAdapter(
                new ArrayList<Task>(0),
                mTasksViewModel,
                getActivity()
        );
        listView.setAdapter(mListAdapter);
    }

    private void setupRefreshLayout() {
        ListView listView = mTasksFragBinding.tasksList;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = mTasksFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTasksViewModel.loadTasks(false);
        startTests();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandle.removeCallbacksAndMessages(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupFab();

        setupListAdapter();

        setupRefreshLayout();

        batteryStatus = getActivity().registerReceiver(batteryReceiver, new IntentFilter(ACTION_BATTERY_CHANGED));
        count = 0;
        inicialBateria = 100;
        startTempo = format.format(new Date());
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
}