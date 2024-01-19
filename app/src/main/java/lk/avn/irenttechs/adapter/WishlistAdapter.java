package lk.avn.irenttechs.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import lk.avn.irenttechs.R;
import lk.avn.irenttechs.model.Products;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
    private static final String TAG = WishlistAdapter.class.getName();
    private List<Products> products;
    private FirebaseStorage storage;
    private Context context;
    private OnItemClickListener listener;

    public WishlistAdapter(List<Products> products, Context context, OnItemClickListener listener) {
        this.products = products;
        this.storage = FirebaseStorage.getInstance();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WishlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.wishlist_items, parent, false);
        return new WishlistAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistAdapter.ViewHolder holder, int position) {
        Products product = products.get(position);
        holder.textName.setText(product.getBrand()+" "+product.getName());
        holder.textQty.setText(product.getQty());
        holder.textPrice.setText("Rs. " + product.getPrice() + ".00");


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


    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onAddToCartClick(int position);
    }

    public void removeItem(int position) {
        products.remove(position);
        notifyItemRemoved(position);
    }

    public String getDocumentId(int position) {
        return products.get(position).getDocumentId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textPrice, textQty;
        ImageView image;
        ImageButton delete;
        Button add_to_cart;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textName = itemView.findViewById(R.id.wishlist_product_name);
            textQty = itemView.findViewById(R.id.wishlist_product_qty);
            textPrice = itemView.findViewById(R.id.wishlist_product_price);
            image = itemView.findViewById(R.id.wishlist_product_image);
            delete = itemView.findViewById(R.id.wishlist_product_bin);
            add_to_cart = itemView.findViewById(R.id.wishlist_product_add_to_card);

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

            add_to_cart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onAddToCartClick(position);
                        }
                    }
                }
            });
        }
    }
}
