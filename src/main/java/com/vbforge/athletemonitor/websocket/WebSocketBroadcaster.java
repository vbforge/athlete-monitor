package com.vbforge.athletemonitor.websocket;

import com.vbforge.athletemonitor.service.ActiveSessionService;
import com.vbforge.athletemonitor.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerStateService    playerStateService;
    private final ActiveSessionService  sessionService;

    @Scheduled(fixedRateString = "${app.websocket.broadcast-interval-ms}")
    public void broadcast() {
        if (!sessionService.isActive()) return;

        Map<String, ?> snapshot = playerStateService.getAllPlayers();
        if (snapshot.isEmpty()) return;

        messagingTemplate.convertAndSend("/topic/all-metrics", snapshot);
    }
}