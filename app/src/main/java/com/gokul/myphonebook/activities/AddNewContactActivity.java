package com.gokul.myphonebook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gokul.myphonebook.R;
import com.gokul.myphonebook.data.ConnectionDetector;
import com.gokul.myphonebook.data.Contact;
import com.gokul.myphonebook.data.SharedPrefManager;
import com.gokul.myphonebook.startup.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddNewContactActivity extends AppCompatActivity {

    private TextInputEditText edFirstName , edLastname ,edNickName, edMobileNumber , edEmail ,edAddress ;
    private MaterialButton btSave;
    //private TextView tvAddImage;
    private ImageView imgProfilePic;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ProgressBar savePB;
    public long lngContactID=0;
    Contact contact;
    boolean isAllFieldsChecked = false;
    String strID= "";
    private static long tempId = 0;
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    private Uri selectedImage ;
    String strFileName="";
    private ConnectionDetector cd;
    private boolean isInternetPresent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contact);
        bindID();
    }

    private void bindID() {
        edFirstName = findViewById(R.id.edFirstName);
        edLastname = findViewById(R.id.edLastName);
        edNickName= findViewById(R.id.edNickName);
        edMobileNumber = findViewById(R.id.edMobileNumber);
        edEmail = findViewById(R.id.edEmail);
        edAddress = findViewById(R.id.edAddress);
        btSave= findViewById(R.id.btSave);
       // tvAddImage= findViewById(R.id.tvAddImage);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        firebaseDatabase = FirebaseDatabase.getInstance("https://myphonebook-d2cd1-default-rtdb.firebaseio.com/");
        // find highest key
        // set tempId = highestKey+1
        contact = new Contact();
        cd = new ConnectionDetector(AddNewContactActivity.this);
        isInternetPresent = cd.isConnectingToInternet();

        SharedPrefManager.init(AddNewContactActivity.this);
        // on below line creating our database reference.
        databaseReference = firebaseDatabase.getReference().child("Contact");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // on below line we are setting data in our firebase database.
                if(snapshot.exists())
                    lngContactID=(snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // displaying a failure message on below line.
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAllFieldsChecked = CheckAllFields();
                if(isInternetPresent) {
                    if (isAllFieldsChecked) {
                        String strFirstName = edFirstName.getText().toString();
                        String strLastName = edLastname.getText().toString();
                        String strMobileNumber = edMobileNumber.getText().toString();
                        String strEmail = edEmail.getText().toString();
                        String strAddress = edAddress.getText().toString();
                        String strNickName = edNickName.getText().toString();
                        tempId++;
                        Snackbar snackbar = Snackbar.make(AddNewContactActivity.this.findViewById(android.R.id.content), "Please wait contact is saving", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                contact.setId(String.valueOf(tempId));
                                contact.setStrFirstName(strFirstName);
                                contact.setStrLastName(strLastName);
                                contact.setStrNickName(strNickName);
                                contact.setStrMobileNum(strMobileNumber);
                                contact.setStrEmail(strEmail);
                                contact.setStrAdd(strAddress);
                                String strImageName = SharedPrefManager.getString("ImageFileName", "");
                                String strImagePath = SharedPrefManager.getString("ImagePath", "");
                                contact.setStrImageName(strImageName);
                                contact.setStrImagePath(strImagePath);
                                databaseReference.child(String.valueOf(tempId)).setValue(contact);
                                startActivity(new Intent(AddNewContactActivity.this, MainActivity.class));
                                // displaying a toast message.
                                Toast.makeText(AddNewContactActivity.this, "Contact Saved", Toast.LENGTH_SHORT).show();
                            }
                        }, 2000);

                        // starting a main activity.
                    }
                }else
                {
                    Snackbar snackbar = Snackbar.make(AddNewContactActivity.this.findViewById(android.R.id.content), "Check your connectivity", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

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