package com.futureelectronics.osso;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.futureelectronics.osso.data.Osso;
import com.futureelectronics.osso.databinding.OssoSettingsFragmentBinding;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OssoSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OssoSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OssoSettingsFragment extends Fragment {
    private final static String TAG = OssoSettingsFragment.class.getSimpleName();
    private OssoDataViewModel mViewModel;

    private OssoSettingsFragmentBinding mBinding;

    public OssoSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OssoSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static OssoSettingsFragment newInstance(String param1, String param2) {
//        OssoSettingsFragment fragment = new OssoSettingsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = OssoSettingsFragmentBinding.inflate(inflater, container, false);
        mBinding.setPetNameChangedCallback(mOssoPetNameChanged);

        setHasOptionsMenu(true);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity()).get(OssoDataViewModel.class);

        subscribeUi(mViewModel.getOsso());
    }

    @Override
    public void onDetach() {
        setHasOptionsMenu(false);
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_osso_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_delete:
                mViewModel.deleteOsso();
                setHasOptionsMenu(false);
                Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment).navigateUp();
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void subscribeUi(LiveData<Osso> liveData) {
        // Update the list when the data changes
        liveData.observe(this, new Observer<Osso>() {
            @Override
            public void onChanged(@Nullable Osso myOsso) {
                mBinding.setOsso(myOsso);
//                if (myOsso != null) {
//                    mBinding.setHasOssos(!myOssos.isEmpty());
//                    mOssoAdapter.setOssoList(myOssos);
//                } else {
//                    mBinding.setHasOssos(false);
//                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }

    public interface OssoPetNameChangedCallback {
        void onPetNameChanged(CharSequence newValue);
    }

    private final OssoPetNameChangedCallback mOssoPetNameChanged = (newValue) -> {
        Log.d(TAG, "New Osso petName = "+newValue.toString());
        mBinding.getOsso().petName = newValue.toString();
        mViewModel.updateOssoInDb(mBinding.getOsso());
    };
}
