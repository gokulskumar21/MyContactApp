package com.gokul.myphonebook.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gokul.myphonebook.R;
import com.gokul.myphonebook.data.ConnectionDetector;
import com.gokul.myphonebook.data.Contact;
import com.gokul.myphonebook.data.SharedPrefManager;
import com.gokul.myphonebook.startup.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyContactDetailsActivity extends AppCompatActivity {
    private TextInputEditText tvName ,tvNickName;
    TextInputEditText edMobileNumber,edEmail,edAddress,edFirstName;
    ImageView ivProfile;
    MaterialButton btUpdate;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String strID ;
    private Uri selectedImage ;
    String strFileName="";
    TextView tvUpload;
    boolean isAllFieldsChecked = false;
    String strImage ,strImagName;
    LinearLayout lvCall,lvEmail,lvAddress;
    private static int RESULT_UPDATE_IMG = 1;
    private ConnectionDetector cd;
    private boolean isInternetPresent = false;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_contact_details);
        bindID();

    }
    private void bindID() {
        tvName= findViewById(R.id.tvName);
        tvNickName= findViewById(R.id.tvNickName);
        edMobileNumber = findViewById(R.id.edMobileNumber);
        edEmail = findViewById(R.id.edEmail);
        edFirstName= findViewById(R.id.edFirstName);
        btUpdate= findViewById(R.id.btUpdate);
        edAddress = findViewById(R.id.edAddress);
        ivProfile= findViewById(R.id.ivProfile);
        tvUpload = findViewById(R.id.tvUpload);
        lvCall= findViewById(R.id.lvCall);
        lvEmail= findViewById(R.id.lvEmail);
        lvAddress= findViewById(R.id.lvAddress);
        cd = new ConnectionDetector(MyContactDetailsActivity.this);
        progress=new ProgressDialog(this);

        isInternetPresent = cd.isConnectingToInternet();
        setData();
        lvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCall = edMobileNumber.getText().toString();
                Snackbar snackbar = Snackbar
                        .make(MyContactDetailsActivity.this.findViewById(android.R.id.content), "Calling on "+ strCall, Snackbar.LENGTH_LONG);
                snackbar.show();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", strCall, null));
                startActivity(intent);
            }
        });

        lvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = edEmail.getText().toString();
                if(!strEmail.isEmpty() && !strEmail.trim().equals(""))
                {
                    Uri uri = Uri.parse("mailto:" + strEmail)
                            .buildUpon()
                            .appendQueryParameter("subject", "Enter subject")
                            .appendQueryParameter("body", "Enter body")
                            .build();
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
                    startActivity(Intent.createChooser(emailIntent, "Welcome"));
                }else
                {
                    Snackbar snackbar = Snackbar
                            .make(MyContactDetailsActivity.this.findViewById(android.R.id.content), "No Email-ID for this contact", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
        lvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strAdd = edAddress.getText().toString();


                if(!strAdd.isEmpty() && !strAdd.trim().equals(""))
                {
                    String map = "http://maps.google.co.in/maps?q=" + strAdd;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(map));
                    startActivity(intent);
                }else
                {
                    Snackbar snackbar = Snackbar
                            .make(MyContactDetailsActivity.this.findViewById(android.R.id.content), "No Address for this contact", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });



        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetPresent) {
                    isAllFieldsChecked = CheckAllFields();
                    if (isAllFieldsChecked) {
                        Contact contact = new Contact();
                        contact.setId(getIntent().getExtras().getString("ID"));
                        contact.setStrFirstName(edFirstName.getText().toString().trim());
                        contact.setStrLastName(tvName.getText().toString().trim());
                        contact.setStrNickName(tvNickName.getText().toString().trim());
                        contact.setStrEmail(edEmail.getText().toString().trim());
                        contact.setStrAdd(edAddress.getText().toString().trim());
                        contact.setStrMobileNum(edMobileNumber.getText().toString().trim());
                        databaseReference.setValue(contact);
                        Snackbar snackbar = Snackbar.make(MyContactDetailsActivity.this.findViewById(android.R.id.content), "Contact Updated", Snackbar.LENGTH_SHORT);
                        snackbar.show();

                        Intent intent = new Intent(MyContactDetailsActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(MyContactDetailsActivity.this.findViewById(android.R.id.content), "Please connect to Internet first", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
            }
        });

    }

    private void setData() {
        strID = getIntent().getExtras().getString("ID");
        String strFullName = getIntent().getExtras().getString("contact_FullName");
        String strMobileNum = getIntent().getExtras().getString("contact_Mobile");
        String strEmailID = getIntent().getExtras().getString("contact_Email");
        String strAddress = getIntent().getExtras().getString("contact_Address");
        String strNickName = getIntent().getExtras().getString("contact_NickName");
        String strFirstName = getIntent().getExtras().getString("contact_Firstname");
        String strLastName = getIntent().getExtras().getString("contact_LastName");

        firebaseDatabase = FirebaseDatabase.getInstance("https://myphonebook-46fc7-default-rtdb.firebaseio.com/");
        databaseReference = firebaseDatabase.getReference("Contact").child(strID);
        tvUpload.setText(strFullName.toString());
       tvName.setText(strLastName.toString());
        tvNickName.setText(strNickName.toString());
        edMobileNumber.setText(strMobileNum.toString());
        edFirstName.setText(strFirstName);
        edEmail.setText(strEmailID.toString());
        edAddress.setText(strAddress.toString());

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_delete:
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MyContactDetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Are you sure want to delete ?");
                materialAlertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Query applesQuery = databaseReference.child("Contact").orderByChild("id").equalTo(strID);

                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });

                        databaseReference.removeValue();
                        // displaying a toast message on below line.
                        // opening a main activity on below line.
                        startActivity(new Intent(MyContactDetailsActivity.this, MainActivity.class));
                        Snackbar.make(MyContactDetailsActivity.this.findViewById(android.R.id.content) , "Done",Snackbar.LENGTH_LONG).show();
                    }
                });
                materialAlertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
//        materialAlertDialogBuilder.setBackground(getResources().get)
                materialAlertDialogBuilder.show();
        }
        return super.onOptionsItemSelected(item);
    }
    private String getFileExt(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean CheckAllFields() {
        if (edFirstName.length() == 0) {
            edFirstName.setError("Enter name");
            return false;
        }

        if (edMobileNumber.length() == 0) {
            edMobileNumber.setError("Enter Number");
            return false;
        }

        if (edEmail.length() == 0) {
            edEmail.setError("Enter E-mailID");
            return false;

        }
        if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.getText().toString()).matches()){
            edEmail.setError(" Email-ID is not proper");
            return false;
        }


        return true;
    }

}