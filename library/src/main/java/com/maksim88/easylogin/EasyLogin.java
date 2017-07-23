package com.maksim88.easylogin;

import android.content.Intent;

import com.maksim88.easylogin.networks.SocialNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maksim on 14.02.16.
 */
public class EasyLogin {

    private static EasyLogin instance = null;

    private Map<SocialNetwork.Network, SocialNetwork> socialNetworksMap = new HashMap<>();

    private EasyLogin() {
    }

    public static void initialize() {
        if (instance == null) {
            instance = new EasyLogin();
        }
    }

    public static EasyLogin getInstance() {
        return instance;
    }


    public void addSocialNetwork(SocialNetwork socialNetwork) {
        if (socialNetworksMap.get(socialNetwork.getNetwork()) != null) {
            throw new RuntimeException("Social network with id = " + socialNetwork.getNetwork() + " already exists");
        }

        socialNetworksMap.put(socialNetwork.getNetwork(), socialNetwork);
    }

    public SocialNetwork getSocialNetwork(SocialNetwork.Network network) throws RuntimeException {
        if (!socialNetworksMap.containsKey(network)) {
            throw new RuntimeException("Social network " + network + " not found");
        }
        return socialNetworksMap.get(network);
    }

    /**
     * Get list of initialized social networks
     * @return list of initialized social networks
     */
    public List<SocialNetwork> getInitializedSocialNetworks() {
        return Collections.unmodifiableList(new ArrayList<>(socialNetworksMap.values()));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (SocialNetwork network : socialNetworksMap.values()) {
            network.onActivityResult(requestCode, resultCode, data);
        }
    }
}
