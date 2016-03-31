package com.nielsmasdorp.sleeply.ui.stream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nielsmasdorp.sleeply.R;
import com.nielsmasdorp.sleeply.model.Stream;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class StreamGridAdapter extends RecyclerView.Adapter<StreamGridAdapter.ViewHolder> {

    private List<Stream> dataSet;
    public OnItemClickListener itemClickListener;
    private Context context;

    public StreamGridAdapter(Context context, List<Stream> myDataset, OnItemClickListener itemClickListener) {

        this.dataSet = myDataset;
        this.itemClickListener = itemClickListener;
        this.context = context;
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

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            itemClickListener.onItemClick(v, getLayoutPosition(), dataSet);
        }

        public void bindStream(Stream stream) {

            getBitMap(stream)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Bitmap>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(Bitmap bitmap) {

                            image.setImageBitmap(bitmap);
                        }
                    });
            name.setText(stream.getTitle());
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position, List<Stream> dataSet);
    }

    private Observable<Bitmap> getBitMap(Stream stream) {

        return Observable.defer(() -> {

            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), stream.getImageResource());
            return Observable.just(bm);
        });
    }
}
