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

    public static final float PID_DEFAULT_P = 1;
    public static final float PID_DEFAULT_I = 1;
    public static final float PID_DEFAULT_D = 1;
    public static final float PID_DELTA_P = 0.1F;
    public static final float PID_DELTA_I = 0.01F;
    public static final float PID_DELTA_D = 0.01F;

    public enum Channel {
        GAS, YAW, PITCH, ROLL, OPTIONS
    }

    private Consts() {
    }
}
