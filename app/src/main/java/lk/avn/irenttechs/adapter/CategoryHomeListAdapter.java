package lk.avn.irenttechs.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import lk.avn.irenttechs.R;
public class CategoryHomeListAdapter extends RecyclerView.Adapter<CategoryHomeListAdapter.ViewHolder> {
    private static final String TAG = CategoryHomeListAdapter.class.getName();
    private ArrayList<String> categoryNames;
    private Context context;
    private OnItemClickListener listener;

    public CategoryHomeListAdapter(ArrayList<String> categoryNames, Context context, OnItemClickListener listener) {
        this.categoryNames = categoryNames;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.home_category_view, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String categoryName = categoryNames.get(position);

        if (hasImage(categoryName)) {
            holder.textName.setText(categoryName);
            setCategoryImage(categoryName, holder.image);
        } else {
            holder.itemView.setVisibility(View.GONE);
            holder.textName.setVisibility(View.GONE);
            holder.image.setVisibility(View.GONE);
            holder.con_back_list_view.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return categoryNames != null ? categoryNames.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageView image;
        ConstraintLayout con_back_list_view;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textName = itemView.findViewById(R.id.category_view_name);
            image = itemView.findViewById(R.id.category_view_image);
            con_back_list_view = itemView.findViewById(R.id.con_back_list_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private boolean hasImage(String categoryName) {
        int imageResource = getImageResource(categoryName);
        return imageResource != 0;
    }

    private void setCategoryImage(String categoryName, ImageView imageView) {
        int imageResource = getImageResource(categoryName);
        imageView.setImageResource(imageResource);
    }

    private int getImageResource(String categoryName) {
        switch (categoryName) {
            case "Laptops":
                return R.drawable.laptop;
            case "Tablets":
                return R.drawable.tablet;
            case "Smartwatches":
                return R.drawable.smartwatch;
            case "Cameras":
                return R.drawable.camera;
            case "Audio Devices":
                return R.drawable.speaker;
            case "Smart Phones":
                return R.drawable.smartphone;
            case "Drones":
                return R.drawable.drone;
            case "VR Headsets":
                return R.drawable.vr;
            case "Projectors":
                return R.drawable.projector;
            case "Power Banks":
                return R.drawable.powerbank;
            case "Printers":
                return R.drawable.printer;
            default:
                return 0;
        }
    }
}
