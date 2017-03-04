package com.maksim88.easylogin.listener;

import com.maksim88.easylogin.networks.SocialNetwork;

/**
 * Created by maksim on 14.02.16.
 */
public interface NetworkListener {

    void onError(SocialNetwork.Network socialNetwork, String requestID, String errorMessage, Object data);
}
