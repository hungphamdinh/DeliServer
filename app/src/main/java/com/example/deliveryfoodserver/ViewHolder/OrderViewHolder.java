package com.example.deliveryfoodserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.example.deliveryfoodserver.Common.Common;
import com.example.deliveryfoodserver.Interface.ItemClickListener;
import com.example.deliveryfoodserver.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
    public TextView txtId,txtPhone,txtStatus,txtAddress;
    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtId=(TextView)itemView.findViewById(R.id.txtOrderId);
        txtStatus=(TextView)itemView.findViewById(R.id.txtOrderStatus);
        txtPhone=(TextView)itemView.findViewById(R.id.txtOrderPhone);
        txtAddress=(TextView)itemView.findViewById(R.id.txtOrderAddress);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener=itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select this action");
        contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(), Common.DELETE);

    }

}
