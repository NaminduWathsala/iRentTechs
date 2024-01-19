package lk.avn.irenttechs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
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

import lk.avn.irenttechs.adapter.AllCategoryListAdapter;
import lk.avn.irenttechs.adapter.CategoryHomeListAdapter;
import lk.avn.irenttechs.adapter.CategoryListAdapter;
import lk.avn.irenttechs.adapter.HomeProductListAdapter;
import lk.avn.irenttechs.model.Products;


public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getName();
    Bundle args;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private  String CategoryValue;
    private HomeProductListAdapter homeProductListAdapter;
    ImageSlider imageSlider;
    ArrayList<SlideModel> slideModels;
    private ArrayList<String> category;
    private CategoryHomeListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
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
            ha.findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText("HOME");
            ImageButton backbtn = ha.findViewById(R.id.back_btn);
            backbtn.setVisibility(View.GONE);
            MaterialToolbar toolbar1 = ha.findViewById(R.id.toolbar);
            toolbar1.setNavigationIcon(R.drawable.menu);
        }

        imageSlider = fragment.findViewById(R.id.product_image_slider);
        slideModels = new ArrayList<>();

        category = new ArrayList<>();
        category.add("Laptops");
        category.add("Tablets");
        category.add("Smartwatches");
        category.add("Cameras");
        category.add("Audio Devices");
        category.add("Smart Phones");
        category.add("Drones");
        category.add("VR Headsets");
        category.add("Projectors");
        category.add("Power Banks");
        category.add("Printers");

        slideModels.add(new SlideModel(R.drawable.c1, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.c2, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.c3, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.c4, ScaleTypes.CENTER_CROP));
        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);

        fragment.findViewById(R.id.view_all_categorys).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllCategoryListFragment allCategoryListFragment = new AllCategoryListFragment();
                Bundle bundle = new Bundle();
                allCategoryListFragment.setArguments(bundle);

                int containerId = R.id.frame_layouts;

                getParentFragmentManager().beginTransaction()
                        .replace(containerId, allCategoryListFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });



        RecyclerView productView = fragment.findViewById(R.id.category_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);


        listAdapter = new CategoryHomeListAdapter(category, getContext(), new CategoryHomeListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String selectedCategory = category.get(position);
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
        productView.setLayoutManager(linearLayoutManager);
        productView.setAdapter(listAdapter);



        RecyclerView productView2 = fragment.findViewById(R.id.home_product_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);

        homeProductListAdapter = new HomeProductListAdapter(products, getContext(), new HomeProductListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String selectedProductId = homeProductListAdapter.getDocumentId(position);

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
        productView2.setAdapter(homeProductListAdapter);


        CollectionReference products_list = firestore.collection("Products");

        Query query = products_list.whereEqualTo("status", "1");

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

                homeProductListAdapter.notifyDataSetChanged();
            }
        });

    }
}
