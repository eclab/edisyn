package edisyn.synth.novationastation;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Station Midi message.
 * This class intends to centralize all specific (to Novation A Station) Midi Sysex Message construction and parsing.
 * The definition is solely driven by A Station specification.
 */
class SysexMessage {
    enum Type {
        // (write) requests to A Station - with payload
        CURRENT_PROGRAM_DUMP((byte) 0x00, 128),
        PROGRAM_DUMP((byte) 0x01, 128),
        // not yet supported
        //PROGRAM_PAIR_DUMP((byte) 0x02, 256),
        //GLOBAL_DATA_DUMP((byte) 0x03, 256),
        // (read) requests to A Station - no payload
        CURRENT_PROGRAM_DUMP_REQUEST((byte) 0x40),
        PROGRAM_DUMP_REQUEST((byte) 0x41),
        // not yet supported
        //PROGRAM_PAIR_DUMP_REQUEST((byte) 0x42),
        //GLOBAL_DATA_DUMP_REQUEST((byte) 0x43)
        ;

        private static final Map<Byte, Type> typemap = Arrays.stream(values())
                .collect(Collectors.toMap(e -> e.typeValue, Function.identity()));

        private final byte typeValue;
        private final int payloadSize;

        Type(byte typeValue) {
            this(typeValue, 0);
        }

        Type(byte typeValue, int payloadSize) {
            this.typeValue = typeValue;
            this.payloadSize = payloadSize;
        }

        int getPayloadSize() {
            return payloadSize;
        }
    }

    static class Builder {
        private final Type type;
        private final byte[] payload;

        private byte controlByte;
        private byte softwareVersion;
        private byte versionIncrement;
        private byte programBank;
        private byte programNumber;

        Builder(Type type) {
            this.type = type;
            payload = new byte[type.payloadSize];
        }

        Builder withControlByte(byte value) {
            controlByte = value;
            return this;
        }

        Builder withSoftwareVersion(byte value) {
            softwareVersion = value;
            return this;
        }

        Builder withVersionIncrement(byte value) {
            versionIncrement = value;
            return this;
        }

        Builder withProgramBank(byte value) {
            programBank = value;
            return this;
        }

        Builder withProgramNumber(byte value) {
            programNumber = value;
            return this;
        }

        Builder withPayload(int index, byte value) {
            if (index >= payload.length) {
                throw new IllegalStateException("payload out of boundary ! Type = " + type + ", index = " + index);
            }
            payload[index] = value;
            return this;
        }

        SysexMessage build() {
            return new SysexMessage(this);
        }
    }

    private static final byte[] START_SEQUENCE = new byte[]{
            (byte) 0xF0,    // Sysex start
            (byte) 0x00,    // Novation ID1
            (byte) 0x20,    // Novation ID2
            (byte) 0x29,    // Novation ID3
            (byte) 0x01,    // DeviceType
            (byte) 0x40,    // A Station
            (byte) 0x7F,    // Sysex channel (7F or current receive channel)
            (byte) 0x00,    // message type
            (byte) 0x00,    // control byte
            (byte) 0x00,    // Software version
            (byte) 0x00,    // Version increment
            (byte) 0x00,    // program bank
            (byte) 0x00     // program number
    };

    private static final byte[] END_SEQUENCE = new byte[]{
            (byte) 0xF7    // Sysex end
    };

    private final int INDEX_TYPE = 7;

    private final byte[] bytes;
    private final Type type;

    SysexMessage(Builder builder) {
        this.type = builder.type;
        bytes = new byte[START_SEQUENCE.length + builder.payload.length + END_SEQUENCE.length];
        int index = 0;
        for (byte b : START_SEQUENCE) {
            bytes[index++] = b;
        }
        for (byte b : builder.payload) {
            bytes[index++] = b;
        }
        for (byte b : END_SEQUENCE) {
            bytes[index++] = b;
        }
        bytes[INDEX_TYPE] = builder.type.typeValue;
        bytes[8] = builder.controlByte;
        bytes[9] = builder.softwareVersion;
        bytes[10] = builder.versionIncrement;
        bytes[11] = builder.programBank;
        bytes[12] = builder.programNumber;
        validate(bytes);
    }

    SysexMessage(byte[] bytes) {
        this.bytes = validate(bytes);
        this.type = Type.typemap.get(bytes[INDEX_TYPE]);
    }

    static SysexMessage parse(byte[] bytes) {
        return new SysexMessage(bytes);
    }

    // get type
    Type getType() {
        return type;
    }

    // get FULL Sysex message as byte array
    byte[] getBytes() {
        return bytes;
    }

    // get the message payload
    byte[] getPayload() {
        return Arrays.copyOfRange(bytes, START_SEQUENCE.length, bytes.length - END_SEQUENCE.length);
    }

    byte getSoftwareVersion() {
        return bytes[9];
    }

    byte getVersionIncrement() {
        return bytes[10];
    }

    byte getProgramBank() {
        return bytes[11];
    }

    byte getProgramNumber() {
        return bytes[12];
    }

    String getFullVersion() {
        byte softwareVersion = getSoftwareVersion();
        int major = softwareVersion >> 3;
        int minor = softwareVersion & 0x7;
        return major + "." + minor + "." + getVersionIncrement();
    }

    private byte[] validate(byte[] bytes) {
        SysexMessage.Type type = Type.typemap.get(bytes[INDEX_TYPE]);
        // validate length
        int expectedLength = START_SEQUENCE.length + type.payloadSize + END_SEQUENCE.length;
        int actualLength = bytes.length;
        if (actualLength != expectedLength) {
            throw new IllegalStateException("Invalid message length for type " + type + ", expected " + expectedLength + ", but was " + actualLength);
        }
        if (!arrayEquals(bytes, 0, 6, START_SEQUENCE, 0, 6)) {
            throw new IllegalStateException("Invalid start sequence");
        }
        if (!arrayEquals(bytes, actualLength - END_SEQUENCE.length, actualLength, END_SEQUENCE, 0, END_SEQUENCE.length)) {
            throw new IllegalStateException("Invalid end sequence");
        }
        return bytes;
    }

    private boolean arrayEquals(byte[] bytes1, int from1, int to1, byte[] bytes2, int from2, int to2) {
        // only from JDK9
        //return Arrays.equals(bytes1, from1, to1, bytes2, from2, to2);
        if (to1 - from1 != to2 - from2) {
            return false;
        }
        boolean equal = true;
        for (int index = 0; index < to1 - from1; ++index) {
            if (bytes1[from1 + index] != bytes2[from2 + index]) {
                equal = false;
                break;
            }
        }
        return equal;
    }
}
