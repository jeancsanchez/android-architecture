package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;

/**
 * @author Jean Carlos (Github: @jeancsanchez)
 * @date 2019-11-20.
 * Jesus loves you.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Injection.provideTasksRepository(this).deleteAllTasks();
    }
}
