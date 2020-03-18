package com.futureelectronics.osso.data;

import android.app.Application;
import android.os.AsyncTask;

import com.futureelectronics.osso.AppExecutors;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Created by Kyle Harman on 12/4/2018.
 */
public class DataRepository {
    private static DataRepository sInstance;
    private OssoDao mOssoDao;
    private LiveData<List<Osso>> mAllOssos;
    private AppExecutors mAppExecutors;

    private DataRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        mOssoDao = db.ossoDao();
        mAllOssos = mOssoDao.getAllOssos();
        mAppExecutors = new AppExecutors();
    }

    public static DataRepository getInstance(Application application) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(application);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<Osso>> getAllOssos() {
        return mAllOssos;
    }

    public LiveData<Osso> getOsso(int id) {
        return mOssoDao.getOsso(id);
    }

    public LiveData<Osso> getOsso(String address) {
        return mOssoDao.getOsso(address);
    }

    public void insert(final Osso osso){
        mAppExecutors.diskIO().execute(() -> {
            mOssoDao.insert(osso);
        });
    }

    public void deleteOsso(final int ossoId){
        mAppExecutors.diskIO().execute(() -> {
            mOssoDao.deleteOsso(ossoId);
        });
    }

//    public void insert(Osso osso){
//        new insertOssoAsyncTask(mOssoDao).execute(osso);
//    }
//
//    private static class insertOssoAsyncTask extends AsyncTask<Osso, Void, Void>{
//        private OssoDao mAsyncTaskDao;
//
//        insertOssoAsyncTask(OssoDao dao){
//            mAsyncTaskDao = dao;
//        }
//
//        @Override
//        protected Void doInBackground(final Osso... params){
//            mAsyncTaskDao.insert(params[0]);
//            return null;
//        }
//    }
}
