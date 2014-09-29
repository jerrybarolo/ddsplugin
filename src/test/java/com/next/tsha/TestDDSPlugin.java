package com.next.tsha;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.next.idlcode.Track_3D;
import com.next.idlcode.Track_3DDataReader;
import com.next.idlcode.Track_3DDataWriter;
import com.next.idlcode.Track_3DTypeSupport;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.DataReaderQos;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.topic.DDSPlugin;
import com.rti.dds.topic.Topic;
import com.rti.dds.topic.TopicQos;
import common.PluginConfiguration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by agerardi on 25/09/2014.
 */
public class TestDDSPlugin extends DataReaderAdapter {
    private Track_3D trkReceived = null;
    private Track_3D trkToSend = null;

    @Test
    public void shouldPropagateDDSDataToEventBus() {
        //setup
        Track_3DDataWriter sdw = createDataWriter();

        DDSPlugin ddsPlugin = new DDSPlugin();
        EventBus eventBus = new EventBus();

        PluginConfiguration conf = new PluginConfiguration(null, null, eventBus);
        ddsPlugin.configure(conf);

        eventBus.register(this);

        ddsPlugin.start();

        //testWait(1000);
        //exercise
        Track_3D trk = new Track_3D();
        trk.trk_id = 1;
        trk.height = 10;
        trk.latitude = 10.0f;
        trk.longitude = 20.0f;
        trk.trk_name = "Nome Traccia";
        sdw.write(trk, InstanceHandle_t.HANDLE_NIL);

        testWait(1000);
        //verify
        System.out.println("Assert");
        Assert.assertEquals(trk, trkReceived);
    }

    @Test
    public void shouldReceiveDDSDataFromEventBus() {
        //setup
        Track_3DDataReader sdw = createDataReader();

        DDSPlugin ddsPlugin = new DDSPlugin();
        EventBus eventBus = new EventBus();

        PluginConfiguration conf = new PluginConfiguration(null, null, eventBus);
        ddsPlugin.configure(conf);

        ddsPlugin.start();

        //testWait(1000);

        //Exercise
        Track_3D trk = new Track_3D();
        trk.trk_id = 1;
        trk.height = 10;
        trk.latitude = 10.0f;
        trk.longitude = 20.0f;
        trk.trk_name = "Nome Traccia";

        eventBus.post(trk);

        testWait(1000);
        //verify
        //System.out.println("Assert");
        Assert.assertEquals(trk, trkReceived);

    }

    private void testWait(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
        }
    }

    @Subscribe
    public void receiveTrack(Track_3D str) {
        System.out.println("Event Bus receiveTrack");
        this.trkReceived = str;
    }

    private Track_3DDataWriter createDataWriter() {
        DomainParticipant participant = DomainParticipantFactory.get_instance().create_participant(
                0, // Domain ID = 0
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (participant == null) {
            System.err.println("Unable to create domain participant");
            return null;
        }

        Track_3DTypeSupport.register_type(
                participant,
                Track_3DTypeSupport.get_type_name());

        TopicQos qos = new TopicQos();
        participant.get_default_topic_qos(qos);
        qos.durability.kind = DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS;
        qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

        // Create the topic "Hello World" for the String type
        Topic topic = participant.create_topic(
                "Track3D_TopicToPlugin",
                Track_3DTypeSupport.get_type_name(),
                qos,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (topic == null) {
            System.err.println("Unable to create topic.");
            return null;
        }
        TopicQos qos2 = new TopicQos();
        topic.get_qos(qos2);


        // Create the data writer using the default publisher
        DataWriterQos Tqos = new DataWriterQos();
        participant.get_default_datawriter_qos(Tqos);
        Tqos.durability.kind = DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS;
        Tqos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        Tqos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

        Track_3DDataWriter dataWriter =
                (Track_3DDataWriter) participant.create_datawriter(
                        topic,
                        Tqos,
                        null, // listener
                        StatusKind.STATUS_MASK_NONE);
        if (dataWriter == null) {
            System.err.println("Unable to create data writer\n");
            return null;
        }
        DataWriterQos Tqos2 = new DataWriterQos();
        dataWriter.get_qos(Tqos2);

        return dataWriter;
    }

    private Track_3DDataReader createDataReader() {
        DomainParticipant participant = DomainParticipantFactory.get_instance().create_participant(
                0, // Domain ID = 0
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (participant == null) {
            System.err.println("Unable to create domain participant");
            return null;
        }

        Track_3DTypeSupport.register_type(
                participant,
                Track_3DTypeSupport.get_type_name());

        TopicQos Tqos = new TopicQos();
        participant.get_default_topic_qos(Tqos);
        Tqos.durability.kind = DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS;
        Tqos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        Tqos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

        // Create the topic "Hello World" for the String type
        Topic topic = participant.create_topic(
                "Track3D_TopicFromPlugin",
                Track_3DTypeSupport.get_type_name(),
                Tqos,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (topic == null) {
            System.err.println("Unable to create topic.");
            return null;
        }

        // Create the data writer using the default publisher
        DataReaderQos qos = new DataReaderQos();
        participant.get_default_datareader_qos(qos);
        qos.durability.kind = DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS;
        qos.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        qos.history.kind = HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;

        // Create the data reader using the default subscriber
        Track_3DDataReader dataReader =
                (Track_3DDataReader) participant.create_datareader(
                        topic,
                        qos,
                        this, // Listener
                        StatusKind.DATA_AVAILABLE_STATUS);
        if (dataReader == null) {
            System.err.println("Unable to create data writer\n");
            return null;
        }

        return dataReader;
    }

    public void on_data_available(DataReader reader) {
        Track_3DDataReader stringReader = (Track_3DDataReader) reader;
        SampleInfo info = new SampleInfo();
        Track_3D data = new Track_3D();
        for (; ; ) {
            try {
                stringReader.take_next_sample(data, info);
                if (info.valid_data) {
                    this.trkReceived = data;
                    ;
                }
            } catch (RETCODE_NO_DATA noData) {
                // No more data to read
                break;
            } catch (RETCODE_ERROR e) {
                // An error occurred
                e.printStackTrace();
            }
        }
    }

}
