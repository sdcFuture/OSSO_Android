package com.futureelectronics.osso;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;

import com.futureelectronics.osso.R;
import com.futureelectronics.osso.bluetooth.BleController;
import com.futureelectronics.osso.data.Osso;
import com.futureelectronics.osso.databinding.MainFragmentBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainFragment extends Fragment {

    private OssoListViewModel mViewModel;

    private MainFragmentBinding mBinding;
    private OssoAdapter mOssoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = MainFragmentBinding.inflate(inflater, container, false);
        mOssoAdapter = new OssoAdapter(mOssoClickCallback);
        mBinding.ossoList.setAdapter(mOssoAdapter);

        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_mainFragment_to_scanFragment);
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(OssoListViewModel.class);

        subscribeUi(mViewModel.getAllOssos());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        BleController.getInstance().disconnectAll();
    }

    private void subscribeUi(LiveData<List<Osso>> liveData) {
        // Update the list when the data changes
        liveData.observe(this, new Observer<List<Osso>>() {
            @Override
            public void onChanged(@Nullable List<Osso> myOssos) {
                if (myOssos != null) {
                    mBinding.setHasOssos(!myOssos.isEmpty());
                    mOssoAdapter.setOssoList(myOssos);
                } else {
                    mBinding.setHasOssos(false);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }

    private final OssoClickCallback mOssoClickCallback = new OssoClickCallback() {
        @Override
        public void onClick(Osso osso) {
            MainFragmentDirections.ActionMainFragmentToOssoDetailsFragment navAction = MainFragmentDirections.actionMainFragmentToOssoDetailsFragment(osso.address, null);
            Navigation.findNavController(getActivity(), R.id.osso_list).navigate(navAction);
        }
    };

}
