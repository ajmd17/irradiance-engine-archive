/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.multiplayer;

import com.badlogic.gdx.math.Vector3;
import irradianceclient.ClientHandler;
import irradianceclient.ClientList;
import irradianceclient.IRClient;
import irradianceserver.info.PlayerInfo;
import irradianceserver.messages.Message;
import irradianceserver.util.Util;

/**
 *
 * @author Andrew
 */
public class MyClientHandler extends ClientHandler {

    @Override
    public void handle(Message msg) {
        if (msg.getType().equals(Message.MessageTypes.FETCH_ID)) {
            IRClient.id = msg.get(1);
            client.init();
            System.out.println("set id to: " + IRClient.id);
        } else {
            if (msg.getType().equals(Message.MessageTypes.PLAYER_SPAWN)) {
                String id = msg.get(1);
                String screenName = msg.get(2);
                float x_pos = Float.parseFloat(msg.get(3));
                float y_pos = Float.parseFloat(msg.get(4));
                float z_pos = Float.parseFloat(msg.get(5));
                PlayerInfo pi = new PlayerInfo(id, screenName, new Vector3(x_pos, y_pos, z_pos));
                ClientList.players.add(pi);
                System.out.println("spawned player: " + screenName);
            } else if (msg.getType().equals(Message.MessageTypes.PLAYER_MOVE)) {
                String id = msg.get(1);
                float x_pos = Float.parseFloat(msg.get(3));
                float y_pos = Float.parseFloat(msg.get(4));
                float z_pos = Float.parseFloat(msg.get(5));
                PlayerInfo p = Util.playerWithID(ClientList.players, id);
                if (p != null) {
                    p.getPosition().set(new Vector3(x_pos, y_pos, z_pos));
                }
            }
        }
    }
}
