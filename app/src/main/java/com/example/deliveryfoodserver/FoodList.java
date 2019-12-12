package com.example.deliveryfoodserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.deliveryfoodserver.Common.Common;
import com.example.deliveryfoodserver.Interface.ItemClickListener;
import com.example.deliveryfoodserver.Model.Category;
import com.example.deliveryfoodserver.Model.Food;
import com.example.deliveryfoodserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoodList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseDatabase database;
    private DatabaseReference foodList;
    private FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    private String categoryId="";
    private FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    private FloatingActionButton btnFab;
    private EditText edtName,edtPrice,edtDiscount,edtDiscription;
    private Button btnSelectImage,btnAdd;
    private Uri saveUri;
    FirebaseStorage storage;
    private StorageReference storageReference;
    private Food newFood;
    private RelativeLayout rootLayout;
    List<String>suggestList=new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        rootLayout=(RelativeLayout)findViewById(R.id.rootLayout);
        database= FirebaseDatabase.getInstance();
        foodList=database.getReference("Foods");
        storage= FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        recyclerView=(RecyclerView)findViewById(R.id.recycler_food);
        btnFab=(FloatingActionButton)findViewById(R.id.btnFloatFood);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        if(getIntent()!=null)
            categoryId=getIntent().getStringExtra("categoryId");//category ID is ID of item in RecycleView
        if(!categoryId.isEmpty()){
            loadListFood(categoryId);
        }
        materialSearchBar=(MaterialSearchBar)findViewById(R.id.search_bar);
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ArrayList<String>suggest=new ArrayList<String>();
                for(String search:suggestList ){
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())) {
                        suggest.add(search);
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);//set suggest
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When searchbar is close, becoming original adapter
                if(!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search is finish, show result
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
        btnFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddFoodDialog();
            }
        });
    }

    private void openAddFoodDialog() {
        LayoutInflater inflater=LayoutInflater.from(FoodList.this);
        View subView=inflater.inflate(R.layout.alert_dialog_add_food,null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Add Food");
        // final EditText inputValue = (EditText) subView.findViewById(R.id.edtValue);
        edtName = (EditText) subView.findViewById(R.id.edtNameAddFood);
        edtPrice=(EditText)subView.findViewById(R.id.edtPriceAddFood);
        edtDiscription=(EditText)subView.findViewById(R.id.edtDiscriptionAddFood);
        edtDiscount=(EditText)subView.findViewById(R.id.edtDiscountAddFood);
        btnSelectImage=(Button)subView.findViewById(R.id.btnChooseImageAddFood);
        btnAdd=(Button)subView.findViewById(R.id.btnAddFood);
        alertDialog.setView(subView);
        alertDialog.create();
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(newFood!=null){
                    dialogInterface.dismiss();
                    foodList.push().setValue(newFood);//push new value
                    //Snackbar.make(drawer,"New category"+newFood.getName()+"was added",Snackbar.LENGTH_SHORT).show();
                    //Toast.makeText(FoodList.this,"Add "+newFood.getName()+" success",Toast.LENGTH_SHORT).show();
                    Snackbar.make(rootLayout, "New category" + newFood.getName() + "was added", Snackbar.LENGTH_SHORT).show();

                }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            }
        });
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        alertDialog.show();

    }
    private void uploadImage() {
        if(saveUri!=null){
            final ProgressDialog mDialog=new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();
            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("images/*"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this,"Uploaded!!!",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set value for newCategory if image upload we can get download link
                            //newCategory=new Category(inputKey.getText().toString(),uri.toString());
                            newFood=new Food();
                            newFood.setName(edtName.getText().toString());
                            newFood.setPrice(edtPrice.getText().toString());
                            newFood.setDescription(edtDiscription.getText().toString());
                            newFood.setDiscount(edtDiscount.getText().toString());
                            newFood.setMenuID(categoryId);
                            newFood.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded"+progress+"%");
                }
            });
        }
    }

    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), Common.PICK_IMAGE_REQUEST);
    }
    private void startSearch(CharSequence text) {
        searchAdapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("name").equalTo(text.toString())//compare by itemName
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodImage);
                final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
//                        Intent intent=new Intent(FoodList.this,FoodDetailActivity.class);
 //                       intent.putExtra("foodId",searchAdapter.getRef(position).getKey());
   //                     startActivity(intent);
                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);//Set adapter for RecycleView when search Result
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren() ){
                            Food foodItem=postSnapshot.getValue(Food.class);
                            suggestList.add(foodItem.getName());//load list of suggest item

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {
        adapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuID").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, final Food model, int position) {
                viewHolder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodImage);
                final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
     //                   Intent intent=new Intent(FoodList.this,FoodDetailActivity.class);
     //                   intent.putExtra("foodId",adapter.getRef(position).getKey());
     //                   startActivity(intent);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Common.PICK_IMAGE_REQUEST&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            saveUri=data.getData();
            btnSelectImage.setText("Image Selected");
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else {
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);

    }
    private void deleteFood(String key) {
        foodList.child(key).removeValue();
    }
    private void showUpdateFoodDialog( String key, Food item) {
        LayoutInflater inflater=LayoutInflater.from(FoodList.this);
        View subView=inflater.inflate(R.layout.alert_dialog_add_food,null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Add Food");
        // final EditText inputValue = (EditText) subView.findViewById(R.id.edtValue);
        edtName = (EditText) subView.findViewById(R.id.edtNameAddFood);
        edtPrice=(EditText)subView.findViewById(R.id.edtPriceAddFood);
        edtDiscription=(EditText)subView.findViewById(R.id.edtDiscriptionAddFood);
        edtDiscount=(EditText)subView.findViewById(R.id.edtDiscountAddFood);
        btnSelectImage=(Button)subView.findViewById(R.id.btnChooseImageAddFood);
        btnAdd=(Button)subView.findViewById(R.id.btnAddFood);
        edtName.setText(item.getName());
        edtPrice.setText(item.getPrice());
        edtDiscount.setText(item.getDiscount());
        edtDiscription.setText(item.getDescription());
        final String local=key;
        final Food localItem=item;
        alertDialog.setView(subView);
        alertDialog.create();
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                        localItem.setName(edtName.getText().toString());
                        localItem.setDiscount(edtDiscount.getText().toString());
                        localItem.setDescription(edtDiscription.getText().toString());
                        localItem.setPrice(edtPrice.getText().toString());
                        foodList.child(local).setValue(localItem);//set new value
                        //Snackbar.make(drawer,"New category"+newFood.getName()+"was added",Snackbar.LENGTH_SHORT).show();
                        Toast.makeText(FoodList.this, "Add " + localItem.getName() + " success", Toast.LENGTH_SHORT).show();
                    //}
               // }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        alertDialog.show();
    }

}
