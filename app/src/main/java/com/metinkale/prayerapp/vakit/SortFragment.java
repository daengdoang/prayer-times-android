package com.metinkale.prayerapp.vakit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.times.Times;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortFragment extends Fragment implements Times.TimesListener {

    private LinearLayoutManager mLinLayMan;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private Main act;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.vakit_sort_main, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.list);
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mLinLayMan = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinLayMan);
        mRecyclerView.setHasFixedSize(true);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback();
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        Times.addListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        Times.removeListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Main) {
            act = (Main) context;

        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mAdapter.getItemCount() != Times.getCount()) mAdapter.dataChanged();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mAdapter.ids, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mAdapter.ids, i, i - 1);
            }
        }
        mAdapter.notifyItemMoved(fromPosition, toPosition);

        Times.drop(fromPosition, toPosition);
    }

    public void onItemDismiss(final int position) {
        final Times times = Times.getTimesAt(position);

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setTitle(R.string.delete);
        dialog.setMessage(getString(R.string.delConfirm, times.getName()));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
                times.delete();
                mAdapter.notifyItemRemoved(position);
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.cancel();
                mAdapter.notifyDataSetChanged();
            }
        });
        dialog.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView city;
        public TextView source;
        public int pos;
        public View handler;

        public ViewHolder(View v) {
            super(v);
            city = (TextView) v.findViewById(R.id.city);
            source = (TextView) v.findViewById(R.id.source);
            handler = v.findViewById(R.id.handle);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<Long> ids = new ArrayList<>();

        private int hide = -1;

        public MyAdapter() {
            dataChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.vakit_sort_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder vh, final int position) {
            vh.pos = position;

            Times c = getItem(position);
            if (c == null) return;
            vh.city.setText(c.getName());
            vh.source.setText(c.getSource().text);

            vh.handler.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        mItemTouchHelper.startDrag(vh);
                    }

                    return false;
                }
            });

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    act.onItemClick(position);
                }
            });
        }


        public void dataChanged() {
            ids.clear();
            ids.addAll(Times.getIds());
            notifyDataSetChanged();
        }


        public Times getItem(int pos) {
            return Times.getTimes(ids.get(pos));
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getID();
        }

        @Override
        public int getItemCount() {
            return ids.size();
        }


    }

    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {


        @Override
        public int getMovementFlags(RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
                target) {
            onItemMove(viewHolder.getAdapterPosition(),
                    target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                      int actionState) {
            // We only want the active item
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof ViewHolder) {
                    ViewHolder itemViewHolder =
                            (ViewHolder) viewHolder;
                    itemViewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof ViewHolder) {
                ViewHolder itemViewHolder =
                        (ViewHolder) viewHolder;
                itemViewHolder.itemView.setBackgroundColor(Color.WHITE);
            }
        }
    }
}