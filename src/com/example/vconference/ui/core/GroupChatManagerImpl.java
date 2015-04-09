package com.example.vconference.ui.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.vconference.ui.ChatActivity;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListenerImpl;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;

public class GroupChatManagerImpl extends QBMessageListenerImpl<QBGroupChat> implements ChatManager {
    private static final String TAG = "GroupChatManagerImpl";

    private ChatActivity chatActivity;

    private QBGroupChatManager groupChatManager;
    private QBGroupChat groupChat;

    public GroupChatManagerImpl(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
        groupChatManager = QBChatService.getInstance().getGroupChatManager();
    }
    
    public void inviteUser(QBDialog dialog, Integer userId, QBEntityCallback callback) {
    	QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
    	requestBuilder.push("occupants_ids", userId); // add another users
    	// requestBuilder.pullAll("occupants_ids", userId); // Remove yourself (user with ID 22)
    	 
    	groupChatManager.updateDialog(dialog, requestBuilder, callback);
    }
    
    public static QBChatMessage createChatNotificationForGroupChatUpdate(QBDialog dialog) {
        String dialogId = String.valueOf(dialog.getDialogId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = TextUtils.join(",", dialog.getOccupants());
        String dialogName = dialog.getName();
        String dialogTypeCode = String.valueOf(dialog.getType().ordinal());
     
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody("optional text");
     
        // Add notification_type=2 to extra params when you updated a group chat 
        //
        chatMessage.setProperty("notification_type", "2");
     
        chatMessage.setProperty("_id", dialogId);
        if (!TextUtils.isEmpty(roomJid)) {
            chatMessage.setProperty("room_jid", roomJid);
        }
        chatMessage.setProperty("occupants_ids", occupantsIds);
        if (!TextUtils.isEmpty(dialogName)) {
            chatMessage.setProperty("name", dialogName);
        }
        chatMessage.setProperty("type", dialogTypeCode);
     
        return chatMessage;
    }


    public void joinGroupChat(QBDialog dialog, QBEntityCallback callback){
        groupChat = groupChatManager.createGroupChat(dialog.getRoomJid());
        join(groupChat, callback);
    }
    
    public Collection<Integer> getOnlineUsers() {
    	try {
			 return groupChat.getOnlineUsers();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public Collection<Integer> getRoomUsers() {
    	try {
			return groupChat.getRoomUserIds();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    	return null;
    }

    private void join(final QBGroupChat groupChat, final QBEntityCallback callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        groupChat.join(history, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {

                groupChat.addMessageListener(GroupChatManagerImpl.this);

                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();

                        Toast.makeText(chatActivity, "Join successful", Toast.LENGTH_LONG).show();
                    }
                });
                Log.w("Chat", "Join successful");
            }

            @Override
            public void onError(final List list) {
                chatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(list);
                    }
                });


                Log.w("Could not join chat, errors:", Arrays.toString(list.toArray()));
            }
        });
    }

    @Override
    public void release() throws XMPPException {
        if (groupChat != null) {
            try {
                groupChat.leave();
            } catch (SmackException.NotConnectedException nce){
                nce.printStackTrace();
            }

            groupChat.removeMessageListener(this);
        }
    }

    @Override
    public void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException {
        if (groupChat != null) {
            try {
                groupChat.sendMessage(message);
            } catch (SmackException.NotConnectedException nce){
                nce.printStackTrace();
            } catch (IllegalStateException e){
                e.printStackTrace();

                Toast.makeText(chatActivity, "You are still joining a group chat, please white a bit", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(chatActivity, "Join unsuccessful", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void processMessage(QBGroupChat groupChat, QBChatMessage chatMessage) {
        // Show message
        Log.w(TAG, "new incoming message: " + chatMessage);
        chatActivity.showMessage(chatMessage);
    }

    @Override
    public void processError(QBGroupChat groupChat, QBChatException error, QBChatMessage originMessage){

    }

	@Override
	public void processMessageDelivered(QBGroupChat sender, String messageID) {
		// TODO Auto-generated method stub
		super.processMessageDelivered(sender, messageID);
	}

	@Override
	public void processMessageRead(QBGroupChat sender, String messageID) {
		// TODO Auto-generated method stub
		super.processMessageRead(sender, messageID);
	}
    
    
}
