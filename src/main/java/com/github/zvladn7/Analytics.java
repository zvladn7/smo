package com.github.zvladn7;

import java.util.ArrayList;

public class Analytics {

    //1 - количество сгенирированных заявок каждым источником
    //2 - вероятность отказа в обслуживании заявок каждого источка
    //а - обслуженные, б - нет;
    // вероянтность=а/(а+б)
    /*
    3 - среднее время пребывания заявок каждого источника в системе
    4 - среднее время ожидания заявок каждого источника
    5 - среднее время обслуживания заявок каждого источника
    6 - дисперсия двух последних величин
        в данном случае - это:
            - для 4: время ожидания/время в системе
            - для 5: время обработки/время в системе
    7 - коэфициент использования устройств (время работы каждого прибора / время реализации)
     */

    private ArrayList<Integer> amountOfFailed;
    private ArrayList<Integer> amountOfProcessed;
    private ArrayList<Integer> timeInSystem;
    private ArrayList<Integer> timeOfWait;
    private ArrayList<Integer> timeOfProcessing;
    private ArrayList<Integer> timeOfDeviceWork;

}
