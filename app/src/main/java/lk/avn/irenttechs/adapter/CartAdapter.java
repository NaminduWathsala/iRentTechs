package lk.avn.irenttechs.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import lk.avn.irenttechs.CartFragment;
import lk.avn.irenttechs.R;
import lk.avn.irenttechs.model.Products;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private static final String TAG = CartAdapter.class.getName();
    private List<Products> products;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private Context context;
    private OnItemClickListener listener;
    private String email;
    private TextView totalTextView;

    public CartAdapter(List<Products> products, Context context, String email, TextView totalTextView, OnItemClickListener listener) {
        this.products = products;
        this.storage = FirebaseStorage.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.context = context;
        this.listener = listener;
        this.email = email;
        this.totalTextView = totalTextView;

    }


    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cart_view, parent, false);
        return new CartAdapter.ViewHolder(view, listener);
    }

    double totalCost = 0;

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        Products product = products.get(position);
        holder.textName.setText(product.getBrand() + " " + product.getName());

        holder.textPrice.setText("Rs. " + product.getPrice() + ".00");

        CollectionReference wishlistCollection = firestore.collection("Cart");
        Query cart_query = wishlistCollection.whereEqualTo("user_email", email).whereEqualTo("product_id", product.getDocumentId()).whereEqualTo("status","1");
        cart_query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (QueryDocumentSnapshot doc : value) {
                    String selected_cart_qty = doc.getString("qty");
                    String to_price = product.getPrice();

                    holder.textQty.setText("Quantity: " + selected_cart_qty);

                    totalCost = totalCost + (Double.parseDouble(selected_cart_qty) * Double.parseDouble(to_price));
                    onChangeItems();
                }

            }
        });


        StorageReference productImagesRef = storage.getReference("Product_images/" + product.getProduct_image());

        productImagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (!listResult.getItems().isEmpty()) {
                        StorageReference firstImageRef = listResult.getItems().get(0);

                        firstImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Picasso.get()
                                    .load(uri)
                                    .resize(200, 200)
                                    .centerCrop()
                                    .into(holder.image);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error listing images", e);
                });
        onChangeItems();

    }

    public void onChangeItems() {

        totalTextView.setText("Rs. " + totalCost + "0");
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onDeleteClick(int position);

        void onChangeQty(int position);
    }

    public void removeItem(int position) {
        products.remove(position);
        notifyItemRemoved(position);
        onChangeItems();
    }

    public String getDocumentId(int position) {
        return products.get(position).getDocumentId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textPrice, textQty;
        ImageView image;
        ImageButton delete, editQTY;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textName = itemView.findViewById(R.id.cart_product_name);
            textQty = itemView.findViewById(R.id.cart_product_qty);
            textPrice = itemView.findViewById(R.id.cart_product_price);
            image = itemView.findViewById(R.id.cart_product_image);
            delete = itemView.findViewById(R.id.cart_product_bin);
            editQTY = itemView.findViewById(R.id.cart_qty_change);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            editQTY.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onChangeQty(position);
                        }
                    }
                }
            });
        }
    }
}
