package boston.mqtt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;

import boston.mqtt.model.LedModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventHandler {

	private static SocketIOServer server;

	@Autowired
	public EventHandler(SocketIOServer server) {
		EventHandler.server = server;
	}

	public static void onEvent(String data) {
		server.getBroadcastOperations().sendEvent("iot/led/1", data);
	}

	public static void onEvent1(LedModel ledModel) {
		server.getBroadcastOperations().sendEvent("iot/led/2", ledModel);
	}

	@OnConnect
	public void onConnect(SocketIOClient client) {
		log.info("client connected: " + client.hashCode());
	}

	@OnDisconnect
	public void onDisconnect(SocketIOClient client) {
		log.warn("client disconnected: " + client.hashCode());
	}

}
