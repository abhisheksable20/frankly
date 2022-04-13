package com.sakesnake.frankly.home.bottomnavupload.writingupload.betawritter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;

import org.jetbrains.annotations.NotNull;

public class ColorPaletteAdapter extends RecyclerView.Adapter<ColorPaletteAdapter.ColorPaletteViewHolder> {

    private int[] colors;
    private Context mContext;
    private GetSelectedColor getSelectedColor;

    public interface GetSelectedColor{
        void getSelectedColor(int color);
    }

    public ColorPaletteAdapter(int[] colors, Context mContext,GetSelectedColor getSelectedColor) {
        this.colors = colors;
        this.mContext = mContext;
        this.getSelectedColor = getSelectedColor;
    }

    public static class ColorPaletteViewHolder extends RecyclerView.ViewHolder {
        CardView colorCardView;
        public ColorPaletteViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            colorCardView = (CardView) itemView.findViewById(R.id.color_palatte_list_card_view);
        }
    }

    @NonNull
    @NotNull
    @Override
    public ColorPaletteAdapter.ColorPaletteViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.color_palatte_list_view,parent,false);
        return new ColorPaletteViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ColorPaletteAdapter.ColorPaletteViewHolder holder, int position) {
        holder.colorCardView.setCardBackgroundColor(colors[position]);

        holder.colorCardView.setOnClickListener(view -> {
            if (getSelectedColor != null)
                getSelectedColor.getSelectedColor(colors[position]);
        });
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }
}
