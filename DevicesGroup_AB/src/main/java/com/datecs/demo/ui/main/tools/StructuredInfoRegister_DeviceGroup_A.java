package com.datecs.demo.ui.main.tools;

public class StructuredInfoRegister_DeviceGroup_A {
    private static final int COL = 48; //used to align text

    /**
     * Type of document
     */
   public enum RecType {
        fiscal,
        reversal,
        invoice,
        credit_note, //Storno of invoice
        cancelled,
        cancelled_storno,
        cancelled_invoice,
        cancelled_credit_note,
    }

    /**
     * Structured information on the receipt. Once on the receipt.
     */
    public static class tagSubClassU {

        String UNP;      //Unique Sale ID. 21 characters with the XXXXXXXX-YYYY-NNNNNNN format.
        int OperNum;     //Operator ID. Between 1 and 16.
        int TillNum;     //Cash register location number. Up to 5 digits
        RecType recType; //Receipt type: 0: fiscal; 1: invoice; 2: reversal; 3: credit note; 4: cancelled.
        int DocNum;      //Global document number.

        public tagSubClassU(String UNP, int operNum, int tillNum, RecType recType, int docNum) {
            this.UNP = UNP;
            OperNum = operNum;
            TillNum = tillNum;
            this.recType = recType;
            DocNum = docNum;
        }


        public tagSubClassU(String dataLine) {
            String[] data = dataLine.split(",");
            this.UNP = data[1];
            this.OperNum = Integer.parseInt(data[2]);
            this.TillNum = Integer.parseInt(data[3]);
            this.recType = RecType.values()[Integer.parseInt(data[4])];
            DocNum = Integer.parseInt(data[5]);
        }


        public String getStringData() {
            String res = alignRight("UNP :", UNP, COL);
            res += alignRight("Operator ID :", OperNum, COL);
            res += alignRight("Till Number :", TillNum, COL);
            res += alignRight("Receipt type :", recType.name(), COL);
            res += alignRight("Document number :", DocNum, COL);
            return res;
        }
    }


    /**
     * Structured decimal digit and tax rate information. Once on the receipt
     */
    public static class tagSubClassV {
        int DecRecTax; // rate record number in the FP. Counted starting from 1.
        int Decimals;  // Decimal digits. 0 or 2.
        int TaxRateA;  // Decimal rate A as a percentage.
        int TaxRateB;  // Decimal rate B as a percentage.
        int TaxRateC;  // Decimal rate C as a percentage.
        int TaxRateD;  // Decimal rate D as a percentage.
        int TaxRateE;  // Decimal rate E as a percentage.
        int TaxRateF;  // Decimal rate F as a percentage.
        int TaxRateG;  // Decimal rate G as a percentage.
        int TaxRateH;  // Decimal rate H as a percentage.

        public tagSubClassV(String dataLine) {
            String[] data = dataLine.split(",");
            DecRecTax = Integer.parseInt(data[1]);
            Decimals = Integer.parseInt(data[2]);
            TaxRateA = Integer.parseInt(data[3]);
            TaxRateB = Integer.parseInt(data[4]);
            TaxRateC = Integer.parseInt(data[5]);
            TaxRateD = Integer.parseInt(data[6]);
            TaxRateE = Integer.parseInt(data[7]);
            TaxRateF = Integer.parseInt(data[8]);
            TaxRateG = Integer.parseInt(data[9]);
            TaxRateH = Integer.parseInt(data[10]);

        }

        public String getStringData() {
            String res = alignRight("Rate record number :", DecRecTax, COL);
            res += alignRight("Decimal digits :", Decimals, COL);
            res += alignRight("Decimal rate A% :", TaxRateA, COL);
            res += alignRight("Decimal rate B% :", TaxRateB, COL);
            res += alignRight("Decimal rate C% :", TaxRateC, COL);
            res += alignRight("Decimal rate D% :", TaxRateD, COL);
            res += alignRight("Decimal rate E% :", TaxRateE, COL);
            res += alignRight("Decimal rate F% :", TaxRateF, COL);
            res += alignRight("Decimal rate G% :", TaxRateG, COL);
            res += alignRight("Decimal rate H% :", TaxRateH, COL);
            return res;
        }
    }

    /**
     * Structured sale or adjustment information. Can appear multiple times on the receipt.
     */
    public static class tagSubClassR {
        int TaxGrPos;               // Tax group (between 1 and 8).
        int TaxRate;                // Tax rate as a percentage.
        double SinglePrice;         // Unit price. Negative for adjustments.
        double Quantity;            // Quantity. Negative for adjustments.
        double Discount_MarkUp;     // Discount/mark-up (depending on the sign). The sign is reversed for adjustments.
        double Price;               // Price.
        String ArticleName;         // Sold product name. If it contains two lines, the separator is <TAB>.

        public tagSubClassR(String dataLine) {
            String[] data = dataLine.split(",");
            TaxGrPos = Integer.parseInt(data[1]);
            TaxRate = Integer.parseInt(data[2]);
            SinglePrice = Double.valueOf(data[3]);
            Quantity = Double.valueOf(data[4]);
            Discount_MarkUp = Double.valueOf(data[5]);
            Price = Double.valueOf(data[6]);
            ArticleName = (data.length > 7) ? data[7] : "";
        }

        public String getStringData() {
            String res = "";
            res += alignRight("Tax group  :", TaxGrPos, COL);
            res += alignRight("Tax rate % :", TaxRate, COL);
            res += alignRight("Unit price :", SinglePrice, COL);
            res += alignRight("Quantity   :", Quantity, COL);
            res += alignRight("Discount/mark-up :", Discount_MarkUp, COL);
            res += alignRight("Price :", Price, COL);
            res += alignRight("Sold product name :", ArticleName, COL);
            return res;
        }
    }

    /**
     * Structured group discount/mark-up information. Can appear multiple times on the receipt
     */
    public static class tagSubClassM {
        double Discount_MarkUp;         // Discount/mark-up (depending on the sign).
        double PercentDiscount_MarkUp;  //Discount/mark-up  percentage. The field can be empty, if it is included in a total.
        double Subtotal;                // Subtotal after the operation.

        public tagSubClassM(String dataLine) {
            String[] data = dataLine.split(",");
            Discount_MarkUp = (data.length > 1) ? Double.valueOf(data[1]) : 0.00;
            PercentDiscount_MarkUp = (data.length > 2) && (!data[2].isEmpty()) ? Double.parseDouble(data[2]) : 0.00;
            Discount_MarkUp = (data.length > 3) ? Double.valueOf(data[3]) : 0.00;
            Subtotal = (data.length > 4) ? Double.valueOf(data[4]) : 0.00;

        }

        public String getStringData() {
            String res = alignRight("Discount/mark-up", Discount_MarkUp, COL);
            res += alignRight("Discount/mark-up  percentage", PercentDiscount_MarkUp, COL);
            res += alignRight("Subtotal ", Subtotal, COL);
            return res;
        }
    }

    /**
     * Structured information on the accumulated sums on the receipt. Once on the receipt.
     */
    public static class tagSubClassT {
        int DiscCnt;// Number of discounts.
        double DiscSum;// Discount total.
        int MarkUpCnt;// Number of mark-ups.
        double MarkUpSum;// Mark-up total.
        int VoidCnt;// Number of adjustments.
        double VoidSum;// Adjustment total.
        int SalesCnt;// Number of mark-ups.
        double Total;// Receipt total.
        double TotGrA;// Tax group total A.
        double TotGrB;// Tax group total B.
        double TotGrC;// Tax group total C.
        double TotGrD;// Tax group total D.
        double TotGrE;// Tax group total E.
        double TotGrF;// Tax group total F.
        double TotGrG;// Tax group total G.
        double TotGrH;// Tax group total H.


        public tagSubClassT(String dataLine) {
            String[] data = dataLine.split(",");
            DiscCnt = Integer.valueOf(data[1]);
            DiscSum = Double.valueOf(data[2]);
            MarkUpCnt = Integer.valueOf(data[3]);
            MarkUpSum = Double.valueOf(data[4]);

            VoidCnt = Integer.valueOf(data[5]);
            VoidSum = Double.valueOf(data[6]);
            SalesCnt = Integer.valueOf(data[7]);
            Total = Double.valueOf(data[8]);

            TotGrA = Double.valueOf(data[9]);
            TotGrB = Double.valueOf(data[10]);
            TotGrC = Double.valueOf(data[11]);
            TotGrD = Double.valueOf(data[12]);
            TotGrE = Double.valueOf(data[13]);
            TotGrF = Double.valueOf(data[14]);
            TotGrG = Double.valueOf(data[15]);
            TotGrH = Double.valueOf(data[16]);

        }

        public String getStringData() {

            String res = "";
            res += alignRight("Number of discounts :", DiscCnt, COL);
            res += alignRight("Discount total :", DiscSum, COL);
            res += alignRight("Number of mark-ups :", MarkUpCnt, COL);
            res += alignRight("Mark-up total :", MarkUpSum, COL);
            res += alignRight("Number of adjustments :", VoidCnt, COL);
            res += alignRight("Adjustment total :", VoidSum, COL);
            res += alignRight("Number of mark-ups :", SalesCnt, COL);
            res += alignRight("Receipt total :", Total, COL);
            res += alignRight("Tax group total A :", TotGrA, COL);
            res += alignRight("Tax group total B :", TotGrB, COL);
            res += alignRight("Tax group total C :", TotGrC, COL);
            res += alignRight("Tax group total D :", TotGrD, COL);
            res += alignRight("Tax group total E :", TotGrE, COL);
            res += alignRight("Tax group total F :", TotGrF, COL);
            res += alignRight("Tax group total G :", TotGrG, COL);
            res += alignRight("Tax group total H :", TotGrH, COL);

            return res;
        }
    }

    /**
     * Structured paid amount information. Once on the receipt.
     */
    public static class tagSubClassP {
        double CashPayd;// Paid in cash.
        double CheqPayd;// Paid by check.
        double CardPayd;// Paid by card.
        double CredPayd;// Paid by credit card.

        double MorePayd[] = new double[11];// Additional payment

        public tagSubClassP(String dataLine) {

            String[] data = dataLine.split(",");
            CashPayd = Double.valueOf(data[1]);
            CheqPayd = Double.valueOf(data[2]);
            CardPayd = Double.valueOf(data[3]);
            CredPayd = Double.valueOf(data[4]);
            if (data.length == 15)
                for (int i = 0; i < 11; i++)
                    MorePayd[i] = Double.valueOf(data[5 + i]);
        }

        public String getStringData() {
            String res = "";
            res += alignRight("Paid in cash :", CashPayd, COL);
            res += alignRight("Paid by check :", CheqPayd, COL);
            res += alignRight("Paid by card :", CardPayd, COL);
            res += alignRight("Paid by credit card :", CredPayd, COL);
            if (MorePayd.length == 10)
                for (int i = 0; i < 11; i++)
                    res += alignRight("Additional payment :" + i, MorePayd[i], COL);
            return res;
        }
    }

    enum ReversalType {
        operator_error,
        return_claim,
        tax_base_reduction
    }

    /**
     * Structured paid amount information. Once on the receipt.
     */
    public static class tagSubClassS {
        ReversalType reversalType; //Reversal type: 0: operator error; 1: return/claim; 2: tax base reduction
        int StornedDocNo;// Reversed document number.
        String StornedDT;// Reversed document date and time in the DD-MM-YYYY hh:mm:ss format.
        String StornedFMIN;// Fiscal memory ID of the reversed document.
        String StornedUNP;// Unique Sale ID of the reversed document.
        long StrornedInvoice;//Invoice number. If not a credit note, contains 0.


        public tagSubClassS(String dataLine) {
            String[] data = dataLine.split(",");
            reversalType = ReversalType.values()[Integer.parseInt(data[1])];
            StornedDocNo = Integer.valueOf(data[2]);
            StornedDT = data[3];
            StornedFMIN = data[4];
            StornedUNP = data[5];
            StrornedInvoice = Long.valueOf(data[6]);
        }

        public String getStringData() {
            String res = "";
            res += alignRight("Reversal type:", reversalType, COL);
            res += alignRight("Reversed document number :", StornedDocNo, COL);
            res += alignRight("Reversed document date and time :", StornedDT, COL);
            res += alignRight("Fiscal memory ID :", StornedFMIN, COL);
            res += alignRight("Unique Sale ID :", StornedUNP, COL);
            res += alignRight("Invoice number :", StrornedInvoice, COL);
            return res;
        }
    }

    enum PINtype {
        BULSTAT, PIN, Personal_Number, Company_Number
    }

    /**
     * Structured invoice information. Once in invoice-type receipts.
     */
    public static class tagSubClassI {
        long Invoice; //Invoice number.
        String PIN;  //ID.
        PINtype piNtype; //Type of PIN

        public tagSubClassI(String dataLine) {
            String[] data = dataLine.split(",");
            Invoice = Long.valueOf(data[1]);
            PIN = data[2];
            piNtype = PINtype.values()[Integer.parseInt(data[3])];
        }

        public String getStringData() {
            String res = "";
            res += alignRight("Invoice number :", Invoice, COL);
            res += alignRight("PIN :", PIN, COL);
            res += alignRight("PIN type:", piNtype, COL);
            return res;
        }
    }

    /**
     * Receipt start and end date and time
     */
    public static class tagSubClassD {
        private String StartDT;// Start date and time in the DD-MM-YYYY hh:mm:ss format. The date printed on the document.

        public String getStartDT() {
            return StartDT;
        }

        public String getEndDT() {
            return EndDT;
        }

        private String EndDT;// date and time in the DD-MM-YYYY hh:mm:ss format.

        public tagSubClassD(String dataLine) {
            String[] data = dataLine.split(",");
            StartDT = data[1];
            EndDT = data[2];
        }

        public String getStringData() {
            String res = "";
            res += alignRight("Receipt opened :", StartDT, COL);
            res += alignRight("Receipt closed :", EndDT, COL);
            return res;
        }
    }

    /**
     * Daily report with clearing.
     */
    public static class tagSubClassZ {
        int ZNo;// Z-report number.
        int DocNum;//  Document number.
        int FiscNum;//  Last fiscal document number. It may be 0 if it is not issued.
        String DateTime;//  Date and time in format DD-MM-YYYY hh:mm:ss.
        double Total;//  Total amount of sales per day.
        double StornoSum;//  Total amount of refunds per day.
        double CashSum;//  Cash availability.

        public tagSubClassZ(String dataLine) {
            String[] data = dataLine.split(",");
            ZNo = Integer.valueOf(data[1]);
            DocNum = Integer.valueOf(data[2]);
            FiscNum = Integer.valueOf(data[3]);
            DateTime = data[4];
            Total = Double.valueOf(data[5]);
            StornoSum = Double.valueOf(data[6]);
            CashSum = Double.valueOf(data[7]);
        }

        public String getStringData() {
            String res = "";
            res += alignRight("Z-report number :", ZNo, COL);
            res += alignRight("Document number :", DocNum, COL);
            res += alignRight("Last fiscal document number :", FiscNum, COL);
            res += alignRight("Date and time :", DateTime, COL);
            res += alignRight("Total :", Total, COL);
            res += alignRight("Total amount of refunds per day :", StornoSum, COL);
            res += alignRight("Cash availability :", CashSum, COL);
            return res;
        }
    }

    /**
     * Serve In / Out sums.
     */
    public static class tagSubClassQ {
        int DocNum;
        String StartDT;
        double ServeInOut;
        Double CashSum;

        public tagSubClassQ(String dataLine) {
            String[] data = dataLine.split(",");
            DocNum = Integer.valueOf(data[1]);
            StartDT = data[2];
            ServeInOut = Double.valueOf(data[3]);
            CashSum = Double.valueOf(data[4]);
        }

        public String getStringData() {
            String res = "";
            res += alignRight("Document number :", DocNum, COL);
            res += alignRight("Date Time :", StartDT, COL);
            res += alignRight("Serve In / Out sum :", ServeInOut, COL);
            res += alignRight("Cash availability :", CashSum, COL);
            return res;
        }
    }

    /**
     * @return - Align the string to the right at the end of the printer columns.
     * <p>
     * Example:
     * abc             123
     * abcd          12334
     * abcdef     12233435
     */
    public static String alignRight(String strLeft, Object r, int cloumn) {
        String strRight = String.valueOf(r);
        if (cloumn < (strLeft.length() + strRight.length())) cloumn = strLeft.length() + strRight.length();
        return String.format("%s%" + (cloumn - strLeft.length()) + "s", strLeft, strRight + "\n");
    }


}
