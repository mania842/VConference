package com.example.vconference.ui.core;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;

public interface ChatManager {

    void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void release() throws XMPPException;
    
	void leaveChat(QBDialog dialog, QBEntityCallbackImpl qbCallbackImpl);
}
