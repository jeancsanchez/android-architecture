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

package com.example.android.architecture.blueprints.todoapp.addedittask.domain.usecase;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.UseCase;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Retrieves a {@link Task} from the {@link TasksRepository}.
 */
public class GetTask extends UseCase<GetTask.RequestValues, GetTask.ResponseValue> {

    private final TasksDataSource mTasksRepository;

    public GetTask(@NonNull TasksDataSource tasksRepository) {
        super(Schedulers.io());
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues values) {
        return mTasksRepository.getTask(values.getTaskId()).map(new Func1<Task, GetTask.ResponseValue>() {
            @Override
            public ResponseValue call(Task task) {
                return new ResponseValue(task);
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String mTaskId;

        public RequestValues(@NonNull String taskId) {
            mTaskId = checkNotNull(taskId, "taskId cannot be null!");
        }

        public String getTaskId() {
            return mTaskId;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private Task mTask;

        public ResponseValue(@NonNull Task task) {
            mTask = checkNotNull(task, "task cannot be null!");
        }

        public Task getTask() {
            return mTask;
        }
    }
}
