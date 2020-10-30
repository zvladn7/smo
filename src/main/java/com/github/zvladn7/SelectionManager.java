package com.github.zvladn7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectionManager {

    public static final int UNSET_PACKAGE_NUMBER_VALUE = -1;

    private final Device[] devices;

    /**
     * Current package for execution on device.
     */
    private int packageNumber;

    /**
     * Current index on the devices ring.
     * This is needed cause of discipline of selection requests from buffer.
     */
    private int circleIndex;

    public SelectionManager(final int amountOfDevices) {
        this.devices = new Device[amountOfDevices];
        this.circleIndex = 0;
        this.packageNumber = UNSET_PACKAGE_NUMBER_VALUE;
        initDevices();
    }

    private void initDevices() {
        for (int i = 0; i < devices.length; ++i) {
            devices[i] = new Device(i);
        }
    }

    public int getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(int packageNumber) {
        this.packageNumber = packageNumber;
    }


    public boolean isAnyDeviceFree() {
        for (final Device device : devices) {
            if (device.isFree()) {
                return true;
            }
        }
        return false;
    }

    public int executeRequest(final Request request, final double currentTime) {
        final int freeDeviceIndex = getFreeDeviceIndex();
        devices[freeDeviceIndex].execute(request, currentTime);
        return freeDeviceIndex;
    }

    private int getFreeDeviceIndex() {
        int deviceIndex = circleIndex;
        do {
            if (deviceIndex >= devices.length) {
                deviceIndex = 0;
            }
            if (devices[deviceIndex].isFree()) {
                circleIndex = deviceIndex + 1;
                return deviceIndex;
            }
            ++deviceIndex;
        } while (deviceIndex == circleIndex);

        throw new IllegalStateException("There is no free devices on invocation");
    }

    public List<DoneInfo> getDoneRequestsWithDevices(final double currentTime) {
        final List<DoneInfo> doneList = new ArrayList<>();
//        for (int deviceIndex = 0; deviceIndex < devices.length; ++deviceIndex) {
//            if (devices[deviceIndex].getDoneTime() < currentTime) {
//                doneList.add(new Pair<>(deviceIndex, devices[deviceIndex].getDoneRequest()));
//                devices[deviceIndex].clearAfterDoneProcessing();
//            }
//        }
        Arrays.stream(devices)
                .filter(device -> device.getDoneTime() < currentTime)
                .forEach(device -> {
                    doneList.add(new DoneInfo(device.getNumber(), device.getDoneRequest(), device.getDoneTime()));
                    device.clearAfterDoneProcessing();
                });
        return doneList;
    }

    class DoneInfo {
        public int deviceNumber;
        public Request doneRequest;
        public double doneTime;

        public DoneInfo(int deviceNumber, Request doneRequest, double doneTime) {
            this.deviceNumber = deviceNumber;
            this.doneRequest = doneRequest;
            this.doneTime = doneTime;
        }
    }

}
