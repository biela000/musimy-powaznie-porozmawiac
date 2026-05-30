package com.jtjmpm.api.controller;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.api.model.SpellRegistry;
import com.jtjmpm.messages.AvailableSpellsMessage;
import com.jtjmpm.messages.WsMessage;
import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpellController implements MessageController {

    private final SpellRegistry spellRegistry;
    private final Gson gson;

    public SpellController(SpellRegistry spellRegistry, Gson gson) {
        this.spellRegistry = spellRegistry;
        this.gson = gson;
    }

    @Override
    public List<MessageType> supportedTypes() {
        return List.of(
                MessageType.GET_SPELLS_LIST
        );
    }

    @Override
    public void handle(WebSocket conn, String rawJson) {
        WsMessage message = gson.fromJson(rawJson, WsMessage.class);

        switch (message.type) {
            case MessageType.GET_SPELLS_LIST -> handleGetSpellsList(conn, rawJson);
        }
    }

    private void handleGetSpellsList(WebSocket conn, String rawJson) {
        System.out.println("Sending spells list to: " + conn.getRemoteSocketAddress());

        AvailableSpellsMessage response = new AvailableSpellsMessage(
                spellRegistry.getAllSpellsAsDTO()
        );

        conn.send(gson.toJson(response));
    }
}