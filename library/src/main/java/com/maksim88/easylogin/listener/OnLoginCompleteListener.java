package com.maksim88.easylogin.listener;

import com.maksim88.easylogin.networks.SocialNetwork;

/**
 * Created by maksim on 14.02.16.
 */
public interface OnLoginCompleteListener extends NetworkListener {
    /**
     * Called when login complete.
     * @param network id of social network where request was complete
     */
    void onLoginSuccess(SocialNetwork.Network network);
}
