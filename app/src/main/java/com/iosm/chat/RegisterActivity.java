package com.iosm.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {


    private Button Registerbutton ;
    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private TextView AlreadyHaveAccountLink ;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){

                SendUserToLoginActivity();
            }
        });




        Registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }


    public void CreateNewAccount(){
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmpassword = UserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "please, Enter you email", Toast.LENGTH_SHORT).show();
        }
       else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "please, Enter you password", Toast.LENGTH_SHORT).show();
        }
       else if(TextUtils.isEmpty(confirmpassword)){
            Toast.makeText(this, "please, Confirm you password", Toast.LENGTH_SHORT).show();
        }
       else if(!password.equals(confirmpassword)){
            Toast.makeText(this, "please , Check your password and your confirmation password", Toast.LENGTH_SHORT).show();
        }

        else{
            loadingbar.setTitle("Creating new Account");
            loadingbar.setMessage("Please Wait ...");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();



                        String CurrentUserId = mAuth.getCurrentUser().getUid();
                        RootRef.child("Users").child(CurrentUserId).setValue("");

                        RootRef.child("Users").child(CurrentUserId).child("device_token").setValue(deviceToken);

                        SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Account created Succefully", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }

                }
            });
        }


    }



    public void InitializeFields(){

        Registerbutton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserConfirmPassword = (EditText) findViewById(R.id.register_password_confirm);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_acount_link);
        loadingbar = new ProgressDialog(this) ;


    }

    public void SendUserToLoginActivity(){
        Intent loginIntent = new Intent(RegisterActivity.this , LoginActivity.class);
        startActivity(loginIntent);
    }

    public void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
