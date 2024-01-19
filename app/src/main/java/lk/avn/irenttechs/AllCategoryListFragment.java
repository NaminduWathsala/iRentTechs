package lk.avn.irenttechs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import lk.avn.irenttechs.adapter.AllCategoryListAdapter;
import lk.avn.irenttechs.adapter.CategoryHomeListAdapter;
import lk.avn.irenttechs.adapter.CategoryListAdapter;

public class AllCategoryListFragment extends Fragment {
    private static final String TAG = AllCategoryListFragment.class.getName();
    private ArrayList<String> category;
    private List<String> categoryNames;
    private AllCategoryListAdapter listAdapter;
    private FirebaseFirestore fireStore;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_category_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        if (getActivity() instanceof HomeActivity) {
            HomeActivity ha = (HomeActivity) getActivity();
            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText("Categories");
            MaterialToolbar toolbar =  ha.findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(null);
            ImageButton backbtn = ha.findViewById(R.id.back_btn);
            backbtn.setVisibility(View.VISIBLE);
            backbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layouts, new HomeFragment());
                        transaction.commit();
                        getActivity().getSupportFragmentManager().popBackStack();

                        toolbar.setNavigationIcon(R.drawable.menu);
                        backbtn.setVisibility(View.GONE);
                    }
                }
            });
        }

        fireStore = FirebaseFirestore.getInstance();

        RecyclerView productView = fragment.findViewById(R.id.all_category_list_recycle_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
        productView.setLayoutManager(gridLayoutManager);

        fireStore.collection("Product_Category").document("Category")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value.exists()) {
                            categoryNames = (List<String>) value.get("name");

                            if (categoryNames != null) {
                                listAdapter = new AllCategoryListAdapter(categoryNames, getContext(), new AllCategoryListAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(int position) {
                                        String selectedCategory = categoryNames.get(position);
                                        Log.i(TAG, selectedCategory);

                                        CategoryProductListFragment categoryProductListFragment = new CategoryProductListFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("category_name", selectedCategory);
                                        categoryProductListFragment.setArguments(bundle);

                                        if (getActivity() instanceof HomeActivity) {
                                            HomeActivity ha = (HomeActivity) getActivity();
                                            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
                                        }

                                        getParentFragmentManager().beginTransaction().replace(R.id.frame_layouts, categoryProductListFragment).addToBackStack(null).commit();
                                    }
                                });

                                productView.setAdapter(listAdapter);
                            }
                        }
                    }
                });


    }
}