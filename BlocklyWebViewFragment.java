package com.meepertek.meeperbots.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
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
import com.meepertek.meeperbots.BotCode.BotCommand;
import com.meepertek.meeperbots.BotManager;
import com.meepertek.meeperbots.Classes.BlocklyDatabase;
import com.meepertek.meeperbots.Classes.ChildInfo;
import com.meepertek.meeperbots.Classes.ControllerClass;
import com.meepertek.meeperbots.Classes.GroupInfo;
import com.meepertek.meeperbots.Classes.MPCClass;
import com.meepertek.meeperbots.CustomAdapter;
import com.meepertek.meeperbots.Global;
import com.meepertek.meeperbots.R;
import com.meepertek.meeperbots.SQLite.data.model.Blockly;
import com.meepertek.meeperbots.SQLite.data.model.Bots;
import com.meepertek.meeperbots.SQLite.data.repo.BlocklyRepo;
import com.meepertek.meeperbots.SQLite.data.repo.BotsRepo;
import com.meepertek.meeperbots.UIManager;
import com.meepertek.meeperbots.model.Bot;
import com.meepertek.meeperbots.util.JavascriptUtil;
import com.meepertek.meeperbots.util.WebChromeClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Orrin Oliver on 8/8/2019.
 */

public class BlocklyWebViewFragment extends Fragment {
    protected @Nullable WebView mWebView = null;
    View view;

    private TextView mGeneratedCode;
    private ImageButton mFullscreenButton;
    private ImageButton playButton;
    RelativeLayout programSave;
    LinearLayout blocklyNameListView;
    RelativeLayout blocklyNameHolder;
    LinearLayout blocklyNameItemToChange;
    EditText renameInput;
    RelativeLayout renameLayout;

    TextView noSavedCodeText;

    private static boolean programIsExecuting = false;

    public ArrayList<String> codeList;

    DatabaseReference blocklyDatabase;
    FirebaseDatabase database;

    private LinkedHashMap<String, GroupInfo> subjectsAnalog = new LinkedHashMap<String, GroupInfo>();
    private LinkedHashMap<String, GroupInfo> subjectsDigital = new LinkedHashMap<String, GroupInfo>();
    private ArrayList<GroupInfo> deptListAnalog = new ArrayList<GroupInfo>();
    private ArrayList<GroupInfo> deptListDigital = new ArrayList<GroupInfo>();

    private CustomAdapter listAdapterDigital;
    private CustomAdapter listAdapterAnalog;
    private ExpandableListView simpleExpandableListViewDigital;
    private ExpandableListView simpleExpandableListViewAnalog;
    FrameLayout mpcComponentList;

    String d1String = "A000T0";
    String a1String = "B000T0";
    String d2String = "C000T0";
    String a2String = "D000T0";
    String d3String = "E000T0";
    String a3String = "F000T0";
    String d4String = "G000T0";
    String a4String = "H000T0";
    String d1Type = "";
    String a1Type = "";
    String d2Type = "";
    String a2Type = "";
    String d3Type = "";
    String a3Type = "";
    String d4Type = "";
    String a4Type = "";

    AutoResizeTextView d1ConfigText;
    AutoResizeTextView a1ConfigText;
    AutoResizeTextView d2ConfigText;
    AutoResizeTextView a2ConfigText;
    AutoResizeTextView d3ConfigText;
    AutoResizeTextView a3ConfigText;
    AutoResizeTextView d4ConfigText;
    AutoResizeTextView a4ConfigText;

    LinearLayout d1ConfigStudHolder;
    LinearLayout a1ConfigStudHolder;
    LinearLayout d2ConfigStudHolder;
    LinearLayout a2ConfigStudHolder;
    LinearLayout d3ConfigStudHolder;
    LinearLayout a3ConfigStudHolder;
    LinearLayout d4ConfigStudHolder;
    LinearLayout a4ConfigStudHolder;

    ImageView d1ConfigStudImage;
    ImageView a1ConfigStudImage;
    ImageView d2ConfigStudImage;
    ImageView a2ConfigStudImage;
    ImageView d3ConfigStudImage;
    ImageView a3ConfigStudImage;
    ImageView d4ConfigStudImage;
    ImageView a4ConfigStudImage;

    boolean configureIsOpen = false;
    boolean configurable = true;
    boolean loadingIsOpen = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_blockly, container, false);
        mWebView = view.findViewById(R.id.blocklyWebView);
        mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
        mWebView.loadUrl("file:///android_asset/blockly/webview.html");

        UIManager.setBlocklyFragment(this);

        final LinearLayout loadListLayout = view.findViewById(R.id.loadListHolder);

        ImageButton hamburgerMenu = view.findViewById(R.id.blocklyHamburger);
        hamburgerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadingIsOpen){
                    loadListLayout.setVisibility(View.GONE);
                    loadingIsOpen = false;
                }
                UIManager.getDrivingScreen().openFlyOutMenu(view);
            }
        });

        programSave = view.findViewById(R.id.ProgramSaverLayout);
        blocklyNameListView = view.findViewById(R.id.blocklyNameListView);
        blocklyNameHolder = view.findViewById(R.id.blocklyNameHolder);

        final EditText programName = programSave.findViewById(R.id.saveNameInput);

        ImageButton openSaveButton = view.findViewById(R.id.saveButton);
        openSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loadingIsOpen){
                    loadListLayout.setVisibility(View.GONE);
                    loadingIsOpen = false;
                }
                programSave.setVisibility(View.VISIBLE);
            }
        });


        RelativeLayout loadListBackground = view.findViewById(R.id.loadListBackground);
        loadListBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadListLayout.setVisibility(View.GONE);
                loadingIsOpen = false;
            }
        });

        ImageButton openLoadButton = view.findViewById(R.id.loadButton);
        openLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!loadingIsOpen) {
                    loadListLayout.setVisibility(View.VISIBLE);
                    loadingIsOpen = true;
                }else{
                    loadListLayout.setVisibility(View.GONE);
                    loadingIsOpen = false;
                }
            }
        });

        Button closeSaveButton = programSave.findViewById(R.id.closeSaveButton);
        closeSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                programSave.setVisibility(View.GONE);
                programName.setText("Name");
                programName.clearFocus();
                try  {
                    InputMethodManager imm = (InputMethodManager) UIManager.getDrivingScreen().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(UIManager.getDrivingScreen().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
            }
        });

        ImageButton saveRoutineButton = programSave.findViewById(R.id.saveNameButton);
        saveRoutineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.loadUrl("javascript:saveCode()");
            }
        });

        noSavedCodeText = view.findViewById(R.id.noSavedCodeText);

        BlocklyRepo blocklyRepo = new BlocklyRepo();
        List<Blockly> blocklyList = blocklyRepo.getBlocklyList();

        for(Blockly blockly : blocklyList){
            createLoadList(blockly);
        }

        if(blocklyNameListView.getChildCount() == 0){
            noSavedCodeText.setVisibility(View.VISIBLE);
        }

        ImageButton loadBackground = view.findViewById(R.id.loadBackground);
        loadBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blocklyNameHolder.setVisibility(View.GONE);
            }
        });

        mGeneratedCode = (TextView) view.findViewById(R.id.generatedCode);
        mFullscreenButton = (ImageButton) view.findViewById(R.id.fullscreenButton);

        mFullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout blocklyLinLay = view.findViewById(R.id.blocklyLayout);
                if(blocklyLinLay.getVisibility() == View.VISIBLE) {
                    blocklyLinLay.setVisibility(View.GONE);
                    mFullscreenButton.setImageResource(R.drawable.ic_fullscreen_close);
                }
                else {
                    blocklyLinLay.setVisibility(View.VISIBLE);
                    mFullscreenButton.setImageResource(R.drawable.ic_fullscreen);
                }
            }
        });

        playButton = view.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loadingIsOpen){
                    loadListLayout.setVisibility(View.GONE);
                    loadingIsOpen = false;
                }
                mWebView.loadUrl("javascript:showCode()");
            }
        });

        final RelativeLayout cloudDownloadHolder = view.findViewById(R.id.cloudDownloadHolder);

        Button localDownloadButton = view.findViewById(R.id.localDownloadButton);
        localDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadListLayout.setVisibility(View.GONE);
                blocklyNameHolder.setVisibility(View.VISIBLE);
                loadingIsOpen = false;
            }
        });
        Button cloudCodeDownloadButton = view.findViewById(R.id.cloudCodeDownloadButton);
        cloudCodeDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadListLayout.setVisibility(View.GONE);
                cloudDownloadHolder.setVisibility(View.VISIBLE);
                loadingIsOpen = false;
            }
        });

        ImageButton reconfigButton = view.findViewById(R.id.reconfigButton);
        reconfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loadingIsOpen){
                    loadListLayout.setVisibility(View.GONE);
                    loadingIsOpen = false;
                }
                openConfigure();
            }
        });

        ImageButton cloudLoadBackground = view.findViewById(R.id.cloudLoadBackground);
        cloudLoadBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloudDownloadHolder.setVisibility(View.GONE);
            }
        });

        ImageButton searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDatabase();
            }
        });

        ImageButton cloudSave = view.findViewById(R.id.cloudUpload);
        cloudSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.loadUrl("javascript:cloudSaveCode()");
            }
        });

        final RelativeLayout cloudSaveHolder = view.findViewById(R.id.cloudSaveHolder);

        ImageButton closeCloudSaveButton = view.findViewById(R.id.outputConfirmButton);
        closeCloudSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                programSave.setVisibility(View.GONE);
                cloudSaveHolder.setVisibility(View.GONE);
            }
        });

        renameInput = view.findViewById(R.id.renameInput);
        renameLayout = view.findViewById(R.id.renameLayout);

        ImageButton closeRenameButton = view.findViewById(R.id.closeRename);
        closeRenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameLayout.setVisibility(View.GONE);
                renameInput.clearFocus();

                try  {
                    InputMethodManager imm = (InputMethodManager)UIManager.getDrivingScreen().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(UIManager.getDrivingScreen().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
            }
        });

        ImageButton renameLayoutButton = view.findViewById(R.id.renameLayoutButton);
        renameLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameRoutine();
            }
        });

        if(UIManager.getHostCode() != null){
            final ImageView toggleUserView = view.findViewById(R.id.toggleUserView);
            toggleUserView.setVisibility(View.VISIBLE);

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
        }

        d1ConfigText = view.findViewById(R.id.d1ConfigText);
        a1ConfigText = view.findViewById(R.id.a1ConfigText);
        d2ConfigText = view.findViewById(R.id.d2ConfigText);
        a2ConfigText = view.findViewById(R.id.a2ConfigText);
        d3ConfigText = view.findViewById(R.id.d3ConfigText);
        a3ConfigText = view.findViewById(R.id.a3ConfigText);
        d4ConfigText = view.findViewById(R.id.d4ConfigText);
        a4ConfigText = view.findViewById(R.id.a4ConfigText);
        d1ConfigStudHolder = view.findViewById(R.id.d1ConfigStudHolder);
        d1ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("D1");
            }
        });
        a1ConfigStudHolder = view.findViewById(R.id.a1ConfigStudHolder);
        a1ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("A1");
            }
        });
        d2ConfigStudHolder = view.findViewById(R.id.d2ConfigStudHolder);
        d2ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("D2");
            }
        });
        a2ConfigStudHolder = view.findViewById(R.id.a2ConfigStudHolder);
        a2ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("A2");
            }
        });
        d3ConfigStudHolder = view.findViewById(R.id.d3ConfigStudHolder);
        d3ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("D3");
            }
        });
        a3ConfigStudHolder = view.findViewById(R.id.a3ConfigStudHolder);
        a3ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("A3");
            }
        });
        d4ConfigStudHolder = view.findViewById(R.id.d4ConfigStudHolder);
        d4ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("D4");
            }
        });
        a4ConfigStudHolder = view.findViewById(R.id.a4ConfigStudHolder);
        a4ConfigStudHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList("A4");
            }
        });

        d1ConfigStudImage = view.findViewById(R.id.d1ConfigStudImage);
        a1ConfigStudImage = view.findViewById(R.id.a1ConfigStudImage);
        d2ConfigStudImage = view.findViewById(R.id.d2ConfigStudImage);
        a2ConfigStudImage = view.findViewById(R.id.a2ConfigStudImage);
        d3ConfigStudImage = view.findViewById(R.id.d3ConfigStudImage);
        a3ConfigStudImage = view.findViewById(R.id.a3ConfigStudImage);
        d4ConfigStudImage = view.findViewById(R.id.d4ConfigStudImage);
        a4ConfigStudImage = view.findViewById(R.id.a4ConfigStudImage);

        mpcComponentList = view.findViewById(R.id.mpcComponentList);

        loadData(true);
        loadData(false);

        //get reference of the ExpandableListView
        simpleExpandableListViewDigital = (ExpandableListView) view.findViewById(R.id.simpleExpandableListViewDigital);
        simpleExpandableListViewAnalog = (ExpandableListView) view.findViewById(R.id.simpleExpandableListViewAnalog);
        // create the adapter by passing your ArrayList data
        listAdapterDigital = new CustomAdapter(UIManager.getDrivingScreen(), deptListDigital);
        listAdapterAnalog = new CustomAdapter(UIManager.getDrivingScreen(), deptListAnalog);
        // attach the adapter to the expandable list view
        simpleExpandableListViewDigital.setAdapter(listAdapterDigital);
        simpleExpandableListViewAnalog.setAdapter(listAdapterAnalog);

        //expand all the Groups
        //expandAll();

        // setOnChildClickListener listener for child row click
        simpleExpandableListViewDigital.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptListDigital.get(groupPosition);
                //get the child info
                ChildInfo detailInfo =  headerInfo.getProductList().get(childPosition);
                //display it or do something with it
                setComponent(detailInfo.getName());
                return false;
            }
        });
        simpleExpandableListViewAnalog.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptListAnalog.get(groupPosition);
                //get the child info
                ChildInfo detailInfo =  headerInfo.getProductList().get(childPosition);
                //display it or do something with it
                setComponent(detailInfo.getName());
                return false;
            }
        });
        // setOnGroupClickListener listener for group heading click
        simpleExpandableListViewDigital.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptListDigital.get(groupPosition);

                if(headerInfo.getName().equals("Nothing")){
                    setComponent("Not Assigned");
                }else if(headerInfo.getName().equals("Close")){
                    hideList();
                }

                return false;
            }
        });
        simpleExpandableListViewAnalog.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptListAnalog.get(groupPosition);

                if(headerInfo.getName().equals("Nothing")){
                    setComponent("Not Assigned");
                }else if(headerInfo.getName().equals("Close")){
                    hideList();
                }

                return false;
            }
        });

        assignMPC();

        return view;
    }

    //create a new item to put into the listview for loading blockly code
    void createLoadList(final Blockly blockly){
        if(!blockly.getBlocklyName().equals("meeper_autoload_xml")) {
            noSavedCodeText.setVisibility(View.GONE);

            final LinearLayout blocklyNameItem = (LinearLayout) (getLayoutInflater().inflate(R.layout.blockly_name_item, null));
            AutoResizeTextView blocklyNameField = (AutoResizeTextView) blocklyNameItem.findViewById(R.id.blocklyName);
            blocklyNameField.setText(blockly.getBlocklyName());

            ImageButton deleteCodeButton = (ImageButton) blocklyNameItem.findViewById(R.id.deleteButton);
            deleteCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final RelativeLayout areYouSureHolder = view.findViewById(R.id.areYouSureHolder);
                    areYouSureHolder.setVisibility(View.VISIBLE);

                    ImageButton deleteNoButton = areYouSureHolder.findViewById(R.id.noDeleteButton);
                    deleteNoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            areYouSureHolder.setVisibility(View.GONE);
                        }
                    });

                    ImageButton deleteYesButton = areYouSureHolder.findViewById(R.id.yesDeleteButton);
                    deleteYesButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //delete from database and list
                            BlocklyRepo blocklyNameRepo = new BlocklyRepo();
                            List<Blockly> blocklyList = blocklyNameRepo.getBlocklyList();

                            for (Blockly blockly : blocklyList) {
                                if (blockly.getBlocklyName().equals(blockly.getBlocklyName())) {
                                    blocklyNameRepo.delete(blockly.getBlocklyId());
                                    break;
                                }
                            }

                            blocklyNameListView.removeView(blocklyNameItem);
                            if(blocklyNameListView.getChildCount() == 0){
                                noSavedCodeText.setVisibility(View.VISIBLE);
                            }

                            areYouSureHolder.setVisibility(View.GONE);

                        }
                    });
                }
            });

            //on click load block and turn invisible
            blocklyNameItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String str = blockly.getBlocklyXML();
                    mWebView.loadUrl("javascript:loadCode('" + str + "')");

                    blocklyNameHolder.setVisibility(View.GONE);
                }
            });

            ImageButton renameCodeButton = blocklyNameItem.findViewById(R.id.renameButton);
            renameCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    renameInput.setText(blockly.getBlocklyName());
                    renameLayout.setVisibility(View.VISIBLE);
                    blocklyNameItemToChange = blocklyNameItem;
                }
            });

            blocklyNameListView.addView(blocklyNameItem);
        }
    }

    void autoLoad(){

        loadBots();

        BlocklyRepo blocklyNameRepo = new BlocklyRepo();
        List<Blockly> blocklyList = blocklyNameRepo.getBlocklyList();

        for(Blockly blockly : blocklyList){
            if(blockly.getBlocklyName().equals("meeper_autoload_xml")){
                String str = blockly.getBlocklyXML();
                mWebView.loadUrl("javascript:loadCode('" + str + "')");
                break;
            }
        }
    }

    public class WebAppInterface {
        Context mContext;
        String data;

        WebAppInterface(Context ctx){
            this.mContext=ctx;
        }


        @JavascriptInterface
        public void sendData(String data2) {
            //Get the string value to process
            data=data2;

            UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setText(data);
                }
            });
        }

        @JavascriptInterface
        public void sendSaveData(String savedText) {
            data = savedText;

            UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    saveData(data, false, null);
                }
            });
        }

        @JavascriptInterface
        public void sendAutosaveData(String savedText){
            data = savedText;
            UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    autoSaveData(data);
                }
            });
        }

        @JavascriptInterface
        public void sendCloudSaveData(String savedText){
            data = savedText;
            UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    saveData(data, true, null);
                }
            });
        }

        @JavascriptInterface
        public void sendRunUserCode(String savedText){
            data = savedText;
            UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendUserData(data);
                }
            });
        }

        @JavascriptInterface
        public void sendLoadSaveData(String savedText, final String code) {
            data = savedText;

            UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    saveData(data, false, code);
                }
            });
        }

        @JavascriptInterface
        public void getAutosaveData(){
            UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    autoLoad();
                }
            });
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void sendPopUp(String didWork) {
            showPopUp(didWork);
        }
    }

    void setText(String code){

        String newCode = code.replaceAll("#\\?.*?!", "");
        codeList = new ArrayList<>();

        StringTokenizer tokens2 = new StringTokenizer(newCode, ";");
        while (tokens2.hasMoreTokens()) {
            codeList.add(tokens2.nextToken().toString() + ";");
        }

        for(int i = 0; i < codeList.size(); i++){
            Log.d("MYTAG", "displayList(" + i + "): " + codeList.get(i));
        }

        //mGeneratedCode.setText(newCode);

        String encoded = JavascriptUtil.makeJsString(code);

        ArrayList<String> commandList = new ArrayList<>();
        StringTokenizer tokens = new StringTokenizer(encoded, "~");
        while (tokens.hasMoreTokens()) {
            commandList.add(tokens.nextToken());
        }

        Log.d("MYTAG", "Encoded: " + encoded);

        mWebView.loadUrl("javascript:autosaveCode()");
        processCode(commandList);
    }

    void autoSaveData(String savedData){
        BlocklyRepo blocklyRepo = new BlocklyRepo();
        Blockly blockly = new Blockly();

        List<Blockly> blocklyNameList = blocklyRepo.getBlocklyList();

        boolean sameName = false;

        for(Blockly name : blocklyNameList){
            if(name.getBlocklyName().equals("meeper_autoload_xml")){
                sameName = true;
                blockly = name;
            }
        }

        if(!sameName){
            blockly.setBlocklyName("meeper_autoload_xml");
            blockly.setBlocklyXML(savedData);
            blocklyRepo.insert(blockly);

            createLoadList(blockly);
        }else{
            blockly.setBlocklyName("meeper_autoload_xml");
            blockly.setBlocklyXML(savedData);
            blocklyRepo.update(blockly);
        }
    }

    void saveData(String savedData, boolean isCloudSave, String loadCode){
        EditText programName = programSave.findViewById(R.id.saveNameInput);

        String code = "";

        if(loadCode != null){
            programName.setText(loadCode);
        }else if(isCloudSave){
            code = generateRandomString();
            programName.setText(code);
        }

        BlocklyRepo blocklyRepo = new BlocklyRepo();
        Blockly blockly = new Blockly();

        List<Blockly> blocklyNameList = blocklyRepo.getBlocklyList();

        boolean sameName = false;

        for(Blockly name : blocklyNameList){
            if(name.getBlocklyName().equals(programName.getText().toString())){
                sameName = true;
                blockly = name;
            }
        }

        if(!sameName){
            blockly.setBlocklyName(programName.getText().toString());
            blockly.setBlocklyXML(savedData);
            blocklyRepo.insert(blockly);

            createLoadList(blockly);
        }else{
            blockly.setBlocklyName(programName.getText().toString());
            blockly.setBlocklyXML(savedData);
            blocklyRepo.update(blockly);

            List<Blockly> blocklyList = blocklyRepo.getBlocklyList();

            blocklyNameListView.removeAllViews();
            for(Blockly newBlockly : blocklyList){
                createLoadList(newBlockly);
            }
        }

        programName.setText("Name");
        programName.clearFocus();

        try  {
            InputMethodManager imm = (InputMethodManager)UIManager.getDrivingScreen().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(UIManager.getDrivingScreen().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {

        }

        if(isCloudSave) {
            ConnectivityManager connectivityManager = (ConnectivityManager) UIManager.getDrivingScreen().getSystemService(Context.CONNECTIVITY_SERVICE);
            if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) ||
                    (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
                //we are connected to a network

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Blockly");

                String id = myRef.push().getKey();

                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c);

                BlocklyDatabase blocklyDB = new BlocklyDatabase(id, savedData, code, formattedDate);
                myRef.child(code).setValue(blocklyDB);

                TextView codeText = view.findViewById(R.id.codeOutput);
                codeText.setText(code);

                RelativeLayout cloudSaveHolder = view.findViewById(R.id.cloudSaveHolder);
                cloudSaveHolder.setVisibility(View.VISIBLE);

                Log.d("MYTAG", "Blockly Added");
            } else {
                Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), "An internet connection is required to save from the cloud, your code was still saved locally.", Toast.LENGTH_LONG).show();
            }
        }else{
            programSave.setVisibility(View.GONE);
        }
    }

    ArrayList<BotCommand> commandsToBeExecuted = new ArrayList();
    ArrayList<BotCommand> alwaysRepeatCommands = new ArrayList();

    void processCode(ArrayList commandList){
        if(repeatForever || programIsExecuting){
            playButton.setImageResource(R.drawable.play_arrow);
            stopExecutionTimerBlockly();

        }else{
            commandsToBeExecuted.clear();
            alwaysRepeatCommands.clear();
            repeatForever = false;
            //stopExecutionTimerBlockly();

            for(int i =0; i < commandList.size(); i++) {
                createCommand(commandList.get(i).toString().trim(), i, commandList, true);
            }

            if(repeatForever){
                for(int i = 0; i < repeatList.size(); i++){
                    createCommand(repeatList.get(i).toString().trim(), i, repeatList, false);
                }
            }

            if(UIManager.getRoomCode() != null){
                if(UIManager.getIsActive()) {
                    blocklyRunUser();
                }
                programIsExecuting = true;
                playButton.setImageResource(R.drawable.ic_stop_image);
                runCommandList();
            }else {
                programIsExecuting = true;
                playButton.setImageResource(R.drawable.ic_stop_image);
                runCommandList();
            }
        }


    }

    private static boolean repeatForever = false;
    ArrayList repeatList = new ArrayList();

    void createCommand(String command, int index, ArrayList commandList, boolean nonRepeating){
        String botCommand = "";
        int duration = 0;
        int iterations = 0;
        long ordinal = 0;
        String speed = "Fast";
        ArrayList<Bot> botList = new ArrayList<>();
        boolean keepClear = false;
        int foreverLoopCount = 0;

        Log.d("MYTAG", "created command: " + command);

        if(command.contains("{")){
            if (command.contains("for")) {
                keepClear = false;
                String[] seperated = command.split("< ");
                String[] seperated2 = seperated[1].split(";");

                int loopCount = 0;
                ArrayList cloneList = new ArrayList();
                for(int i = index+1; i < commandList.size(); i++){
                    if(commandList.get(i).toString().trim().contains("}")){
                        //if there are no nested loops
                        if(loopCount == 0){
                            //clone commands x amount of times
                            if(!keepClear) {
                                for (int j = 0; j < Math.round(Float.parseFloat(seperated2[0]) - 1); j++) {
                                    for (int k = cloneList.size(); k > 0; k--) {
                                        commandList.add(i, cloneList.get(k - 1));
                                    }
                                }
                            }
                            break;
                        }else{
                            // checking for } (end of loop)
                            if(keepClear){
                                if(loopCount > foreverLoopCount){
                                    cloneList.add(commandList.get(i).toString().trim());
                                }
                            }else{
                                cloneList.add(commandList.get(i).toString().trim());
                            }
                            loopCount--;
                        }
                    }else if (commandList.get(i).toString().trim().contains("Forever")){
                        cloneList.clear();
                        loopCount++;
                        foreverLoopCount = loopCount;
                        keepClear = true;
                    }else if (commandList.get(i).toString().trim().contains("while") ||
                            commandList.get(i).toString().trim().contains("else if") || commandList.get(i).toString().trim().contains("else") ||
                            commandList.get(i).toString().trim().contains("if")){

                        loopCount++;
                        cloneList.add(commandList.get(i).toString().trim());
                    }else if(commandList.get(i).toString().trim().contains("for")) {
                        loopCount++;
                        cloneList.add(commandList.get(i).toString().trim());
                    }else{
                        cloneList.add(commandList.get(i).toString().trim());
                    }
                }

            }else if (command.contains("Forever")){

                int loopCount = 0;
                ArrayList cloneList = new ArrayList();
                for(int i = index+1; i < commandList.size(); i++) {
                    if (commandList.get(i).toString().trim().contains("}")) {
                        //if there are no nested loops
                        if (loopCount == 0) {
                            //create a list that will always repeat
                            repeatList.clear();
                            for (int k = 0; k < cloneList.size(); k++) {
                                repeatList.add(cloneList.get(k));
                            }

                            int removeAfter = index + cloneList.size();
                            for(int k = commandList.size() - 1; k > removeAfter; k--){
                                commandList.remove(k);
                            }

                            break;
                        } else {
                            // checking for } (end of loop)
                            loopCount--;
                            cloneList.add(commandList.get(i).toString().trim());
                        }
                    }else if(commandList.get(i).toString().trim().contains("Forever")){
                        cloneList.clear();
                    } else if (commandList.get(i).toString().trim().contains("while") ||
                            commandList.get(i).toString().trim().contains("else if") || commandList.get(i).toString().trim().contains("else") ||
                            commandList.get(i).toString().trim().contains("if") || commandList.get(i).toString().trim().contains("for")) {
                        loopCount++;
                        cloneList.add(commandList.get(i).toString().trim());
                    }else{
                        cloneList.add(commandList.get(i).toString().trim());
                    }
                }

                repeatForever = true;
            }else if(command.contains("while")){
                Log.d("MYTAG", "while loop");
            }else if(command.contains("else if")){
                Log.d("MYTAG", "else if");
                String[] seperated = command.split("\\(");

                if (seperated[1].contains("&&")) {
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }else if(seperated[1].contains("||")){
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }else if(seperated[1].contains("!=")){
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }else if(seperated[1].contains("==")){
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }
            }else if(command.contains("else")){
                Log.d("MYTAG", "else");
                String[] seperated = command.split("\\(");
                for(int i = 0; i < seperated.length; i++){
                    Log.d("MYTAG", seperated[i]);
                }
            }else if(command.contains("if")){
                Log.d("MYTAG", "if");
                String[] seperated = command.split("\\(");

                if (seperated[1].contains("&&")) {
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }else if(seperated[1].contains("||")){
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }else if(seperated[1].contains("!=")){
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }else if(seperated[1].contains("==")){
                    Log.d("MYTAG", seperated[1] + "!@!@");
                }

            }
        }else if(command.contains("}")){

        } else if (command.contains("start")) {

        } else if(command.contains("(")){
            boolean isMPC = false;
            if (!command.contains("wait") && !command.toUpperCase().contains("TOGGLE")) {
                String[] seperated = command.split("\\(");
                String[] seperated2 = seperated[1].split(",");

                for (int i = 2; i < seperated.length; i++) {
                    String botAddress;
                    if(seperated[i].contains("#?")){
                        botAddress = seperated[i].substring(seperated[i].indexOf("#?") + 2, seperated[i].indexOf("!"));
                    }else{
                        //all bot/circuit/motor
                        botAddress = seperated[i];
                    }

                    if(botAddress.contains(":")){
                        for (Bot bot : BotManager.getConnectedBots()) {
                            if (bot.getAddress().equals(botAddress) && !botList.contains(bot)) {
                                botList.add(bot);
                            }
                        }
                        for (Bot bot : BotManager.getSharedBots()) {
                            if (bot.getSharedAddress().equals(botAddress) && !botList.contains(bot)) {
                                botList.add(bot);
                            }
                        }
                        for (Bot bot : BotManager.getConnectedCircuits()) {
                            if (bot.getAddress().equals(botAddress) && !botList.contains(bot)) {
                                botList.add(bot);
                            }
                        }
                        for (Bot bot : BotManager.getSharedCircuits()) {
                            if (bot.getSharedAddress().equals(botAddress) && !botList.contains(bot)) {
                                botList.add(bot);
                            }
                        }
                        for (Bot bot : BotManager.getConnectedMotors()) {
                            if (bot.getAddress().equals(botAddress) && !botList.contains(bot)) {
                                botList.add(bot);
                            }
                        }
                        for(Bot bot : BotManager.getConnectedMPCs()){
                            if (bot.getAddress().equals(botAddress) && !botList.contains(bot)) {
                                isMPC = true;
                                botList.add(bot);
                            }
                        }
                        for(Bot bot : BotManager.getSharedMPCs()){
                            if (bot.getSharedAddress().equals(botAddress) && !botList.contains(bot)) {
                                isMPC = true;
                                botList.add(bot);
                            }
                        }
                    }else{
                        if(botAddress.toUpperCase().contains("BOT")) {
                            botList.clear();
                            botList.addAll(BotManager.getConnectedBots());
                            botList.addAll(BotManager.getSharedBots());
                            //botList = BotManager.getConnectedBots();
                        }else if(botAddress.toUpperCase().contains("CIRCUIT")){
                            botList.clear();
                            botList.addAll(BotManager.getConnectedCircuits());
                            botList.addAll(BotManager.getSharedCircuits());
                            //botList = BotManager.getConnectedCircuits();
                        }else if(botAddress.toUpperCase().contains("MOTOR")){
                            botList = BotManager.getConnectedMotors();
                        }else if(botAddress.toUpperCase().contains("BOARDS")){
                            isMPC = true;
                            botList.clear();
                            botList.addAll(BotManager.getConnectedMPCs());
                            botList.addAll(BotManager.getSharedMPCs());
                        }
                    }
                }
                if(seperated2.length > 1 && !isMPC) {
                    botCommand = seperated[0];
                    duration = Math.round(Float.parseFloat(seperated2[0])) * 100;
                    speed = seperated2[1].trim();
                }else if(isMPC){
                    botCommand = seperated[0];

                    if(!seperated2[0].equals("%blarg%")){
                        if(seperated2[0].equals(null) || seperated2[0].equals("")){
                            duration = 2000;
                        }else {
                            duration = Math.round(Float.parseFloat(seperated2[0])) * 100;
                        }
                    }else{
                        duration = 2000;
                    }
                }
            } else if (command.toUpperCase().contains("TOGGLE")) {
                String[] seperated = command.split("\\(");
                String[] seperated2 = seperated[1].split(",");

                for (int i = 2; i < seperated.length; i++) {
                    String botAddress;
                    if(seperated[i].contains("#?")){
                        botAddress = seperated[i].substring(seperated[i].indexOf("#?") + 2, seperated[i].indexOf("!"));
                    }else{
                        //all bot/circuit/motor
                        botAddress = seperated[i];
                    }

                    if(botAddress.contains(":")){
                        for (Bot bot : BotManager.getConnectedCircuits()) {
                            if (bot.getAddress().equals(botAddress) && !botList.contains(bot)) {
                                botList.add(bot);
                            }
                        }
                        for (Bot bot : BotManager.getSharedCircuits()) {
                            if (bot.getSharedAddress().equals(botAddress) && !botList.contains(bot)) {
                                botList.add(bot);
                            }
                        }
                        for(Bot bot : BotManager.getConnectedMPCs()){
                            if (bot.getAddress().equals(botAddress) && !botList.contains(bot)) {
                                isMPC = true;
                                botList.add(bot);
                            }
                        }
                        for(Bot bot : BotManager.getSharedMPCs()){
                            if (bot.getSharedAddress().equals(botAddress) && !botList.contains(bot)) {
                                isMPC = true;
                                botList.add(bot);
                            }
                        }
                    }else{
                        if(botAddress.toUpperCase().contains("CIRCUIT")) {
                            botList.clear();
                            botList.addAll(BotManager.getConnectedCircuits());
                            botList.addAll(BotManager.getSharedCircuits());
                        }else if(botAddress.toUpperCase().contains("BOARDS")){
                            isMPC = true;
                            botList.clear();
                            botList.addAll(BotManager.getConnectedMPCs());
                            botList.addAll(BotManager.getSharedMPCs());
                        }
                    }
                }
                botCommand = seperated[0];
                if(seperated2[0].equals("true")){
                    duration = 9898;
                }else{
                    duration = 0;
                }
                //duration = Integer.parseInt(seperated2[0]) * 100;
                speed = seperated2[1].trim();

            } else {
                String[] seperated = command.split("\\(");
                String[] seperated2 = seperated[1].split("\\)");

                for (int i = 2; i < seperated.length; i++) {
                    String[] botAddressSeperator = seperated[i].split(",");
                    //String refinedList = seperated[i].replace(",", "").replace(")", "").replace(";", "").trim();
                    String refinedList = botAddressSeperator[1].trim();
                    for (Bot bot : BotManager.getConnectedBots()) {
                        if (bot.getAddress().equals(refinedList)) {
                            botList.add(bot);
                        }
                    }
                    for (Bot bot : BotManager.getSharedBots()) {
                        if (bot.getSharedAddress().equals(refinedList)) {
                            botList.add(bot);
                        }
                    }
                    //botList.add(refinedList);
                }
                botCommand = seperated[0];
                duration = Integer.parseInt(seperated2[0]) * 100;
            }

            Log.d("MYTAG", botCommand + ", " + duration + ", " + iterations + ", " + ordinal + ", " + speed + ", " + botList);

            BotCommand cloneCmd = new BotCommand();
            cloneCmd.setCommand(botCommand);
            cloneCmd.setDuration(duration);
            cloneCmd.setIterations(iterations);
            cloneCmd.setBotList(botList);
            cloneCmd.setListOrdinal(ordinal);
            cloneCmd.setSpeed(speed);

            if (nonRepeating) {
                commandsToBeExecuted.add(cloneCmd);
            } else {
                alwaysRepeatCommands.add(cloneCmd);
            }
        }
    }

    static Timer executionTimer = new Timer();
    static Timer repeatTimer = new Timer();
    private void runCommandList() {
        int totalDuration = 0;
        int index = 0;

        mGeneratedCode.setText("");

        executionTimer = new Timer();

        for (final BotCommand botCommandFinal : commandsToBeExecuted) {
            final int finalIndex = index;

            //Timer exTimer = new Timer();
            executionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ArrayList<Bot> tempList = botCommandFinal.getBotList();
                    UIManager.setRecievedResponse(false);
                    Log.d("MYTAG", "final index: " + finalIndex + " commandstobeexecuedteddf count: " + commandsToBeExecuted.size());
                    displayCode(finalIndex);

                    for (Bot bot:botCommandFinal.getBotList()) {
                        if(!bot.getIsVirtual()) {
                            BotsRepo botsRepo = new BotsRepo();
                            final List<Bots> botsList = botsRepo.GetBot(bot.getAddress());
                            if (botsList.size() > 0) {
                                botsList.get(0).setTimeDriven(botsList.get(0).getTimeDriven() + (botCommandFinal.getDuration() / 1000));
                                botsRepo.update(botsList.get(0));

                                if (BotManager.getConnectedBots().size() > 0 && bot.getAddress() == BotManager.getConnectedBots().get(0).getAddress()) {
                                    UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            UIManager.getDrivingScreen().displayOdometer(Integer.toString((int) (botsList.get(0).getTimeDriven() * 2.3)));
                                        }
                                    });
                                }
                            }
                        }
                    }

                    if(botCommandFinal.getCommandType() == Global.kMPC_COMMAND) {
                        if(!botCommandFinal.getBotList().get(0).getIsSharedBot()) {
                            UIManager.setCurrentBot(botCommandFinal.getBotList().get(0).getAddress());
                        }else{
                            UIManager.setCurrentBot(botCommandFinal.getBotList().get(0).getSharedAddress());
                        }
                    }

                    botCommandFinal.executeBotCommand();
                }
            }, totalDuration);


            if(botCommandFinal.getCommand().contains("Spin") && !(botCommandFinal.getCommand().contains("Custom"))){

                float spin = 0;
                for (Bot bot : botCommandFinal.getBotList()) {
                    if(bot.getIsVirtual()){
                        spin = .25f;
                    }else {
                        BotsRepo botsRepo = new BotsRepo();
                        List<Bots> botsList = botsRepo.GetBot(bot.getAddress());

                        if (botsList.size() > 0) {
                            if (!botCommandFinal.getCommand().contains("Hard")) {
                                if (botCommandFinal.getCommand().contains("Left")) {
                                    switch(UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getLeftTurnSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftTurnSlow360(8000);
                                                }else {
                                                    botsList.get(0).setLeftTurnSlow360(3200);
                                                }
                                            }
                                            if (botsList.get(0).getLeftTurnSlow360() > spin) {
                                                spin = botsList.get(0).getLeftTurnSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getLeftTurnMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftTurnMedium360(2200);
                                                }else {
                                                    botsList.get(0).setLeftTurnMedium360(1500);
                                                }
                                            }
                                            if (botsList.get(0).getLeftTurnMedium360() > spin) {
                                                spin = botsList.get(0).getLeftTurnMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getLeftTurnFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftTurnFast360(1800);
                                                }else {
                                                    botsList.get(0).setLeftTurnFast360(1100);
                                                }
                                            }
                                            if (botsList.get(0).getLeftTurnFast360() > spin) {
                                                spin = botsList.get(0).getLeftTurnFast360();
                                            }
                                            break;
                                    }
                                } else if (botCommandFinal.getCommand().contains("Right")) {
                                    switch(UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getRightTurnSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightTurnSlow360(8000);
                                                }else {
                                                    botsList.get(0).setRightTurnSlow360(3200);
                                                }
                                            }
                                            if (botsList.get(0).getRightTurnSlow360() > spin) {
                                                spin = botsList.get(0).getRightTurnSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getRightTurnMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightTurnMedium360(2200);
                                                }else {
                                                    botsList.get(0).setRightTurnMedium360(1500);
                                                }
                                            }
                                            if (botsList.get(0).getRightTurnMedium360() > spin) {
                                                spin = botsList.get(0).getRightTurnMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getRightTurnFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightTurnFast360(1800);
                                                }else {
                                                    botsList.get(0).setRightTurnFast360(1100);
                                                }
                                            }
                                            if (botsList.get(0).getRightTurnFast360() > spin) {
                                                spin = botsList.get(0).getRightTurnFast360();
                                            }
                                            break;
                                    }
                                }
                            }else{
                                if (botCommandFinal.getCommand().contains("Left")) {
                                    switch(UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getLeftSpinSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftSpinSlow360(2500);
                                                }else {
                                                    botsList.get(0).setLeftSpinSlow360(1300);
                                                }
                                            }
                                            if (botsList.get(0).getLeftSpinSlow360() > spin) {
                                                spin = botsList.get(0).getLeftSpinSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getLeftSpinMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftSpinMedium360(1000);
                                                }else {
                                                    botsList.get(0).setLeftSpinMedium360(700);
                                                }
                                            }
                                            if (botsList.get(0).getLeftSpinMedium360() > spin) {
                                                spin = botsList.get(0).getLeftSpinMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getLeftSpinFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftSpinFast360(600);
                                                }else {
                                                    botsList.get(0).setLeftSpinFast360(600);
                                                }
                                            }
                                            if (botsList.get(0).getLeftSpinFast360() > spin) {
                                                spin = botsList.get(0).getLeftSpinFast360();
                                            }
                                            break;
                                    }
                                } else if (botCommandFinal.getCommand().contains("Right")) {
                                    switch(UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getRightSpinSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightSpinSlow360(2500);
                                                }else {
                                                    botsList.get(0).setRightSpinSlow360(1300);
                                                }
                                            }
                                            if (botsList.get(0).getRightSpinSlow360() > spin) {
                                                spin = botsList.get(0).getRightSpinSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getRightSpinMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightSpinMedium360(1000);
                                                }else {
                                                    botsList.get(0).setRightSpinMedium360(700);
                                                }
                                            }
                                            if (botsList.get(0).getRightSpinMedium360() > spin) {
                                                spin = botsList.get(0).getRightSpinMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getRightSpinFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightSpinFast360(600);
                                                }else {
                                                    botsList.get(0).setRightSpinFast360(600);
                                                }
                                            }
                                            if (botsList.get(0).getRightSpinFast360() > spin) {
                                                spin = botsList.get(0).getRightSpinFast360();
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }

                int duration = 0;
                if(botCommandFinal.getCommand().contains("90")){
                    duration = (int)(spin * .25);
                }
                else if(botCommandFinal.getCommand().contains("180")){
                    duration = (int)(spin * .5);
                }
                else if (botCommandFinal.getCommand().contains("270")){
                    duration = (int)(spin * .75);
                }
                else if (botCommandFinal.getCommand().contains("360")){
                    duration = (int)(spin);
                }

                totalDuration = totalDuration + duration + 500;
            }else if (botCommandFinal.getCommand().contains("Custom")){
                float spin = 0;
                for (Bot bot : botCommandFinal.getBotList()) {
                    if(bot.getIsVirtual()){
                        spin = .25f;
                    }else {
                        BotsRepo botsRepo = new BotsRepo();
                        List<Bots> botsList = botsRepo.GetBot(bot.getAddress());

                        if(botCommandFinal.getCommand().contains("Spin")){
                            if (botCommandFinal.getCommand().contains("Left")) {
                                if (botsList.size() > 0) {
                                    switch (UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getLeftSpinSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftSpinSlow360(2500);
                                                }else {
                                                    botsList.get(0).setLeftSpinSlow360(1300);
                                                }
                                            }
                                            if (botsList.get(0).getLeftSpinSlow360() > spin) {
                                                spin = botsList.get(0).getLeftSpinSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getLeftSpinMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftSpinMedium360(1000);
                                                }else {
                                                    botsList.get(0).setLeftSpinMedium360(700);
                                                }
                                            }
                                            if (botsList.get(0).getLeftSpinMedium360() > spin) {
                                                spin = botsList.get(0).getLeftSpinMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getLeftSpinFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftSpinFast360(600);
                                                }else {
                                                    botsList.get(0).setLeftSpinFast360(600);
                                                }
                                            }
                                            if (botsList.get(0).getLeftSpinFast360() > spin) {
                                                spin = botsList.get(0).getLeftSpinFast360();
                                            }
                                            break;
                                        default:
                                            if(botsList.get(0).getLeftSpinFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftSpinFast360(600);
                                                }else {
                                                    botsList.get(0).setLeftSpinFast360(600);
                                                }
                                            }
                                            if (botsList.get(0).getLeftSpinFast360() > spin) {
                                                spin = botsList.get(0).getLeftSpinFast360();
                                            }
                                            break;
                                    }
                                }
                            } else if (botCommandFinal.getCommand().contains("Right")) {
                                if (botsList.size() > 0) {
                                    switch (UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getRightSpinSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightSpinSlow360(2500);
                                                }else {
                                                    botsList.get(0).setRightSpinSlow360(1300);
                                                }
                                            }
                                            if (botsList.get(0).getRightSpinSlow360() > spin) {
                                                spin = botsList.get(0).getRightSpinSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getRightSpinMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightSpinMedium360(1000);
                                                }else {
                                                    botsList.get(0).setRightSpinMedium360(700);
                                                }
                                            }
                                            if (botsList.get(0).getRightSpinMedium360() > spin) {
                                                spin = botsList.get(0).getRightSpinMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getRightSpinFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightSpinFast360(600);
                                                }else {
                                                    botsList.get(0).setRightSpinFast360(600);
                                                }
                                            }
                                            if (botsList.get(0).getRightSpinFast360() > spin) {
                                                spin = botsList.get(0).getRightSpinFast360();
                                            }
                                            break;
                                        default:
                                            if(botsList.get(0).getRightSpinFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightSpinFast360(600);
                                                }else {
                                                    botsList.get(0).setRightSpinFast360(600);
                                                }
                                            }
                                            if (botsList.get(0).getRightSpinFast360() > spin) {
                                                spin = botsList.get(0).getRightSpinFast360();
                                            }
                                            break;
                                    }
                                }
                            }
                        }else {
                            if (botCommandFinal.getCommand().contains("Left")) {
                                if (botsList.size() > 0) {
                                    switch (UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getLeftTurnSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftTurnSlow360(8000);
                                                }else {
                                                    botsList.get(0).setLeftTurnSlow360(3200);
                                                }
                                            }
                                            if (botsList.get(0).getLeftTurnSlow360() > spin) {
                                                spin = botsList.get(0).getLeftTurnSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getLeftTurnMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftTurnMedium360(2200);
                                                }else {
                                                    botsList.get(0).setLeftTurnMedium360(1500);
                                                }
                                            }
                                            if (botsList.get(0).getLeftTurnMedium360() > spin) {
                                                spin = botsList.get(0).getLeftTurnMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getLeftTurnFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftTurnFast360(1800);
                                                }else {
                                                    botsList.get(0).setLeftTurnFast360(1100);
                                                }
                                            }
                                            if (botsList.get(0).getLeftTurnFast360() > spin) {
                                                spin = botsList.get(0).getLeftTurnFast360();
                                            }
                                            break;
                                        default:
                                            if(botsList.get(0).getLeftTurnFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setLeftTurnFast360(1800);
                                                }else {
                                                    botsList.get(0).setLeftTurnFast360(1100);
                                                }
                                            }
                                            if (botsList.get(0).getLeftTurnFast360() > spin) {
                                                spin = botsList.get(0).getLeftTurnFast360();
                                            }
                                            break;
                                    }
                                }
                            } else if (botCommandFinal.getCommand().contains("Right")) {
                                if (botsList.size() > 0) {
                                    switch (UIManager.getDrivingScreen().selectedSpeed){
                                        case "Slow":
                                            if(botsList.get(0).getRightTurnSlow360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightTurnSlow360(8000);
                                                }else {
                                                    botsList.get(0).setRightTurnSlow360(3200);
                                                }
                                            }
                                            if (botsList.get(0).getRightTurnSlow360() > spin) {
                                                spin = botsList.get(0).getRightTurnSlow360();
                                            }
                                            break;
                                        case "Medium":
                                            if(botsList.get(0).getRightTurnMedium360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightTurnMedium360(2200);
                                                }else {
                                                    botsList.get(0).setRightTurnMedium360(1500);
                                                }
                                            }
                                            if (botsList.get(0).getRightTurnMedium360() > spin) {
                                                spin = botsList.get(0).getRightTurnMedium360();
                                            }
                                            break;
                                        case "Fast":
                                            if(botsList.get(0).getRightTurnFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightTurnFast360(1800);
                                                }else {
                                                    botsList.get(0).setRightTurnFast360(1100);
                                                }
                                            }
                                            if (botsList.get(0).getRightTurnFast360() > spin) {
                                                spin = botsList.get(0).getRightTurnFast360();
                                            }
                                            break;
                                        default:
                                            if(botsList.get(0).getRightTurnFast360() == 0){
                                                if(bot.getPrefix().toUpperCase().equals(UIManager.MB3_NAME)){
                                                    botsList.get(0).setRightTurnFast360(1800);
                                                }else {
                                                    botsList.get(0).setRightTurnFast360(1100);
                                                }
                                            }
                                            if (botsList.get(0).getRightTurnFast360() > spin) {
                                                spin = botsList.get(0).getRightTurnFast360();
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }

                int duration = (int) ((spin / 100) * ((float)botCommandFinal.getDuration() / 360));
                totalDuration = totalDuration + duration + 500;

            }else{
                if(botCommandFinal.getDuration() == 9898){
                    totalDuration = totalDuration + 1500;
                }else if(botCommandFinal.getDuration() == 0){
                    totalDuration = totalDuration + 1000;
                } else{
                    totalDuration = totalDuration + botCommandFinal.getDuration() + 500;
                }
            }

            index++;

        }
        repeatTimer = new Timer();
        repeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                UIManager.setCurrentCommand("");
                UIManager.setCurrentBot("");

                if(repeatForever){
                    //todo check what is in always repeate commands
                    commandsToBeExecuted.clear();
                    commandsToBeExecuted.addAll(alwaysRepeatCommands);
                    runCommandList();
                }else{
                    programIsExecuting = false;
                    UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setImageResource(R.drawable.play_arrow);
                        }
                    });

                    for(Bot bot : BotManager.getConnectedCircuits()){
                        bot.stopMotorCircuit("clear", true);
                    }

                    for(Bot bot : BotManager.getSharedBots()){
                        bot.stopMotorCircuit("clear", true);
                    }
                    UIManager.setCurrentIndex(-1);
                }
            }
        }, totalDuration);
    }

    public static void stopExecutionTimerBlockly(){
        executionTimer.cancel();
        executionTimer.purge();
        repeatTimer.cancel();
        repeatTimer.purge();
        UIManager.setCurrentCommand("");
        UIManager.setCurrentIndex(-1);
        UIManager.setCurrentBot("");

        executionTimer = new Timer();
        repeatTimer = new Timer();
        BotManager.stopBots();

        repeatForever = false;
        programIsExecuting = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopExecutionTimerBlockly();
    }

    void searchDatabase(){
        final EditText codeInput = view.findViewById(R.id.codeInput);
        codeInput.clearFocus();

        database = FirebaseDatabase.getInstance();
        blocklyDatabase = database.getReference("Blockly");
        final ArrayList<BlocklyDatabase> blocklyList = new ArrayList<>();

        ConnectivityManager connectivityManager = (ConnectivityManager) UIManager.getDrivingScreen().getSystemService(Context.CONNECTIVITY_SERVICE);
        if((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null &&  connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) ||
                (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            //we are connected to a network

            blocklyDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        BlocklyDatabase blocklyDB = postSnapshot.getValue(BlocklyDatabase.class);

                        blocklyList.add(blocklyDB);
                    }

                    boolean foundCode = false;

                    for(BlocklyDatabase bdb : blocklyList){
                        //Log.d("MYTAG", "BlocklyDB, id: " + bdb.getId() + ", codeInput: " + codeInput.getText().toString() + ", code: " + bdb.getCode() + ", date: " + bdb.getDate() + ", xml: " + bdb.getXml());
                        if(bdb.getCode().toUpperCase().equals(codeInput.getText().toString().toUpperCase())){
                            mWebView.loadUrl("javascript:cloudLoadCode('" + bdb.getXml() + "','" + codeInput.getText().toString().toUpperCase() + "')");

                            codeInput.setText("");
                            RelativeLayout cloudDownloadHolder = view.findViewById(R.id.cloudDownloadHolder);
                            cloudDownloadHolder.setVisibility(View.GONE);
                            foundCode = true;
                            break;
                        }
                    }

                    if(!foundCode){
                        Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), ("No Blockly code was found with code " + codeInput.getText().toString()), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), "An internet connection is required to download code.", Toast.LENGTH_LONG).show();
        }
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
        blocklyDatabase = database.getReference("Blockly");
        final ArrayList<BlocklyDatabase> blocklyList = new ArrayList<>();

        foundCode = false;

        blocklyDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BlocklyDatabase blocklyDB = postSnapshot.getValue(BlocklyDatabase.class);

                    blocklyList.add(blocklyDB);
                }

                for (BlocklyDatabase bdb : blocklyList) {
                    if (bdb.getCode().toUpperCase().equals(sb.toString().toUpperCase())) {
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

    void renameRoutine(){

        AutoResizeTextView blocklyNameText = blocklyNameItemToChange.findViewById(R.id.blocklyName);

        BlocklyRepo blocklyRepo = new BlocklyRepo();

        List<Blockly> blocklyList = blocklyRepo.getBlocklyList();

        for (Blockly blockly: blocklyList) {
            if(blockly.getBlocklyName().equals(blocklyNameText.getText().toString())){
                blockly.setBlocklyName(renameInput.getText().toString());
                blocklyRepo.update(blockly);
                break;
            }
        }

        blocklyNameText.setText(renameInput.getText().toString());
        renameInput.clearFocus();
        renameLayout.setVisibility(View.GONE);

        try  {
            InputMethodManager imm = (InputMethodManager)UIManager.getDrivingScreen().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(UIManager.getDrivingScreen().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {

        }
    }

    void showPopUp(String didWork){

        AlertDialog.Builder loadingAlert = new AlertDialog.Builder(getActivity());

        if(didWork.equals("true")){
            loadingAlert.setMessage("Your code was loaded successfully.");

        }else{
            loadingAlert.setMessage("There was an issue loading your code.");

        }
        loadingAlert.create();
        loadingAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        loadingAlert.show();
    }

    void blocklyRunUser(){
        mWebView.loadUrl("javascript:runUserCode()");
    }

    void sendUserData(String data){
        ConnectivityManager connectivityManager = (ConnectivityManager) UIManager.getDrivingScreen().getSystemService(UIManager.getDrivingScreen().CONNECTIVITY_SERVICE);
        if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) ||
                (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            //we are connected to a network

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("ControllerV2");

            String id = myRef.push().getKey();

            ControllerClass botCodeDB = new ControllerClass(id, data, UIManager.getRoomCode(), java.text.DateFormat.getDateTimeInstance().format(new Date()), "blockly");
            myRef.child(UIManager.getRoomCode()).child("codeInfo").setValue(botCodeDB);

            Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), ("Your code has been sent to the Control Room."), Toast.LENGTH_LONG).show();
        }
    }

    public void loadUserCode(String data){

        mWebView.loadUrl("javascript:cloudLoadCode('" + data + "','" + null + "')");
    }

    public void botDisconnected(String botName){

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String name = botName;
        if(name.length() > 3 && (name.substring(0,3).toUpperCase().equals(UIManager.CIRCUIT_NAME) || name.substring(0,3).toUpperCase().equals(UIManager.SNAP_CIRCUIT_NAME) || name.substring(0,3).toUpperCase().equals(UIManager.MB3_NAME) || name.substring(0,3).toUpperCase().equals(UIManager.MBPLUS_NAME) || name.substring(0,3).toUpperCase().equals(UIManager.MB2_NAME))){
            name = name.substring(3);
        }

        String test = mGeneratedCode.getText().toString() + "\n\n" + currentTime + ": Bot " + name + " was disconnected.";
        //mGeneratedCode.setText(test);
    }

    public void loadBots(){

        ArrayList<String> botNameList = new ArrayList<>();
        ArrayList<String> botUUIDList = new ArrayList<>();
        ArrayList<String> circuitNameList = new ArrayList<>();
        ArrayList<String> circuitUUIDList = new ArrayList<>();
        ArrayList<String> motorNameList = new ArrayList<>();
        ArrayList<String> motorUUIDList = new ArrayList<>();
        ArrayList<String> boardNameList = new ArrayList<>();
        ArrayList<String> boardUUIDList = new ArrayList<>();
        ArrayList<ArrayList> pioList = new ArrayList<>();
        ArrayList<String> pioList2 = new ArrayList<>();

        for(Bot bot : BotManager.getConnectedBots()){
            Log.d("MYTAG", "ADDING BOT: " + bot.getName());
            if(bot.getChangedName() == null) {
                botNameList.add("'" + bot.getOnlyName() + "'");
            }else{
                botNameList.add("'" + bot.getChangedName() + "'");
            }
            botUUIDList.add("'"+bot.getAddress()+"'");
        }

        for(Bot bot : BotManager.getSharedBots()){
            botNameList.add("'"+bot.getSharedName()+"'");
            botUUIDList.add("'"+bot.getSharedAddress()+"'");
        }

        for(Bot circuit : BotManager.getConnectedCircuits()){
            Log.d("MYTAG", "ADDING CIRCUIT: " + circuit.getName());
            if(circuit.getChangedName() == null) {
                circuitNameList.add("'" + circuit.getOnlyName() + "'");
            }else{
                circuitNameList.add("'" + circuit.getChangedName() + "'");
            }
            circuitUUIDList.add("'"+circuit.getAddress()+"'");
        }

        for(Bot circuit : BotManager.getSharedCircuits()){
            circuitNameList.add("'"+circuit.getSharedName()+"'");
            circuitUUIDList.add("'"+circuit.getSharedAddress()+"'");
        }

        for(Bot motor : BotManager.getConnectedMotors()){
            if(motor.getChangedName() == null) {
                motorNameList.add("'" + motor.getOnlyName() + "'");
            }else{
                motorNameList.add("'" + motor.getChangedName() + "'");
            }
            botUUIDList.add("'"+motor.getAddress()+"'");
        }

        for(Bot bot : BotManager.getConnectedMPCs()){
            ArrayList<String> tempList = new ArrayList<>();
            if(bot.getChangedName() == null) {
                boardNameList.add("'" + bot.getOnlyName() + "'");
            }else{
                boardNameList.add("'" + bot.getChangedName() + "'");
            }
            boardUUIDList.add("'"+bot.getAddress()+"'");

            tempList.add("'" + bot.getAddress() + "'");

            if (bot.getD1SubType().equals("") || bot.getD1SubType() == null) {
                tempList.add("'D1, null'");
            }else{
                tempList.add("'D1, " + bot.getD1SubType() + "'");
            }

            if (bot.getA1SubType().equals("") || bot.getA1SubType() == null) {
                tempList.add("'A1, null'");
            }else{
                tempList.add("'A1, " + bot.getA1SubType() + "'");
            }

            if (bot.getD2SubType().equals("") || bot.getD2SubType() == null) {
                tempList.add("'D2, null'");
            }else{
                tempList.add("'D2, " + bot.getD2SubType() + "'");
            }

            if (bot.getA2SubType().equals("") || bot.getA2SubType() == null) {
                tempList.add("'A2, null'");
            }else{
                tempList.add("'A2, " + bot.getA2SubType() + "'");
            }

            if (bot.getD3SubType().equals("") || bot.getD3SubType() == null) {
                tempList.add("'D3, null'");
            }else{
                tempList.add("'D3, " + bot.getD3SubType() + "'");
            }

            if (bot.getA3SubType().equals("") || bot.getA3SubType() == null) {
                tempList.add("'A3, null'");
            }else{
                tempList.add("'A3, " + bot.getA3SubType() + "'");
            }

            if (bot.getD4SubType().equals("") || bot.getD4SubType() == null) {
                tempList.add("'D4, null'");
            }else{
                tempList.add("'D4, " + bot.getD4SubType() + "'");
            }

            if (bot.getA4SubType().equals("") || bot.getA4SubType() == null) {
                tempList.add("'A4, null'");
            }else{
                tempList.add("'A4, " + bot.getA4SubType() + "'");
            }
            Log.d("MYTAG", "taco type: " + tempList);

            pioList.add(tempList);
            pioList.add(tempList);

        }

        for(Bot bot : BotManager.getSharedMPCs()){
            boardNameList.add("'"+bot.getSharedName()+"'");
            boardUUIDList.add("'"+bot.getSharedAddress()+"'");
        }
        pioList2.add("'test'");

        mWebView.loadUrl("javascript:showBots(" + botNameList + "," + botUUIDList + "," + circuitNameList + "," + circuitUUIDList + "," + motorNameList + "," + motorUUIDList + "," + boardNameList + "," + boardUUIDList + "," + pioList + ")");

        ArrayList<MPCClass> typeList = new ArrayList<>();

        for(Bot bot : BotManager.getConnectedMPCs()){

                MPCClass mpcClassD1 = new MPCClass();
                mpcClassD1.setPio("D1");
                mpcClassD1.setType(bot.getD1SubType());
                typeList.add(mpcClassD1);

                MPCClass mpcClassA1 = new MPCClass();
                mpcClassA1.setPio("A1");
                mpcClassA1.setType(bot.getA1SubType());
                typeList.add(mpcClassA1);

                MPCClass mpcClassD2 = new MPCClass();
                mpcClassD2.setPio("D2");
                mpcClassD2.setType(bot.getD2SubType());
                typeList.add(mpcClassD2);

                MPCClass mpcClassA2 = new MPCClass();
                mpcClassA2.setPio("A2");
                mpcClassA2.setType(bot.getA2SubType());
                typeList.add(mpcClassA2);

                MPCClass mpcClassD3 = new MPCClass();
                mpcClassD3.setPio("D3");
                mpcClassD3.setType(bot.getD3SubType());
                typeList.add(mpcClassD3);

                MPCClass mpcClassA3 = new MPCClass();
                mpcClassA3.setPio("A3");
                mpcClassA3.setType(bot.getA3SubType());
                typeList.add(mpcClassA3);

                MPCClass mpcClassD4 = new MPCClass();
                mpcClassD4.setPio("D4");
                mpcClassD4.setType(bot.getD4SubType());
                typeList.add(mpcClassD4);

                MPCClass mpcClassA4 = new MPCClass();
                mpcClassA4.setPio("A4");
                mpcClassA4.setType(bot.getA4SubType());
                typeList.add(mpcClassA4);

                removeNull(typeList);


            for(MPCClass mpcClass : typeList){
                Log.d("MYTAG", "mpcClass: " + mpcClass.getPio() + " " + mpcClass.getType());
            }

            ArrayList<String> commandList = new ArrayList<>();
            ArrayList<String> valueList = new ArrayList<>();
            for(int i = 0; i < typeList.size(); i++){

                switch (typeList.get(i).getType()){
                    case "LED":
                        switch (typeList.get(i).getPio()){
                            case "D1":
                                commandList.add("'turnd1ledonkliks'");
                                commandList.add("'turnd1ledontoggle'");
                                valueList.add("'turnD1LEDOnKliks'");
                                valueList.add("'turnD1LEDOnToggle'");
                                break;
                            case "A1":
                                commandList.add("'turna1ledonkliks'");
                                commandList.add("'turna1ledontoggle'");
                                valueList.add("'turnA1LEDOnKliks'");
                                valueList.add("'turnA1LEDOnToggle'");
                                break;
                            case "D2":
                                commandList.add("'turnd2ledonkliks'");
                                commandList.add("'turnd2ledontoggle'");
                                valueList.add("'turnD2LEDOnKliks'");
                                valueList.add("'turnD2LEDOnToggle'");
                                break;
                            case "A2":
                                commandList.add("'turna2ledonkliks'");
                                commandList.add("'turna2ledontoggle'");
                                valueList.add("'turnA2LEDOnKliks'");
                                valueList.add("'turnA2LEDOnToggle'");
                                break;
                            case "D3":
                                commandList.add("'turnd3ledonkliks'");
                                commandList.add("'turnd3ledontoggle'");
                                valueList.add("'turnD3LEDOnKliks'");
                                valueList.add("'turnD3LEDOnToggle'");
                                break;
                            case "A3":
                                commandList.add("'turna3ledonkliks'");
                                commandList.add("'turna3ledontoggle'");
                                valueList.add("'turnA3LEDOnKliks'");
                                valueList.add("'turnA3LEDOnToggle'");
                                break;
                            case "D4":
                                commandList.add("'turnd4ledonkliks'");
                                commandList.add("'turnd4ledontoggle'");
                                valueList.add("'turnD4LEDOnKliks'");
                                valueList.add("'turnD4LEDOnToggle'");
                                break;
                            case "A4":
                                commandList.add("'turna4ledonkliks'");
                                commandList.add("'turna4ledontoggle'");
                                valueList.add("'turnA4LEDOnKliks'");
                                valueList.add("'turnA4LEDOnToggle'");
                                break;
                        }
                        break;
                    case "Speaker":
                        switch (typeList.get(i).getPio()){
                            case "D1":
                                commandList.add("'turnd1speakeronkliks'");
                                commandList.add("'turnd1speakerontoggle'");
                                valueList.add("'turnD1SpeakerOnKliks'");
                                valueList.add("'turnD1SpeakerOnToggle'");
                                break;
                            case "A1":
                                commandList.add("'turna1speakeronkliks'");
                                commandList.add("'turna1speakerontoggle'");
                                valueList.add("'turnA1SpeakerOnKliks'");
                                valueList.add("'turnA1SpeakerOnToggle'");
                                break;
                            case "D2":
                                commandList.add("'turnd2speakeronkliks'");
                                commandList.add("'turnd2speakerontoggle'");
                                valueList.add("'turnD2SpeakerOnKliks'");
                                valueList.add("'turnD2SpeakerOnToggle'");
                                break;
                            case "A2":
                                commandList.add("'turna2speakeronkliks'");
                                commandList.add("'turna2speakerontoggle'");
                                valueList.add("'turnA2SpeakerOnKliks'");
                                valueList.add("'turnA2SpeakerOnToggle'");
                                break;
                            case "D3":
                                commandList.add("'turnd3speakeronkliks'");
                                commandList.add("'turnd3speakerontoggle'");
                                valueList.add("'turnD3SpeakerOnKliks'");
                                valueList.add("'turnD3SpeakerOnToggle'");
                                break;
                            case "A3":
                                commandList.add("'turna3speakeronkliks'");
                                commandList.add("'turna3speakerontoggle'");
                                valueList.add("'turnA3SpeakerOnKliks'");
                                valueList.add("'turnA3SpeakerOnToggle'");
                                break;
                            case "D4":
                                commandList.add("'turnd4speakeronkliks'");
                                commandList.add("'turnd4speakerontoggle'");
                                valueList.add("'turnD4SpeakerOnKliks'");
                                valueList.add("'turnD4SpeakerOnToggle'");
                                break;
                            case "A4":
                                commandList.add("'turna4speakeronkliks'");
                                commandList.add("'turna4speakerontoggle'");
                                valueList.add("'turnA4SpeakerOnKliks'");
                                valueList.add("'turnA4SpeakerOnToggle'");
                                break;
                        }
                        break;
                    case "Photoresistor":
                        switch (typeList.get(i).getPio()){
                            case "D1":
                                commandList.add("'d1photoresistor'");
                                valueList.add("'D1Photoresistor'");
                                break;
                            case "A1":
                                commandList.add("'a1photoresistor'");
                                valueList.add("'A1Photoresistor'");
                                break;
                            case "D2":
                                commandList.add("'d2photoresistor'");
                                valueList.add("'D2Photoresistor'");
                                break;
                            case "A2":
                                commandList.add("'a2photoresistor'");
                                valueList.add("'A2Photoresistor'");
                                break;
                            case "D3":
                                commandList.add("'d3photoresistor'");
                                valueList.add("'D3Photoresistor'");
                                break;
                            case "A3":
                                commandList.add("'a3photoresistor'");
                                valueList.add("'A3Photoresistor'");
                                break;
                            case "D4":
                                commandList.add("'d4photoresistor'");
                                valueList.add("'D4Photoresistor'");
                                break;
                            case "A4":
                                commandList.add("'a4photoresistor'");
                                valueList.add("'A4Photoresistor'");
                                break;
                        }
                        break;
                    case "Instant":
                        switch (typeList.get(i).getPio()){
                            case "D1":
                                commandList.add("'d1touchcount'");
                                valueList.add("'D1TouchCount'");
                                commandList.add("'resetd1count'");
                                valueList.add("'resetD1Count'");
                                commandList.add("'d1touching'");
                                valueList.add("'D1Touching'");
                                break;
                            case "A1":
                                commandList.add("'a1touchcount'");
                                valueList.add("'A1TouchCount'");
                                commandList.add("'reseta1count'");
                                valueList.add("'resetA1Count'");
                                commandList.add("'a1touching'");
                                valueList.add("'A1Touching'");
                                break;
                            case "D2":
                                commandList.add("'d2touchcount'");
                                valueList.add("'D2TouchCount'");
                                commandList.add("'resetd2count'");
                                valueList.add("'resetD2Count'");
                                commandList.add("'d2touching'");
                                valueList.add("'D2Touching'");
                                break;
                            case "A2":
                                commandList.add("'a2touchcount'");
                                valueList.add("'A2TouchCount'");
                                commandList.add("'reseta2count'");
                                valueList.add("'resetA2Count'");
                                commandList.add("'a2touching'");
                                valueList.add("'A2Touching'");
                                break;
                            case "D3":
                                commandList.add("'d3touchcount'");
                                valueList.add("'D3TouchCount'");
                                commandList.add("'resetd3count'");
                                valueList.add("'resetD3Count'");
                                commandList.add("'d3touching'");
                                valueList.add("'D3Touching'");
                                break;
                            case "A3":
                                commandList.add("'a3touchcount'");
                                valueList.add("'A3TouchCount'");
                                commandList.add("'reseta3count'");
                                valueList.add("'resetA3Count'");
                                commandList.add("'a3touching'");
                                valueList.add("'A3Touching'");
                                break;
                            case "D4":
                                commandList.add("'d4touchcount'");
                                valueList.add("'D4TouchCount'");
                                commandList.add("'resetd4count'");
                                valueList.add("'resetD4Count'");
                                commandList.add("'d4touching'");
                                valueList.add("'D4Touching'");
                                break;
                            case "A4":
                                commandList.add("'a4touchcount'");
                                valueList.add("'A4TouchCount'");
                                commandList.add("'reseta4count'");
                                valueList.add("'resetA4Count'");
                                commandList.add("'a4touching'");
                                valueList.add("'A4Touching'");
                                break;
                        }
                        break;
                    case "Delayed":
                        switch (typeList.get(i).getPio()){
                            case "D1":
                                commandList.add("'d1touchcount'");
                                valueList.add("'D1TouchCount'");
                                commandList.add("'resetd1count'");
                                valueList.add("'resetD1Count'");
                                break;
                            case "A1":
                                commandList.add("'a1touchcount'");
                                valueList.add("'A1TouchCount'");
                                commandList.add("'reseta1count'");
                                valueList.add("'resetA1Count'");
                                break;
                            case "D2":
                                commandList.add("'d2touchcount'");
                                valueList.add("'D2TouchCount'");
                                commandList.add("'resetd2count'");
                                valueList.add("'resetD2Count'");
                                break;
                            case "A2":
                                commandList.add("'a2touchcount'");
                                valueList.add("'A2TouchCount'");
                                commandList.add("'reseta2count'");
                                valueList.add("'resetA2Count'");
                                break;
                            case "D3":
                                commandList.add("'d3touchcount'");
                                valueList.add("'D3TouchCount'");
                                commandList.add("'resetd3count'");
                                valueList.add("'resetD3Count'");
                                break;
                            case "A3":
                                commandList.add("'a3touchcount'");
                                valueList.add("'A3TouchCount'");
                                commandList.add("'reseta3count'");
                                valueList.add("'resetA3Count'");
                                break;
                            case "D4":
                                commandList.add("'d4touchcount'");
                                valueList.add("'D4TouchCount'");
                                commandList.add("'resetd4count'");
                                valueList.add("'resetD4Count'");
                                break;
                            case "A4":
                                commandList.add("'a4touchcount'");
                                valueList.add("'A4TouchCount'");
                                commandList.add("'reseta4count'");
                                valueList.add("'resetA4Count'");
                                break;
                        }
                        break;
                    case "Temp":
                    case "Humidity":
                        switch (typeList.get(i).getPio()){
                            case "D1":
                                commandList.add("'d1temperature'");
                                valueList.add("'D1Temperature'");
                                commandList.add("'d1humidity'");
                                valueList.add("'D1Humidity'");
                                break;
                            case "A1":
                                commandList.add("'a1temperature'");
                                valueList.add("'A1Temperature'");
                                commandList.add("'a1humidity'");
                                valueList.add("'A1Humidity'");
                                break;
                            case "D2":
                                commandList.add("'d2temperature'");
                                valueList.add("'D2Temperature'");
                                commandList.add("'d2humidity'");
                                valueList.add("'D2Humidity'");
                                break;
                            case "A2":
                                commandList.add("'a2temperature'");
                                valueList.add("'A2Temperature'");
                                commandList.add("'a2humidity'");
                                valueList.add("'A2Humidity'");
                                break;
                            case "D3":
                                commandList.add("'d3temperature'");
                                valueList.add("'D3Temperature'");
                                commandList.add("'d3humidity'");
                                valueList.add("'D3Humidity'");
                                break;
                            case "A3":
                                commandList.add("'a3temperature'");
                                valueList.add("'A3Temperature'");
                                commandList.add("'a3humidity'");
                                valueList.add("'A3Humidity'");
                                break;
                            case "D4":
                                commandList.add("'d4temperature'");
                                valueList.add("'D4Temperature'");
                                commandList.add("'d4humidity'");
                                valueList.add("'D4Humidity'");
                                break;
                            case "A4":
                                commandList.add("'a4temperature'");
                                valueList.add("'A4Temperature'");
                                commandList.add("'a4humidity'");
                                valueList.add("'A4Humidity'");
                                break;
                        }
                        break;
                }
            }

            mWebView.loadUrl("javascript:generateBoard('" + bot.getAddress() + "'," + commandList + "," + valueList + ")");
        }
    }

    void displayCode(final int index){
        UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!codeList.get(index).contains("%blarg%")) {
                    String code = mGeneratedCode.getText() + codeList.get(index);

                    UIManager.setCurrentCommand("");
                    //UIManager.setCurrentIndex(-1);
                    UIManager.setCurrentBot("");

                    mGeneratedCode.setText(code);
                    UIManager.setRecievedResponse(true);
                }else{
                    String[] separated = codeList.get(index).split("\\(");
                    UIManager.setCurrentCommand(separated[0]);
                    UIManager.setCurrentIndex(index);
                    Log.d("MYTAG", "currentCommand: " + UIManager.getCurrentCommand());
                    //check for current command in callback with the index
                    //if it receives it send it back through display code from callback and set currentcommand to ""
                    //if runcommandlist starts next command and currentcommand hasn't been set to "" display error
                }
            }
        });
    }

    public void setError(final String command, final String botAddress, final String botName){
        UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String newCode = codeList.get(UIManager.getCurrentIndex());

                Log.d("MYTAG", "ERROR: " + command);
                if(newCode.contains(UIManager.getCurrentCommand()) && newCode.contains("%blarg%")) {
                    switch (command) {
                        case Global.kD1_TOUCHING:
                        case "d1Touching":
                        case Global.kA1_TOUCHING:
                        case "a1Touching":
                        case Global.kD2_TOUCHING:
                        case "d2Touching":
                        case Global.kA2_TOUCHING:
                        case "a2Touching":
                        case Global.kD3_TOUCHING:
                        case "d3Touching":
                        case Global.kA3_TOUCHING:
                        case "a3Touching":
                        case Global.kD4_TOUCHING:
                        case "d4Touching":
                        case Global.kA4_TOUCHING:
                        case "a4Touching":
                            newCode = mGeneratedCode.getText() + newCode.replaceFirst("%blarg%", "false");
                            break;
                        default:
                            //codeView.setText("No value returned, check your circuit and config.");
                            newCode = mGeneratedCode.getText() + "\nNo value returned, check your circuit and config.";
                            break;
                    }

                    mGeneratedCode.setText(newCode);

                    UIManager.setCurrentBot("");
                    UIManager.setCurrentCommand("");
                    //UIManager.setCurrentIndex(-1);

                    //textView.setTag("");
                }
            }
            //todo if two of the same touch commands are in, if the first one is true second one is automatically true also
        });
    }

    void removeNull(ArrayList<MPCClass> typeList){

        boolean removed = false;

        for(MPCClass mpcClass : typeList) {

            if (mpcClass.getType() == (null)) {
                typeList.remove(mpcClass);
                removed = true;
                break;
            } else if (mpcClass.getType().equals("null")) {
                typeList.remove(mpcClass);
                removed = true;
                break;
            } else if (mpcClass.getType().equals("empty")) {
                typeList.remove(mpcClass);
                removed = true;
                break;
            } else if (mpcClass.getType().equals("")) {
                typeList.remove(mpcClass);
                removed = true;
                break;
            }
        }

        if(removed){
            removeNull(typeList);
        }

    }

    void hideList(){
        mpcComponentList.setVisibility(View.GONE);
        simpleExpandableListViewAnalog.setVisibility(View.GONE);
        simpleExpandableListViewDigital.setVisibility(View.GONE);
        collapseAll();
    }

    //method to collapse all groups
    private void collapseAll() {
        int count = listAdapterDigital.getGroupCount();
        for (int i = 0; i < count; i++){
            simpleExpandableListViewDigital.collapseGroup(i);
        }
        int countAnalog = listAdapterAnalog.getGroupCount();
        for (int i = 0; i < countAnalog; i++){
            simpleExpandableListViewAnalog.collapseGroup(i);
        }
    }

    //load some initial data into out list
    private void loadData(boolean useAnalog){

        addProduct("General Digital","LED", R.drawable.mpc_led_full, getResources().getString(R.string.basicDescripton), "Digital", useAnalog);
        addProduct("General Digital","Speaker", R.drawable.mpc_speaker_full, getResources().getString(R.string.basicDescripton), "Digital", useAnalog);

        if(useAnalog) {
            addProduct("General Analog", "Photoresistor", R.drawable.mpc_photoresistor_full, getResources().getString(R.string.basicDescripton), "Analog", useAnalog);
        }

        addProduct("Touch", "Instant", R.drawable.mpc_touch_full, getResources().getString(R.string.basicDescripton), "Digital", useAnalog);
        addProduct("Touch", "Delayed", R.drawable.mpc_touch_full, getResources().getString(R.string.basicDescripton), "Digital", useAnalog);

        addProduct("Temp and Humidity", "Temp", R.drawable.mpc_temperature_full, getResources().getString(R.string.basicDescripton), "Digital", useAnalog);
        //addProduct("Temp and Humidity", "Humidity", R.drawable.purple_circuit_icon_256, getResources().getString(R.string.basicDescripton));

        addProduct("Nothing", null, 0, "", "None", useAnalog);
        addProduct("Close", null, 0, "", "None", useAnalog);

    }

    //here we maintain our products in various departments
    private int addProduct(String department, String product, int image, String description, String mode, boolean useAnalog){

        int groupPosition = 0;

        //check the hash map if the group already exists
        GroupInfo headerInfo;
        if(useAnalog) {
            headerInfo = subjectsAnalog.get(department);
        }else{
            headerInfo = subjectsDigital.get(department);
        }
        //add the group if doesn't exists
        if(headerInfo == null){
            headerInfo = new GroupInfo();
            headerInfo.setName(department);
            if(useAnalog) {
                subjectsAnalog.put(department, headerInfo);
                deptListAnalog.add(headerInfo);
            }else{
                subjectsDigital.put(department, headerInfo);
                deptListDigital.add(headerInfo);
            }
        }

        //get the children for the group
        ArrayList<ChildInfo> productList = headerInfo.getProductList();
        //size of the children list
        int listSize = productList.size();
        //add to the counter
        listSize++;

        if(product != null) {
            //create a new child and add that to the group
            ChildInfo detailInfo = new ChildInfo();
            detailInfo.setType(department);
            detailInfo.setName(product);
            detailInfo.setImage(image);
            detailInfo.setDescription(description);
            detailInfo.setMode(mode);
            productList.add(detailInfo);
            headerInfo.setProductList(productList);
        }

        //find the group position inside the list
        if(useAnalog) {
            groupPosition = deptListAnalog.indexOf(headerInfo);
        }else{
            groupPosition = deptListDigital.indexOf(headerInfo);
        }
        return groupPosition;
    }

    String snapStudChanging = "";

    void showList(String snapStud){
        snapStudChanging = snapStud;
        Log.d("MYTAG", "showList: " + snapStudChanging);
        if(snapStud.contains("D")){
            simpleExpandableListViewAnalog.setVisibility(View.GONE);
            simpleExpandableListViewDigital.setVisibility(View.VISIBLE);
        }else{
            simpleExpandableListViewDigital.setVisibility(View.GONE);
            simpleExpandableListViewAnalog.setVisibility(View.VISIBLE);
        }
        mpcComponentList.setVisibility(View.VISIBLE);

    }

    void setComponent(String type){
        String pioCommand = "000T0";
        String pioType = null;
        String pioSubType = type;
        String mode = "None";

        Log.d("MYTAG", "type: " + type);
        switch (type){
            case "LED":
            case "Speaker":
                pioCommand = "100D0";
                pioType = "General Digital";
                mode = "Digital";
                break;
            case "Photoresistor":
                pioCommand = "110A1";
                pioType = "General Analog";
                mode = "Analog";
                break;
            case "Instant":
                pioCommand = "110T0";
                pioType = "Touch";
                mode = "Digital";
                break;
            case "Delayed":
                pioCommand = "110T1";
                pioType = "Touch";
                mode = "Digital";
                break;
            case "Temp":
            case "Humidity":
                pioCommand = "110E1";
                pioType = "Temp and Humidity";
                mode = "Digital";
                break;
            case "Not Assigned":
            default:
                pioCommand = "000T0";
                pioType = "";
                pioSubType = "";
                type = "Not Assigned";
                mode = "None";
                break;
        }

        switch (snapStudChanging){
            case "D1":
                d1String = "A" + pioCommand;
                d1Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setD1Type(pioType);
                    bot.setD1SubType(pioSubType);
                }

                d1ConfigText.setText(type);

                if(mode.equals("Analog")){
                    d1ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    d1ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    d1ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
            case "A1":
                a1String = "B" + pioCommand;
                a1Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setA1Type(pioType);
                    bot.setA1SubType(pioSubType);
                }

                a1ConfigText.setText(type);

                if(mode.equals("Analog")){
                    a1ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    a1ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    a1ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
            case "D2":
                d2String = "C" + pioCommand;
                d2Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setD2Type(pioType);
                    bot.setD2SubType(pioSubType);
                }

                d2ConfigText.setText(type);

                if(mode.equals("Analog")){
                    d2ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    d2ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    d2ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
            case "A2":
                a2String = "D" + pioCommand;
                a2Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setA2Type(pioType);
                    bot.setA2SubType(pioSubType);
                }

                a2ConfigText.setText(type);

                if(mode.equals("Analog")){
                    a2ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    a2ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    a2ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
            case "D3":
                d3String = "E" + pioCommand;
                d3Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setD3Type(pioType);
                    bot.setD3SubType(pioSubType);
                }

                d3ConfigText.setText(type);

                if(mode.equals("Analog")){
                    d3ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    d3ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    d3ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
            case "A3":
                a3String = "F" + pioCommand;
                a3Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setA3Type(pioType);
                    bot.setA3SubType(pioSubType);
                }

                a3ConfigText.setText(type);

                if(mode.equals("Analog")){
                    a3ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    a3ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    a3ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
            case "D4":
                d4String = "G" + pioCommand;
                d4Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setD4Type(pioType);
                    bot.setD4SubType(pioSubType);
                }

                d4ConfigText.setText(type);

                if(mode.equals("Analog")){
                    d4ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    d4ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    d4ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
            case "A4":
                a4String = "H" + pioCommand;
                a4Type = pioType;
                for(Bot bot : BotManager.getConnectedMPCs()){
                    bot.setA4Type(pioType);
                    bot.setA4SubType(pioSubType);
                }

                a4ConfigText.setText(type);

                if(mode.equals("Analog")){
                    a4ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(mode.equals("Digital")){
                    a4ConfigStudImage.setImageResource(R.drawable.stud_green);
                }else{
                    a4ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }
                break;
        }

        String config1 = "<C" + d1String + a1String;
        String config2 = d2String + a2String;
        String config3 = d3String + a3String;
        String config4 = d4String + a4String + ">";

        for(Bot bot : BotManager.getConnectedMPCs()){
            bot.configure(config1,config2,config3,config4);
        }

        //reset toolbox
        loadBots();
        checkCode();
        hideList();
    }

    void openConfigure(){
        LinearLayout reconfigLayout = view.findViewById(R.id.blocklyReconfigureLayout);

        if(configurable) {
            if (configureIsOpen) {
                reconfigLayout.setVisibility(View.GONE);
                configureIsOpen = false;
            } else {
                reconfigLayout.setVisibility(View.VISIBLE);
                configureIsOpen = true;
            }
        }else{
            reconfigLayout.setVisibility(View.GONE);
        }
    }

    void assignMPC(){
        //if mpc has already been assigned reassign layout here
        if(BotManager.getConnectedMPCs().size() > 0 && BotManager.getConnectedMPCs().get(0) != null){
            Bot bot = BotManager.getConnectedMPCs().get(0);

            if(bot.getD1SubType() != null && bot.getD1SubType() != "") {
                if(bot.getD1Type().contains("Analog")){
                    d1ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getD1Type().equals("Not Assigned")){
                    d1ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    d1ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                d1ConfigText.setText(bot.getD1SubType());
            }else{
                d1ConfigText.setText("Not Assigned");
                d1ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
            if(bot.getA1SubType() != null && bot.getA1SubType() != "") {
                if(bot.getA1Type().contains("Analog")){
                    a1ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getA1Type().equals("Not Assigned")){
                    a1ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    a1ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                a1ConfigText.setText(bot.getA1SubType());
            }else{
                a1ConfigText.setText("Not Assigned");
                a1ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
            if(bot.getD2SubType() != null && bot.getD2SubType() != "") {
                if(bot.getD2Type().contains("Analog")){
                    d2ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getD2Type().equals("Not Assigned")){
                    d2ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    d2ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                d2ConfigText.setText(bot.getD2SubType());
            }else{
                d2ConfigText.setText("Not Assigned");
                d2ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
            if(bot.getA2SubType() != null && bot.getA2SubType() != "") {
                if(bot.getA2Type().contains("Analog")){
                    a2ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getA2Type().equals("Not Assigned")){
                    a2ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    a2ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                a2ConfigText.setText(bot.getA2SubType());
            }else{
                a2ConfigText.setText("Not Assigned");
                a2ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
            if(bot.getD3SubType() != null && bot.getD3SubType() != "") {
                if(bot.getD3Type().contains("Analog")){
                    d3ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getD3Type().equals("Not Assigned")){
                    d3ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    d3ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                d3ConfigText.setText(bot.getD3SubType());
            }else{
                d3ConfigText.setText("Not Assigned");
                d3ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
            if(bot.getA3SubType() != null && bot.getA3SubType() != "") {
                if(bot.getA3Type().contains("Analog")){
                    a3ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getA3Type().equals("Not Assigned")){
                    a3ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    a3ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                a3ConfigText.setText(bot.getA3SubType());
            }else{
                a3ConfigText.setText("Not Assigned");
                a3ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
            if(bot.getD4SubType() != null && bot.getD4SubType() != "") {
                if(bot.getD4Type().contains("Analog")){
                    d4ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getD4Type().equals("Not Assigned")){
                    d4ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    d4ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                d4ConfigText.setText(bot.getD4SubType());
            }else{
                d4ConfigText.setText("Not Assigned");
                d4ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
            if(bot.getA4SubType() != null && bot.getA4SubType() != "") {
                if(bot.getA4Type().contains("Analog")){
                    a4ConfigStudImage.setImageResource(R.drawable.stud_purple);
                }else if(bot.getA4Type().equals("Not Assigned")){
                    a4ConfigStudImage.setImageResource(R.drawable.stud_grey);
                }else{
                    a4ConfigStudImage.setImageResource(R.drawable.stud_green);
                }
                a4ConfigText.setText(bot.getA4SubType());
            }else{
                a4ConfigText.setText("Not Assigned");
                a4ConfigStudImage.setImageResource(R.drawable.stud_grey);
            }
        }
    }

    void checkCode(){
        mWebView.loadUrl("javascript:checkCode()");
    }
}
