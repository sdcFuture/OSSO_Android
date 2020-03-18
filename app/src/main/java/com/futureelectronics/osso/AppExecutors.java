package com.futureelectronics.osso;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Kyle Harman on 1/22/2019.
 */
public class AppExecutors {
    private final Executor mDiskIO;

    private AppExecutors(Executor diskIO) {
        this.mDiskIO = diskIO;
    }

    public AppExecutors() {
        this(Executors.newSingleThreadExecutor());
    }

    public Executor diskIO() {
        return mDiskIO;
    }
}
