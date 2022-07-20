package com.meepertek.meeperbots.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.meepertek.meeperbots.AutoResizeTextView;
import com.meepertek.meeperbots.BotManager;
import com.meepertek.meeperbots.Classes.ControllerClass;
import com.meepertek.meeperbots.Classes.ControllerPersonClass;
import com.meepertek.meeperbots.ConnectedBay;
import com.meepertek.meeperbots.R;
import com.meepertek.meeperbots.SQLite.data.model.Bots;
import com.meepertek.meeperbots.UIManager;
import com.meepertek.meeperbots.model.Bot;
import com.meepertek.meeperbots.model.BotV2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Skorm_000 on 5/11/2020.
 */

public class ControlRoom extends Fragment {
    View view;
    LinearLayout mainScreen;
    LinearLayout hostScreen;
    LinearLayout joinedScreen;

    RelativeLayout joinScreen;

    ImageButton closeRoomButton;
    ImageButton leaveRoomButton;

    ImageView toggleUserView;

    AutoResizeTextView hostCodeText;
    AutoResizeTextView headerText;
    TextView joinedText;

    DatabaseReference controllerDatabase;
    FirebaseDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_control_room, container, false);

        mainScreen = view.findViewById(R.id.mainScreen);
        hostScreen = view.findViewById(R.id.hostScreen);
        joinScreen = view.findViewById(R.id.joinScreen);
        joinedScreen = view.findViewById(R.id.joinedLayout);
        hostCodeText = view.findViewById(R.id.hostCodeText);
        joinedText = view.findViewById(R.id.joinedText);
        closeRoomButton = view.findViewById(R.id.closeRoomButton);
        leaveRoomButton = view.findViewById(R.id.leaveRoomButton);
        headerText = view.findViewById(R.id.controlHeader);
        toggleUserView = view.findViewById(R.id.toggleUserView);

        ImageButton hostBotCodeButton = view.findViewById(R.id.hostBotCodeButton);
        hostBotCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIManager.getFlyOutMenu().openBotCODE(view);
            }
        });
        ImageButton hostBlocklyButton = view.findViewById(R.id.hostBlocklyButton);
        hostBlocklyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIManager.getFlyOutMenu().openBlockly(view);
            }
        });
        ImageButton joinedBotCodeButton = view.findViewById(R.id.joinedBotCodeButton);
        joinedBotCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIManager.getFlyOutMenu().openBotCODE(view);
            }
        });
        ImageButton joinedBlocklyButton = view.findViewById(R.id.joinedBlocklyButton);
        joinedBlocklyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIManager.getFlyOutMenu().openBlockly(view);
            }
        });
        LinearLayout userButtonLayout = view.findViewById(R.id.userButtonLayout);
        userButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggleUserView.getVisibility() == View.VISIBLE){
                    //UIManager.getDrivingScreen().findViewById(R.id.hostPeopleHolder).setVisibility(View.VISIBLE);
                    UIManager.getDrivingScreen().showUsers();
                }
            }
        });


        if (UIManager.getHostCode() != null) {
            mainScreen.setVisibility(View.GONE);
            headerText.setText("CONTROL ROOM");
            hostScreen.setVisibility(View.VISIBLE);
            hostCodeText.setText("Congratulations! You have set up Control Room " + UIManager.getHostCode() + ".");
            toggleUserView.setVisibility(View.VISIBLE);
            closeRoomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef2 = database.getReference("ControllerV2").child(UIManager.getHostCode());
                    myRef2.removeValue();
                    UIManager.getDrivingScreen().stopListening();
                    //UIManager.setHostCode(null);
                    hostScreen.setVisibility(View.GONE);
                    headerText.setText("CONTROL CENTER");
                    mainScreen.setVisibility(View.VISIBLE);
                    toggleUserView.setVisibility(View.GONE);

                }
            });
        } else if (UIManager.getRoomCode() != null) {
            mainScreen.setVisibility((View.GONE));
            headerText.setText("CONTROL ROOM");
            joinedScreen.setVisibility(View.VISIBLE);
            joinedText.setText("Welcome to Control Room " + UIManager.getRoomCode() + "!");

            leaveRoomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIManager.getDrivingScreen().leaveRoom();
                    joinedScreen.setVisibility(View.GONE);
                    joinScreen.setVisibility(View.GONE);
                    headerText.setText("CONTROL CENTER");
                    mainScreen.setVisibility(View.VISIBLE);
                }
            });
        }

        ImageButton hostButton = view.findViewById(R.id.hostButton);
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = generateRandomString();

                ConnectivityManager connectivityManager = (ConnectivityManager) UIManager.getDrivingScreen().getSystemService(UIManager.getDrivingScreen().CONNECTIVITY_SERVICE);
                if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) ||
                        (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
                    //we are connected to a network

                    database = FirebaseDatabase.getInstance();
                    controllerDatabase = database.getReference("ControllerV2");
                    final ArrayList<ControllerClass> controlList = new ArrayList<>();

                    String id = controllerDatabase.push().getKey();

                    Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c);

                    ControllerClass botCodeDB = new ControllerClass(id, "", code, formattedDate, "");
                    controllerDatabase.child(code).child("codeInfo").setValue(botCodeDB);

                    String botList = "";

                    for(Bot bot : BotManager.getConnectedBots()){
                        botList += bot.getName() + "%meep%" + bot.getAddress() + "@meep@";
                    }

                    for(Bot bot : BotManager.getConnectedCircuits()) {
                        botList += bot.getName() + "%meep%" + bot.getAddress() + "@meep@";
                    }

                    controllerDatabase.child(code).child("botInfo").setValue(botList);

                    headerText.setText("CONTROL ROOM");
                    hostCodeText.setText("Congratulations! You have set up Control Room " + code + ".");
                    UIManager.setHostCode(code);
                    UIManager.getDrivingScreen().hostRoom();

                    mainScreen.setVisibility(View.GONE);
                    hostScreen.setVisibility(View.VISIBLE);
                    toggleUserView.setVisibility(View.VISIBLE);
                    closeRoomButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef2 = database.getReference("ControllerV2").child(UIManager.getHostCode());
                            myRef2.removeValue();
                            UIManager.getDrivingScreen().stopListening();
                            //UIManager.setHostCode(null);
                            hostScreen.setVisibility(View.GONE);
                            headerText.setText("CONTROL CENTER");
                            mainScreen.setVisibility(View.VISIBLE);
                            toggleUserView.setVisibility(View.GONE);

                        }
                    });

                } else {
                    Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), "An internet connection is required use the Control Room.", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageButton joinButton = view.findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mainScreen.setVisibility(View.GONE);

                joinScreen.setVisibility(View.VISIBLE);

                ImageButton searchButton = view.findViewById(R.id.searchButton);
                searchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchDatabase();
                    }
                });
            }
        });

        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText codeInput = view.findViewById(R.id.joinInput);
                codeInput.clearFocus();
                codeInput.setText("");
                EditText nameInput = view.findViewById(R.id.nameInput);
                nameInput.clearFocus();
                nameInput.setText("");
                joinScreen.setVisibility(View.GONE);
            }
        });

        UIManager.setControlRoomFragment(this);

        return view;
    }

    boolean foundCode = false;
    String generateRandomString() {
        String data = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
        Random random = new Random();

        final StringBuilder sb = new StringBuilder(4);

        for (int i = 0; i < 4; i++) {
            sb.append(data.charAt(random.nextInt(data.length())));
        }

        database = FirebaseDatabase.getInstance();
        controllerDatabase = database.getReference("ControllerV2");
        final ArrayList<ControllerClass> controllerList = new ArrayList<>();

        foundCode = false;

        controllerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.child("codeInfo").getChildren()) {
                    ControllerClass controllerClass = postSnapshot.getValue(ControllerClass.class);

                    controllerList.add(controllerClass);
                }

                for (ControllerClass cdb : controllerList) {
                    if (cdb.getCode().toUpperCase().equals(sb.toString().toUpperCase())) {
                        foundCode = true;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (foundCode) {
            String newString = generateRandomString();

            sb.delete(0,4);

            for (int i = 0; i < 4; i++) {
                sb.append(newString.charAt(i));
            }
        }

        return sb.toString();

    }

    void searchDatabase(){
        final EditText codeInput = view.findViewById(R.id.joinInput);
        codeInput.clearFocus();
        final EditText nameInput = view.findViewById(R.id.nameInput);
        nameInput.clearFocus();

        database = FirebaseDatabase.getInstance();
        controllerDatabase = database.getReference("ControllerV2");
        final ArrayList<ControllerClass> controllerList = new ArrayList<>();
        final ArrayList<String> tempBotList = new ArrayList<>();

        ConnectivityManager connectivityManager = (ConnectivityManager) UIManager.getDrivingScreen().getSystemService(Context.CONNECTIVITY_SERVICE);
        if((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null &&  connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) ||
                (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            //we are connected to a network

            if(nameInput.getText().length() <= 0){
                Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), ("Please enter a name that the host will be able to easily recognize you with."), Toast.LENGTH_LONG).show();
            }else {
                controllerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            ControllerClass botCodeDB = postSnapshot.child("codeInfo").getValue(ControllerClass.class);
                            String botListString = postSnapshot.child("botInfo").getValue(String.class);
                            if (botCodeDB != null) {
                                controllerList.add(botCodeDB);
                                tempBotList.add(botListString);
                            }
                        }

                        boolean foundCode = false;

                        int index = 0;

                        for (ControllerClass cdb : controllerList) {
                            if (cdb.getCode().toUpperCase().equals(codeInput.getText().toString().toUpperCase())) {

                                foundCode = true;
                                UIManager.setRoomCode(codeInput.getText().toString().toUpperCase());
                                codeInput.setText("");

                                String id = controllerDatabase.push().getKey();
                                ControllerPersonClass personClass = new ControllerPersonClass(id, nameInput.getText().toString(), "false");
                                controllerDatabase.child(UIManager.getRoomCode()).child("people").child(id).setValue(personClass);
                                UIManager.setUserId(id);

                                //set up joined confirmation layout
                                joinScreen.setVisibility(View.GONE);
                                mainScreen.setVisibility(View.GONE);
                                headerText.setText("CONTROL ROOM");
                                joinedText.setText("Welcome to Control Room " + UIManager.getRoomCode() + "!");
                                joinedScreen.setVisibility(View.VISIBLE);
                                UIManager.getDrivingScreen().createJoinListener(id);
                                UIManager.getDrivingScreen().findViewById(R.id.speedLayout).setVisibility(View.VISIBLE);

                                ImageButton leaveRoomButton = view.findViewById(R.id.leaveRoomButton);
                                leaveRoomButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        UIManager.getDrivingScreen().leaveRoom();
                                        joinedScreen.setVisibility(View.GONE);
                                        headerText.setText("CONTROL CENTER");
                                        mainScreen.setVisibility(View.VISIBLE);
                                    }
                                });

                                break;
                            }
                            index++;
                        }

                        if (!foundCode) {
                            Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), ("Control Room cannot be found with code " + codeInput.getText().toString() + ". Please check with your friend or teacher to ensure it is set up."), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
        else {
            Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), "An internet connection is required to use the Control Room.", Toast.LENGTH_LONG).show();
        }
    }

    public void closeRoomFromAnywhere(){
        joinedScreen.setVisibility(View.GONE);
        headerText.setText("CONTROL CENTER");
        mainScreen.setVisibility(View.VISIBLE);
    }
}
