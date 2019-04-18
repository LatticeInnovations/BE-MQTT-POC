package boston.mqtt.modules.run;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import boston.mqtt.config.MqttUtil;
import boston.mqtt.model.RunResponseProto.RunResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public final class RunService {

	private RunService() {
	}

	public static void generateRunId(long clientId) {

		RunPublishUtil.mqttPublishRunId(MqttUtil.mqttAsyncClient,
				RunResponse.newBuilder().setRunId("R11-001").build().toByteArray(), clientId);
	}
}
