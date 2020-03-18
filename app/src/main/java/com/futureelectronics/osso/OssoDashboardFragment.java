package com.futureelectronics.osso;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futureelectronics.osso.data.Osso;
import com.futureelectronics.osso.databinding.OssoDashboardFragmentBinding;

public class OssoDashboardFragment extends Fragment {
    private final static String TAG = OssoDashboardFragment.class.getSimpleName();

    private OssoDataViewModel mViewModel;

    private OssoDashboardFragmentBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = OssoDashboardFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity()).get(OssoDataViewModel.class);

        subscribeUi(mViewModel.getOsso());
    }

    private void subscribeUi(LiveData<Osso> liveData) {
        if(liveData != null) {
            // Update the list when the data changes
            liveData.observe(this, new Observer<Osso>() {
                @Override
                public void onChanged(@Nullable Osso myOsso) {
                    mBinding.setOsso(myOsso);
                    // espresso does not know how to wait for data binding's loop so we execute changes
                    // sync.
                    mBinding.executePendingBindings();
                }
            });
        }
    }



}
