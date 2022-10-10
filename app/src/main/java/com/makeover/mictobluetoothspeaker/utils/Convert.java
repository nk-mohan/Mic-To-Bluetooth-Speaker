package com.makeover.mictobluetoothspeaker.utils;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;

public class Convert {

    private static final int SAMPLE_RATE_INDEX = 4;
    private static final int CHANNELS = 1;

    public byte[] createAdtsHeader(int length) {
        int frameLength = length + 7;
        byte[] adtsHeader = new byte[7];

        adtsHeader[0] = (byte) 0xFF; // Sync Word
        adtsHeader[1] = (byte) 0xF1; // MPEG-4, Layer (0), No CRC
        adtsHeader[2] = (byte) ((MediaCodecInfo.CodecProfileLevel.AACObjectLC - 1) << 6);
        adtsHeader[2] |= (((byte) SAMPLE_RATE_INDEX) << 2);
        adtsHeader[2] |= (((byte) CHANNELS) >> 2);
        adtsHeader[3] = (byte) (((CHANNELS & 3) << 6) | ((frameLength >> 11) & 0x03));
        adtsHeader[4] = (byte) ((frameLength >> 3) & 0xFF);
        adtsHeader[5] = (byte) (((frameLength & 0x07) << 5) | 0x1f);
        adtsHeader[6] = (byte) 0xFC;

        return adtsHeader;
    }

    public boolean checkBufferInfo(int flags) {
        return (flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
    }
}
