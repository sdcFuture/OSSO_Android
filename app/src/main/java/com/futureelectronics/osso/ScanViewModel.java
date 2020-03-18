package com.futureelectronics.osso;

import android.app.Application;

import com.futureelectronics.osso.data.DataRepository;
import com.futureelectronics.osso.data.Osso;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ScanViewModel extends AndroidViewModel {
    // TODO: Implement the ViewModel

    private DataRepository mRepository;

    public ScanViewModel(Application application){
        super(application);
        mRepository = DataRepository.getInstance(application);
    }

    public void insertOsso(Osso osso){
        mRepository.insert(osso);
    }

    public int getOssoId(String address)
    {
        LiveData<Osso> osso = mRepository.getOsso(address);

        if(osso == null || osso.getValue() == null){
            return -1;
        }

        return osso.getValue().id;
    }
}
