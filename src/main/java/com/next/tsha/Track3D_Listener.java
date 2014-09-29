package com.next.tsha;

import com.google.common.eventbus.EventBus;
import com.next.idlcode.Track_3D;
import com.next.idlcode.Track_3DDataReader;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;

/**
 * Created by agerardi on 25/09/2014.
 */
public class Track3D_Listener extends DataReaderAdapter {

    private EventBus m_eb;

    public Track3D_Listener(EventBus eb) {
        m_eb = eb;
    }

    /*
     * This method gets called back by DDS when one or more data samples have
     * been received.
     */
    public void on_data_available(DataReader reader) {
        System.out.println("on_data_available");
        Track_3DDataReader stringReader = (Track_3DDataReader) reader;
        SampleInfo info = new SampleInfo();
        Track_3D data = new Track_3D();
        for (; ; ) {
            try {
                stringReader.take_next_sample(data, info);
                if (info.valid_data) {
                    System.out.println("event bus post");
                    m_eb.post(data);
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
