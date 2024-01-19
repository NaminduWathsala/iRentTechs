package lk.avn.irenttechs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Date;

import lk.avn.irenttechs.adapter.CategoryListAdapter;
import lk.avn.irenttechs.custom.CustomLoading;
import lk.avn.irenttechs.model.Cart;
import lk.avn.irenttechs.model.Products;
import lk.avn.irenttechs.model.Wishlist;

public class SingleProductViewFragment extends Fragment {
    private static final String TAG = SingleProductViewFragment.class.getName();
    Bundle args;
    private static String errorName,successName,successtitle,availableQTY;
    private static FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Products> products;
    private String productValue;
    private CategoryListAdapter productListAdapter;
    private TextView spv_name, spv_price, spv_description, spv_qty;
    private ImageButton wishlist;
    ImageSlider imageSlider;
    private static CustomLoading customLoading;
    ArrayList<SlideModel> slideModels;
    private String email;
    Button add_to_card;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_product_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);


        if (getActivity() instanceof HomeActivity) {
            HomeActivity ha = (HomeActivity) getActivity();
            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText("Product View");
            MaterialToolbar toolbar = ha.findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(null);
            ImageButton backbtn = ha.findViewById(R.id.back_btn);
            backbtn.setVisibility(View.VISIBLE);
            backbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            });
        }


        customLoading = new CustomLoading(getActivity());

        args = getArguments();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        spv_name = fragment.findViewById(R.id.spv_name);
        spv_price = fragment.findViewById(R.id.spv_price);
        spv_description = fragment.findViewById(R.id.spv_description);
        spv_qty = fragment.findViewById(R.id.spv_qty);

        wishlist = fragment.findViewById(R.id.add_to_wishlist);
        add_to_card = fragment.findViewById(R.id.add_to_card);

        imageSlider = fragment.findViewById(R.id.product_image_slider);
        slideModels = new ArrayList<>();


        if (getArguments() != null) {
            productValue = getArguments().getString("product_id");
        }

        CollectionReference products_list = firestore.collection("Products");

        Query query = products_list.whereEqualTo("documentId", productValue);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (QueryDocumentSnapshot doc : value) {

                    spv_name.setText(doc.getString("brand") + " " + doc.getString("name"));
                    spv_price.setText("Rs." + doc.getString("price") + ".00");
                    spv_description.setText(doc.getString("description"));
                    spv_qty.setText("Available Items: " + doc.getString("qty"));
                    addImages(doc.getString("product_image"));
                    availableQTY = doc.getString("qty");
                }
            }
        });

        SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", getContext().MODE_PRIVATE);
        email = preferences.getString("EMAIL", null);

        CollectionReference wishlistCollection = firestore.collection("Wishlist");
        Query wishlistquery = wishlistCollection.whereEqualTo("user_email", email)
                .whereEqualTo("product_id", productValue);

        wishlistquery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        wishlist.setImageResource(R.drawable.wishlist_select);
                    } else {
                        wishlist.setImageResource(R.drawable.wishlist);
                    }
                } else {
                    Log.e(TAG, "Error getting wishlist items: ", task.getException());
                }
            }
        });


        wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

                if (login_preferences.getString("ID", null) == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {

                    CollectionReference wishlistCollection = firestore.collection("Wishlist");

                    Query query = wishlistCollection.whereEqualTo("user_email", email)
                            .whereEqualTo("product_id", productValue);

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                        String documentId = doc.getString("documentId");
                                        if (documentId != null) {
                                            wishlistCollection.document(documentId).delete();
                                            wishlist.setImageResource(R.drawable.wishlist);
                                        }
                                    }
                                } else {
                                    Date date = new Date();

                                    SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", getContext().MODE_PRIVATE);
                                    String userEmail = preferences.getString("EMAIL", null);

                                    Wishlist addWishlist = new Wishlist(userEmail, productValue, date.toString(), "1");

                                    wishlistCollection.add(addWishlist).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                String generatedDocumentId = task.getResult().getId();
                                                Log.d(TAG, "Generated Document ID: " + generatedDocumentId);

                                                wishlistCollection.document(generatedDocumentId).update("documentId", generatedDocumentId);
                                                wishlist.setImageResource(R.drawable.wishlist_select);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "Something Went wrong");
                                        }
                                    });
                                }
                            } else {
                                Log.e(TAG, "Error getting wishlist items: ", task.getException());
                            }
                        }
                    });

                }
            }
        });

        add_to_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences login_preferences = getActivity().getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);

                if (login_preferences.getString("ID", null) == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    new SingleProductViewFragment.cardQTYDialog(email, productValue, availableQTY, requireActivity().getSupportFragmentManager()).show(getActivity().getSupportFragmentManager(), "Success");
                }
            }
        });

    }

    public void addImages(String imageId) {
        StorageReference imageRef = storage.getReference("/Product_images/" + imageId + "/");

        imageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                if (listResult != null && listResult.getItems() != null) {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    if (isAdded()) {
                                        if (slideModels == null) {
                                            slideModels = new ArrayList<>();
                                        }

                                        slideModels.add(new SlideModel(uri.toString(), ScaleTypes.CENTER_INSIDE));

                                        if (isAdded()) {
                                            requireActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (imageSlider != null) {
                                                        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_INSIDE);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Image URL is null");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting image download URL", e);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "No images found in the storage reference");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error listing images", e);
            }
        });
    }

    public static void productAddToCart(String p_email, String p_id, String p_qty, FragmentManager fragmentManager) {
        Date date = new Date();

        Cart cart = new Cart(p_email, p_id,p_qty, date.toString(), "1");

        customLoading.show();

        CollectionReference cartCollection = firestore.collection("Cart");
        cartCollection.add(cart).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                customLoading.dismiss();
                if (task.isSuccessful()) {
                    String generatedDocumentId = task.getResult().getId();
                    Log.d(TAG, "Generated Document ID: " + generatedDocumentId);

                    cartCollection.document(generatedDocumentId).update("documentId", generatedDocumentId);

                    successtitle = "Success!";
                    successName = "Your product has been added to the Cart ";
                    new SingleProductViewFragment.SuccessDialog().show(fragmentManager, "Success");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                customLoading.dismiss();
                Log.d(TAG, "Something Went wrong");
            }
        });
    }


    public static class cardQTYDialog extends DialogFragment {
        private String email;
        private String productId;
        private String availableQTY;
        FragmentManager fragmentManager;

        public cardQTYDialog(String email, String productId,String availableQTY, FragmentManager fragmentManager) {
            this.email = email;
            this.productId = productId;
            this.availableQTY = availableQTY;
            this.fragmentManager = fragmentManager;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.cart_qty_message, null);

            EditText numberPicker = customView.findViewById(R.id.numberPicker);
            Button minusButton = customView.findViewById(R.id.minusButton);
            Button plusButton = customView.findViewById(R.id.plusButton);


            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(numberPicker.getText().toString());
                    if (value > 0) {
                        value--;
                        numberPicker.setText(String.valueOf(value));
                    }
                }
            });

            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(numberPicker.getText().toString());
                    value++;
                    numberPicker.setText(String.valueOf(value));
                }
            });


            Button cartButton = customView.findViewById(R.id.cart_product_add_to_card);
            cartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String qty_number = numberPicker.getText().toString();

                    if (qty_number.equals("0") || qty_number.isEmpty()) {
                        errorName = "Please Add your Quantity";
                        new SingleProductViewFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else if (Integer.parseInt(qty_number) > Integer.parseInt(availableQTY)) {
                        errorName = "We Don't have that much products sorry !";
                        new SingleProductViewFragment.ErrorDialog().show(getActivity().getSupportFragmentManager(), "Error");
                    } else {
                        productAddToCart(email, productId, qty_number,requireActivity().getSupportFragmentManager());
                        dismiss();
                    }
                }
            });

            Button cancelButton = customView.findViewById(R.id.cart_cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }


    public static class ErrorDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.error_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(errorName);

            Button okButton = customView.findViewById(R.id.success_dialog_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }

    public static class SuccessDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.success_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(successName);

            TextView messageTextView2 = customView.findViewById(R.id.success_dialog_title);
            messageTextView2.setText(successtitle);

            Button okButton = customView.findViewById(R.id.success_dialog_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }


}