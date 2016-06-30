package com.gemtek.modulecloud;

import com.cloudAgent.CloudAgentCommand;

public interface ReceivingCommandCallback {
    public void onReceivingCommand(String peerId, CloudAgentCommand cloudAgentCommand);
}

