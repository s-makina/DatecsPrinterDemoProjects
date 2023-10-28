package com.datecs.demo.ui.main.tools;// *****************************************************************************
//  REG_mode.h   version:  1.0   ·  date: 23.03.2009
//  ----------------------------------------------------------------------------
//
//  ----------------------------------------------------------------------------
//  Copyright (C) Datecslab Ltd 2009 - All Rights Reserved
// *****************************************************************************
//
// *****************************************************************************

import java.util.Arrays;

class StructuredInfoRegister_DeviceGroup_B {
    private final int MAX_PRINT_COLUMNS = 42;
    private static final int SZ_PLU_NAME = 22;

    public static String DateToString(scDATE_TIME strDateTime) {
        return strDateTime.DD + "-" +
                strDateTime.MM + "-" +
                strDateTime.YY + " " +
                strDateTime.hh + ":" +
                strDateTime.mm + ":" +
                strDateTime.ss;
    }

    //------------------------------------------------------------------------------
// КОМАНДИ ИЗПОЛЗВАНИ В РЕЖИМ РЕГИСТРИРАНЕ И ЗА ГЕНЕРИРАНЕ НА АРХИВА
// маркираните с '*' не се записват в архива
//------------------------------------------------------------------------------
    public enum tagRClass {

        UNKNOWN(0),        //*невалиден
        PLUSELL(1),        // продажба на артикул
        DPxSELL(2),        // продажба на департамент
        PAYMENT(3),        // плащане
        OTS_NDB(4),        // отсъпка/надбавка
        RA_PO(5),          // служебно въведени/изведени
        QTYSET(6),         //*установяване на количество
        PRCSET(7),         //*установяване на цена
        VD_PLUSELL(8),     // войд на продажба на артикул
        VD_DPxSELL(9),     // войд на продажба на департамент
        VD_OTS_NDB(10),    // войд на отстъпка/надбавка
        ABONAT(11),        // печат на номер на абонат
        BEGPAY(12),        // начало на плащане (текста "Обща сума")
        BOTTLE(13),        //*продажба/приемане на амбалаж
        VD_BOTTLE(14),     //*войд на продажба/приемане на амбалаж
        COUPON(15),        //*купони ( за гръцки модели )
        TICKET(16),        //*билет( куверт ) ( за гръцки модели )
        //RETURN_PLUSELL(17),    // замяна на артикул
        ALL_VOID(18),      // анулиране на целия бон
        REPORTS(19),       // всички отчети
        TEXT_LINE(20),     // печат на текстов ред
        OPEN_BON(21),      // отваряне на бон
        CLOSE_BON(22),     // затваряне на бон
        HEADER_FOOTER(23), // заглавие и рекламни редове на касова бележка
        PINPAD_INFO(24),   // печат на информация за бележката от пинпад
        INTERNAL(254);     // служебен запис в архива
        private final int value;

        tagRClass(int value) {
            this.value = value;
        }

        public static tagRClass getById(int id) {
            for (tagRClass m : tagRClass.values()) {
                if (m.value == id) {
                    return m;
                }
            }
            return null;
        }
    }

    enum tagRSubClassPay {
        PY_PY0(0),              // Плащане 'В БРОЙ'
        PY_PY1(1),              // Плащане #1
        PY_PY2(2),              // Плащане #2
        PY_PY3(3),              // Плащане #3
        PY_PY4(4),              // Плащане #4
        PY_PY5(5),              // Плащане #5
        PY_FOREIGN1(6),         // Плащане в алтернативна валута
        PY_TOTAL(7),            // общо платена сума
        PY_STL(14),             // междинна сума
        PY_STL_FOREIGN(15),     // междинна сума в алтернативна валута
        PY_RESTO(16),           // ресто в основна валута
        PY_RESTO_FOREIG(17),    // ресто в алтернативна валута
        PY_ORDERS(18);          // край на поръчка в ресторантска версия
        private final int value;

        private tagRSubClassPay(int value) {
            this.value = value;
        }

        public static tagRSubClassPay getById(int id) {
            for (tagRSubClassPay m : tagRSubClassPay.values()) {
                if (m.value == id) {
                    return m;
                }
            }
            return null;
        }

    }

    enum tagRSubClassRAPO {
        RECD_ON,          // служебно въведени суми в основна валута
        PAID_OUT,         // служебно изведени суми в основна валута
        RECD_ON_FOREIGN,  // служебно въведени суми в алтернативна валута
        PAID_OUT_FOREIGN  // служебно изведени суми в алтернативна валута
    }

    enum tagRSubClassPerc {
        PERC_NONE,         // няма оперция 'отстъпки/надбавки'
        PERC_SURCHARGE,         // процентна надбавка
        PERC_DISCOUNT,         // процентна отстъпка
        VAL_SURCHARGE,         // стойностна надбавка
        VAL_DISCOUNT,         // стойностна отсъпка
        PERC_UNKNOWN,         // неизвестно, ползва се при войд операции за да се провери дали има "отс/надб"
        VAL_DISCOUNTplu,         // специална стойностна отсъпкa над артикул
        VAL_DISCOUNTstl          // специална стойностна отсъпкa над междинна сума
    }

    enum tagRSubClassBottle {
        BOTTLE_SELL,         // касиера дава на амбалаж
        BOTTLE_TAKE          // касиера приема на амбалаж
    }

    enum tagRSubClassReports {
        REPORT_DAILYZ,         // дневен отчет с нулиране
        REPORT_DAILYX,         // дневен отчет без нулиране
        REPORT_OPERZ,         // отчет оператори с нулиране
        REPORT_OPERX,         // отчет оператори без нулиране
        REPORT_PLUZ,         // отчет продадени артикули с нулиране
        REPORT_PLUX,         // отчет продадени артикули без нулиране
        REPORT_PLUPARAM,         // отчет параметри на артикули
        REPORT_DEPART,         // отчет департаменти
        REPORT_ITEMGRP         // отчет стокови групи
    }

    enum tagOpenSubtype {
        OPEN_SELL,
        OPEN_INV,
        OPEN_OTHER
    }


    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    class tagRCMDbuffer {
        public byte _bf[] = new byte[96];
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDtextline {
        //  tagRClass type;         // 00       Тип на операцията 'TEXT_LINE'
        byte filler0;               // 01       1 байта не се ползват
        byte text[] = new byte[42]; // 02-2B    до 42 байта текст
        byte filler1[] = new byte[51];         // 2C-5E    51 байта не се ползват
        byte checksum;                         // 5F       контролна сума

        public tagRCMDtextline(byte[] data) {
            // this.type = type;
            this.filler0 = data[1];
            this.text = Arrays.copyOfRange(data, 2, 44);
            this.filler1 = Arrays.copyOfRange(data, 44, 95);
            this.checksum = data[95];
        }
    }


    static class scDATE_TIME {
        public byte DD;
        public byte MM;
        public byte YY;
        public byte hh;
        public byte mm;
        public byte ss;

        public scDATE_TIME(byte[] data) {
            if (data.length != 6) throw new RuntimeException("Array not fit in DATE_TIME");
            this.DD = data[0];
            this.MM = data[1];
            this.YY = data[2];
            this.hh = data[3];
            this.mm = data[4];
            this.ss = data[5];
        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDopen {

        tagRClass type;         //      00   Тип OPEN_BON
        scDATE_TIME strDateTime;//      Дата и час на бона по който е сторното
        byte nOperator;         //
        int nBon;               //      Номер на бон
        int flags;              //      Флагове
        long invN;              //      Номер на фактура
        long strToInvoice;      //      Номер на фактура към която е сторното (за случай на кредитно известие)
        int strNumberDoc;       //      Номер на бона по който е сторното
        short klenType;         //      Тип на бона (тип с който се записва в индекса на КЛЕН)
        byte strType;           //      Тип на сторно операцията: 0 - операторска грешка, 1 - връщане/рекламация
        byte[] strFMnumber = new byte[9]; //      Номер на фискалната памет на бона по който е сторното
        byte[] nSale = new byte[32];        //      Уникален номер на продажба ако е от програма за продажбите CCCCCCCC-CCCC-DDDDDDD трябват 21+1, но оставяме 32
        byte dp;                  //      Позиция на десетичната точка (да има т.к.ще потрябва).
        byte[] deviceID = new byte[8 + 1];    //
        byte[] dummy = new byte[5];            //
        byte checksum;            // 5F       контролна сума

        public tagRCMDopen(byte[] data) {
            //this.type = type;
            this.strDateTime = new scDATE_TIME(Arrays.copyOfRange(data, 1, 7));
            this.nOperator = data[7];
            this.nBon = bytesToInt(Arrays.copyOfRange(data, 8, 12));
            this.flags = bytesToInt(Arrays.copyOfRange(data, 12, 16));
            this.invN = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.strToInvoice = bytesToLong(Arrays.copyOfRange(data, 24, 32));
            this.strNumberDoc = bytesToInt(Arrays.copyOfRange(data, 32, 36));
            this.klenType = bytesToShort(Arrays.copyOfRange(data, 36, 38));
            this.strType = data[38];
            this.strFMnumber = Arrays.copyOfRange(data, 39, 48);
            this.nSale = Arrays.copyOfRange(data, 48, 80);
            this.dp = data[80];
            this.deviceID = Arrays.copyOfRange(data, 81, 90);
            this.dummy = Arrays.copyOfRange(data, 90, 95);
            this.checksum = data[95];
        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDsell {
        tagRClass type;      // 00       Тип на командата  'SELL'            *** PLU/DEP ***
        byte cancel;         // 01       флаг за void операция               *** PLU/DEP ***
        byte fMyPluDB;       // 02       Артикул програмиран в нашата база   *** PLU ***
        byte vat;            // 03       Дан.група          (vat)            *** PLU/DEP ***
        int prc;             // 04-07    Единична цена      (price)          *** PLU/DEP ***
        long suma;           // 08-0F    Сума = qty*prc                      *** PLU/DEP ***
        long icode;          // 10-17    Номенклатурен код  (item code)      *** PLU ***
        long bcode;          // 18-1F    Бар код            (bar code)       *** PLU ***
        int qty;             // 20-23    Продадено кол-во   (sold qty)       *** PLU/DEP ***
        byte[] name = new byte[SZ_PLU_NAME];    // 24-39    Име                (name)           *** PLU/DEP ***
        tagRSubClassPerc typeIncDec;     // 3A       тип на отс./над   "Търсене на VD"   *** PLU/DEP ***
        byte dep;            // 3B       Номер на щанд      (department)     *** PLU/DEP ***
        int sIncDec;         // 3C-3F    процент или ст-ст "Търсене на VD"   *** PLU/DEP ***
        byte grp;            // 40       Номер на ст.група  (group)          *** PLU ***
        byte filler1;        // 41       1  байта не се ползват
        byte[] ext_name = new byte[SZ_PLU_NAME]; // 42-58    разширител на името                 *** PLU ***
        byte[] filler2 = new byte[7];     // 59-5Е    7  байта не се ползват
        byte checksum;       // 5F       контролна сума


        public tagRCMDsell(byte[] data) {

            //  this.type = type;
            this.cancel = data[1];
            this.fMyPluDB = data[2];
            this.vat = data[3];
            this.prc = bytesToInt(Arrays.copyOfRange(data, 4, 8));
            this.suma = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.icode = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.bcode = bytesToLong(Arrays.copyOfRange(data, 24, 32));
            this.qty = bytesToInt(Arrays.copyOfRange(data, 32, 36));
            this.name = Arrays.copyOfRange(data, 36, 58);
            this.typeIncDec = tagRSubClassPerc.values()[data[58]];
            this.dep = data[59];
            this.sIncDec = bytesToInt(Arrays.copyOfRange(data, 60, 64));
            this.grp = data[64];
            this.filler1 = data[65];
            this.ext_name = Arrays.copyOfRange(data, 66, 88);
            this.filler2 = Arrays.copyOfRange(data, 88, 95);
            ;
            this.checksum = data[95];

        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDpay {
        tagRClass type;           // 00       Тип на командата PAYMENT'
        tagRSubClassPay subtype;        // 01       Подтип на командата
        byte[] name_currency = new byte[6];// 02-07    имената на валутите (напр. "ЛВ EUR")
        long sInput;         // 08-0F    Въведена сума
        long sResto;         // 10-17    Рестото
        long sRestoForeign;  // 18-1F    Ресто в алтернативна валута
        long sPYN;           // 20-27    Сума подадена от клиента в алт.валута
        long sPYosnov;       // 28-2F    sPYN, но преизчислена в осн.валута
        long sTLneosn;       // 30-37    сумата на бона преизчислена според курса
        int sExchRate;      // 38-3B    курс на основна -> алт.валута
        byte[] pay_name = new byte[10];   // 3C-45    име на плащането
        byte[] filler1 = new byte[25];    // 46-5E    25 байта не се ползват
        byte checksum;       // 5F       контролна сума


        public tagRCMDpay(byte[] data) {
            //  this.type = type;
            this.subtype = tagRSubClassPay.getById(data[1]);
            this.name_currency = Arrays.copyOfRange(data, 2, 8);
            this.sInput = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.sResto = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.sRestoForeign = bytesToLong(Arrays.copyOfRange(data, 24, 32));
            this.sPYN = bytesToLong(Arrays.copyOfRange(data, 32, 40));
            this.sPYosnov = bytesToLong(Arrays.copyOfRange(data, 40, 48));
            this.sTLneosn = bytesToLong(Arrays.copyOfRange(data, 48, 56));
            this.sExchRate = bytesToInt(Arrays.copyOfRange(data, 56, 60));
            this.pay_name = Arrays.copyOfRange(data, 60, 70);
            this.filler1 = Arrays.copyOfRange(data, 70, 95);
            this.checksum = data[95];
        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDtotal {
        tagRClass type;           // 00       Тип на командата BEGPAY'
        byte[] name_currency = new byte[6];// 01-06    имената на валутите (напр. "ЛВ EUR")
        byte filler0;     // 07       1 байта не се ползват
        long sSTL;           // 08-0F    Обща сума
        long sVAT;        // 10-4F    Разпределение по данъчни групи
        long sVAT9;          // 50-57    8 байта не се ползват
        int sExchRate;      // 58-5B    курс на основна -> алт.валута
        byte[] filler1 = new byte[3];     // 5C-5E    3 байта не се ползват
        byte checksum;       // 5F       контролна сума

        public tagRCMDtotal(byte[] data) {

            this.name_currency = Arrays.copyOfRange(data, 1, 7);
            this.filler0 = data[8];
            this.sSTL = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.sVAT = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.sVAT9 = bytesToLong(Arrays.copyOfRange(data, 24, 32));
            this.sExchRate = bytesToInt(Arrays.copyOfRange(data, 32, 36));
            this.filler1 = Arrays.copyOfRange(data, 36, 39);
            this.checksum = data[39];
        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDrapo {
        tagRClass type;                    // 00       Тип на командата RAPO
        tagRSubClassRAPO subtype;          // 01       Подтип на командата
        byte[] name_currency = new byte[6];// 02-07    имената на валутите (напр. "ЛВ EUR")
        long sInput;         // 08-0F    Въведена сума
        int sExchRate;      // 10-13    курс на основна -> алт.валута
        byte[] filler1 = new byte[75];    // 14-5E    79 байта не се ползват
        byte checksum;       // 5F       контролна сума

        public tagRCMDrapo(byte[] data) {
            this.type = type;
            this.subtype = subtype;
            this.name_currency = name_currency;
            this.sInput = sInput;
            this.sExchRate = sExchRate;
            this.filler1 = filler1;
            this.checksum = checksum;
        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDperc {
        tagRClass type;           // 00       Тип на командата OTS_NDB
        tagRSubClassPerc subtype;        // 01       Подтип на командата
        byte cancel;         // 02       флаг за void oоперация
        byte flagSTL;        // 03       Флаг за операция над STL,ako!=0
        byte nVAT;           // 04       Показва към коя дан.гр.да се натрупа
        byte dep;            // 05       департамент
        byte grp;            // 06       секция
        byte filler0;     // 07       1 байта не се ползват
        long sSuma;          // 08-0F    Сума на транзакцията
        long sSTL;           // 10-17    Обща сума по дан.групи преди %STL
        long[] sVAT = new long[8];        // 18-57    Разпределение по данъчни групи
        //          Сума по дан.групи преди %STL,
        //          тези регистри ще ми трябват само ако
        //          ще се прави отстъпка/надбавка над
        //          междинна сума да не е последната транзакция
        short percIncDec; // 58-59    Стойност на отстъпка/надбавка
        byte[] filler1 = new byte[5];     // 5А-5Е    5 байта не се ползват
        byte checksum;       // 5F       контролна сума

        public tagRCMDperc(byte[] data) {
            //this.type = type;
            this.subtype = tagRSubClassPerc.values()[data[1]];
            this.cancel = data[2];
            this.flagSTL = data[3];
            this.nVAT = data[4];
            this.dep = data[5];
            this.grp = data[6];
            this.filler0 = data[7];
            this.sSuma = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.sSTL = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.sVAT = bytesToLongArray(Arrays.copyOfRange(data, 24, 88), 8);
            this.percIncDec = bytesToShort(Arrays.copyOfRange(data, 88, 90));
            this.filler1 = Arrays.copyOfRange(data, 90, 95);
            this.checksum = data[95];
        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDallvoid {
        tagRClass type;           // 00       Тип на командата ALL_VOID
        byte[] filler0 = new byte[5];     // 01-07    7 байта не се ползват
        short nCorrections;   //          Брой поцизии, които трябва да се отброят като корекции в дн. отчет
        long sTL;            // 08-0F    Обща сума на операциите
        long[] sVAT = new long[8];        // 10-4F    Разпределение по данъчни групи
        byte[] filler1 = new byte[15];    // 50-5Е    15 байта не се ползват
        byte checksum;       // 5F       контролна сума

        public tagRCMDallvoid(byte[] data) {
            //this.type = type;
            this.filler0 = Arrays.copyOfRange(data, 1, 6);
            this.nCorrections = bytesToShort(Arrays.copyOfRange(data, 6, 8));
            this.sTL = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.sVAT = bytesToLongArray(Arrays.copyOfRange(data, 16, 80), 8);
            this.filler1 = Arrays.copyOfRange(data, 80, 95);
            this.checksum = data[95];
        }
    }

    //------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
    static class tagRCMDhdrftr {

        tagRClass type;        // 00       Тип на командата HEADER_FOOTER
        byte subtype;     // 01       Подтип 0=хедър; 1=футър;
        short nLine;       // 02-03    номер на ред
        byte[] text = new byte[24];    // 04-1B    текст за отпечатване
        byte[] filler1 = new byte[67]; // 1C-5E    67 байта не се ползват
        byte checksum;    // 5F       контролна сума

        public tagRCMDhdrftr(byte[] data) {
            // this.type = type;
            this.subtype = data[1];
            this.nLine = bytesToShort(Arrays.copyOfRange(data, 2, 18));
            this.text = Arrays.copyOfRange(data, 18, 42);
            this.filler1 = Arrays.copyOfRange(data, 42, 109);
            this.checksum = data[109];
        }
    }

    //------------------------------------------------------------------------------

    private static long[] bytesToLongArray(byte[] data, int size) {
        long[] res = new long[size];
        if (data.length != (size * 8)) throw new RuntimeException("Data not fit in size");
        for (int i = 0; i < size; i++)
            res[i] = bytesToLong(Arrays.copyOfRange(data, 8 * i, (8 * i) + 8));
        return res;
    }

    public static int bytesToInt(byte[] b) {
        int result = 0;
        if (b.length > 4) throw new RuntimeException("Too big to fit in int");
        for (int i = 0; i < 4; i++) {
            result <<= 8;
            result |= (b[3 - i] & 0xFF);
        }
        return result;
    }

    public static short bytesToShort(byte[] b) {
        short result = 0;
        if (b.length > 2) throw new RuntimeException("Too big to fit in long");
        for (int i = 0; i < 2; i++) {
            result <<= 8;
            result |= (b[1 - i] & 0xFF);
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        if (b.length > 8) throw new RuntimeException("Too big to fit in long");
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[7 - i] & 0xFF);
        }
        return result;
    }

    //------------------------------------------------------------------------------
    class tagRCMD {

        tagRClass type;
        tagRCMDsell sell;       // PLUSELL,DPxSELL,VD_PLUSELL,VD_DPxSELL
        tagRCMDpay pay;        // PAYMENT
        tagRCMDrapo rapo;       // RA_PO
        tagRCMDperc perc;       // OTS_NDB,VD_OTS_NDB
        tagRCMDtotal begpay;     // BEGPAY
        tagRCMDallvoid allvoid;    // ALL_VOID
        tagRCMDopen open;       // OPEN_BON
        tagRCMDtextline text;       // TEXT_LINE
        tagRCMDbuffer buf;        //
    }

//==============================================================================
}
/******************************************************************************/
/*  END                                                                       */
/******************************************************************************/

