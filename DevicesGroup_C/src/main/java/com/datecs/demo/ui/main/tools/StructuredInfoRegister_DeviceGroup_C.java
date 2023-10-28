package com.datecs.demo.ui.main.tools;

import java.io.*;
import java.util.Arrays;

public class StructuredInfoRegister_DeviceGroup_C {
    public tagRClass Type;

    private static final int SZ_PLU_NAME = 72;
    private static int MAX_PRINT_COLUMNS = 64;

    public static String DateToString(scDATE_TIME date) {
        String sDST = " ";
        if (date.dst == 1) sDST += "DST";
        return String.format("%02d", date.dd) + "-" +
                String.format("%02d", date.MM) + "-" +
                String.format("%02d", date.year - 2000) + " " +
                String.format("%02d", date.hh) + ":" +
                String.format("%02d", date.mm) + ":" +
                String.format("%02d",+ date.ss) + sDST;
    }

    public enum tagRClass {
        UNKNOWN(0),         //*невалиден
        PLUSELL(1),         // продажба на артикул
        DPxSELL(2),         // продажба на департамент
        PAYMENT(3),         // плащане
        OTS_NDB(4),         // отсъпка/надбавка
        RA_PO(5),           // служебно въведени/изведени
        //QTYSET ( 6),            //*установяване на количество
        //PRCSET ( 7),             //*установяване на цена
        VD_PLUSELL(8),       // войд на продажба на артикул
        VD_DPxSELL(9),       // войд на продажба на департамент
        VD_OTS_NDB(10),      // войд на отстъпка/надбавка
        ABONAT(11),          // печат на номер на абонат
        BEGPAY(12),          // начало на плащане (текста "Обща сума")
        //BOTTLE ( 13),            //*продажба/приемане на амбалаж
        //VD_BOTTLE ( 14),         //*войд на продажба/приемане на амбалаж
        //COUPON ( 15),            //*купони ( за гръцки модели )
        //TICKET ( 16),        //*билет( куверт ) ( за гръцки модели )
        //RETURN_PLUSELL ( 17),        // замяна на артикул
        ALL_VOID(18),        // анулиране на целия бон
        // REPORTS ( 19),        // всички отчети
        TEXT_LINE(20),        // печат на текстов ред
        PINPAD_INFO(24),        // печат на информация за бележката от пинпад
        INVOICE(27),            // информация за фактура
        OPEN_BON(101),        // отваряне на бон
        CLOSE_BON(102),        // затваряне на бон
        ADD_INFO(103),         // допълнителна информация only in DP-05C
        CARGO(104),            // карго only in DP-05C
        BARCODE_LINE(220);        // баркод

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

    public enum tagRSubClassPerc {
        PERC_NONE,         // няма оперция 'отстъпки/надбавки'
        PERC_SURCHARGE,         // процентна надбавка
        PERC_DISCOUNT,         // процентна отстъпка
        VAL_SURCHARGE,         // стойностна надбавка
        VAL_DISCOUNT,         // стойностна отсъпка
        PERC_UNKNOWN,         // неизвестно, ползва се при войд операции за да се провери дали има "отс/надб"
        VAL_DISCOUNTplu,         // специална стойностна отсъпкa над артикул
        VAL_DISCOUNTstl,          // специална стойностна отсъпкa над междинна сума
        CARD_DISCOUNT             // картова отстъпка
    }

    public enum tagRSubClassPay {
        PY_PY0(0),         // Плащане 'В БРОЙ'
        PY_PY1(1),         // Плащане #1
        PY_PY2(2),         // Плащане #2
        PY_PY3(3),         // Плащане #3
        PY_PY4(4),         // Плащане #4
        PY_PY5(5),         // Плащане #5
        PY_FOREIGN1(6),         // Плащане в алтернативна валута
        PY_TOTAL(7),         // общо платена сума
        PY_STL(14),        // междинна сума
        PY_STL_FOREIGN(15),        // междинна сума в алтернативна валута
        PY_RESTO(16),        // ресто в основна валута
        PY_RESTO_FOREIG(17),        // ресто в алтернативна валута
        PY_ORDERS(18);       // край на поръчка в ресторантска версия

        private final int value;

        tagRSubClassPay(int value) {
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

    public enum tagRSubClassRAPO {
        RECD_ON,         // служебно въведени суми в основна валута
        PAID_OUT,         // служебно изведени суми в основна валута
        RECD_ON_FOREIGN,         // служебно въведени суми в алтернативна валута
        PAID_OUT_FOREIGN          // служебно изведени суми в алтернативна валута
    }

    public enum tagRSubClassOpen {
        SELL,       // продажба
        INVOICE,        //фактура
        OTHER          // други
    }

    public enum tagRSubClassBarcode {
        LEFTSIDE,
        RIGHTSIDE
    }

    public class tagRCMType implements Serializable {
        public tagRClass type;           // 00       Тип на командата

        public tagRCMType(tagRClass type) {
            super();
            this.type = type;
        }
    }

    public static class tagRCMDsell implements Serializable {
        public tagRClass type;      // 00       Тип на командата  'SELL'            *** PLU/DEP ***
        public byte cancel;         // 01       флаг за void операция               *** PLU/DEP ***
        public byte fMyPluDB;       // 02       Артикул програмиран в нашата база   *** PLU ***
        public byte vat;            // 03       Дан.група          (vat)            *** PLU/DEP ***
        public int prc;             // 04-07    Единична цена      (price)          *** PLU/DEP ***
        public long suma;           // 08-0F    Сума = qty*prc                      *** PLU/DEP ***
        public long icode;          // 10-17    Номенклатурен код  (item code)      *** PLU ***
        public long bcode;          // 18-1F    Бар код            (bar code)       *** PLU ***
        public int qty;             // 20-23    Продадено кол-во   (sold qty)       *** PLU/DEP ***
        public byte[] name = new byte[SZ_PLU_NAME];    // 24-39    Име                (name)           *** PLU/DEP ***
        public tagRSubClassPerc typeIncDec;     // 3A       тип на отс./над   "Търсене на VD"   *** PLU/DEP ***
        byte[] filler1 = new byte[3];     // 41       3  байта не се ползват
        public int sIncDec;        // 3C-3F    процент или ст-ст "Търсене на VD"   *** PLU/DEP ***
        public byte dep;            // 3B       Номер на щанд      (department)     *** PLU/DEP ***
        public byte grp;            // 40       Номер на ст.група  (group)          *** PLU ***
        public byte unit;       //
        public byte fVDstorno; //

        public tagRCMDsell(byte[] data) {
            //this.type = data[0];
            this.cancel = data[1];
            this.fMyPluDB = data[2];
            this.vat = data[3];
            this.prc = bytesToInt(Arrays.copyOfRange(data, 4, 8));
            this.suma = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.icode = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.bcode = bytesToLong(Arrays.copyOfRange(data, 24, 32));
            this.qty = bytesToInt(Arrays.copyOfRange(data, 32, 36));
            this.name = Arrays.copyOfRange(data, 36, 108);
            this.typeIncDec = tagRSubClassPerc.values()[data[108]];
            this.filler1 = Arrays.copyOfRange(data, 109, 112);
            this.sIncDec = bytesToInt(Arrays.copyOfRange(data, 112, 116));
            this.dep = data[116];
            this.grp = data[117];
            this.unit = data[118];
            this.fVDstorno = data[119];
        }
    }

    public static class tagRCMDpay implements Serializable {
        public tagRClass type;           // 00       Тип на командата PAYMENT'
        public tagRSubClassPay subtype;  // 01       Подтип на командата
        public byte[] name_currency = new byte[6];// 02-07    имената на валутите (напр. "ЛВ EUR")
        public long sInput;         // 08-0F    Въведена сума
        public long sResto;         // 10-17    Рестото
        public long sRestoForeign;  // 18-1F    Ресто в алтернативна валута
        public long sPYN;           // 20-27    Сума подадена от клиента в алт.валута
        public long sPYosnov;       // 28-2F    sPYN, но преизчислена в осн.валута
        public int sTLneosn;       // 30-37    сумата на бона преизчислена според курса
        public byte[] RRN = new byte[16];// 02-07
        public int sExchRate;      // 38-3B    курс на основна -> алт.валута
        public byte[] pay_name = new byte[16];   // 3C-45    име на плащането

        public tagRCMDpay(byte[] data) {
            //this.type = data[0];
            this.subtype = tagRSubClassPay.getById(data[1]);
            this.name_currency = Arrays.copyOfRange(data, 2, 8);
            this.sInput = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.sResto = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.sRestoForeign = bytesToLong(Arrays.copyOfRange(data, 24, 32));
            this.sPYN = bytesToLong(Arrays.copyOfRange(data, 32, 40));
            this.sPYosnov = bytesToLong(Arrays.copyOfRange(data, 0x28, 48));
            this.sTLneosn = bytesToInt(Arrays.copyOfRange(data, 48, 52));
            this.RRN = Arrays.copyOfRange(data, 52, 68);
            this.sExchRate = bytesToInt(Arrays.copyOfRange(data, 68, 72));
            this.pay_name = Arrays.copyOfRange(data, 72, 86);

        }
    }


    public static class tagRCMDperc implements Serializable {
        public tagRClass type;           // 00       Тип на командата OTS_NDB
        public tagRSubClassPerc subtype;        // 01       Подтип на командата
        public byte cancel;         // 02       флаг за void oоперация
        public byte flagSTL;        // 03       Флаг за операция над STL,ako!=0
        public long nVAT; //=new byte[8];           //04       Показва към коя дан.гр.да се натрупа //TODO: Kasha
        public byte dep;            // 05       департамент
        public byte grp;            // 06       секция
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        byte[] filler0 = new byte[2]; // 07       1 байта не се ползват
        public long sSuma;          // 08-0F    Сума на транзакцията
        public long sSTL;           // 10-17    Обща сума по дан.групи преди %STL
        //TODO:Rossi
        public long[] sBRUT = new long[8];        // 18-57    Разпределение по данъчни групи
        //          Сума по дан.групи преди %STL,
        //          тези регистри ще ми трябват само ако
        //          ще се прави отстъпка/надбавка над
        //          междинна сума да не е последната транзакция
        public short percIncDec;     // 58-59    Стойност на отстъпка/надбавка
        public byte fVDstorno;      // 5F       контролна сума


        public tagRCMDperc(byte[] data) {
            //this.type = data[0];
            this.subtype = tagRSubClassPerc.values()[data[1]];
            this.cancel = data[2];
            this.flagSTL = data[3];
            this.nVAT = bytesToLong(Arrays.copyOfRange(data, 4, 12));
            this.dep = data[5];
            this.grp = data[6];
            this.filler0 = Arrays.copyOfRange(data, 14, 16);
            this.sSuma = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.sSTL = bytesToLong(Arrays.copyOfRange(data, 24, 32));
            this.sBRUT = bytesToLongArray(Arrays.copyOfRange(data, 32, 96), 8);
            this.percIncDec = bytesToShort(Arrays.copyOfRange(data, 96, 98));
            this.fVDstorno = data[98];
        }
    }

    private static long[] bytesToLongArray(byte[] data, int size) {
        long[] res = new long[size];
        if (data.length != (size * 8)) throw new RuntimeException("Data not fit in size");
        for (int i = 0; i < size; i++)
            res[i] = bytesToLong(Arrays.copyOfRange(data, 8 * i, (8 * i) + 8));
        return res;
    }

    public static class tagRCMDrapo implements Serializable {
        public tagRClass type;           // 00       Тип на командата RAPO
        public tagRSubClassRAPO subtype;        // 01       Подтип на командата
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 6)]
        public byte[] name_currency = new byte[6];// 02-07    имената на валутите (напр. "ЛВ EUR")
        public long sInput;         // 08-0F    Въведена сума
        public int sExchRate;      // 10-13    курс на основна -> алт.валута //TODO:Rossi
        public byte type_2;       // 5F      тип
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 15)]
        public byte[] AbonatNomer = new byte[15];// 02-07    абонатен номер
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 16)]
        public byte[] AbonatKarta = new byte[16];// 02-07    абонатна карта


        public tagRCMDrapo(byte[] data) {
            //this.type =  data[0];
            this.subtype = tagRSubClassRAPO.values()[data[1]];
            this.name_currency = Arrays.copyOfRange(data, 2, 8);
            this.sInput = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.sExchRate = bytesToInt(Arrays.copyOfRange(data, 16, 20));
        }
    }

    //Pack =8)]
    public static class tagRCMDAbonat implements Serializable {
        public tagRClass type;           // 00       Тип на командата ABONAT
        public byte[] AbonatNomer = new byte[15];// 02-07    абонатен номер
        public byte[] AbonatKarta = new byte[16];// 02-07    абонатна карта

        public tagRCMDAbonat(byte[] data) {
            //this.type =  data[0];
            AbonatNomer = Arrays.copyOfRange(data, 1, 16);
            AbonatKarta = Arrays.copyOfRange(data, 16, 32);
        }
    }

    public static class tagRCMDtotal implements Serializable {
        public tagRClass type;           // 00       Тип на командата BEGPAY'
        public byte[] name_currency = new byte[6];// 01-06    имената на валутите (напр. "ЛВ EUR")
        byte filler0;                    // 07       1 байта не се ползват
        public long sSTL;                // 08-0F    Обща сума
        public long[] sBRUT = new long[8];        // 10-4F
        public int sExchRate;                   // 58-5B    курс на основна -> алт.валута

        public tagRCMDtotal(byte[] data) {
            //this.type =  data[0];
            this.name_currency = Arrays.copyOfRange(data, 1, 7);
            this.filler0 = data[7];
            this.sSTL = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.sBRUT = bytesToLongArray(Arrays.copyOfRange(data, 16, 80), 8);
            this.sExchRate = bytesToInt(Arrays.copyOfRange(data, 80, 84));
        }
    }

    public static class tagRCMDallvoid implements Serializable {
        public tagRClass type;           //00 Тип на командата ALL_VOID
        //TODO:Rossi
        byte[] filler = new byte[7];     //01-07 7 байта не се ползват
        public long sTL;                 //08-0F   Обща сума на операциите
        public long[] sBRUT = new long[8]; //10-4F

        public tagRCMDallvoid(byte[] data) {
            //this.type =  data[0];
            this.filler = Arrays.copyOfRange(data, 1, 8);
            this.sTL = data[8];
            this.sBRUT = bytesToLongArray(Arrays.copyOfRange(data, 8, 72), 8);
        }
    }

    public static class tagRCMDtextline implements Serializable {
        public tagRClass type;         // 00       Тип на операцията 'TEXT_LINE'
        //    [MarshalAs(UnmanagedType.ByValArray, SizeConst = MAX_PRINT_COLUMNS)]
        public byte[] text = new byte[MAX_PRINT_COLUMNS]; // 02-2B    до 42 байта текст

        public tagRCMDtextline(byte[] data) {
            //this.type =  data[0];
            text = Arrays.copyOfRange(data, 1, 65);
        }
    }

    public static class tagRCMDInvoice implements Serializable {
        public tagRClass type;               // 00       Тип на операцията 'Invoice'
        public long invoiceNumber;            // Номер на фактура

        public tagRCMDInvoice(byte[] data) {
            //this.type =  data[0];
            this.invoiceNumber = bytesToLong(Arrays.copyOfRange(data, 1, 8));
        }
    }

    public class tagRCMDbuffer implements Serializable {
        byte[] bf = new byte[320];
    }

    public static class tagRCMDopen implements Serializable {
        public tagRClass type;                  // 00   Тип OPEN_BON
        public tagRSubClassOpen subtype;        // 01       Подтип на командата
        public byte nClerk;                     //
        public byte stornoType;                 //
        public byte dec_point;                  //
        //TODO: Rossi
        byte[] filler = new byte[3];
        public long nInvoice;                    //Номер на фактура
        public long nInvoiceStorno;              //Номер на сторно фактура
        //public Int32 flags;                    //Флагове
        //public Int64 invN;                     //Номер на фактура
        //public Int64 strToInvoice;             //Номер на фактура към която е сторното (за случай на кредитно известие)
        public int strNumberDoc;               //Номер на бона по който е сторното
        public scDATE_TIME bonDateTime;          //Дата и час на бона
        public scDATE_TIME strDateTime;          //Дата и час на бона по който е сторното
        public byte[] paymentRemap = new byte[6];             //
        public byte[] strFMnumber = new byte[17];  //      Номер на фискалната памет на бона по който е сторното
        public byte[] nSale = new byte[22];        //      Уникален номер на продажба ако е от програма за продажбите CCCCCCCC-CCCC-DDDDDDD трябват 21+1, но оставяме 32


        public tagRCMDopen(byte[] data) {
            //   this.type = data[0];
            this.subtype = tagRSubClassOpen.values()[data[1]];
            this.nClerk = data[2];
            this.stornoType = data[3];
            this.dec_point = data[4];
            this.filler = Arrays.copyOfRange(data, 5, 8);
            this.nInvoice = bytesToLong(Arrays.copyOfRange(data, 8, 16));
            this.nInvoiceStorno = bytesToLong(Arrays.copyOfRange(data, 16, 24));
            this.strNumberDoc = bytesToInt(Arrays.copyOfRange(data, 24, 28));
            this.bonDateTime = new scDATE_TIME(Arrays.copyOfRange(data, 28, 36));
            this.strDateTime = new scDATE_TIME(Arrays.copyOfRange(data, 36, 44));
            this.paymentRemap = Arrays.copyOfRange(data, 44, 50);
            this.strFMnumber = Arrays.copyOfRange(data, 50, 67);
            this.nSale = Arrays.copyOfRange(data, 67, 89);

        }
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

    public static class tagRCMDClose implements Serializable {
        public tagRClass type;         // 00   Тип
        public tagRSubClassOpen subtype;        // 01       Подтип на командата
        public byte ClientEikType;           //
        byte filler;
        public long nInvoice;               //      Номер на фактура
        byte[] filler1 = new byte[4];
        public scDATE_TIME bonDateTime;//      Дата и час на бона
        public byte[] clientEIK = new byte[17]; //
        public byte[] IDnumber = new byte[17];


        public tagRCMDClose(byte[] data) {
            //this.type = data[0];
            this.subtype = tagRSubClassOpen.values()[data[1]];
            ClientEikType = data[2];
            this.filler = data[3];
            this.nInvoice = bytesToLong(Arrays.copyOfRange(data, 4, 12));
            this.filler1 = Arrays.copyOfRange(data, 12, 16);
            this.bonDateTime = new scDATE_TIME(Arrays.copyOfRange(data, 16, 24));
            this.clientEIK = Arrays.copyOfRange(data, 24, 41);
            this.IDnumber = Arrays.copyOfRange(data, 41, 58);
        }


    }

    //Pack =8)]
    public static class tagRCMDAddInfo {
        public tagRClass type;         // 00   Тип
        public byte[] bonus = new byte[64]; //
        byte filler;
        public byte[] name = new byte[72]; //
        public byte[] TaxN = new byte[16]; //
        public byte typeTaxN;
        public byte[] RECname = new byte[72]; //
        public byte[] VATN = new byte[14]; //
        public byte[] Address1 = new byte[36];
        public byte[] Address2 = new byte[36];

        public tagRCMDAddInfo(byte[] data) {
            //this.type = data[0];
            this.bonus = Arrays.copyOfRange(data, 1, 65);
            this.filler = data[65];
            this.name = Arrays.copyOfRange(data, 66, 138);
            TaxN = Arrays.copyOfRange(data, 138, 154);
            this.typeTaxN = data[154];
            this.RECname = Arrays.copyOfRange(data, 155, 227);
            this.VATN = Arrays.copyOfRange(data, 227, 241);
            Address1 = Arrays.copyOfRange(data, 241, 277);
            Address2 = Arrays.copyOfRange(data, 277, 313);
        }
    }

    public static class tagRCMDCargo {
        public tagRClass type;         // 00   Тип
        public byte[] tovaritelnica = new byte[20]; //

        public tagRCMDCargo(byte[] data) {
            //this.type = data[0];
            this.tovaritelnica = Arrays.copyOfRange(data, 1, 21);
        }
    }

    public static class tagRCMDBarcodeLine {
        public tagRClass type;         // 00   Тип
        public tagRSubClassBarcode subtype;        // 01       Подтип на командата
        public byte[] base64Text = new byte[64];

        public tagRCMDBarcodeLine(byte[] data) {
            //this.type = data[0];
            this.subtype = tagRSubClassBarcode.values()[data[1]];
            this.base64Text = Arrays.copyOfRange(data, 2, 66);
        }
    }


    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------


    public static class scDATE_TIME {
        /**
         * Example:
         * E3 07 03 12 → 18 March 2019
         * 10 37 32 → 16:55:50
         */

        public short year;
        public byte MM;
        public byte dd;

        public byte hh;
        public byte mm;
        public byte ss;
        public byte dst;


        public scDATE_TIME(byte[] data) {
            if (data.length > 8) throw new RuntimeException("Too big to fit in DATE_TIME");
            this.year = bytesToShort(new byte[]{data[0], data[1]});
            this.MM = data[2];
            this.dd = data[3];

            this.hh = data[4];
            this.mm = data[5];
            this.ss = data[6];
            this.dst = data[7];
        }
    }

}
