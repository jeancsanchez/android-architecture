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

package com.example.android.architecture.blueprints.todoapp;


import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

/**
 * Runs {@link UseCaseOld}s using a {@link UseCaseScheduler}.
 */
public class UseCaseHandler {

    private static UseCaseHandler INSTANCE;

    private final UseCaseScheduler mUseCaseScheduler;

    public UseCaseHandler(UseCaseScheduler useCaseScheduler) {
        mUseCaseScheduler = useCaseScheduler;
    }

    public <T extends UseCaseOld.RequestValues, R extends UseCaseOld.ResponseValue> void execute(
            final UseCaseOld<T, R> useCaseOld, T values, UseCaseOld.UseCaseCallback<R> callback) {
        useCaseOld.setRequestValues(values);
        useCaseOld.setUseCaseCallback(new UiCallbackWrapper(callback, this));

        // The network request might be handled in a different thread so make sure
        // Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mUseCaseScheduler.execute(new Runnable() {
            @Override
            public void run() {

                useCaseOld.run();
                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }
            }
        });
    }

    public <V extends UseCaseOld.ResponseValue> void notifyResponse(final V response,
                                                                    final UseCaseOld.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.notifyResponse(response, useCaseCallback);
    }

    private <V extends UseCaseOld.ResponseValue> void notifyError(final Error error,
                                                                  final UseCaseOld.UseCaseCallback<V> useCaseCallback) {
        mUseCaseScheduler.onError(error, useCaseCallback);
    }

    private static final class UiCallbackWrapper<V extends UseCaseOld.ResponseValue> implements
            UseCaseOld.UseCaseCallback<V> {
        private final UseCaseOld.UseCaseCallback<V> mCallback;
        private final UseCaseHandler mUseCaseHandler;

        public UiCallbackWrapper(UseCaseOld.UseCaseCallback<V> callback,
                UseCaseHandler useCaseHandler) {
            mCallback = callback;
            mUseCaseHandler = useCaseHandler;
        }

        @Override
        public void onSuccess(V response) {
            mUseCaseHandler.notifyResponse(response, mCallback);
        }

        @Override
        public void onError(Error error) {
            mUseCaseHandler.notifyError(error, mCallback);
        }
    }

    public static UseCaseHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UseCaseHandler(new UseCaseThreadPoolScheduler());
        }
        return INSTANCE;
    }
}
