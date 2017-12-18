package com.spacestar.quadrocopter;

public class Consts {
    public static final String SERVER_IP = "192.168.1.1";
    public static final int SERVER_PORT = 8888;

    public static final long UDP_PERIOD = 50;
    public static final long WIFI_PERIOD = 500;
    public static final int STATE_SIZE = 5;

    public static final double ADC_CONST = 0.01278;
    public static final double MAX_VOLTAGE = 12.9;
    public static final double MIN_VOLTAGE = 9;
    public static final double SMALL_VOLTAGE = 9.9;

    public enum Channel {
        GAS, YAW, PITCH, ROLL, OPTIONS
    }

    private Consts() {
    }
}
