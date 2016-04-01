package com.nielsmasdorp.sleeply.ui.stream;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.model.Stream;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class StreamGridAdapter extends RecyclerView.Adapter<StreamGridAdapter.ViewHolder> {

    private RequestManager glide;
    private List<Stream> dataSet;
    private OnItemClickListener itemClickListener;

    public StreamGridAdapter(RequestManager glide) {

        this.dataSet = new ArrayList<>();
        this.glide = glide;
    }

    public void setData(List<Stream> streams, OnItemClickListener listener) {
        this.dataSet = streams;
        this.itemClickListener = listener;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stream_grid_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.bindStream(dataSet.get(position));
    }

    @Override
    public int getItemCount() {

        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.streamImage)
        ImageView image;
        @Bind(R.id.streamTitle)
        TextView name;

        Stream stream;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            itemClickListener.onItemClick(v, getAdapterPosition(), dataSet);
        }

        public void bindStream(Stream stream) {

            this.stream = stream;

            glide.load(stream.getSmallImgRes()).into(image);
            name.setText(stream.getTitle());
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position, List<Stream> dataSet);
    }

}
