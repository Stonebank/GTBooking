package com.hk;

public class Launch {

    public static void main(String[] args) {
        GTBooker gtBooker = new GTBooker();

        gtBooker.openPage();
        gtBooker.selectHairdresser();

        gtBooker.selectServiceAndConfirm();
        gtBooker.confirmExtraService();

        gtBooker.findAppointmentDays();
        gtBooker.findAppointments();

        gtBooker.enterPhoneNumber();
        gtBooker.acceptTOS();
        gtBooker.acceptMarketingAndFinish();

    }

}
