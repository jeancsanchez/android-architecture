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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
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
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static android.content.Intent.ACTION_BATTERY_CHANGED;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment {

    private Intent batteryStatus;

    private TasksViewModel mTasksViewModel;

    private TasksFragBinding mTasksFragBinding;

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_BATTERY_CHANGED)) {
                batteryStatus = intent;
            }
        }
    };

    private FloatingActionButton fab;
    private int count = 1;

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupFab();

        setupListAdapter();

        setupRefreshLayout();

        batteryStatus = getActivity().registerReceiver(batteryReceiver, new IntentFilter(ACTION_BATTERY_CHANGED));
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

        if (getBatteryPercentage() > 99) {
            count += 1;
            Snackbar.make(
                    mTasksFragBinding.getRoot(),
                    "Execução número: " + count,
                    Snackbar.LENGTH_INDEFINITE
            ).show();

            fab.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fab.performClick();
                }
            }, 1000);
        } else {
            Snackbar.make(mTasksFragBinding.getRoot(), "Número de execuções: " + count, Snackbar.LENGTH_INDEFINITE).show();
            writeToFile("MVVM: " + count, getActivity());
        }
    }

    private float getBatteryPercentage() {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (level / (float) scale) * 100;
    }


    private static void writeToFile(String data, Context context) {
        /*-  registrar o tempo levado para executar todas
        -  gráfico excucoes x bateria
        -  gráfico tempo x bateria
        -  gráfico execucoes x tempo*/
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}