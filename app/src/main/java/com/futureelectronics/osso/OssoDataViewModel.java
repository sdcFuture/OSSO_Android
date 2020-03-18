package com.futureelectronics.osso;

import android.app.Application;

import com.futureelectronics.osso.data.DataRepository;
import com.futureelectronics.osso.data.Osso;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class OssoDataViewModel extends AndroidViewModel {
    private MediatorLiveData<Osso> mOssoSelected;
    private DataRepository mRepository;

    public OssoDataViewModel(Application application){
        super(application);
        mRepository = DataRepository.getInstance(application);
        mOssoSelected = new MediatorLiveData<>();
    }

    public void selectOsso(int ossoId) {
        mOssoSelected.addSource(mRepository.getOsso(ossoId),
                osso -> {
                    Osso currentOsso = mOssoSelected.getValue();
                    if(currentOsso != null && osso!=null && currentOsso.id == osso.id){
                        copyOssoIgnores(currentOsso, osso);
                    }
                    mOssoSelected.postValue(osso);
                });
    }

    public void selectOsso(String address) {
        mOssoSelected.addSource(mRepository.getOsso(address),
                osso -> {
                    Osso currentOsso = mOssoSelected.getValue();
                    if(currentOsso != null && osso!=null && currentOsso.id == osso.id){
                        copyOssoIgnores(currentOsso, osso);
                    }
                    mOssoSelected.postValue(osso);
                });
    }

    public LiveData<Osso> getOsso() {
        return mOssoSelected;
    }

    public void updateOsso(Osso osso){
        mOssoSelected.postValue(osso);
    }

    public void updateOssoInDb(Osso osso)
    {
        mRepository.insert(osso);
    }

    public void deleteOsso(){
        if(mOssoSelected != null && mOssoSelected.getValue() != null){
            mRepository.deleteOsso(mOssoSelected.getValue().id);
            mOssoSelected.postValue(null);
        }
    }

    private void copyOssoIgnores(Osso source, Osso dest){
        dest.exposure_time = source.exposure_time;
        dest.uv_index = source.uv_index;
        dest.temperature = source.temperature;
        dest.humidity = source.humidity;
        dest.ir_temperature = source.ir_temperature;
        dest.rssi = source.rssi;
    }

    private boolean savedInfoIsDiff(Osso a, Osso b)
    {
        if(a.lastLatitude != b.lastLatitude
            || a.lastLongitude != b.lastLongitude
            || a.steps != b.steps
            || a.barks != b.barks
            || a.battery != b.battery
            || !a.address.equals(b.address)
            || !a.petName.equals(b.petName)
            || !a.fw.equals(b.fw)){
            return false;
        }

        return true;
    }
}
