package com.futureelectronics.osso;

import android.app.Application;

import com.futureelectronics.osso.data.DataRepository;
import com.futureelectronics.osso.data.Osso;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

/**
 * Created by Kyle Harman on 12/5/2018.
 */
public class OssoListViewModel extends AndroidViewModel {

    private DataRepository mRepository;

    private LiveData<List<Osso>> mAllOssos;

    public OssoListViewModel(Application application){
        super(application);
        mRepository = DataRepository.getInstance(application);
        mAllOssos = mRepository.getAllOssos();
    }

    public LiveData<List<Osso>> getAllOssos() { return mAllOssos;}

    public void insert(Osso osso) { mRepository.insert(osso);}

}
