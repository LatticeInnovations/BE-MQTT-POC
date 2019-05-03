package boston.mqtt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;

import boston.mqtt.config.MqttUtil;

@Component
public class SrvRunner implements CommandLineRunner {

    private final SocketIOServer server;

    @Autowired
    public SrvRunner(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args) throws Exception {
    	
    	MqttUtil mqttUtil = new MqttUtil();
		mqttUtil.mqttConnectAndSubscribe("subscriber");
        
        server.start();
        Thread.sleep(Integer.MAX_VALUE);
        server.stop();
        
        
    }

    
}
