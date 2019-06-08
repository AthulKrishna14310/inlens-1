package com.integrals.inlens.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.integrals.inlens.AlbumProcedures.AlbumStartingServices;
import com.integrals.inlens.Helper.UploadDatabaseHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.integrals.inlens.Helper.CurrentDatabase;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.R;


public class CreateCloudAlbum extends AppCompatActivity {
    private ImageView                           SetPostImage;
    private EditText                            CommunityAlbumTitle;
    private EditText                            CommunityAlbumDescription;
    private Button                              SubmitButton;
    private ImageButton                         DisplayButton;
    private Uri                                 ImageUri;
    private DatabaseReference                   PostDatabaseReference;
    private DatabaseReference                   CommunityDatabaseReference;
    private StorageReference                    PostStorageReference;
    private static final int                    GALLERY_REQUEST = 3;
    private ProgressDialog                      InProgressDialog;
    private FirebaseAuth                        InAuthentication;
    private FirebaseUser                        InUser;
    private DatabaseReference                   InUserReference;
    private String                              PostKey;
    private DatabaseReference                   photographerReference,databaseReference,ComNotyRef;
    private String                              UserID;
    private Boolean                             PhotographerCreated = false;
    private ProgressBar                         UploadProgress;
    private TextView                            UploadProgressTextView;
    private Boolean                             OngoingTask =      false;
    private static final int                    GALLERY_PICK=1 ;
    private Boolean                             CloudAlbumDone=false;
    private TextView                            DateofCompletion;
    private String                              date;
    private String                              AlbumTime;
    private DatePickerDialog.OnDateSetListener  dateSetListener;
    private Calendar calendar;
    private TextView EventPicker ;
    private Dialog EventDialog,QRCodeDialog;
    private String EventType = "";
    private String CheckTimeTaken="";
    private ImageButton CreateCloudAlbumBackButton;
    private Boolean EventTypeSet = false ,AlbumDateSet = false;
    private AlbumStartingServices albumStartingServices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cloud_album);

        EventDialogInit();
         albumStartingServices= new AlbumStartingServices(getApplicationContext());
         InAuthentication = FirebaseAuth.getInstance();
        InUser = InAuthentication.getCurrentUser();
        CommunityDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Communities");
        InUserReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(InUser.getUid());
        UserID = InUser.getUid();

        CreateCloudAlbumBackButton = findViewById(R.id.create_cloud_album_backbutton);
        EventPicker = findViewById(R.id.EventTypeText);
        DisplayButton = (ImageButton) findViewById(R.id.DisplayImage);
        UploadProgressTextView = (TextView) findViewById(R.id.UploadProgressTextView);
        CommunityAlbumTitle = (EditText) findViewById(R.id.AlbumTitleEditText);
        CommunityAlbumDescription = (EditText) findViewById(R.id.AlbumDescriptionEditText);
        SubmitButton = (Button) findViewById(R.id.DoneButton);
        SetPostImage = (ImageView) findViewById(R.id.CoverPhoto);
        UploadProgress = (ProgressBar) findViewById(R.id.UploadProgress);
        PostStorageReference = FirebaseStorage.getInstance().getReference();
        PostDatabaseReference = InUserReference.child("Communities");
        InProgressDialog = new ProgressDialog(this);
        Calendar calender = Calendar.getInstance();
        //Created By Elson Jose
        DateofCompletion = findViewById(R.id.TimeEditText);


        int Month = calender.get(Calendar.MONTH);
        Month++;


        CheckTimeTaken=calender.get(Calendar.DAY_OF_MONTH) + "-"
                + String.valueOf(Month) + "-"+calender.get(Calendar.YEAR)
        ;

        DateofCompletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        CreateCloudAlbum.this,
                        dateSetListener,
                        year,month,day
                );
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month=month+1;
                if(month<10)
                {
                    AlbumTime = day + "-" +"0"+ month + "-" + year;
                    if(!checkNumberOfDays(CheckTimeTaken,AlbumTime)){
                        DateofCompletion.setText("Album Active until " + AlbumTime + " midnight");
                        DateofCompletion.setTextSize(12);
                        AlbumDateSet = true;
                        }else {
                        AlbumTime = "";
                        Toast.makeText(getApplicationContext(),"Album creation valid only for 5 days",Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {    AlbumTime = day + "-" + month + "-" + year;
                     if(!checkNumberOfDays(CheckTimeTaken,AlbumTime)){
                         DateofCompletion.setText("Album Active until " + AlbumTime + " midnight");
                         DateofCompletion.setTextSize(12);
                            AlbumDateSet = true;
                     }else {
                         AlbumTime = "";
                         Toast.makeText(getApplicationContext(),"Album creation valid only for 5 days",Toast.LENGTH_LONG).show();
                    }

                }


            }
        };




        DisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(OngoingTask==false) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setAspectRatio((int) 390,285)
                            .setFixAspectRatio(true)
                            .start(CreateCloudAlbum.this);
                }

            }
        });
        SetPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(OngoingTask==false) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setAspectRatio((int) 390,285)
                            .setFixAspectRatio(true)
                            .start(CreateCloudAlbum.this);
                }
            }
        });

        SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(EventTypeSet && AlbumDateSet)
                {





                    PostingStarts();



















                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Please fill up all the provided fields and add album cover photo ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        EventPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EventDialog.show();

            }
        });

        CreateCloudAlbumBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(CreateCloudAlbum.this,MainActivity.class).putExtra("QRCodeVisible",false));
                overridePendingTransition(R.anim.activity_fade_in,R.anim.activity_fade_out);
                finish();

            }
        });

    }

    private void EventDialogInit() {

        EventDialog = new Dialog(this,android.R.style.Theme_Light_NoTitleBar);
        EventDialog.setCancelable(true);
        EventDialog.setCanceledOnTouchOutside(false);
        EventDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        EventDialog.setContentView(R.layout.event_type_layout);
        EventDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

        Window EventDialogwindow = EventDialog.getWindow();
        EventDialogwindow.setGravity(Gravity.BOTTOM);
        EventDialogwindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        EventDialogwindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        EventDialogwindow.setDimAmount(0.75f);

        final Button EventWedding = EventDialog.findViewById(R.id.event_type_wedding_btn);
        final Button EventCeremony = EventDialog.findViewById(R.id.event_type_ceremony_btn);
        final Button EventOthers = EventDialog.findViewById(R.id.event_type_others_btn);
        final Button EventParty = EventDialog.findViewById(R.id.event_type_party_btn);
        final Button EventTravel = EventDialog.findViewById(R.id.event_type_travel_btn);
        final Button EventHangout = EventDialog.findViewById(R.id.event_type_hangouts_btn);
        final TextView SelectedEvent = EventDialog.findViewById(R.id.selected_event_type);
        SelectedEvent.setText("Selected Event Type : "+EventType );
        final ImageButton EventTypeDone  = EventDialog.findViewById(R.id.event_done_btn);

        if(!TextUtils.isEmpty(EventType))
        {
            EventTypeDone.setVisibility(View.VISIBLE);
        }
        else
        {
            EventTypeDone.setVisibility(View.GONE);

        }

        EventTypeDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(EventType))
                {
                    EventDialog.dismiss();
                    EventPicker.setText(String.format("Event Selected : %s", EventType));
                    EventPicker.setTextSize(12);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please select an event type.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        EventCeremony.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventParty,EventTravel,EventHangout);
                EventCeremony.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventType = "Ceremony";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventWedding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventCeremony,EventOthers,EventParty,EventTravel,EventHangout);
                EventWedding.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventType = "Wedding";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventCeremony,EventParty,EventTravel,EventHangout);
                EventOthers.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventType = "Others";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventCeremony,EventTravel,EventHangout);
                EventParty.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventType = "Party";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventParty,EventCeremony,EventHangout);
                EventTravel.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventType = "Travel";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });

        EventHangout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCheckFalse(EventWedding,EventOthers,EventParty,EventCeremony,EventParty);
                EventHangout.setBackgroundResource(R.drawable.radiobutton_pressed);
                EventType = "Hangouts";
                SelectedEvent.setText("Selected Event Type : "+EventType );
                if(!TextUtils.isEmpty(EventType))
                {
                    EventTypeDone.setVisibility(View.VISIBLE);
                    EventTypeSet = true;
                }
                else
                {
                    EventTypeDone.setVisibility(View.GONE);

                }
            }
        });


    }

    private void SetCheckFalse(Button btn1,Button btn2,Button btn3,Button btn4,Button btn5) {

        btn1.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn2.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn3.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn4.setBackgroundResource(R.drawable.radiobutton_unpressed);
        btn5.setBackgroundResource(R.drawable.radiobutton_unpressed);
    }


    private void PostingStarts() {

        final String TitleValue = CommunityAlbumTitle.getText().toString().trim();
        final String DescriptionValue = CommunityAlbumDescription.getText().toString().trim();
        if (!TextUtils.isEmpty(TitleValue) && !(TextUtils.isEmpty(EventType)&& EventTypeSet && AlbumDateSet && (!TextUtils.isEmpty(AlbumTime)))) {
            SubmitButton.setEnabled(false);
            if(ImageUri==null)
            {
                final String pushid = CommunityDatabaseReference.push().getKey();
                final DatabaseReference CommunityPost = CommunityDatabaseReference.child(pushid);
                final DatabaseReference NewPost = PostDatabaseReference.child(pushid);
                final Uri DownloadUri = Uri.parse("default");
                InUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        CommunityPost.child("AlbumTitle").setValue(TitleValue);
                        CommunityPost.child("AlbumDescription").setValue(DescriptionValue);
                        CommunityPost.child("AlbumCoverImage").setValue((DownloadUri).toString());
                        CommunityPost.child("User_ID").setValue(InUser.getUid());
                        CommunityPost.child("PostedByProfilePic").setValue(dataSnapshot.child("Profile_picture").getValue());
                        CommunityPost.child("UserName").setValue(dataSnapshot.child("Name").getValue());
                        CommunityPost.child("ActiveIndex").setValue("T");
                        CommunityPost.child("AlbumExpiry").setValue(AlbumTime);
                        CommunityPost.child("AlbumType").setValue(EventType);
                        CommunityPost.child("EndingTimestamp").setValue(GetTimeStamp(AlbumTime));
                        CommunityPost.child("CreatedTimestamp").setValue(ServerValue.TIMESTAMP);
                        PostKey = CommunityPost.getKey().trim();

                        NewPost.child("AlbumTitle").setValue(TitleValue);
                        NewPost.child("AlbumDescription").setValue(DescriptionValue);
                        NewPost.child("AlbumCoverImage").setValue((DownloadUri).toString());
                        NewPost.child("User_ID").setValue(InUser.getUid());
                        NewPost.child("PostedByProfilePic").setValue(dataSnapshot.child("Profile_picture").getValue());
                        NewPost.child("UserName").setValue(dataSnapshot.child("Name").getValue());
                        NewPost.child("CommunityID").setValue(PostKey);
                        NewPost.child("CreatedTimestamp").setValue(ServerValue.TIMESTAMP);
                        NewPost.child("AlbumType").setValue(EventType);
                        InUserReference.child("live_community").setValue(pushid);
                        InProgressDialog.setMessage("Saving new data....");

                        CurrentDatabase currentDatabase1 = new CurrentDatabase(getApplicationContext(), "", null, 1);
                        currentDatabase1.DeleteDatabase();
                        UploadDatabaseHelper uploadDatabaseHelper = new UploadDatabaseHelper(getApplicationContext(), "", null, 1);
                        uploadDatabaseHelper.DeleteDatabase();


                        CurrentDatabase currentDatabase= new CurrentDatabase(getApplicationContext(),"",null,1);
                        currentDatabase.InsertUploadValues(PostKey,0,1,0,AlbumTime,1,1,"CUREE");
                        currentDatabase.close();
                        InProgressDialog.setMessage("Finishing....");
                        if (PhotographerCreated == false) {
                            photographerReference = FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("Communities")
                                    .child(PostKey)
                                    .child("CommunityPhotographer");

                            DatabaseReference databaseReference = photographerReference.push();
                            databaseReference.child("Photographer_UID").setValue(UserID);
                            databaseReference.child("Name").setValue(dataSnapshot.child("Name").getValue());
                            databaseReference.child("Profile_picture").setValue(dataSnapshot.child("Profile_picture").getValue());
                            databaseReference.child("Email_ID").setValue(dataSnapshot.child("Email").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        if(task.isComplete()){
                                            if(!CloudAlbumDone) {
                                                InProgressDialog.dismiss();
                                                UploadProgressTextView.setText("Cloud Album Created.");
                                                UploadProgress.setVisibility(View.INVISIBLE);
                                                SubmitButton.setVisibility(View.VISIBLE);
                                                CloudAlbumDone=true;
                                                StartServices();
                                                SharedPreferences AlbumClickDetails = getSharedPreferences("LastClickedAlbum",MODE_PRIVATE);
                                                SharedPreferences.Editor  AlbumEditor = AlbumClickDetails.edit();
                                                AlbumEditor.putInt("last_clicked_position",0);
                                                AlbumEditor.apply();

                                            }

                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(getApplicationContext(),"Error Detected",Toast.LENGTH_SHORT).show();
                                    SubmitButton.setEnabled(true);

                                }
                            });
                            CreateSituation();
                            PhotographerCreated = true;

                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CreateCloudAlbum.this, "Sorry database error ...please try again", Toast.LENGTH_LONG).show();
                    }
                });

            }
            else
            {

                StorageReference
                        FilePath = PostStorageReference
                        .child("CommunityCoverPhoto")
                        .child(ImageUri.getLastPathSegment());
                FilePath
                        .putFile(ImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                final Uri DownloadUri = taskSnapshot.getDownloadUrl();
                                String pushid = CommunityDatabaseReference.push().getKey();
                                final DatabaseReference CommunityPost = CommunityDatabaseReference.child(pushid);
                                final DatabaseReference NewPost = PostDatabaseReference.child(pushid);
                                InUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        CommunityPost.child("AlbumTitle").setValue(TitleValue);
                                        CommunityPost.child("AlbumDescription").setValue(DescriptionValue);
                                        CommunityPost.child("AlbumCoverImage").setValue((DownloadUri).toString());
                                        CommunityPost.child("User_ID").setValue(InUser.getUid());
                                        CommunityPost.child("PostedByProfilePic").setValue(dataSnapshot.child("Profile_picture").getValue());
                                        CommunityPost.child("UserName").setValue(dataSnapshot.child("Name").getValue());
                                        CommunityPost.child("ActiveIndex").setValue("T");
                                        CommunityPost.child("AlbumExpiry").setValue(AlbumTime);
                                        CommunityPost.child("AlbumType").setValue(EventType);
                                        CommunityPost.child("CreatedTimestamp").setValue(ServerValue.TIMESTAMP);
                                        PostKey = CommunityPost.getKey().trim();

                                        NewPost.child("AlbumTitle").setValue(TitleValue);
                                        NewPost.child("AlbumDescription").setValue(DescriptionValue);
                                        NewPost.child("AlbumCoverImage").setValue((DownloadUri).toString());
                                        NewPost.child("User_ID").setValue(InUser.getUid());
                                        NewPost.child("PostedByProfilePic").setValue(dataSnapshot.child("Profile_picture").getValue());
                                        NewPost.child("UserName").setValue(dataSnapshot.child("Name").getValue());
                                        NewPost.child("CommunityID").setValue(PostKey);
                                        NewPost.child("CreatedTimestamp").setValue(ServerValue.TIMESTAMP);
                                        NewPost.child("AlbumType").setValue(EventType);

                                        InProgressDialog.setMessage("Saving new data....");
                                        CurrentDatabase currentDatabase= new CurrentDatabase(getApplicationContext(),"",null,1);
                                        currentDatabase.InsertUploadValues(PostKey,0,1,0,AlbumTime,1,1,"CUREE");
                                        currentDatabase.close();
                                        InProgressDialog.setMessage("Finishing....");
                                        if (PhotographerCreated == false) {
                                            photographerReference = FirebaseDatabase.getInstance()
                                                    .getReference()
                                                    .child("Communities")
                                                    .child(PostKey)
                                                    .child("CommunityPhotographer");

                                            DatabaseReference databaseReference = photographerReference.push();
                                            databaseReference.child("Photographer_UID").setValue(UserID);
                                            databaseReference.child("Name").setValue(dataSnapshot.child("Name").getValue());
                                            databaseReference.child("Profile_picture").setValue(dataSnapshot.child("Profile_picture").getValue());
                                            databaseReference.child("Email_ID").setValue(dataSnapshot.child("Email").getValue());
                                            CreateSituation();
                                            PhotographerCreated = true;

                                        }



                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(CreateCloudAlbum.this, "Sorry database error ...please try again", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                     @Override
                                                     public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                         OngoingTask = true;
                                                         UploadProgressTextView.setVisibility(View.VISIBLE);
                                                         SubmitButton.setVisibility(View.INVISIBLE);
                                                         UploadProgress.setVisibility(View.VISIBLE);
                                                         double progress =
                                                                 (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                                                         .getTotalByteCount());
                                                         String UploadIndex = "Creating new Cloud-Album, "+ (int) progress + "%" + " completed.";
                                                         UploadProgressTextView.setText(UploadIndex);

                                                     }
                                                 }
                ).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.isComplete()){
                                if(!CloudAlbumDone) {
                                    InProgressDialog.dismiss();
                                    UploadProgressTextView.setText("Cloud Album Created.");
                                    UploadProgress.setVisibility(View.INVISIBLE);
                                    SubmitButton.setVisibility(View.VISIBLE);
                                    CloudAlbumDone=true;
                                    StartServices();
                                    SharedPreferences AlbumClickDetails = getSharedPreferences("LastClickedAlbum",MODE_PRIVATE);
                                    SharedPreferences.Editor  AlbumEditor = AlbumClickDetails.edit();
                                    AlbumEditor.putInt("last_clicked_position",0);
                                    AlbumEditor.apply();
                                    finish();

                                }

                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getApplicationContext(),"Error Detected",Toast.LENGTH_SHORT).show();
                        SubmitButton.setEnabled(true);
                    }
                });

            }


        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    "Please fill up all the provided fields and add album cover photo ", Toast.LENGTH_SHORT).show();
        }
    }

    private String GetTimeStamp(String albumTime) {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = (Date)formatter.parse(albumTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long output=date.getTime()/1000L;
        String str=Long.toString(output);
        long timestamp = Long.parseLong(str) * 1000;

        timestamp+=86399000;
        return String.valueOf(timestamp);
    }

    private Boolean checkNumberOfDays(String DateStart, String DateEnd){

        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        String inputString1 = DateStart;
        String inputString2 = DateEnd;

        try {
            Date date1 = myFormat.parse(inputString1);
            Date date2 = myFormat.parse(inputString2);
            long diff = date2.getTime() - date1.getTime();
            if(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)>=5){
                return true;
            }else {
                return false;
            }

            }
            catch (ParseException e) {
            e.printStackTrace();
            return false;
             }


    }



    private void StartServices() {
        SharedPreferences sharedPreferences = getSharedPreferences("InCommunity.pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("UsingCommunity::", true);
        editor.commit();
        SharedPreferences sharedPreferences1 = getSharedPreferences("Owner.pref", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.putBoolean("ThisOwner::", true);
        editor1.commit();

        albumStartingServices.initiateJobServices();
        albumStartingServices.intiateNotificationAtStart();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(this);
            finish();

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                DisplayButton.setVisibility(View.INVISIBLE);
                ImageUri = result.getUri();
                SetPostImage.setImageURI(ImageUri);
            }
        }
        else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(getApplicationContext(),"Crop failed. ",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {

        if (OngoingTask == true) {
            Toast.makeText(getApplicationContext(), "Creating your Cloud-Album. Please wait.", Toast.LENGTH_SHORT).show();
        }

    }



    private void CreateSituation()
    {

        CurrentDatabase currentDatabase=new CurrentDatabase(getApplicationContext(),"",null,1);
        final String CommunityID=currentDatabase.GetLiveCommunityID();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Communities")
                .child(CommunityID).child("Situations");
        ComNotyRef = FirebaseDatabase.getInstance().getReference().child("Communities")
                .child(CommunityID).child("CommunityPhotographer");
        calendar=Calendar.getInstance();
        String SituationTimeIntervel=calendar.get(Calendar.YEAR)+ "-"
                +calendar.get(Calendar.MONTH)+"-"
                +calendar.get(Calendar.DAY_OF_MONTH)+"T"
                +calendar.get(Calendar.HOUR_OF_DAY)+"-"
                +calendar.get(Calendar.MINUTE)+"-"
                +calendar.get(Calendar.SECOND);

        Map situationmap = new HashMap();
        situationmap.put("name","Event Started");
        situationmap.put("time", ServerValue.TIMESTAMP);
        situationmap.put("owner", FirebaseAuth.getInstance().getCurrentUser().getUid());
        final String push_id =databaseReference.push().getKey();
        // Added by Athul Krishna For Implementation of Situation Upload
        situationmap.put("SituationKey",push_id);
        situationmap.put("SituationTime",SituationTimeIntervel);
        final Map member = new HashMap();
        member.put("memid",FirebaseAuth.getInstance().getCurrentUser().getUid());
        final DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("ComNoty");
        ComNotyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String id = snapshot.child("Photographer_UID").getValue().toString();
                    dref.child(id).push().child("comid").setValue(CommunityID);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child(push_id).setValue(situationmap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    databaseReference.child(push_id).child("members").push().setValue(member);
                    Toast.makeText(CreateCloudAlbum.this,"New Situation Created : "+"Event Started",Toast.LENGTH_SHORT).show();
                    CloudAlbumDone=false;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if(e.toString().contains("FirebaseNetworkException"))
                    Toast.makeText(CreateCloudAlbum.this,"Not Connected to Internet.",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(CreateCloudAlbum.this,"Unable to create new Situation.", Toast.LENGTH_SHORT).show();
            }
        });



       currentDatabase.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home)
        {
            startActivity(new Intent(CreateCloudAlbum.this,MainActivity.class).putExtra("QRCodeVisible",false));
            overridePendingTransition(R.anim.activity_fade_in,R.anim.activity_fade_out);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            startActivity(new Intent(CreateCloudAlbum.this,MainActivity.class).putExtra("QRCodeVisible",false));
            overridePendingTransition(R.anim.activity_fade_in,R.anim.activity_fade_out);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}