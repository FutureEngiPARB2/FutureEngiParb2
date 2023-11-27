package com.cvpro.competition.ModelManagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.cvpro.competition.R;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class ModelAdapter  extends RecyclerView.Adapter<ModelAdapter.ModelHolder> {
    // List to store all the contact details
    private ArrayList<Model> modelsList;
    private Context mContext;
    SharedPreferences prefs;
    // Counstructor for the Class
    public ModelAdapter(ArrayList<Model> modelsList, Context context) {
        this.modelsList = modelsList;
        this.mContext = context;
    }
    // This method creates views for the RecyclerView by inflating the layout
    // Into the viewHolders which helps to display the items in the RecyclerView
    @Override
    public ModelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.model_card, parent, false);
        prefs = mContext.getSharedPreferences("CVPRO", Context.MODE_PRIVATE);
        return new ModelHolder(view);
    }

    @Override
    public int getItemCount() {
        return modelsList == null? 0: modelsList.size();
    }

    // This method is called when binding the data to the views being created in RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ModelHolder holder, final int position) {
        final Model model = modelsList.get(position);

        // Set the data to the views here
        holder.tvName.setText(model.getName());
        holder.icDelete.setVisibility(model.isDelete ? View.VISIBLE : View.GONE);

        holder.icDelete.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle("Delete Confirmation");
            alert.setMessage("Are you sure to delete this file?");
            alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                // continue with delete
                deleteModel(model.getName(), position);
            });
            alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
                // close dialog
                dialog.cancel();
            });
            alert.show();
        });
        // You can set click listeners to individual items in the view-holder here
        // make sure you pass down the listener or make the Data members of the viewHolder public
    }

    private void deleteModel(String filename, int position) {
        File cDir = mContext.getCacheDir();
        if (cDir.exists()) {
            for (File file : Objects.requireNonNull(cDir.listFiles())) {
                if (file.getName().replace(".tflite", "").equals(filename)) {
                    file.delete();
                    removeAt(position);

                    ArrayList<Model> allModels = new ArrayList<>();
                    SharedPreferences prefs = mContext.getSharedPreferences("CVPRO", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<Model>>() {}.getType();
                    if (prefs.getString("models", null) != null) {
                        allModels = gson.fromJson(prefs.getString("models", null), type);
                    }

                    for (int i=0; i < allModels.size(); i++) {
                        if (allModels.get(i).getName().equals(file.getName())) {
                            allModels.remove(allModels.get(i));
                        }
                    }
                    String json = gson.toJson(allModels);
                    editor.putString("models", json);
                    editor.apply();
                }
            }
        }
    }

    public void removeAt(int position) {
        Log.d("Removed : ", position + "");
        modelsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, modelsList.size());
    }

    // This is your ViewHolder class that helps to populate data to the view
    public static class ModelHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private ImageView icDelete;

        public ModelHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_file_name_model);
            icDelete = itemView.findViewById(R.id.ic_delete_model);
        }
    }
}
