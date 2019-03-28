package boston.mqtt.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "led_details_2")
public class LedModel2 {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "led_id")
	private long id;
	@Column(name = "counter")
	private int counter;
	@Column(name = "client_id")
	private String clientId;

	public LedModel2(int counter, String clientId) {
		super();
		this.counter = counter;
		this.clientId = clientId;
	}

}