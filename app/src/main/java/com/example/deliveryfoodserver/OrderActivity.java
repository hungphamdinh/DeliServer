package com.example.deliveryfoodserver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;


import com.example.deliveryfoodserver.Common.Common;
import com.example.deliveryfoodserver.Interface.ItemClickListener;
import com.example.deliveryfoodserver.Model.Request;
import com.example.deliveryfoodserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

import static com.example.deliveryfoodserver.Common.Common.changeCodeToStatus;

public class OrderActivity extends AppCompatActivity {
    private RecyclerView recyclerMenu;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference request;
    private FirebaseDatabase database;
    private FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    private MaterialSpinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        database= FirebaseDatabase.getInstance();
        request=database.getReference("Requests");
        recyclerMenu=(RecyclerView)findViewById(R.id.listOrderRecycler);
        recyclerMenu.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerMenu.setLayoutManager(layoutManager);
        loadOrders();
    }
    private void loadOrders() {
        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>
                (Request.class,R.layout.order_layout,
                        OrderViewHolder.class,
                        request) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, int position) {
                viewHolder.txtId.setText(adapter.getRef(position).getKey());
                viewHolder.txtAddress.setText(model.getAddress());
     //           String a=changeCodeToStatus(model.getStatus());
                viewHolder.txtStatus.setText(changeCodeToStatus(model.getStatus()));
                viewHolder.txtPhone.setText(model.getPhone());
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent trackingOrder=new Intent(OrderActivity.this,TrackingOrderActivity.class);
                        Common.currentRequest=model;
                        startActivity(trackingOrder);
//                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
//                        if (launchIntent != null) {
//                            Common.currentRequest=model;
//                            startActivity(launchIntent);//null pointer check in case package name was not found
//                        }
                    }
                });
            }

        };
        adapter.notifyDataSetChanged();
        recyclerMenu.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateOrderDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else {
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);    }

    private void deleteOrder(String key) {
        request.child(key).removeValue();
    }

    private void showUpdateOrderDialog(String key, final Request item) {
        LayoutInflater inflater=this.getLayoutInflater();
        View subView=inflater.inflate(R.layout.alert_dialog_order,null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderActivity.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");
        //alertDialog.create();
        spinner=(MaterialSpinner) subView.findViewById(R.id.statusSpinner);
        spinner.setItems("Place","On my way","Shipped");
        alertDialog.setView(subView);
        //alertDialog.show();
        final String localKey=key;
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                request.child(localKey).setValue(item);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
}





