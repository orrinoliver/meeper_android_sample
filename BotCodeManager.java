package com.meepertek.meeperbots.BotCode;

import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.meepertek.meeperbots.AutoResizeTextView;
import com.meepertek.meeperbots.BotManager;
import com.meepertek.meeperbots.Classes.ControllerClass;
import com.meepertek.meeperbots.Global;
import com.meepertek.meeperbots.R;
import com.meepertek.meeperbots.SQLite.data.model.Bots;
import com.meepertek.meeperbots.SQLite.data.repo.BotsRepo;
import com.meepertek.meeperbots.TronView;
import com.meepertek.meeperbots.UIManager;
import com.meepertek.meeperbots.model.Bot;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Home Office on 8/31/2016.
 */
public class BotCodeManager extends Fragment
{
    static TronView tv;
    static ArrayList<ArrayList<Bot>> listOfAffectedBotsLists = new ArrayList<>();
    private static ArrayList<BotCommand> commandsToBeExecuted = new ArrayList();

    public static boolean programIsExecuting = false;
    public static boolean keepLooping = false;
    
    static LinearLayout uiCommandList;

    public static int vBotBlueCurrentX, vBotBlueCurrentY, vBotGreenCurrentX, vBotGreenCurrentY,vBotOrangeCurrentX, vBotOrangeCurrentY, vBotYellowCurrentX, vBotYellowCurrentY;
    public static float vBotBlueBearing, vBotGreenBearing, vBotOrangeBearing, vBotYellowBearing;

    public static void saveToRoom(){
        uiCommandList = (LinearLayout) UIManager.getDrivingScreen().findViewById(R.id.codeBay2);
        if(uiCommandList != null && uiCommandList.getChildCount() > 3) {
            ArrayList<BotCommand> commandArrayList = setUpCommandListCodeManager();
            if(UIManager.getIsActive()) {
                botCodeRunUser(commandArrayList);
            }
            BotCodeViewFragment botCodeView = new BotCodeViewFragment();
            botCodeView.createCode();

            final LinearLayout codeViewBay = (LinearLayout) UIManager.getDrivingScreen().findViewById(R.id.codeViewBay);
            if(codeViewBay != null && codeViewBay.getChildCount() != 0) {
                commentCount = 0;
                uniqueCount = 0;
                alreadyUsedList.clear();
                turnVisible(codeViewBay, codeViewBay.getChildCount(), true);
            }
        }
    }

    public static void RunProgram(){

        tv = (TronView) UIManager.getDrivingScreen().findViewById(R.id.tronViewCodeBay);
        uiCommandList = (LinearLayout) UIManager.getDrivingScreen().findViewById(R.id.codeBay2);
        ImageButton stopProgramButton = (ImageButton) UIManager.getDrivingScreen().findViewById(R.id.stopProgramButton);

        if(uiCommandList != null && uiCommandList.getChildCount() > 3) {
            BotCodeViewFragment botCodeView = new BotCodeViewFragment();
            stopProgramButton.setVisibility(View.VISIBLE);
            programIsExecuting = true;
            runCommands();
        }
    }

    private static BotCommand cloneBotCommand(BotCommand botCommand){
        BotCommand cloneCmd= new BotCommand();
        cloneCmd.setCommand(botCommand.getCommand());
        cloneCmd.setDuration(botCommand.getDuration());
        cloneCmd.setIterations(botCommand.getIterations());
        cloneCmd.setBotList(botCommand.getBotList());
        cloneCmd.setListOrdinal(botCommand.getListOrdinal());
        cloneCmd.setSpeed(botCommand.getSetSpeed());
        cloneCmd.setCommandType(botCommand.getCommandType());
        return cloneCmd;
    }
    private static void compile(ArrayList<BotCommand> commandList)
    {
        LoopStack loopStack = new LoopStack();

        commandsToBeExecuted = new ArrayList();
        int indexCounter = 0;

        //loop through commands
        //non-loop and non-conditional commands will be added as is
        //loops and conditionals will be converted as necessary
        for (int i = 0; i < commandList.size(); i++)
        {
            if (!(commandList.get(i).getCommand() == Global.kBEGIN_LOOP) && !(commandList.get(i).getCommand() == Global.kEND_LOOP)) {
                BotCommand cloneCommand = cloneBotCommand(commandList.get(i));
                commandsToBeExecuted.add(cloneCommand);
            }
            else if (commandList.get(i).getCommand() == Global.kBEGIN_LOOP || commandList.get(i).getCommand() == Global.kEND_LOOP)
            {
                //this is where we process loops into a list of normally executable commands
                //first we need to get the entire loop block
                //we do this by searching for the location of the the corresponding block
                //we can determine the location of the end loop using a stack
                //loop through the remaining commands and add any loop commands to the stack
                //remove a loop command from the stack when an end loop is encountered
                //full loop block has been found when stack is empty
                //this method ensures that nested loops are ignored

                ArrayList<BotCommand> loopBlock = new ArrayList();

                BotCommand cloneCommand = cloneBotCommand(commandList.get(i));
                loopBlock.add(cloneCommand);
                loopStack.addLoopToStack(commandList.get(i));
                indexCounter = i+1;

                Log.d("LoopInfo", "the loop block");
                Log.d("LoopInfo", "index of beginning loop command: "+indexCounter);
                while(!(loopStack.isEmpty()))
                {
                    if(commandList.size() > indexCounter) {
                        BotCommand loopChildCommand = cloneBotCommand(commandList.get(indexCounter));
                        loopBlock.add(loopChildCommand);
                        if (loopChildCommand.getCommand() == Global.kBEGIN_LOOP) {
                            loopStack.addLoopToStack(loopChildCommand);
                        } else if (loopChildCommand.getCommand() == Global.kEND_LOOP) {
                            loopStack.removeLoopFromStack();
                        }
                        indexCounter++;
                    }
                }
                for(int j = 0; j < loopBlock.size(); j++)
                {
                    Log.d("LoopInfo", loopBlock.get(j).getCommand() + " LOOPBLOCK GET COMMAND");
                }

                loopBlock = convertLoopCommands(loopBlock);
                commandsToBeExecuted.addAll(loopBlock);

                i = indexCounter-1;
            }
        }
        for (BotCommand command : commandsToBeExecuted) {
            Log.d("LooperInfo", command + " : " + command.getCommand());
        }
    }

    private static ArrayList convertLoopCommands(ArrayList<BotCommand> loopBlock)
    {
        if(doesLoopBlockContainNestedLoop(loopBlock))
        {
            ArrayList<BotCommand> nestedLoop = getNestedLoopBlock(loopBlock);
            Log.d("LoopInfo", "the nested loop block");
            for(int i = 0; i < nestedLoop.size(); i++)
            {
                Log.d("LoopInfo", nestedLoop.get(i).getCommand());
            }
            ArrayList<BotCommand> expandedNestedLoop = convertLoopCommands(nestedLoop);
            int indexOfNestedLoopCommand = getIndexOfFirstNestedLoop(loopBlock);
            loopBlock.removeAll(nestedLoop);

            Log.d("LoopInfo", "loop block with removed nested loop");
            for(int i = 0; i < loopBlock.size(); i++)
            {
                Log.d("LoopInfo", loopBlock.get(i).getCommand());
            }
            loopBlock.addAll(indexOfNestedLoopCommand, expandedNestedLoop);

            Log.d("LoopInfo", "the spliced parent loop");
            for(int i = 0; i < loopBlock.size(); i++)
            {
                Log.d("LoopInfo", loopBlock.get(i).getCommand());
            }
            return convertLoopCommands(loopBlock);
        }
        else
        {
            BotCommand loopCommand = (BotCommand) loopBlock.get(0);
            int loopCounter = loopCommand.getIterations();
            ArrayList<BotCommand> expandedCommandList = new ArrayList();
            for(int i = 0; i < loopCounter; i++)
            {
                for(int j = 1; j < loopBlock.size()-1; j++)
                {
                    expandedCommandList.add(loopBlock.get(j));
                }
            }

            Log.d("LoopInfo", "the expanded loop List");
            for(int i =0; i < expandedCommandList.size(); i++)
            {
                Log.d("LoopInfo", expandedCommandList.get(i).getCommand());
            }

            return expandedCommandList;
        }
    }

    private static ArrayList<BotCommand> getNestedLoopBlock(ArrayList<BotCommand> loopBlock)
    {
        LoopStack loopStack = new LoopStack();
        ArrayList<BotCommand> nestedLoop = new ArrayList();

        for(int i = getIndexOfFirstNestedLoop(loopBlock); i < loopBlock.size(); i++)
        {
            BotCommand command = loopBlock.get(i);
            nestedLoop.add(command);
            if(command.getCommand() == Global.kBEGIN_LOOP)
            {
                loopStack.addLoopToStack(command);
            }
            else if(command.getCommand() == Global.kEND_LOOP)
            {
                loopStack.removeLoopFromStack();
            }

            if(loopStack.isEmpty())
            {
                return nestedLoop;
            }
        }
        return nestedLoop;
    }
    private static int getIndexOfFirstNestedLoop(ArrayList<BotCommand>loopBlock)
    {
        for(int i = 1; i < loopBlock.size(); i++)
        {
            BotCommand command = loopBlock.get(i);
            if(command.getCommand() == Global.kBEGIN_LOOP)
            {
                return i;
            }
        }
        return -1;
    }

    private static boolean doesLoopBlockContainNestedLoop(ArrayList<BotCommand> loopBlock)
    {
        for(int i = 1; i < loopBlock.size(); i++)
        {
            BotCommand command = loopBlock.get(i);
            if(command.getCommand() == Global.kBEGIN_LOOP)
            {
                return true;
            }
        }
        return false;
    }

    private static class LoopStack
    {
        private ArrayList<BotCommand> loops = new ArrayList();

        public void addLoopToStack(BotCommand botCommand)
        {
            botCommand.setCommand(Global.kBEGIN_LOOP);
            loops.add(0, botCommand);
        }

        public BotCommand removeLoopFromStack()
        {
            return loops.remove(0);
        }

        public boolean isEmpty()
        {
            if(loops.size() == 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    private static void runCommands(){

        vBotBlueCurrentX = 0;
        vBotBlueCurrentY = 0;
        vBotGreenCurrentX = 0;
        vBotGreenCurrentY = 0;
        vBotOrangeCurrentX = 0;
        vBotOrangeCurrentY = 0;
        vBotYellowCurrentX = 0;
        vBotYellowCurrentY = 0;

        vBotBlueBearing = 0;
        vBotGreenBearing = 0;
        vBotOrangeBearing = 0;
        vBotYellowBearing = 0;

        if(uiCommandList.getChildCount() > 3){
            setUpCommandListCodeManager();
        }

        resetTronView();
        commentCount = 0;
        uniqueCount = 0;
        alreadyUsedList.clear();

        if(checkBotAssignment()){
            if(commandsToBeExecuted.size() > 0){
                BotCodeViewFragment botCodeView = new BotCodeViewFragment();
                botCodeView.createCode();
                runCommandList();
            }else{
                stopExecutionTimer();
            }
        }else{
            AlertDialog.Builder botSelectionCheckDialog = new AlertDialog.Builder(UIManager.getDrivingScreen());
            botSelectionCheckDialog.setTitle("No Meeper(s) Found");
            botSelectionCheckDialog.setMessage("The Meeper for this code was not found.  Please reassign each command to a Meeper.");
            botSelectionCheckDialog.create();
            botSelectionCheckDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    stopExecutionTimer();
                }
            });
            botSelectionCheckDialog.show();
        }
    }

    private static void resetTronView(){
        //reset tron view
        UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.reset();
            }
        });
        //setup tron view
        ArrayList<Bot> drawBots = new ArrayList<>();

        listOfAffectedBotsLists = bot_code_fragment.getListOfAffectedBotsLists();

        for(ArrayList<Bot> botList: listOfAffectedBotsLists)
        {
            drawBots.addAll(botList);
        }

        tv.setupBots(drawBots);

    }

    private static void setRowHighlight(final LinearLayout uiCommandList, final BotCommand botCommand) {

        final LinearLayout codeViewBay = (LinearLayout) UIManager.getDrivingScreen().findViewById(R.id.codeViewBay);

        //clear all rows
        UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int n = 2; n < uiCommandList.getChildCount() - 1; n++) {
                    ((LinearLayout)uiCommandList.getChildAt(n)).getChildAt(1).setBackgroundResource(R.drawable.code_normal_normal);
                }
            }
        });

        //highlight current row
        UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(uiCommandList.getChildAt((int) botCommand.getListOrdinal()) != null) {
                    ((LinearLayout)uiCommandList.getChildAt((int) botCommand.getListOrdinal())).getChildAt(1).setBackgroundResource(R.drawable.code_normal_normal_highlighted);
                    if(codeViewBay != null && !alreadyUsedList.contains(botCommand.getListOrdinal())) {
                        alreadyUsedList.add(botCommand.getListOrdinal());
                        turnVisible(codeViewBay, uniqueCount + commentCount, false);
                        uniqueCount++;
                    }
                }
            }
        });

    }

    static int uniqueCount = 0;
    static ArrayList<Long> alreadyUsedList = new ArrayList();
    static int commentCount = 0;

    static void turnVisible(LinearLayout codeViewBay, int index, boolean endedEarly){
        if(!endedEarly) {
            Log.d("MYTAG", "Code tag: " + codeViewBay.getChildAt(index).getTag());
            if (codeViewBay.getChildCount() > index && codeViewBay.getChildAt(index).getTag() != null && (codeViewBay.getChildAt(index).getTag().toString().equals("Comment") || codeViewBay.getChildAt(index).getTag().toString().equals("Loop"))) {
                if(codeViewBay.getChildAt(index).getTag() == "") {
                    codeViewBay.getChildAt(index).setVisibility(View.VISIBLE);
                }
                turnVisible(codeViewBay, index + 1, false);
                commentCount++;
            }else if(codeViewBay.getChildCount() > index){
                if(index - 1 > 0 && codeViewBay.getChildAt(index - 1).getVisibility() == View.INVISIBLE){
                    turnVisible(codeViewBay, index - 1, false);
                }
                if(codeViewBay.getChildAt(index).getTag() == "") {
                    codeViewBay.getChildAt(index).setVisibility(View.VISIBLE);
                }
            }
        }else{
            /*if(index > 0) {
            codeViewBay.getChildAt(index - 1).setVisibility(View.VISIBLE);
            turnVisible(codeViewBay, index - 1, true);
            }*/
        }
    }

    static Timer exTimer = new Timer();
    private static void clearCommandListHighlights(final LinearLayout uiCommandList, int scheduleDelay)
    {
        exTimer.cancel();
        exTimer.purge();
        exTimer = new Timer();
        
        exTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                UIManager.getDrivingScreen().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //clear highlight of row
                        if(uiCommandList != null) {
                            for (int i = 2; i < uiCommandList.getChildCount() - 1; i++) {
                                ((LinearLayout) uiCommandList.getChildAt(i)).getChildAt(1).setBackgroundResource(R.drawable.code_normal_normal);
                            }
                        }
                        ImageButton stopProgramButton = (ImageButton) UIManager.getDrivingScreen().findViewById(R.id.stopProgramButton);
                        if(stopProgramButton != null) {
                            stopProgramButton.setVisibility(View.GONE);
                        }

                        final LinearLayout codeViewBay = (LinearLayout) UIManager.getDrivingScreen().findViewById(R.id.codeViewBay);
                        if(codeViewBay != null && codeViewBay.getChildCount() != 0) {
                            commentCount = 0;
                            uniqueCount = 0;
                            alreadyUsedList.clear();
                            turnVisible(codeViewBay, codeViewBay.getChildCount(), true);
                        }

                        for(Bot bot : BotManager.getConnectedCircuits()){
                            bot.stopMotorCircuit("clear", true);
                        }

                        for(Bot bot : BotManager.getConnectedMPCs()){
                            bot.stopMotorCircuit("clear", true);
                        }

                        UIManager.setCurrentCommand("");
                        UIManager.setCurrentBot("");

                        if(keepLooping){
                            RunProgram();
                        }else {
                            programIsExecuting = false;
                        }
                    }
                });

            }
        }, scheduleDelay);
    }

    static ArrayList<BotCommand> setUpCommandListCodeManager() {
        ArrayList<BotCommand> commandArrayList = new ArrayList<>();

        commandArrayList.clear();

        if(uiCommandList == null) {
            uiCommandList = (LinearLayout) UIManager.getDrivingScreen().findViewById(R.id.codeBay2);
        }

        for (int i = 2; i < uiCommandList.getChildCount() - 1; i++) {
            LinearLayout commandBlock = (LinearLayout) ((LinearLayout)uiCommandList.getChildAt(i)).getChildAt(1);

            //FrameLayout textHolder = (FrameLayout) commandBlock.getChildAt(2);
            AutoResizeTextView durationTextView = commandBlock.findViewById(R.id.botTimerText); //textHolder.getChildAt(0);

            double durationDouble = 10;
            if(durationTextView.getText().toString().contains(DecimalFormatSymbols.getInstance().getInfinity())){
                durationDouble = 98.98;
            }else{
                durationDouble = Double.parseDouble(durationTextView.getText() + "");
            }
            int duration = (int) (durationDouble * 100);

            ImageView speedImage = (ImageView) commandBlock.findViewById(R.id.speedImage);

            String speedChosen = "Fast";

            if(commandBlock.getId() != R.id.loopButton && commandBlock.getId() != R.id.botCodeEndLoop) {
                if(speedImage != null) {
                    if (speedImage.getDrawable().getConstantState() == ContextCompat.getDrawable(UIManager.getDrivingScreen(), R.drawable.speed3).getConstantState() ||
                            speedImage.getDrawable().getConstantState() == ContextCompat.getDrawable(UIManager.getDrivingScreen(), R.drawable.circuith_highlighted).getConstantState()) {
                        speedChosen = "Fast";
                    } else if (speedImage.getDrawable().getConstantState() == ContextCompat.getDrawable(UIManager.getDrivingScreen(), R.drawable.speed2).getConstantState()) {
                        speedChosen = "Medium";
                    } else if (speedImage.getDrawable().getConstantState() == ContextCompat.getDrawable(UIManager.getDrivingScreen(), R.drawable.speed1).getConstantState() ||
                            speedImage.getDrawable().getConstantState() == ContextCompat.getDrawable(UIManager.getDrivingScreen(), R.drawable.circuitl_highlighted).getConstantState()) {
                        speedChosen = "Slow";
                    }
                }else{
                    speedChosen = "Fast";
                }
            }

            String comment = "";
            EditText commentCode = commandBlock.findViewById(R.id.commentText);
            if(!commentCode.getText().equals("") && !commentCode.getText().equals(null)){
                comment = commentCode.getText().toString();
            }


            switch (commandBlock.getId()) {
                case R.id.moveForward:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kFORWARD,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.moveBackwards:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kREVERSE,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.moveLeft:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kLEFT,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.moveRight:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kRIGHT,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.hardTurnLeft:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kFAST_LEFT,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.hardTurnRight:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kFAST_RIGHT,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.loopButton:
                    int iterations = duration/100;
                    commandArrayList.add(makeBotCommand(duration,iterations,i,Global.kBEGIN_LOOP,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.botCodeEndLoop:
                    commandArrayList.add(makeBotCommand(0,0,i,Global.kEND_LOOP,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftLeft90:
                    commandArrayList.add(makeBotCommand(250,1,i,Global.kSPIN_SOFT_LEFT_90,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftLeft180:
                    commandArrayList.add(makeBotCommand(500,1,i,Global.kSPIN_SOFT_LEFT_180,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftLeft270:
                    commandArrayList.add(makeBotCommand(750,1,i,Global.kSPIN_SOFT_LEFT_270,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftLeft360:
                    commandArrayList.add(makeBotCommand(1000,1,i,Global.kSPIN_SOFT_LEFT_360,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftRight90:
                    commandArrayList.add(makeBotCommand(250,1,i,Global.kSPIN_SOFT_RIGHT_90,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftRight180:
                    commandArrayList.add(makeBotCommand(500,1,i,Global.kSPIN_SOFT_RIGHT_180,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftRight270:
                    commandArrayList.add(makeBotCommand(750,1,i,Global.kSPIN_SOFT_RIGHT_270,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinSoftRight360:
                    commandArrayList.add(makeBotCommand(1000,1,i,Global.kSPIN_SOFT_RIGHT_360,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardLeft90:
                    commandArrayList.add(makeBotCommand(250,1,i,Global.kSPIN_HARD_LEFT_90,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardLeft180:
                    commandArrayList.add(makeBotCommand(500,1,i,Global.kSPIN_HARD_LEFT_180,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardLeft270:
                    commandArrayList.add(makeBotCommand(750,1,i,Global.kSPIN_HARD_LEFT_270,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardLeft360:
                    commandArrayList.add(makeBotCommand(1000,1,i,Global.kSPIN_HARD_LEFT_360,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardRight90:
                    commandArrayList.add(makeBotCommand(250,1,i,Global.kSPIN_HARD_RIGHT_90,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardRight180:
                    commandArrayList.add(makeBotCommand(500,1,i,Global.kSPIN_HARD_RIGHT_180,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardRight270:
                    commandArrayList.add(makeBotCommand(750,1,i,Global.kSPIN_HARD_RIGHT_270,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.spinHardRight360:
                    commandArrayList.add(makeBotCommand(1000,1,i,Global.kSPIN_HARD_RIGHT_360,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.waitButton:
                    commandArrayList.add(makeBotCommand(duration,1,i,"WAIT",Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.moveLeftReverse:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kREVERSE_LEFT,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.moveRightReverse:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kREVERSE_RIGHT,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.moveLeftForward:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kFORWARD_LEFT,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.moveRightForward:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kFORWARD_RIGHT,Global.kDIRECTION_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.customAngleLeftTurn:
                    commandArrayList.add(makeBotCommand(duration * 10,1,i,Global.kCUSTOM_LEFT_TURN_ANGLE,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.customAngleRightTurn:
                    commandArrayList.add(makeBotCommand(duration * 10,1,i,Global.kCUSTOM_RIGHT_TURN_ANGLE,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.customAngleLeftSpin:
                    commandArrayList.add(makeBotCommand(duration * 10,1,i,Global.kCUSTOM_LEFT_SPIN_ANGLE,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.customAngleRightSpin:
                    commandArrayList.add(makeBotCommand(duration * 10,1,i,Global.kCUSTOM_RIGHT_SPIN_ANGLE,Global.kTURN_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.ifButton:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kBEGIN_IF,Global.kSTRUCTURE_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.endIfButton:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kEND_IF,Global.kSTRUCTURE_COMMAND,false, speedChosen, comment));
                    break;
                case R.id.motorLeftForward:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kMOTOR_LEFT_FORWARD, Global.kMOTOR_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.motorLeftReverse:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kMOTOR_LEFT_REVERSE, Global.kMOTOR_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.motorRightForward:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kMOTOR_RIGHT_FORWARD, Global.kMOTOR_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.motorRightReverse:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kMOTOR_RIGHT_REVERSE, Global.kMOTOR_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.circuitD1On:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kCIRCUIT_D1_ON, Global.kCIRCUIT_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.circuitD2On:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kCIRCUIT_D2_ON, Global.kCIRCUIT_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.circuitD3On:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kCIRCUIT_D3_ON, Global.kCIRCUIT_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.circuitD4On:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kCIRCUIT_D4_ON, Global.kCIRCUIT_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.singleCircuitOn:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kSINGLE_CIRUIT_ON, Global.kCIRCUIT_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_led:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_LED, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_speaker:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_SPEAKER, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_count:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_COUNT, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_reset:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_RESET, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_touching:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_TOUCHING, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_photoresistor:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_PHOTORESISTOR, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_temperature:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_TEMP, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d1_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD1_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a1_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA1_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d2_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD2_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a2_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA2_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d3_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD3_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a3_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA3_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.d4_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kD4_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
                case R.id.a4_humidity:
                    commandArrayList.add(makeBotCommand(duration,1,i,Global.kA4_HUMID, Global.kMPC_COMMAND, false, speedChosen, comment));
                    break;
            }

        }

        compile(commandArrayList);

        return  commandArrayList;
    }

    private static BotCommand makeBotCommand (int duration, int iterations, int ordinal, String command, String commandType, boolean loopCommand, String speedChosen, String comment)
    {
        BotCommand botCommand = new BotCommand();

        botCommand.setCommand(command);
        botCommand.setDuration(duration);
        botCommand.setIterations(iterations);
        botCommand.setCommandType(commandType);
        botCommand.setSpeed(speedChosen);
        botCommand.setComment(comment);

        if(command.equals(Global.kBEGIN_LOOP)) {
            botCommand.setBeginLoop(true);
        }
        else {
            botCommand.setBeginLoop(false);
        }

        if(command.equals(Global.kEND_LOOP)) {
            botCommand.setEndLoop(true);
        }
        else {
            botCommand.setEndLoop(false);
        }

        botCommand.setLoopCommand(loopCommand);
        botCommand.setListOrdinal(ordinal);

        listOfAffectedBotsLists = bot_code_fragment.getListOfAffectedBotsLists();

        if(listOfAffectedBotsLists.size() > ordinal - 2 && !listOfAffectedBotsLists.get(ordinal - 2).isEmpty())
        {
            botCommand.setBotList(listOfAffectedBotsLists.get(ordinal - 2));
        }

        return botCommand;
    }

    private static boolean checkBotAssignment() {

        //iterate through all commands, check that non-loop commands have bots assigned
        for(BotCommand botCommand: commandsToBeExecuted)
        {
            if(!botCommand.getCommand().equals(Global.kBEGIN_LOOP) && !botCommand.getCommand().equals(Global.kEND_LOOP)
                    && !botCommand.getCommand().equals(Global.kBEGIN_IF) && !botCommand.getCommand().equals(Global.kEND_IF))
            {
                if(botCommand.getBotList() == null)
                {
                    return false;
                }
            }
        }

        return true;
    }

    static boolean skip = false;

    static Timer executionTimer = new Timer();
    private static void runCommandList() {
        int totalDuration = 0;
        int index = 0;
        skip = false;

        executionTimer = new Timer();

        for (final BotCommand botCommandFinal : commandsToBeExecuted) {
            final int finalIndex = index;

            //Timer exTimer = new Timer();
            executionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //highlight current row
                    setRowHighlight(uiCommandList, botCommandFinal);

                    if(botCommandFinal.getCommand().equals(Global.kBEGIN_IF)){
                        //test here to see if == true
                        boolean allSkipped = true;
                        for (Bot bot : botCommandFinal.getBotList()) {
                            if(bot.getExecuteCommand()) {
                                boolean trueStatement = false;
                                bot.setRSSI(0);
                                bot.checkRSSI();

                                while (bot.getRSSI() >= 0) {
                                    Log.d("MYTAG", "ZERO");
                                }

                                if (bot.getRSSI() < -30 && bot.getRSSI() > -75) {
                                    trueStatement = true;
                                } else if (bot.getRSSI() <= -75) {
                                    trueStatement = false;
                                }

                                if (!trueStatement) {//change to !trueStatement
                                    skipCode((int) botCommandFinal.getListOrdinal() - 2, bot);
                                }else{
                                    allSkipped = false;
                                }
                            }
                        }
                        if(allSkipped){
                            skip = true;
                        }
                    }

                    if(botCommandFinal.getCommand().equals(Global.kEND_IF)){
                        boolean allSkipped = true;
                        for (Bot bot: botCommandFinal.getBotList()) {
                            if(!bot.getExecuteCommand()){
                                if(botCommandFinal.getListOrdinal() - 2 == bot.getEndIfIndex()){
                                    bot.setExecuteCommand(true);
                                    allSkipped = false;
                                }
                            }
                        }
                        if(allSkipped){
                            skip = true;
                        }else{
                            skip = false;
                        }
                    }

                    if(botCommandFinal.getCommand() != Global.kEND_LOOP && botCommandFinal.getCommand() != Global.kBEGIN_LOOP && botCommandFinal.getCommand() != null
                            && botCommandFinal.getCommand() != Global.kEND_IF && botCommandFinal.getCommand() != Global.kELSE /*&& !skip*/){
                        ArrayList<Bot> tempList = botCommandFinal.getBotList();

                        for (Bot bot: tempList) {
                            if(bot.getIsVirtual() && !bot.getIsSharedBot()){
                                switch (bot.getBotFrameColor()){
                                    case "Blue":
                                        if(vBotBlueCurrentX != 0 || vBotBlueCurrentY != 0) {
                                            bot.setCurrentX(vBotBlueCurrentX);
                                            bot.setCurrentY(vBotBlueCurrentY);
                                            bot.setBearing(vBotBlueBearing);
                                        }
                                        break;
                                    case "Green":
                                        if(vBotGreenCurrentX != 0 || vBotGreenCurrentY != 0) {
                                            bot.setCurrentX(vBotGreenCurrentX);
                                            bot.setCurrentY(vBotGreenCurrentY);
                                            bot.setBearing(vBotGreenBearing);
                                        }
                                        break;
                                    case "Orange":
                                        if(vBotOrangeCurrentX != 0 || vBotOrangeCurrentY != 0) {
                                            bot.setCurrentX(vBotOrangeCurrentX);
                                            bot.setCurrentY(vBotOrangeCurrentY);
                                            bot.setBearing(vBotOrangeBearing);
                                        }
                                        break;
                                    case "Yellow":
                                        if(vBotYellowCurrentX != 0 || vBotYellowCurrentY != 0) {
                                            bot.setCurrentX(vBotYellowCurrentX);
                                            bot.setCurrentY(vBotYellowCurrentY);
                                            bot.setBearing(vBotYellowBearing);
                                        }
                                        break;
                                }
                            }
                        }

                        if(botCommandFinal.getCommandType() != Global.kMOTOR_COMMAND && botCommandFinal.getCommandType() != Global.kCIRCUIT_COMMAND) {
                            for (Bot bot : botCommandFinal.getBotList()) {
                                if (!bot.getIsVirtual()) {
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
                        }

                        if(botCommandFinal.getCommandType() == Global.kMPC_COMMAND) {
                            UIManager.setRecievedResponse(false);
                            if(!botCommandFinal.getBotList().get(0).getIsSharedBot()) {
                                UIManager.setCurrentBot(botCommandFinal.getBotList().get(0).getAddress());
                            }else{
                                UIManager.setCurrentBot(botCommandFinal.getBotList().get(0).getSharedAddress());
                            }
                            setCurrentCommand(botCommandFinal.getCommand());
                            Log.d("MYTAG", "START?");
                        }
                        botCommandFinal.executeBotCommand();
                    }
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
                /*float spin = 0;
                for (Bot bot : botCommandFinal.getBotList()) {
                    if(bot.getIsVirtual()){
                        spin = .25f;
                    }else {
                        BotsRepo botsRepo = new BotsRepo();
                        List<Bots> botsList = botsRepo.GetBot(bot.getAddress());

                        if(botCommandFinal.getCommand().contains("Spin")){
                            if (botCommandFinal.getCommand().contains("Left")) {
                                if (botsList.size() > 0) {
                                    if (botsList.get(0).getLeftTurnHard360() > spin) {
                                        spin = botsList.get(0).getLeftTurnHard360();
                                    }
                                }
                            } else if (botCommandFinal.getCommand().contains("Right")) {
                                if (botsList.size() > 0) {
                                    if (botsList.get(0).getRightTurnHard360() > spin) {
                                        spin = botsList.get(0).getRightTurnHard360();
                                    }
                                }
                            }
                        }else {
                            if (botCommandFinal.getCommand().contains("Left")) {
                                if (botsList.size() > 0) {
                                    if (botsList.get(0).getLeftTurnSoft360() > spin) {
                                        spin = botsList.get(0).getLeftTurnSoft360();
                                    }
                                }
                            } else if (botCommandFinal.getCommand().contains("Right")) {
                                if (botsList.size() > 0) {
                                    if (botsList.get(0).getRightTurnSoft360() > spin) {
                                        spin = botsList.get(0).getRightTurnSoft360();
                                    }
                                }
                            }
                        }
                    }
                }

                int duration = (int) ((spin / 1000) * ((float)botCommandFinal.getDuration() / 360));
                totalDuration = totalDuration + duration + 500;*/

            }else{
                //everything has a delay
                /*if(botCommandFinal.getDuration() == 9898){
                    totalDuration = totalDuration + 1000;
                }else if(botCommandFinal.getDuration() == 0){
                   totalDuration = totalDuration + 500;
                } else{
                    totalDuration = totalDuration + botCommandFinal.getDuration() + 500;
                }*/
                //todo circuits have no delay
               if(botCommandFinal.getCommandType() != Global.kCIRCUIT_COMMAND && botCommandFinal.getCommandType() != Global.kMPC_COMMAND){
                   totalDuration = totalDuration + botCommandFinal.getDuration() + 500;
               }else{
                   if(botCommandFinal.getDuration() == 9898){
                       totalDuration = totalDuration + 100;
                   }else if(botCommandFinal.getDuration() == 0){
                       totalDuration = totalDuration + 100;
                   } else{
                       totalDuration = totalDuration + botCommandFinal.getDuration() + 100;
                   }
               }
            }

            index++;

        }

        //cleanup any remaining highlights
        clearCommandListHighlights(uiCommandList, totalDuration);

    }

    public static void stopExecutionTimer(){
        executionTimer.cancel();
        executionTimer.purge();
        exTimer.cancel();
        exTimer.purge();
        exTimer = new Timer();
        keepLooping = false;

        UIManager.setCurrentBot("");
        UIManager.setCurrentCommand("");

        executionTimer = new Timer();
        BotManager.stopBots();

        clearCommandListHighlights(uiCommandList, 100);

        ImageButton stopProgramButton = (ImageButton) UIManager.getDrivingScreen().findViewById(R.id.stopProgramButton);
        stopProgramButton.setVisibility(View.GONE);
        programIsExecuting = false;


    }

    static void skipCode(int startIndex, Bot bot){

        int ifCounter = 0;
        int endIfCounter = 0;

        //skip = true;

        for(int i = startIndex; i < commandsToBeExecuted.size(); i++){
            Log.d("MYTAG", "startIndex: " + startIndex + ", command: " + commandsToBeExecuted.get(i).getCommand());
            if(commandsToBeExecuted.get(i).getCommand() == Global.kBEGIN_IF){
                ifCounter++;
            }else if(commandsToBeExecuted.get(i).getCommand() == Global.kEND_IF){
                endIfCounter++;
                if(ifCounter == endIfCounter){
                    bot.setEndIfIndex(i);
                    bot.setExecuteCommand(false);
                    break;
                }
            }
        }
    }

    static void botCodeRunUser(ArrayList<BotCommand> commandList){
        Log.d("MYTAG", commandList.size() + " " + commandList);
        String cloudString = "";
        for(int i = 0; i < commandList.size(); i++){
            //~meep~command~@~duration~@~speed~@~iterations~@~commandOrder~@~commandType~@~comment
            cloudString += "~meep~" + commandList.get(i).getCommand() + "~@~" + commandList.get(i).getDuration() + "~@~"
                    + commandList.get(i).getSetSpeed() + "~@~" + commandList.get(i).getIterations() + "~@~" + i + "~@~"
                    + commandList.get(i).getCommandType() + "~@~";
            if(commandList.get(i).getComment() == null || commandList.get(i).getComment().equals("")){
                cloudString += "~null~~@~";
            }else{
                cloudString += commandList.get(i).getComment() +  "~@~";
            }

            ArrayList<String> sharedList = new ArrayList<>();
            String sharedBot = "";
            if(commandList.get(i).getBotList() != null){

            for(Bot bot : commandList.get(i).getBotList()){
                if(bot.getIsSharedBot()){
                    //cloudString += "~@~" + commandList.get(i).getBotList();
                    sharedBot +=  bot.getSharedAddress() +"~meepShared~";
                    sharedList.add(sharedBot);
                }
            }
        }
            cloudString += sharedBot;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) UIManager.getDrivingScreen().getSystemService(UIManager.getDrivingScreen().CONNECTIVITY_SERVICE);
        if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) ||
                (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            //we are connected to a network

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("ControllerV2");

            String id = myRef.push().getKey();

            ControllerClass botCodeDB = new ControllerClass(id, cloudString, UIManager.getRoomCode(), java.text.DateFormat.getDateTimeInstance().format(new Date()), "botCode");
            myRef.child(UIManager.getRoomCode()).child("codeInfo").setValue(botCodeDB);

            Toast.makeText(UIManager.getDrivingScreen().getApplicationContext(), ("Your code has been sent to the Control Room."), Toast.LENGTH_LONG).show();
        }
    }

    static void setCurrentCommand(String command){
        Log.d("MYTAG", "command?: " + command);
        switch (command){
            case Global.kD1_LED:
            case Global.kA1_LED:
            case Global.kD2_LED:
            case Global.kA2_LED:
            case Global.kD3_LED:
            case Global.kA3_LED:
            case Global.kD4_LED:
            case Global.kA4_LED:
            case Global.kD1_SPEAKER:
            case Global.kA1_SPEAKER:
            case Global.kD2_SPEAKER:
            case Global.kA2_SPEAKER:
            case Global.kD3_SPEAKER:
            case Global.kA3_SPEAKER:
            case Global.kD4_SPEAKER:
            case Global.kA4_SPEAKER:
            case Global.kD1_RESET:
            case Global.kA1_RESET:
            case Global.kD2_RESET:
            case Global.kA2_RESET:
            case Global.kD3_RESET:
            case Global.kA3_RESET:
            case Global.kD4_RESET:
            case Global.kA4_RESET:
                UIManager.setCurrentCommand("");
                break;
            case Global.kD1_COUNT:
                UIManager.setCurrentCommand("RAC");
                break;
            case Global.kA1_COUNT:
                UIManager.setCurrentCommand("RBC");
                break;
            case Global.kD2_COUNT:
                UIManager.setCurrentCommand("RCC");
                break;
            case Global.kA2_COUNT:
                UIManager.setCurrentCommand("RDC");
                break;
            case Global.kD3_COUNT:
                UIManager.setCurrentCommand("REC");
                break;
            case Global.kA3_COUNT:
                UIManager.setCurrentCommand("RFC");
                break;
            case Global.kD4_COUNT:
                UIManager.setCurrentCommand("RGC");
                break;
            case Global.kA4_COUNT:
                UIManager.setCurrentCommand("RHC");
                break;
            case Global.kD1_TOUCHING:
                UIManager.setCurrentCommand("instantAT");
                break;
            case Global.kA1_TOUCHING:
                UIManager.setCurrentCommand("instantBT");
                break;
            case Global.kD2_TOUCHING:
                UIManager.setCurrentCommand("instantCT");
                break;
            case Global.kA2_TOUCHING:
                UIManager.setCurrentCommand("instantDT");
                break;
            case Global.kD3_TOUCHING:
                UIManager.setCurrentCommand("instantET");
                break;
            case Global.kA3_TOUCHING:
                UIManager.setCurrentCommand("instantFT");
                break;
            case Global.kD4_TOUCHING:
                UIManager.setCurrentCommand("instantGT");
                break;
            case Global.kA4_TOUCHING:
                UIManager.setCurrentCommand("instantHT");
                break;
            case Global.kD1_PHOTORESISTOR:
                UIManager.setCurrentCommand("RAA");
                break;
            case Global.kA1_PHOTORESISTOR:
                UIManager.setCurrentCommand("RBA");
                break;
            case Global.kD2_PHOTORESISTOR:
                UIManager.setCurrentCommand("RCA");
                break;
            case Global.kA2_PHOTORESISTOR:
                UIManager.setCurrentCommand("RDA");
                break;
            case Global.kD3_PHOTORESISTOR:
                UIManager.setCurrentCommand("REA");
                break;
            case Global.kA3_PHOTORESISTOR:
                UIManager.setCurrentCommand("RFA");
                break;
            case Global.kD4_PHOTORESISTOR:
                UIManager.setCurrentCommand("RGA");
                break;
            case Global.kA4_PHOTORESISTOR:
                UIManager.setCurrentCommand("RHA");
                break;
            case Global.kD1_TEMP:
                UIManager.setCurrentCommand("RAT");
                //commandPrefix = "%temp%";
                break;
            case Global.kA1_TEMP:
                UIManager.setCurrentCommand("RBT");
                //commandPrefix = "%temp%";
                break;
            case Global.kD2_TEMP:
                UIManager.setCurrentCommand("RCT");
                //commandPrefix = "%temp%";
                break;
            case Global.kA2_TEMP:
                UIManager.setCurrentCommand("RDT");
                //commandPrefix = "%temp%";
                break;
            case Global.kD3_TEMP:
                UIManager.setCurrentCommand("RET");
                //commandPrefix = "%temp%";
                break;
            case Global.kA3_TEMP:
                UIManager.setCurrentCommand("RFT");
                //commandPrefix = "%temp%";
                break;
            case Global.kD4_TEMP:
                UIManager.setCurrentCommand("RGT");
                //commandPrefix = "%temp%";
                break;
            case Global.kA4_TEMP:
                UIManager.setCurrentCommand("RHT");
                //commandPrefix = "%temp%";
                break;
            case Global.kD1_HUMID:
                UIManager.setCurrentCommand("RAT");
                //commandPrefix = "%humid%";
                break;
            case Global.kA1_HUMID:
                UIManager.setCurrentCommand("RBT");
                //commandPrefix = "%humid%";
                break;
            case Global.kD2_HUMID:
                UIManager.setCurrentCommand("RCT");
                //commandPrefix = "%humid%";
                break;
            case Global.kA2_HUMID:
                UIManager.setCurrentCommand("RDT");
                //commandPrefix = "%humid%";
                break;
            case Global.kD3_HUMID:
                UIManager.setCurrentCommand("RET");
                //commandPrefix = "%humid%";
                break;
            case Global.kA3_HUMID:
                UIManager.setCurrentCommand("RFT");
                //commandPrefix = "%humid%";
                break;
            case Global.kD4_HUMID:
                UIManager.setCurrentCommand("RGT");
                //commandPrefix = "%humid%";
                break;
            case Global.kA4_HUMID:
                UIManager.setCurrentCommand("RHT");
                //commandPrefix = "%humid%";
                break;
        }
    }
}


