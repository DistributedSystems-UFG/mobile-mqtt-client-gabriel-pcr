package com.example.basicandroidmqttclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.basicandroidmqttclient.MESSAGE";
    public static final String brokerURI = "54.243.87.127";

    Activity thisActivity;
    TextView subMsgTextView;

    private ListView listViewSubscribedMessages;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> subscribedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;
        thisActivity = this;
        listViewSubscribedMessages = findViewById(R.id.listViewSubscribedMessages);

        subscribedMessages = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subscribedMessages);
        listViewSubscribedMessages.setAdapter(adapter);
    }

    /**
     * Called when the user taps the Send button
     */
    public void publishMessage(View view) {
        EditText topicName = (EditText) findViewById(R.id.editTextTopicName);
        EditText value = (EditText) findViewById(R.id.editTextValue);

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();
        client.publishWith()
                .topic(topicName.getText().toString())
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(value.getText().toString().getBytes())
                .send();
        client.disconnect();
    }

    public void sendSubscription(View view) {
        EditText topicName = findViewById(R.id.editTextTopicNameSub);
        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        client.toAsync().subscribeWith()
                .topicFilter(topicName.getText().toString())
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    String receivedMessage = new String(msg.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    thisActivity.runOnUiThread(() -> {
                        subscribedMessages.add(msg.getTopic() + ": " + receivedMessage);
                        adapter.notifyDataSetChanged();
                    });
                })
                .send();
    }
}