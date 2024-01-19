package lk.avn.irenttechs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lk.avn.irenttechs.adapter.CategoryHomeListAdapter;
import lk.avn.irenttechs.adapter.HomeProductListAdapter;
import lk.avn.irenttechs.adapter.SearchResultsAdapter;
import lk.avn.irenttechs.model.Products;

public class SearchFragment extends Fragment {

    private static final String TAG = SearchFragment.class.getName();
    Bundle args;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private String CategoryValue;
    private SearchResultsAdapter searchResultsAdapter;
    AutoCompleteTextView search_input;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);

        args = getArguments();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        products = new ArrayList<>();

        if (getActivity() instanceof HomeActivity) {
            HomeActivity ha = (HomeActivity) getActivity();
            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText("Search");
            MaterialToolbar toolbar = ha.findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(null);
            ImageButton backbtn = ha.findViewById(R.id.back_btn);
            backbtn.setVisibility(View.VISIBLE);
            backbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                        toolbar.setNavigationIcon(R.drawable.menu);
                        backbtn.setVisibility(View.GONE);
                    }
                }
            });
        }


        search_input = getView().findViewById(R.id.search_edit_text);
        fetchProductNames();


        fragment.findViewById(R.id.search_producrs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String search_value = search_input.getText().toString();
                searchFunction(fragment, search_value);
            }
        });


    }
    private void fetchProductNames() {
        CollectionReference productsList = firestore.collection("Products");

        productsList.whereEqualTo("status", "1")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> productNames = new ArrayList<>();
                        Set<String> brandNames = new HashSet<>();
                        Set<String> categoryNames = new HashSet<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Products product = document.toObject(Products.class);
                            if (product != null) {
                                productNames.add(product.getName());
                                brandNames.add(product.getBrand());
                                categoryNames.add(product.getCategory());
                            }
                        }
                        productNames.addAll(brandNames);
                        productNames.addAll(categoryNames);
                        setAutoCompleteAdapter(productNames);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching product names", e);
                    }
                });
    }

    private void setAutoCompleteAdapter(List<String> productNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, productNames);
        search_input.setAdapter(adapter);
    }

    public void searchFunction(View fragment, String search_value) {
        RecyclerView productView2 = fragment.findViewById(R.id.search_rv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);

        searchResultsAdapter = new SearchResultsAdapter(products, getContext(), new SearchResultsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String selectedProductId = searchResultsAdapter.getDocumentId(position);

                Log.i(TAG, "Selected Product ID: " + selectedProductId);

                Bundle bundle = new Bundle();
                bundle.putString("product_id", selectedProductId);

                SingleProductViewFragment singleProductViewFragment = new SingleProductViewFragment();
                singleProductViewFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frame_layouts, singleProductViewFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        productView2.setLayoutManager(gridLayoutManager);
        productView2.setAdapter(searchResultsAdapter);

        products.clear();

        CollectionReference products_list = firestore.collection("Products");

        Query nameQuery = products_list
                .whereEqualTo("status", "1")
                .whereEqualTo("name", search_value);

        Query brandQuery = products_list
                .whereEqualTo("status", "1")
                .whereEqualTo("brand", search_value);

        Query categoryQuery = products_list
                .whereEqualTo("status", "1")
                .whereEqualTo("category", search_value);

        nameQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Products product = document.toObject(Products.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                searchResultsAdapter.notifyDataSetChanged();
            }
        });

        brandQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Products product = document.toObject(Products.class);
                    if (product != null && !products.contains(product)) {
                        products.add(product);
                    }
                }
                searchResultsAdapter.notifyDataSetChanged();
            }
        });
        categoryQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Products product = document.toObject(Products.class);
                    if (product != null && !products.contains(product)) {
                        products.add(product);
                    }
                }
                searchResultsAdapter.notifyDataSetChanged();
            }
        });
    }
}