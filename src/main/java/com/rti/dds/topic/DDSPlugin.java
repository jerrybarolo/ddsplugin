package com.rti.dds.topic;

import com.google.common.eventbus.Subscribe;
import com.next.idlcode.Track_3D;
import com.next.idlcode.Track_3DDataReader;
import com.next.idlcode.Track_3DDataWriter;
import com.next.idlcode.Track_3DTypeSupport;
import com.next.tsha.Track3D_Listener;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import common.APlugin;

import java.util.HashMap;

/**
 * Created by agerardi on 25/09/2014.
 */
public class DDSPlugin extends APlugin {

    private Track_3DDataReader m_3Ddr;
    private Track_3DDataWriter m_3Ddw;
    private HashMap<String, Class> topics;
    private DomainParticipant participant;
    private Track_3D trkReceived;

    public DDSPlugin() {
        /* To load my_custom_qos_profiles.xml, as explained above, we need
             * to modify the DDSTheParticipantFactory Profile QoSPolicy */
        DomainParticipantFactoryQos factoryQos =
                new DomainParticipantFactoryQos();
        DomainParticipantFactory.get_instance().get_qos(factoryQos);

        	/* We are only going to add one XML file to the url_profile
             * sequence, so we set a maximum length of 1. */
        factoryQos.profile.url_profile.setMaximum(1);

        	/* The XML file will be loaded from the working directory. That
        	 * means, you need to run the example like this:
             * ./objs/<architecture>/profiles_publisher
             * (see README.txt for more information on how to run the example).
             *
             * Note that you can specify the absolute path of the XML QoS file
             * to avoid this problem.
             */
        boolean loaded = factoryQos.profile
                .url_profile
                .add("file://Custom_qos.xml");

        DomainParticipantFactory.get_instance().set_qos(factoryQos);

        // Create the DDS Domain participant on domain ID 0
        participant = DomainParticipantFactory.get_instance().create_participant(
                0, // Domain ID = 0
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null, // listener
                StatusKind.STATUS_MASK_NONE);

        Track_3DTypeSupport.register_type(
                participant,
                Track_3DTypeSupport.get_type_name());
    }

    private void createTrack_3D_DataReaderWriter() throws Exception {

        // Create the topic "Track3D_Topic" for the Track3D type
        Topic topicReader = participant.create_topic_with_profile(
                "Track3D_TopicToPlugin",
                Track_3DTypeSupport.get_type_name(),
                "Track3D_QosLibrary",
                "Track3D_Profile",
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (topicReader == null) {
            throw new Exception("Unable to create topic Reader.");
        }

        // Create Datawriter using xml QoS file
        m_3Ddr = (Track_3DDataReader) participant.create_datareader_with_profile(
                topicReader, "Track3D_QosLibrary",
                "Track3D_Profile",
                new Track3D_Listener(this.configuration.getEventBus()), StatusKind.DATA_AVAILABLE_STATUS);

        if (m_3Ddr == null) {
            throw new Exception("Unable to create DDS Track_3DDataReader");
        }

        Topic topicWriter = participant.create_topic_with_profile(
                "Track3D_TopicFromPlugin",
                Track_3DTypeSupport.get_type_name(),
                "Track3D_QosLibrary",
                "Track3D_Profile",
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (topicWriter == null) {
            throw new Exception("Unable to create topic Writer.");
        }

        // Create Dta reader using xml QoS file
        m_3Ddw = (Track_3DDataWriter) participant.create_datawriter_with_profile(
                topicWriter, "Track3D_QosLibrary",
                "Track3D_Profile",
                null /* listener */, StatusKind.STATUS_MASK_NONE);

        if (m_3Ddw == null) {
            throw new Exception("Unable to create DDS Track_3DDataWriter");
        }
    }

    @Subscribe
    public void receiveTrack3D(Track_3D trk) {
        m_3Ddw.write(trk, InstanceHandle_t.HANDLE_NIL);
    }

    @Override
    public void stop() {
        //Rilascio delle risorse
        participant.delete_contained_entities();
        DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
    }

    @Override
    public void start() {
        configuration.getEventBus().register(this);

        try {
            createTrack_3D_DataReaderWriter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
