package lk.avn.irenttechs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import lk.avn.irenttechs.adapter.CategoryHomeListAdapter;
import lk.avn.irenttechs.adapter.CategoryListAdapter;
import lk.avn.irenttechs.model.Products;

public class CategoryProductListFragment extends Fragment {
    private static final String TAG = CategoryProductListFragment.class.getName();
    Bundle args;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private  String CategoryValue;
    private CategoryListAdapter productListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);



        args = getArguments();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        products = new ArrayList<>();

        if (args != null) {
            CategoryValue = args.getString("category_name");
        }

        if (getActivity() instanceof HomeActivity) {
            HomeActivity ha = (HomeActivity) getActivity();
            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText(CategoryValue);
            MaterialToolbar toolbar =  ha.findViewById(R.id.toolbar);
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

        RecyclerView productView = fragment.findViewById(R.id.category_list_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        productListAdapter = new CategoryListAdapter(products, getContext(), new CategoryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String selectedProductId = productListAdapter.getDocumentId(position);

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
        productView.setLayoutManager(linearLayoutManager);
        productView.setAdapter(productListAdapter);


        CollectionReference products_list = firestore.collection("Products");

        Query query = products_list.whereEqualTo("category", CategoryValue).whereEqualTo("status", "1");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (DocumentChange change : value.getDocumentChanges()) {
                    Products product = change.getDocument().toObject(Products.class);
                    switch (change.getType()) {
                        case ADDED:
                            products.add(product);
                            break;
                        case MODIFIED:
                            Products old = products.stream().filter(i -> i.getDocumentId().equals(product.getDocumentId())).findFirst().orElse(null);

                            if (old != null) {
                                old.setName(product.getName());
                                old.setBrand(product.getBrand());
                                old.setPrice(product.getPrice());
                                old.setQty(product.getQty());
                                old.setProduct_image(product.getProduct_image());
                            }
                            break;
                        case REMOVED:
                            products.remove(product);
                            break;
                    }
                }

                productListAdapter.notifyDataSetChanged();
            }
        });


    }
}