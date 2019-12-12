package com.example.deliveryfoodserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deliveryfoodserver.Common.Common;
import com.example.deliveryfoodserver.Interface.ItemClickListener;
import com.example.deliveryfoodserver.Model.Category;
import com.example.deliveryfoodserver.Service.ListenOrder;
import com.example.deliveryfoodserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseDatabase database;
    private DatabaseReference category;
    private TextView txtFullName;
    private Category newCategory;
    private RecyclerView recyclerMenu;
    private FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private RecyclerView.LayoutManager layoutManager;
    private Uri saveUri;
    private Button btnSelectImage,btnAdd;
    private EditText inputKey;
    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);
        database= FirebaseDatabase.getInstance();
        category=database.getReference("Category");
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(HomeActivity.this,CartActivity.class));
                openDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //txtFullName=(TextView)headerView.findViewById(R.id.txtFullName);
        //txtFullName.setText(Common.currentUser.getUsername());
        //Load menu
        recyclerMenu=(RecyclerView) findViewById(R.id.recycle_menu);
        recyclerMenu.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerMenu.setLayoutManager(layoutManager);
        loadMenu();
        Intent service=new Intent(HomeActivity.this, ListenOrder.class);
        startService(service);
    }
    private void openDialog() {
        LayoutInflater inflater=LayoutInflater.from(HomeActivity.this);
        View subView=inflater.inflate(R.layout.alert_dialog_data,null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Save Data");
        alertDialog.setMessage("Add Food");
        // final EditText inputValue = (EditText) subView.findViewById(R.id.edtValue);
        inputKey = (EditText) subView.findViewById(R.id.edtKey);
        btnSelectImage=(Button)subView.findViewById(R.id.btnChooseImage);
        btnAdd=(Button)subView.findViewById(R.id.btnAdd);
        alertDialog.setView(subView);
        alertDialog.create();
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(saveUri==null||inputKey.getText().toString().equals(""))//||inputKey.getText().toString().equals(newCategory.getName())){
                {
                    Toast.makeText(HomeActivity.this,"Pleae check your inform again",Toast.LENGTH_LONG).show();
                }
                else {
                    if (newCategory != null) {
                        category.push().setValue(newCategory);//push new value
                        Snackbar.make(drawer, "New category" + newCategory.getName() + "was added", Snackbar.LENGTH_SHORT).show();
                    }
                }
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
                    Toast.makeText(HomeActivity.this,"Uploaded!!!",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set value for newCategory if image upload we can get download link
                            newCategory=new Category(inputKey.getText().toString(),uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(HomeActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

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
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),Common.PICK_IMAGE_REQUEST);
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void loadMenu() {
        adapter=new FirebaseRecyclerAdapter<Category, MenuViewHolder>
                (Category.class,R.layout.menu_item,MenuViewHolder.class,category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                viewHolder.txtMenuName.setText(""+model.getName());
                final Category clickItem=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                     //   Toast.makeText(HomeActivity.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(HomeActivity.this,FoodList.class);
                        intent.putExtra("categoryId",adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerMenu.setAdapter(adapter);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //OrderFragment orderFragment=new OrderFragment(this);
        if (id == R.id.nav_menu) {
            // Handle the camera action
            //startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }

        else if (id == R.id.nav_cart)
        {
            //startActivity(new Intent(HomeActivity.this,CartActivity.class));
        }
        else if (id == R.id.nav_order) {
            //OrderFragment orderFragment=new Fragment(HomeActivity.this);
            //setFragment(orderFragment);
            startActivity(new Intent(HomeActivity.this,OrderActivity.class));

        } else if (id == R.id.nav_signout) {
            Intent signIn=new Intent(HomeActivity.this,LoginActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.replace(R.id.main_frame,fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else {
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);

    }

    private void deleteCategory(final String key) {
        final DatabaseReference foods=database.getReference("Foods");
        final Query foodInCategory=foods.orderByChild("menuId").equalTo(key);
        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnaphot:dataSnapshot.getChildren()) {
                    postSnaphot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        category.child(key).removeValue();
    }

    private void showUpdateDialog(final String key, final Category item) {
        LayoutInflater inflater=LayoutInflater.from(HomeActivity.this);
        View subView=inflater.inflate(R.layout.alert_dialog_data,null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Update Data");
        alertDialog.setMessage("Update Food");
        // final EditText inputValue = (EditText) subView.findViewById(R.id.edtValue);
        inputKey = (EditText) subView.findViewById(R.id.edtKey);
        btnSelectImage=(Button)subView.findViewById(R.id.btnChooseImage);
        btnAdd=(Button)subView.findViewById(R.id.btnAdd);
        alertDialog.setView(subView);
        alertDialog.create();
        alertDialog.setIcon(R.drawable.ic_restaurant_menu_black_24dp);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(inputKey.getText().toString().equals("")||inputKey.getText().toString().equals(item.getName()))
                {
                    Toast.makeText(HomeActivity.this,"Please check your inform again",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    item.setName(inputKey.getText().toString());
                    category.child(key).setValue(item);//open the child by key and set new value to this

                }
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
                changeImage(item);
            }
        });
        alertDialog.show();

    }

    private void changeImage(final Category item) {
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
                    Toast.makeText(HomeActivity.this,"Uploaded!!!",Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set value for newCategory if image upload we can get download link
                            item.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(HomeActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

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
}
