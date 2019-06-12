package com.integrals.inlens.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.integrals.inlens.Helper.CurrentDatabase;
import com.integrals.inlens.MainActivity;
import com.integrals.inlens.Models.Blog;
import com.integrals.inlens.Models.SituationModel;
import com.integrals.inlens.R;
import com.integrals.inlens.ViewHolder.GridImageAdapter;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudAlbum extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private String CommunityIDLocal;
    private String CommunityID;
    private Calendar calendar;
    private SituationAdapter adapter;
    private String sowner, stime, stitle, sKey, sTime;
    private List<SituationModel> SituationList;
    private List<String> SituationIDList;
    private DatabaseReference db, ComNotyRef, deleteDatabaseReference;
    private String Album;
    private Button NewSituation;
    private String ReturnName = "Oops";
    private String TimeEnd, TimeStart, GlobalID;
    private Boolean LastPost;
    private DatabaseReference databaseReferencePhotoList = null;
    private Dialog createNewSituation;
    List<String> ImageList = new ArrayList<>();

    private String Name;
    private Button SwipeControl;
    private Boolean SwipeUp = false;
    private String TestCommunityID = null;
    private String LocalID;
    private String CurrentUser;
    private EditText SitEditName;
    private Activity cloudalbumcontext;

    //////QR CODE DIALOG///////////////////////////////////////////////////////////////////////////////|
    private String PhotographerID;
    ImageView QRCodeImageView;
    private String Default = "No current community";
    private String QRCommunityID = "1122333311101";
    private TextView textView;
    private Button InviteLinkButton;
    private Dialog QRCodeDialog;

    //For Snackbar
    private CoordinatorLayout RootForCloudAlbum;

    private ImageButton DimBackground;
    private FloatingActionButton MainCloudFab, CreateSitFab, DeleteAlbumFab, InviteAlbumFab;
    private Animation FabOpen, FabClose, FabRotateForward, FabRotateBackward;
    private Boolean IsFabOpen = false;

    private ImageButton CloudAlbumBackButton;
    private TextView CloudAlbumTitle;
    private View CloudBottomSheetView;
    private BottomSheetBehavior CloudBottomSheetBehavior;
    private TextView CloudBottomSheetTitle;
    private RecyclerView CloudBottomSheetRecyclerView;

    //imported from photolist helper

    private List<Blog>          BlogList;
    private List<String>        BlogListID ,BlogImagesList;
    private String              PhotoThumb;
    private String              BlogTitle,ImageThumb,BlogDescription,Location;
    private String              TimeTaken,UserName,User_ID,WeatherDetails,PostedByProfilePic;
    private String              OriginalImageName;
    private String              PhotoListeHelperTimeEnd,PhotoListeHelperTimeStart,PhotoListeHelperGlobalID;
    private Boolean             PhotoListeHelperLastPost;
    private String              PhotoListeHelperCommunityID;
    private DatabaseReference   PhotoListeHelperdatabaseReferencePhotoList;
    private GridImageAdapter    gridImageAdapter;
    private static final String FILE_NAME = "UserInfo.ser";

    ///////////////////////////////////////////////////////////////////////////////////////////////////|
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_cloud_album);


        QRCodeInit();
        RootForCloudAlbum = findViewById(R.id.root_for_cloud_album);
        CloudBottomSheetView = findViewById(R.id.cloudalbum_bottomsheet_view);
        CloudBottomSheetBehavior = BottomSheetBehavior.from(CloudBottomSheetView);
        CloudBottomSheetTitle = findViewById(R.id.cloudalbum_bottomsheet_title);
        CloudBottomSheetRecyclerView = findViewById(R.id.cloudalbum_bottomsheet_recyclerview);
        CloudBottomSheetRecyclerView.setHasFixedSize(true);
        CloudBottomSheetRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        if (!IsConnectedToNet()) {
            Snackbar.make(RootForCloudAlbum, "Unable to connect to internet.", Snackbar.LENGTH_SHORT).show();
        }

        CloudAlbumTitle = findViewById(R.id.cloudalbum_tooolbar_title);
        CloudAlbumBackButton = findViewById(R.id.cloudalbum_tooolbar_backbutton);


        cloudalbumcontext = this;
        CommunityIDLocal = getIntent().getStringExtra("PostKeyLocal::");
        SwipeControl = (Button) findViewById(R.id.SwipeControl);
        String AlbumName = getIntent().getStringExtra("AlbumName");
        CloudAlbumTitle.setText(AlbumName);
        CommunityID = getIntent().getStringExtra("GlobalID::");
        LocalID = getIntent().getStringExtra("LocalID::");
        CurrentUser = getIntent().getStringExtra("UserID::");
        recyclerView = (RecyclerView) findViewById(R.id.SituationRecyclerView);
        deleteDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Users")
                .child(CurrentUser)
                .child("Communities").child(LocalID);

        final GridLayoutManager gridLayoutManager = new
                GridLayoutManager(getApplicationContext(), 2,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);
        Album = AlbumName;
        SituationList = new ArrayList<>();
        SituationIDList = new ArrayList<>();
        TimeEnd = "2100-8-6T13-22-45";
        TimeStart = "2000-8-6T13-22-45";
        Name = "Album Photos";
        LastPost = false;
        db = FirebaseDatabase.getInstance().getReference().child("Users");
        SharedPreferences sPreferences = getSharedPreferences("ComIDPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        if (!TextUtils.isEmpty(CommunityID) && !TextUtils.isEmpty(Album)) {
            editor.putString("GlobalID::", CommunityID);
            editor.putString("Album", AlbumName);
            editor.commit();
        } else {
            CommunityID = sPreferences.getString("GlobalID::", "");
            AlbumName = sPreferences.getString("Album", "");

        }


        //////////////////////////////////////////////////////////////////////////////////////////
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Communities")
                .child(CommunityID).child("Situations");
        ComNotyRef = FirebaseDatabase.getInstance().getReference().child("Communities")
                .child(CommunityID).child("CommunityPhotographer");

        FabAnimationAndButtonsInit();


        ///////////Create New Situation////////////////////////////////////////////////////////////
        createNewSituation = new Dialog(CloudAlbum.this, android.R.style.Theme_Light_NoTitleBar);
        createNewSituation.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createNewSituation.setContentView(R.layout.create_new_situation_layout);
        createNewSituation.setCancelable(false);
        createNewSituation.setCanceledOnTouchOutside(true);
        createNewSituation.getWindow().getAttributes().windowAnimations = R.style.UpBottomSlideDialogAnimation;

        Window createNewSituationWindow = createNewSituation.getWindow();
        createNewSituationWindow.setGravity(Gravity.TOP);
        createNewSituationWindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        createNewSituationWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        createNewSituationWindow.setDimAmount(0.75f);
        createNewSituationWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        SitEditName = createNewSituation.findViewById(R.id.situation_name);
        SitEditName.requestFocus();
        Button Done, Cancel;
        Done = createNewSituation.findViewById(R.id.done_btn);
        Cancel = createNewSituation.findViewById(R.id.cancel_btn);
        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(SitEditName.getText().toString())) {
                    calendar = Calendar.getInstance();
                    String SituationTimeIntervel = calendar.get(Calendar.YEAR) + "-"
                            + calendar.get(Calendar.MONTH) + "-"
                            + calendar.get(Calendar.DAY_OF_MONTH) + "T"
                            + calendar.get(Calendar.HOUR_OF_DAY) + "-"
                            + calendar.get(Calendar.MINUTE) + "-"
                            + calendar.get(Calendar.SECOND);


                    Map situationmap = new HashMap();
                    situationmap.put("name", SitEditName.getText().toString().trim());
                    situationmap.put("time", ServerValue.TIMESTAMP);
                    situationmap.put("owner", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    final String push_id = databaseReference.push().getKey();


                    // Added by  For Implementation of Situation Upload
                    situationmap.put("SituationKey", push_id);
                    situationmap.put("SituationTime", SituationTimeIntervel);
                    final Map member = new HashMap();
                    member.put("memid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    final DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("ComNoty");
                    ComNotyRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // Situation Notification function by elson jose

                            Map notymap = new HashMap();
                            notymap.put("name", SitEditName.getText().toString().trim());
                            notymap.put("ownername", GetUserName(FirebaseAuth.getInstance().getCurrentUser().getUid()));


                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String id = snapshot.child("Photographer_UID").getValue().toString();
                                if (!id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                    dref.child(id).push().setValue(notymap);
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    databaseReference.child(push_id).setValue(situationmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                databaseReference.child(push_id).child("members").push().setValue(member);
                                Toast.makeText(CloudAlbum.this, "New Situation Created : " + SitEditName.getText().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            if (e.toString().contains("FirebaseNetworkException"))
                                Toast.makeText(CloudAlbum.this, "Not Connected to Internet.", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(CloudAlbum.this, "Unable to create new Situation.", Toast.LENGTH_SHORT).show();


                        }
                    });
                    createNewSituation.dismiss();
                } else {
                    Toast.makeText(CloudAlbum.this, "No name given", Toast.LENGTH_LONG).show();
                }

            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewSituation.dismiss();
            }
        });

////////////////////////////////////////////////////////////////////////////////////////////////////
        CurrentDatabase currentDatabase = new CurrentDatabase(getApplicationContext(),
                "", null, 1);
        TestCommunityID = currentDatabase.GetLiveCommunityID();
        currentDatabase.close();

        //////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////
        databaseReferencePhotoList = FirebaseDatabase.getInstance().getReference().child("Communities")
                .child(CommunityID).child("BlogPosts");

        CloudAlbumBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        DimBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(IsFabOpen)
                {
                    AnimateFab();
                    IsFabOpen = false;
                }
                else if( DimBackground.isShown())
                {
                    CloudBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                else if(CloudBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || CloudBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                {
                    CloudBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                else
                {
                   DimBackground.setVisibility(View.GONE);
                }


            }
        });

////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    private void FabAnimationAndButtonsInit() {

        DimBackground = findViewById(R.id.cloudalbum_dim_background);
        FabOpen = AnimationUtils.loadAnimation(this, R.anim.main_fab_open);
        FabClose = AnimationUtils.loadAnimation(this, R.anim.main_fab_close);
        FabRotateForward = AnimationUtils.loadAnimation(this, R.anim.main_fab_rotate_forward);
        FabRotateBackward = AnimationUtils.loadAnimation(this, R.anim.main_fab_rotate_backward);

        MainCloudFab = findViewById(R.id.cloudalbum_fab_btn);
        DeleteAlbumFab = findViewById(R.id.cloudalbum_delete_fab_btn);
        InviteAlbumFab = findViewById(R.id.cloudalbum_invite_fab_btn);
        CreateSitFab = findViewById(R.id.cloudalbum_new_situation_fab_btn);


        FirebaseDatabase.getInstance().getReference().child("Communities")
                .child(CommunityID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("AlbumExpiry"))
                {
                    String CreatedTimestamp = dataSnapshot.child("AlbumExpiry").getValue().toString();
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();
                    int result = dateformat.compareTo(CreatedTimestamp);
                    if(result >= 0)
                    {
                        MainCloudFab.setVisibility(View.GONE);

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        MainCloudFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(IsFabOpen)
                {
                    AnimateFab();
                    IsFabOpen = false;
                }
                else
                {
                    AnimateFab();
                    IsFabOpen = true;
                }

            }
        });

        DeleteAlbumFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToNet()) {
                    DeleteCurrentAlbum();
                } else {
                    Toast.makeText(getApplicationContext(), "Umable to connec to Internet.", Toast.LENGTH_SHORT).show();
                }

                AnimateFab();
            }
        });

        InviteAlbumFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QRCodeDialog.show();
                AnimateFab();
            }
        });

        CreateSitFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToNet()) {
                    createNewSituation.show();
                    int countdef = SituationIDList.size() + 1;
                    SitEditName.setText(String.format("New Situation %s", String.valueOf(countdef)));
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to connect to Internet.", Toast.LENGTH_SHORT).show();

                }
                AnimateFab();

            }
        });

    }

    private void AnimateFab() {

        if (IsFabOpen) {

            DimBackground.setVisibility(View.GONE);
            DeleteAlbumFab.clearAnimation();
            DeleteAlbumFab.setAnimation(FabClose);
            DeleteAlbumFab.getAnimation().start();

            InviteAlbumFab.clearAnimation();
            InviteAlbumFab.setAnimation(FabClose);
            InviteAlbumFab.getAnimation().start();

            CreateSitFab.clearAnimation();
            CreateSitFab.setAnimation(FabClose);
            CreateSitFab.getAnimation().start();


            DeleteAlbumFab.setVisibility(View.INVISIBLE);
            InviteAlbumFab.setVisibility(View.INVISIBLE);
            CreateSitFab.setVisibility(View.INVISIBLE);

            MainCloudFab.clearAnimation();
            MainCloudFab.setAnimation(FabRotateBackward);
            MainCloudFab.getAnimation().start();

            IsFabOpen = false;
        } else {

            DeleteAlbumFab.clearAnimation();
            DeleteAlbumFab.setAnimation(FabOpen);
            DeleteAlbumFab.getAnimation().start();

            InviteAlbumFab.clearAnimation();
            InviteAlbumFab.setAnimation(FabOpen);
            InviteAlbumFab.getAnimation().start();

            CreateSitFab.clearAnimation();
            CreateSitFab.setAnimation(FabOpen);
            CreateSitFab.getAnimation().start();

            DeleteAlbumFab.setVisibility(View.VISIBLE);
            InviteAlbumFab.setVisibility(View.VISIBLE);
            CreateSitFab.setVisibility(View.VISIBLE);

            MainCloudFab.clearAnimation();
            MainCloudFab.setAnimation(FabRotateForward);
            MainCloudFab.getAnimation().start();
            IsFabOpen = true;

            DimBackground.setVisibility(View.VISIBLE);

        }

    }


    private void QRCodeInit() {

        QRCodeDialog = new Dialog(this, android.R.style.Theme_Light_NoTitleBar);
        QRCodeDialog.setCancelable(true);
        QRCodeDialog.setCanceledOnTouchOutside(true);
        QRCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        QRCodeDialog.setContentView(R.layout.activity_qrcode_generator);
        QRCodeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        QRCodeDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

        Window QRCodewindow = QRCodeDialog.getWindow();
        QRCodewindow.setGravity(Gravity.BOTTOM);
        QRCodewindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        QRCodewindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        QRCodewindow.setDimAmount(0.75f);
        QRCodewindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        CurrentDatabase currentDatabase = new CurrentDatabase(getApplicationContext(), "", null, 1);
        QRCommunityID = currentDatabase.GetLiveCommunityID();
        currentDatabase.close();

        InviteLinkButton = QRCodeDialog.findViewById(R.id.InviteLinkButton);
        PhotographerID = QRCommunityID;

        ImageButton QRCodeCloseBtn = QRCodeDialog.findViewById(R.id.QR_dialog_closebtn);
        QRCodeCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                QRCodeDialog.dismiss();

            }
        });
        textView = QRCodeDialog.findViewById(R.id.textViewAlbumQR);
        QRCodeImageView = QRCodeDialog.findViewById(R.id.QR_Display);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        String data = GetFileData();
        final String UserInfo[] = data.split("\n");

        if(UserInfo[1].equals("Not Available"))
        {
            Toast.makeText(getApplicationContext(), "You do not participate in any community.", Toast.LENGTH_SHORT).show();
        }
        else
        {

            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(UserInfo[1], BarcodeFormat.QR_CODE, 200, 200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                QRCodeImageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                QRCodeImageView.setVisibility(View.INVISIBLE);
                textView.setText("You must be in an album to generate QR code");
            } catch (NullPointerException e) {
                QRCodeImageView.setVisibility(View.INVISIBLE);
                textView.setText("You must be in an album to generate QR code");

            }
        }
        InviteLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent SharingIntent = new Intent(Intent.ACTION_SEND);
                SharingIntent.setType("text/plain");

                SharingIntent.putExtra(Intent.EXTRA_TEXT, "InLens Cloud-Album Invite Link \n\n" + GenarateDeepLinkForInvite(UserInfo[1]));
                startActivity(SharingIntent);

            }
        });
    }

    private String GetFileData() {


        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder builder = new StringBuilder();
            String text;
            while ((text = bufferedReader.readLine()) != null)
            {
                builder.append(text).append("\n");
            }

            return builder.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        finally {
            if(fileInputStream!=null)
            {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "no data";
    }


    private static String GenarateDeepLinkForInvite(String CommunityID) {
        return "https://inlens.page.link/?link=https://integrals.inlens.in/comid=" + CommunityID + "/&apn=com.integrals.inlens";
    }

    private String GetUserName(String uid) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("Name").getValue().toString();
                ReturnName = name;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return ReturnName;
    }

    private boolean IsConnectedToNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

    }


    @Override
    public void onBackPressed() {

        if(IsFabOpen)
        {
            AnimateFab();
            IsFabOpen = false;
        }
        else if(CloudBottomSheetBehavior.getState()==(BottomSheetBehavior.STATE_COLLAPSED) || CloudBottomSheetBehavior.getState()==(BottomSheetBehavior.STATE_EXPANDED))
        {
            CloudBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        else
        {
            super.onBackPressed();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        CloudBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SituationIDList.clear();
                SituationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                    if (snapshot.hasChildren()) {
                        String SituationId = snapshot.getKey();


                        if (snapshot.hasChild("owner")) {
                            String ownerid = snapshot.child("owner").getValue().toString();
                            sowner = ownerid;
                        }

                        if (snapshot.hasChild("time")) {
                            String time = snapshot.child("time").getValue().toString();
                            stime = time;
                        }

                        if (snapshot.hasChild("name")) {
                            String title = snapshot.child("name").getValue().toString();
                            stitle = title;

                            if (snapshot.hasChild("SituationKey")) {
                                String SituationKey = snapshot.child("SituationKey").getValue().toString();
                                sKey = SituationKey;

                            }
                            if (snapshot.hasChild("SituationTime")) {
                                String SituationTime = snapshot.child("SituationTime").getValue().toString();
                                sTime = SituationTime;

                            }

                        }

                        if (!SituationIDList.contains(SituationId)) {
                            SituationIDList.add(SituationId);
                            SituationModel model = new SituationModel(sowner, stime, stitle, sKey, sTime);
                            SituationList.add(model);
                        }

                    }


                }

                adapter = new SituationAdapter(getApplicationContext(),
                        SituationList,
                        SituationIDList,
                        databaseReference,
                        databaseReferencePhotoList,
                        CommunityID, cloudalbumcontext,CloudBottomSheetBehavior);

                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


////////////////////////////Situation View Implemented/////////////////////////////////////////////|


    private void DeleteCurrentAlbum() {/*
                deleteDatabaseReference.removeValue().addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                             Toast.makeText(getApplicationContext(),"Album removed",Toast.LENGTH_SHORT).show();
                             finish();

                            }
                        }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Unable to remove album . Please check your internet connection.",Toast.LENGTH_SHORT).show();

                    }
                });
    */
        Toast.makeText(getApplicationContext(), "This feature to be available soon", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    public class SituationAdapter extends RecyclerView.Adapter<SituationAdapter.SituationViewHolder> {

        Context context;
        List<SituationModel> Situation;
        List<String> SIdList;
        DatabaseReference databaseReference;
        String CommunityID;
        Activity CloudAlbum;
        Dialog Renamesituation;
        DatabaseReference databaseReferencePhotoList;
        BottomSheetBehavior SituationBottomSheetBehavior;


        public SituationAdapter(Context context,
                                List<SituationModel> situation,
                                List<String> SIdList,
                                DatabaseReference databaseReference,
                                DatabaseReference db,
                                String communityID,
                                Activity cloudAlbum ,BottomSheetBehavior bottomsheetbehavior) {
            this.context = context;
            Situation = situation;
            this.SIdList = SIdList;
            this.databaseReference = databaseReference;
            CommunityID = communityID;
            CloudAlbum = cloudAlbum;
            this.databaseReferencePhotoList = db;
            SituationBottomSheetBehavior = bottomsheetbehavior;

        }


        @NonNull
        @Override
        public SituationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.cloud_album_layout, parent, false);
            return new SituationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final SituationViewHolder holder, final int position) {

            SituationBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            if(Situation.get(position).getTime().contains("/") ||Situation.get(position).getTime().contains("-")  )
            {
                holder.Time.setText("created on : " + Situation.get(position).getTime());
            }
            else
            {
                long time = Long.parseLong(Situation.get(position).getTime());
                CharSequence Time = DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                String timesubstring = Time.toString().substring(Time.length() - 8);
                Date date = new Date(time);
                String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();
                holder.Time.setText("created on : " + dateformat + " @ " + timesubstring);
            }



            holder.Title.setText(String.format("%s", Situation.get(position).getTitle()));

            holder.EditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowDialog(position);
                }
            });


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CloudBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    CloudBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                        @Override
                        public void onStateChanged(@NonNull View bottomSheet, int newState) {

                            if (CloudBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || CloudBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                            {
                                DimBackground.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                DimBackground.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                            DimBackground.setVisibility(View.VISIBLE);
                        }
                    });

                    try {
                        DisplayBottomSheet(Situation.get(position).getSituationTime(),
                                Situation.get(position + 1).getSituationTime(),
                                CommunityID,
                                Situation.get(position).getTitle(),
                                false,cloudalbumcontext);

                    } catch (IndexOutOfBoundsException e) {
                        DisplayBottomSheet(Situation.get(position).getSituationTime(),
                                Situation.get(position).getSituationTime(),
                                CommunityID,
                                Situation.get(position).getTitle(),
                                true,cloudalbumcontext);
                    }

                }
            });


            databaseReferencePhotoList.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (snapshot.hasChildren()) {
                            try {
                                if (CheckIntervel(snapshot.child("TimeTaken").getValue().toString(), Situation.get(position).getSituationTime(),Situation.get(position+1).getSituationTime(),false)) {

                                    if (snapshot.hasChild("ImageThumb")) {
                                        String imageThumb = snapshot.child("ImageThumb").getValue().toString();
                                        StoreImage(imageThumb,holder.CloudAlbumLayout_ImageView, holder.CloudAlbumPBar);
                                    }
                                    else
                                    {
                                        holder.CloudAlbumPBar.setVisibility(View.GONE);
                                    }


                                }
                            }
                            catch (IndexOutOfBoundsException e)
                            {
                                if (CheckIntervel(snapshot.child("TimeTaken").getValue().toString(), Situation.get(position).getSituationTime(),Situation.get(position).getSituationTime(),true)) {

                                    if (snapshot.hasChild("ImageThumb")) {
                                        String imageThumb = snapshot.child("ImageThumb").getValue().toString();
                                        StoreImage(imageThumb,holder.CloudAlbumLayout_ImageView,holder.CloudAlbumPBar);
                                    }
                                    else
                                    {
                                        holder.CloudAlbumPBar.setVisibility(View.GONE);
                                    }


                                }
                            }
                            catch (NullPointerException e){
                                e.printStackTrace();
                            }



                        }
                        else
                        {
                            holder.CloudAlbumPBar.setVisibility(View.GONE);
                        }


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




        }

        private void StoreImage(String imageThumb,
                                ViewFlipper cloudAlbumLayout_ImageView,
                                ProgressBar cloudAlbumPBar) {
            LoadImage(imageThumb,cloudAlbumLayout_ImageView,cloudAlbumPBar);

        }

        private void LoadImage(String img, ViewFlipper cloudAlbumLayout_ImageView,
                               final ProgressBar cloudAlbumPBar) {

            cloudAlbumPBar.setVisibility(View.VISIBLE);

            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get().load(img).into(imageView, new Callback() {
                @Override
                public void onSuccess() {

                    cloudAlbumPBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    cloudAlbumPBar.setVisibility(View.GONE);

                }
            });
            cloudAlbumLayout_ImageView.addView(imageView);

            cloudAlbumLayout_ImageView.setInAnimation(context,R.anim.cloud_album_fade_in);
            cloudAlbumLayout_ImageView.setOutAnimation(context,R.anim.cloud_album_fade_out);

        }


        @Override
        public int getItemCount() {
            return Situation.size();
        }


        private void ShowDialog(final int position) {

            BottomSheet.Builder BottomS = new BottomSheet.Builder(CloudAlbum);
            BottomS.title("Edit Situation : " + Situation.get(position).getTitle())
                    .sheet(R.menu.situationmenu).listener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    switch (i) {
                        case R.id.renamesituation:
                            RenameSituation(SIdList.get(position));
                            Renamesituation.show();
                            break;
                        case R.id.deletesituation: {
                            if (SIdList.size() <= 1) {
                                Toast.makeText(context, "Unable to perform deletion. Album should have at least one situation.", Toast.LENGTH_LONG).show();
                            } else {

                                final android.app.AlertDialog.Builder alertbuilder = new android.app.AlertDialog.Builder(CloudAlbum);
                                alertbuilder.setTitle("Delete Situation " + Situation.get(position).getTitle())
                                        .setMessage("You are about to delete the situation. Are you sure you want to continue ?")
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialogInterface, int i) {


                                                databaseReference.child(SIdList.get(position)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Toast.makeText(context, "Successfully deleted the situation", Toast.LENGTH_LONG).show();
                                                        dialogInterface.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Toast.makeText(context, "Failed to delete the situation", Toast.LENGTH_LONG).show();
                                                        dialogInterface.dismiss();
                                                    }
                                                });


                                            }
                                        })
                                        .create()
                                        .show();

                            }

                        }

                        break;
                    }

                }
            }).show();

        }


        private void RenameSituation(final String s) {

            Renamesituation = new Dialog(CloudAlbum,android.R.style.Theme_Light_NoTitleBar);
            Renamesituation.setContentView(R.layout.create_new_situation_layout);
            Renamesituation.setCancelable(false);
            Renamesituation.setCanceledOnTouchOutside(true);
            Renamesituation.getWindow().getAttributes().windowAnimations = R.style.UpBottomSlideDialogAnimation;

            Window Renamesituationwindow = Renamesituation.getWindow();
            Renamesituationwindow.setGravity(Gravity.TOP);
            Renamesituationwindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
            Renamesituationwindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            Renamesituationwindow.setDimAmount(0.75f);
            Renamesituationwindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            final EditText SituationName = Renamesituation.findViewById(R.id.situation_name);
            SituationName.requestFocus();
            Button Done, Cancel;
            Done = Renamesituation.findViewById(R.id.done_btn);
            Cancel = Renamesituation.findViewById(R.id.cancel_btn);
            Done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!TextUtils.isEmpty(SituationName.getText().toString())) {
                        databaseReference.child(s).child("name").setValue(SituationName.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    Toast.makeText(context, "Situation renamed as : " + SituationName.getText().toString(), Toast.LENGTH_SHORT).show();
                                    SituationName.setText("");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                if (e.toString().contains("FirebaseNetworkException"))
                                    Toast.makeText(context, "Not Connected to Internet.", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(context, "Unable to rename new Situation.", Toast.LENGTH_SHORT).show();

                                SituationName.setText("");
                            }
                        });
                        Renamesituation.dismiss();
                    } else {
                        Toast.makeText(context, "No name given", Toast.LENGTH_LONG).show();
                    }

                }
            });

            Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Renamesituation.dismiss();
                }
            });

        }


        public class SituationViewHolder extends RecyclerView.ViewHolder {

            TextView Name, Time, Title;
            ImageButton EditButton;
            ViewFlipper CloudAlbumLayout_ImageView;
            ProgressBar CloudAlbumPBar;

            public SituationViewHolder(View itemView) {
                super(itemView);

                EditButton =  itemView.findViewById(R.id.EditSituationCard);
                Time = itemView.findViewById(R.id.SituationTimeCL);
                Title = itemView.findViewById(R.id.SituationNametextViewCloud_Layout);
                CloudAlbumLayout_ImageView = itemView.findViewById(R.id.cloud_album_layout_imageview);
                CloudAlbumPBar = itemView.findViewById(R.id.cloud_album_layout_progressbar);
            }
        }

    }

    public void DisplayBottomSheet(String timeStart, String timeEnd, String globalID, final String Name, Boolean lastPost , final Activity cloudalbum) {

        BlogList=new ArrayList<>();
        BlogListID=new ArrayList<>();
        PhotoListeHelperTimeStart=timeStart;
        PhotoListeHelperTimeEnd=timeEnd;
        PhotoListeHelperCommunityID=globalID;
        PhotoListeHelperLastPost=lastPost;

        try {

            databaseReferencePhotoList.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    BlogList.clear();
                    BlogListID.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                        if (snapshot.hasChildren()) {
                            try {


                                if(CheckIntervel(snapshot.child("TimeTaken").getValue().toString(), PhotoListeHelperTimeStart, PhotoListeHelperTimeEnd,PhotoListeHelperLastPost)) {

                                    String BlogListIDString = snapshot.getKey();

                                    if (snapshot.hasChild("Image")) {
                                        String photoThumb = snapshot.child("Image").getValue().toString();
                                        PhotoThumb = photoThumb;
                                    }

                                    if (snapshot.hasChild("BlogTitle")) {
                                        String blogTitle = snapshot.child("BlogTitle").getValue().toString();
                                        BlogTitle = blogTitle;
                                    }

                                    if (snapshot.hasChild("Location")) {
                                        String location = snapshot.child("Location").getValue().toString();
                                        Location = location;
                                    }

                                    if (snapshot.hasChild("TimeTaken")) {
                                        String timeTaken = snapshot.child("TimeTaken").getValue().toString();
                                        TimeTaken = timeTaken;
                                    }

                                    if (snapshot.hasChild("OriginalImageName")) {
                                        String originalImageName = snapshot.child("OriginalImageName").getValue().toString();
                                        OriginalImageName = originalImageName;
                                    }
                                    if (snapshot.hasChild("ImageThumb")) {
                                        String imageThumb = snapshot.child("ImageThumb").getValue().toString();
                                        ImageThumb = imageThumb;
                                    }


                                    if (snapshot.hasChild("WeatherDetails")) {
                                        String weatherDetails = snapshot.child("WeatherDetails").getValue().toString();
                                        WeatherDetails = weatherDetails;
                                    }


                                    if (snapshot.hasChild("UserName")) {
                                        String userName = snapshot.child("UserName").getValue().toString();
                                        UserName = userName;
                                    }


                                    if (snapshot.hasChild("User_ID")) {
                                        String user_id = snapshot.child("User_ID").getValue().toString();
                                        User_ID = user_id;
                                    }

                                    if (snapshot.hasChild("PostedByProfilePic")) {
                                        String postedByProfilePic = snapshot.child("PostedByProfilePic").getValue().toString();
                                        PostedByProfilePic = postedByProfilePic;
                                    }

                                    if (!BlogListID.contains(BlogListIDString)) {
                                        BlogListID.add(BlogListIDString);
                                        Blog model = new Blog("", PhotoThumb, ImageThumb,
                                                "", BlogTitle, Location, TimeTaken,
                                                UserName, User_ID,
                                                WeatherDetails,
                                                PostedByProfilePic,
                                                OriginalImageName);
                                        BlogList.add(model);
                                    }
                                }

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }

                        else
                        {
                            CloudBottomSheetTitle.setText("No images yet");
                        }


                    }

                    if(BlogListID.size()==0)
                    {
                        CloudBottomSheetTitle.setText("No images yet "+ new String(Character.toChars(0x1F605)) );
                    }
                    else
                    {
                        CloudBottomSheetTitle.setText(Name);
                    }

                    gridImageAdapter = new GridImageAdapter( CloudAlbum.this,BlogList,BlogListID,cloudalbum,databaseReferencePhotoList);
                    try {
                        CloudBottomSheetRecyclerView.setAdapter(gridImageAdapter);

                    }catch (WindowManager.BadTokenException e){
                        e.printStackTrace();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }





                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }


    }

    private boolean CheckIntervel(String timeTaken, String timeStart, String timeEnd, boolean LastPost) {

        Boolean Result=false;
        try{
            SimpleDateFormat objSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
            java.util.Date dt_1 = objSDF.parse(timeTaken);
            java.util.Date dt_2 = objSDF.parse(timeStart);
            java.util.Date dt_3=  objSDF.parse(timeEnd);
            if(LastPost==false) {
                if (dt_1.after(dt_2) && (dt_1.before(dt_3))) {
                    Result = true;
                }
            }
            else
            {
                if(dt_1.after(dt_2)){
                    Result=true;
                }
            }

        }
        catch (ParseException e){
            Toast.makeText(getApplicationContext(),"Parse Exception",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return Result;
    }

}


