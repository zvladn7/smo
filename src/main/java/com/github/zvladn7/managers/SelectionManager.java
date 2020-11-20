package com.github.zvladn7.managers;

import com.github.zvladn7.components.Device;
import com.github.zvladn7.components.Request;

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

    public SelectionManager(final int amountOfDevices, final double lamda) {
        this.devices = new Device[amountOfDevices];
        this.circleIndex = 0;
        this.packageNumber = UNSET_PACKAGE_NUMBER_VALUE;
        initDevices(lamda);
    }

    private void initDevices(final double lamda) {
        for (int i = 0; i < devices.length; ++i) {
            devices[i] = new Device(i, lamda);
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
        } while (deviceIndex != circleIndex);

        throw new IllegalStateException("There is no free devices on invocation");
    }

    public List<DoneInfo> getDoneRequestsWithDevices(final double currentTime) {
        final List<DoneInfo> doneList = new ArrayList<>();
        Arrays.stream(devices)
                .filter(device -> device.getDoneTime() < currentTime)
                .forEach(device -> {
                    final double startTime = device.getStartTime();
                    final double doneTime = device.getDoneTime();
                    final double timeOnDevice = doneTime - startTime;
                    doneList.add(new DoneInfo(device.getNumber(), device.getDoneRequest(), doneTime, timeOnDevice));
                    device.clearAfterDoneProcessing();
                });
        return doneList;
    }

    public class DoneInfo {
        public int deviceNumber;
        public Request doneRequest;
        public double doneTime;
        public double timeOfWork;

        public DoneInfo(final int deviceNumber,
                        final Request doneRequest,
                        final double doneTime,
                        final double timeOfWork) {
            this.deviceNumber = deviceNumber;
            this.doneRequest = doneRequest;
            this.doneTime = doneTime;
            this.timeOfWork = timeOfWork;
        }
    }

}
