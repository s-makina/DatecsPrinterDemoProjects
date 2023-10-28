package com.datecs.demo.ui.main.tools; /**
 * The structured information offers a way to extract the necessary data for each fiscal document in EJ(issued so far).
 * You can search for document data in a global number or in an issue time interval.
 * After reading the data from EJ, they are arranged in the appropriate structures (classes)
 * in a form convenient for arithmetic operations with them.
 * <p>
 * This class supports DATECS DP-05, DP-25, DP-35, WP-50, DP-150
 */

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.FiscalResponse;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdEJournal;

import java.io.*;
import android.util.Base64;


public class cmdEJStructInfoB extends DatecsFiscalDevice {

    public enum DocTypeToRead {
        all_types,
        fiscal_receipts,
        daily_Z_reports,
        cash_in_out,
        daily_X_reports,
        non_fiscal_receipts,
        invoice_receipts,
        fiscal_receipts_storno,
        invoice_receipts_storno
    }

    private volatile boolean userBreak = false;

    /**
     * Read the next line of the document.
     * <p>
     * Supported only on  DP-05, DP-25, DP-35, WP-50, DP-150
     *
     * @return Presents the document read data as description - value
     * @throws FiscalException
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String readEjLinesToString() throws Exception {
        if (isConnectedPrinter())
            throw new UnsupportedOperationException("Not supported yet.");
        FiscalResponse R = new FiscalResponse(0);
        String res = "";
        byte[] data;
        while (!userBreak) {
            R = getConnectedECRV1().command125Variant1Version2();
            checkErrorCode();
            switch (R.get("ErrCode")) {
                case "P":
                    data = Base64.decode(R.get("Data"),0);
                    res += ParseInfoFromStructure_B(data);
                    break;
                case "F":
                    return res;
                default:
                    break;
            }

        }
        return null;
    }

    /**
     * Read the next line of the document.
     * <p>
     * Supported only on  DP-05, DP-25, DP-35, WP-50, DP-150
     *
     * @return Presents the document read data as DocInfo
     * @throws FiscalException
     * @throws IOException
     */

    public DocInfo readEjLinesInfo() throws Exception {
        if (isConnectedPrinter())
            throw new UnsupportedOperationException("Not supported yet.");
        FiscalResponse R = new FiscalResponse(0);
        DocInfo res = new DocInfo();
        byte[] data;
        while (!userBreak) {
            R = getConnectedECRV1().command125Variant1Version2();
            checkErrorCode();
            switch (R.get("ErrCode")) {
                case "P":
                    data = Base64.decode(R.get("Data"),0);
                    GetInfoFromStructure_B(data, res);
                    break;
                case "F":
                    return res;
                default:
                    break;
            }

        }
        return null;
    }

    private void GetInfoFromStructure_B(byte[] data, DocInfo res) throws UnsupportedEncodingException {
        StructuredInfoRegister_DeviceGroup_B.tagRClass type = getStructureType(data);
        switch (type) {
            case UNKNOWN:
                break;
            case PLUSELL:
            case DPxSELL:
            case VD_PLUSELL:
            case VD_DPxSELL: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDsell sell = new StructuredInfoRegister_DeviceGroup_B.tagRCMDsell(data);
                //Code here : Sell - use required information from the document
                break;
            }
            case PAYMENT: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDpay pay = new StructuredInfoRegister_DeviceGroup_B.tagRCMDpay(data);
                //Code here : Payment - use required information from the document
                break;
            }
            case OTS_NDB:
            case VD_OTS_NDB: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDperc discSurc = new StructuredInfoRegister_DeviceGroup_B.tagRCMDperc(data);
                //Code here : Discount/Surcharge - use required information from the document
                break;
            }
            case RA_PO: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDrapo rapo = new StructuredInfoRegister_DeviceGroup_B.tagRCMDrapo(data);
                //Code here : Rapo - use required information from the document
                break;
            }

            case QTYSET: {
                //не се използва
                break;
            }
            case PRCSET: {
                //не се използва
                break;
            }

            case ABONAT: {
                //не се използва
                break;
            }
            case BEGPAY: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDtotal total = new StructuredInfoRegister_DeviceGroup_B.tagRCMDtotal(data);
                //Code here : Total - use required information from the document
                break;
            }
            case BOTTLE: {
                //не се използва
                break;
            }
            case VD_BOTTLE: {
                //не се използва
                break;
            }
            case COUPON: {
                //не се използва
                break;
            }
            case TICKET: {
                //не се използва
                break;
            }
            case ALL_VOID: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDallvoid allVoid = new StructuredInfoRegister_DeviceGroup_B.tagRCMDallvoid(data);
                //Code here : All void - use required information from the document
                res.setRecIsCanceled(true);
                break;
            }
            case REPORTS: {
                //не се използва
                break;
            }
            case TEXT_LINE: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDtextline textLine = new StructuredInfoRegister_DeviceGroup_B.tagRCMDtextline(data);
                //Code here : Text line - use required information from the document
                break;
            }
            case OPEN_BON: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDopen open = new StructuredInfoRegister_DeviceGroup_B.tagRCMDopen(data);
                //Open receipt
                //open.strDateTime; //Дата и час на бона по който е сторното:
                res.setOperator(open.nOperator); //Номер на оператор
                res.setStornoOfDocument(open.strNumberDoc); //"Номер на бона, по който е сторното
                res.setInvoiceNum(open.invN ); //Номер на фактура
                res.setCreditNoteNum(open.strToInvoice); //"Номер на фактура, към която е сторното (за случай на кредитно известие)
                res.setStornoOfDocument(open.strNumberDoc); //Номер на бона, по който е сторното
                res.setFMNumber(new String(open.strFMnumber, "cp1251"));//Номер на фискалната памет на бона по който е сторното
                break;
            }
            case CLOSE_BON: {
                //не се използва
                break;
            }
            case HEADER_FOOTER: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDhdrftr headerFooter = new StructuredInfoRegister_DeviceGroup_B.tagRCMDhdrftr(data);
                break;
            }
            case PINPAD_INFO:                 //не се използва
                break;

            case INTERNAL:
                //не се използва
                break;


        }

    }


    public int[] SearchDocumentsInPeriod(String fromDT, String toDT) throws Exception {

        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedPrinter())
            throw new UnsupportedOperationException("Not supported yet.");

        if (isConnectedECR())
            R = getConnectedECRV1().command124Variant0Version0("1", fromDT, toDT);

        checkErrorCode();
        if (R.get("ErrCode").equals("P")) {
            int[] docRange = new int[R.getInt("LastDoc") - R.getInt("FirstDoc") + 1];
            int j = 0;

            for (int i = R.getInt("FirstDoc"); i <= R.getInt("LastDoc"); i++) {
                docRange[j] = i;
                j++;
            }
            return docRange;
        }
        return null;

    }

    /**
     * DATECS  DP-05, DP-25, DP-35, WP-50, DP-150
     *
     * @param DocType
     * @param DocNum
     * @return
     * @throws Exception
     */
    public cmdEJournal.EJ_DocumentsFound set_SearchByNumber(DocTypeToRead DocType, String DocNum) throws Exception {

        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedPrinter())
            throw new UnsupportedOperationException("Not supported yet.");

        if (isConnectedECR())
            R = getConnectedECRV1().command125Variant0Version0(DocNum, String.valueOf(DocType.ordinal()));

        checkErrorCode();
        if (R.get("ErrCode").equals("P"))
            return new cmdEJournal.EJ_DocumentsFound(
                    R.get("DocNumber"),
                    R.get("Date"),
                    R.get("Type"),
                    R.get("Znumber")
            );
        else return null;
    }


    private StructuredInfoRegister_DeviceGroup_B.tagRClass getStructureType(byte[] data) {
        return StructuredInfoRegister_DeviceGroup_B.tagRClass.getById(data[0]);
    }

    /**
     * Reads the raw data from the EJ and sorts it by description in a String.
     *
     * @param data
     * @return
     * @throws IOException
     */
    private String ParseInfoFromStructure_B(byte[] data) throws IOException {
        //var c = new StructuredInfoRegister_DeviceGroup_B(data);
        StructuredInfoRegister_DeviceGroup_B.tagRClass type = getStructureType(data);
        String rtxEJ = "";
        switch (type) {
            case UNKNOWN:
                break;
            case PLUSELL:
            case DPxSELL:
            case VD_PLUSELL:
            case VD_DPxSELL: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDsell sell = new StructuredInfoRegister_DeviceGroup_B.tagRCMDsell(data);
                rtxEJ += ("Sell:\t" + "\r\n");
                rtxEJ += ("Флаг за void операция:\t" + sell.cancel + "\r\n");
                rtxEJ += ("PLU номер:\t" + sell.fMyPluDB + "\r\n");
                rtxEJ += ("Данъчна група:\t" + sell.vat + "\r\n");
                rtxEJ += ("Единична цена:\t" + sell.prc + "\r\n");
                rtxEJ += ("Сума qty*prc:\t" + sell.suma + "\r\n"); // Сума = qty*prc
                rtxEJ += ("Номенклатурен код:\t" + sell.icode + "\r\n");
                rtxEJ += ("Баркод:\t" + sell.bcode + "\r\n");
                rtxEJ += ("Продадено количество:\t" + sell.qty + "\r\n");
                rtxEJ += ("Име:\t" + new String(sell.name, "cp1251") + "\r\n");
                rtxEJ += ("Тип отстъпка/надбавка:\t" + sell.typeIncDec + "\r\n");
                rtxEJ += ("Номер на департамент:\t" + sell.dep + "\r\n");
                rtxEJ += ("Процент или стойност:\t" + sell.sIncDec + "\r\n");
                rtxEJ += ("Номер на стокова група:\t" + sell.grp + "\r\n");
                rtxEJ += ("Разширено име:\t" + new String(sell.ext_name, "cp1251") + "\r\n");
                rtxEJ += ("Контролна сума:\t" + sell.checksum + "\r\n");
                rtxEJ += ("\r\n");
                break;
            }
            case PAYMENT: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDpay pay = new StructuredInfoRegister_DeviceGroup_B.tagRCMDpay(data);
                rtxEJ += ("Payment:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + pay.subtype + "\r\n");
                rtxEJ += ("Име на валута:\t" + new String(pay.name_currency, "cp1251") + "\r\n");
                rtxEJ += ("Въведена сума:\t" + pay.sInput + "\r\n");
                rtxEJ += ("Ресто:\t" + pay.sResto + "\r\n");
                rtxEJ += ("Ресто в алтернативна валута:\t" + pay.sRestoForeign + "\r\n");
                rtxEJ += ("Сума подадена от клиента в алт.валута:\t" + pay.sPYN + "\r\n");
                rtxEJ += ("Сума преизчислена в осн.валута" + pay.sPYosnov + "\r\n");
                rtxEJ += ("Сума преизчислена според курса:\t" + pay.sTLneosn + "\r\n");
                rtxEJ += ("Курс на основна -> алт.валута:\t" + pay.sExchRate + "\r\n");
                rtxEJ += ("Име на плащането:\t" + new String(pay.pay_name, "cp1251") + "\r\n");
                rtxEJ += ("Контролна сума:\t" + pay.checksum + "\r\n");
                break;
            }
            case OTS_NDB:
            case VD_OTS_NDB: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDperc discSurc = new StructuredInfoRegister_DeviceGroup_B.tagRCMDperc(data);
                rtxEJ += ("Discount/Surcharge:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + discSurc.subtype + "\r\n");
                rtxEJ += ("Флаг за void операция:\t" + discSurc.cancel + "\r\n");
                rtxEJ += ("Флаг за операция над STL:\t" + discSurc.flagSTL + "\r\n");
                rtxEJ += ("Данъчна група:\t" + discSurc.nVAT + "\r\n");
                rtxEJ += ("Департамент:\t" + discSurc.dep + "\r\n");
                rtxEJ += ("Номер на стокова група:\t" + discSurc.grp + "\r\n");
                rtxEJ += ("Сума на транзакцията:\t" + discSurc.sSuma + "\r\n");
                rtxEJ += ("Обща сума по дан. групи преди %STL:\t" + discSurc.sSTL + "\r\n");
                rtxEJ += ("Сума по дан.групи преди %STL:\t" + discSurc.sVAT + "\r\n");
                rtxEJ += ("Стойност на отстъпка/надбавка:\t" + discSurc.percIncDec + "\r\n");
                rtxEJ += ("Контролна сума:\t" + discSurc.checksum + "\r\n" + "\r\n");
                break;
            }
            case RA_PO: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDrapo rapo = new StructuredInfoRegister_DeviceGroup_B.tagRCMDrapo(data);
                rtxEJ += ("Rapo:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + rapo.subtype + "\r\n");
                rtxEJ += ("Име на валута:\t" + new String(rapo.name_currency, "cp1251") + "\r\n");
                rtxEJ += ("Въведена сума:\t" + rapo.sInput + "\r\n");
                rtxEJ += ("Курс на основна -> алт.валута:\t" + rapo.sExchRate + "\r\n");
                rtxEJ += ("Контролна сума:\t" + rapo.checksum + "\r\n" + "\r\n");
                break;
            }

            case QTYSET: {
                //не се използва
                break;
            }
            case PRCSET: {
                //не се използва
                break;
            }

            case ABONAT: {
                //не се използва
                break;
            }
            case BEGPAY: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDtotal total = new StructuredInfoRegister_DeviceGroup_B.tagRCMDtotal(data);
                rtxEJ += ("Total:\t" + "\r\n");
                rtxEJ += ("Име на валута:\t" + new String(total.name_currency, "cp1251") + "\r\n");
                rtxEJ += ("Обща сума:\t" + total.sSTL + "\r\n");
                rtxEJ += ("Данъчна група:\t" + total.sVAT + "\r\n");
                rtxEJ += ("Курс на основна -> алт.валута:\t" + total.sExchRate + "\r\n");
                rtxEJ += ("Контролна сума:\t" + total.checksum + "\r\n" + "\r\n");
                break;
            }
            case BOTTLE: {
                //не се използва
                break;
            }
            case VD_BOTTLE: {
                //не се използва
                break;
            }
            case COUPON: {
                //не се използва
                break;
            }
            case TICKET: {
                //не се използва
                break;
            }
            case ALL_VOID: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDallvoid allVoid = new StructuredInfoRegister_DeviceGroup_B.tagRCMDallvoid(data);
                rtxEJ += ("All void:\t" + "\r\n");
                rtxEJ += ("Брой корекции в дн. отчет:\t" + allVoid.nCorrections + "\r\n");
                rtxEJ += ("Обща сума:\t" + allVoid.sTL + "\r\n");
                rtxEJ += ("Данъчна група:\t" + allVoid.sVAT + "\r\n");
                rtxEJ += ("Контролна сума:\t" + allVoid.checksum + "\r\n" + "\r\n");
                break;
            }
            case REPORTS: {
                //не се използва
                break;
            }
            case TEXT_LINE: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDtextline textLine = new StructuredInfoRegister_DeviceGroup_B.tagRCMDtextline(data);
                rtxEJ += ("Text line:\t" + "\r\n");
                rtxEJ += ("Текст:\t" + new String(textLine.text, "cp1251") + "\r\n");
                rtxEJ += ("Контролна сума:\t" + textLine.checksum + "\r\n" + "\r\n");
                break;
            }
            case OPEN_BON: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDopen open = new StructuredInfoRegister_DeviceGroup_B.tagRCMDopen(data);
                rtxEJ += ("Open receipt:\t" + "\r\n");
                rtxEJ += ("Дата и час на бона по който е сторното:\t" + StructuredInfoRegister_DeviceGroup_B.DateToString(open.strDateTime) + "\r\n");
                rtxEJ += ("Номер на оператор:\t" + open.nOperator + "\r\n");
                rtxEJ += ("Номер на бон:\t" + open.nBon + "\r\n");
                rtxEJ += ("Флагове:\t" + open.flags + "\r\n");
                rtxEJ += ("Номер на фактура:\t" + open.invN + "\r\n");
                rtxEJ += ("Номер на фактура, към която е сторното (за случай на кредитно известие):\t" + open.strToInvoice + "\r\n");
                rtxEJ += ("Номер на бона, по който е сторното:\t" + open.strNumberDoc + "\r\n");
                rtxEJ += ("Тип на бона:\t" + open.klenType + "\r\n");
                rtxEJ += ("Тип на сторно операция:\t" + open.strType + "\r\n");//      Тип на сторно операцията: 0 - операторска грешка, 1 - връщане/рекламация
                rtxEJ += ("Номер на фискалната памет на бона по който е сторното:\t" + new String(open.strFMnumber, "cp1251") + "\r\n");
                rtxEJ += ("Уникален номер на продажба:\t" + new String(open.nSale, "cp1251") + "\r\n");
                rtxEJ += ("Позиция на десетичната точка:\t" + open.dp + "\r\n");
                rtxEJ += ("Номер на устройството:\t" + new String(open.deviceID, "cp1251") + "\r\n");
                rtxEJ += ("Контролна сума:\t" + open.checksum + "\r\n" + "\r\n");
                break;
            }
            case CLOSE_BON: {
                //не се използва
                break;
            }
            case HEADER_FOOTER: {
                StructuredInfoRegister_DeviceGroup_B.tagRCMDhdrftr headerFooter = new StructuredInfoRegister_DeviceGroup_B.tagRCMDhdrftr(data);
                rtxEJ += ("Header and footer:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + headerFooter.subtype + "\r\n");
                rtxEJ += ("Номер на ред:\t" + headerFooter.nLine + "\r\n");
                rtxEJ += ("Текст за отпечатване:\t" + new String(headerFooter.text, "cp1251") + "\r\n");
                rtxEJ += ("Контролна сума:\t" + headerFooter.checksum + "\r\n" + "\r\n");
                break;
            }
            case PINPAD_INFO:                 //не се използва
                break;

            case INTERNAL:
                //не се използва
                break;
        }

        return rtxEJ;
    }

    /**
     * @param docNum
     * @return
     * @throws Exception
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String ReadDocumentByNumber(int docNum) throws Exception {

        cmdEJournal.EJ_DocumentsFound df = new cmdEJStructInfoB().set_SearchByNumber(DocTypeToRead.all_types, String.valueOf(docNum));
        String res = "";
        if (df != null) {
            if (Integer.valueOf(df.getDocNumber()) == docNum)
                res += new cmdEJStructInfoB().readEjLinesToString();
        }
        if (df != null) {
        }
        return res;
    }

    /**
     * This example is used to issue a reversal (storno) document information.
     * <p>
     * The proposed demonstration is used to find a document in the EJ and extract the necessary data:
     * -Type of document
     * -Date and time of finishing
     *
     * @param docNum
     * @return
     * @throws Exception
     */


    public DocInfo ReadDocInfo(int docNum) throws Exception {
        cmdEJournal.EJ_DocumentsFound df = new cmdEJStructInfoB().set_SearchByNumber(DocTypeToRead.all_types, String.valueOf(docNum));
        DocInfo res;
        cmdEJStructInfoB cmd = new cmdEJStructInfoB();
        if (df == null) return null;
        if (Integer.valueOf(df.getDocNumber()) == docNum) {
            res = cmd.readEjLinesInfo();
            res.setDate(df.getDate().split(" ")[0]);
            res.setTime(df.getDate().split(" ")[1]);
            res.setType(DocInfo.DocumentType.values()[Integer.parseInt(df.getType())]);
            return res;
        }
        return null;
    }

    public static class DocInfo {
        private long invoiceNum;

        public boolean ismCanceled() {
            return mCanceled;
        }

        private boolean mCanceled = false;

        public void setRecIsCanceled(boolean b) {
            mCanceled = b;
        }

        public void setInvoiceNum(long invoiceNum) {
            this.invoiceNum = invoiceNum;
        }

        public long getInvoiceNumber() {
            return invoiceNum;
        }

        public enum DocumentType {
            notUsed,
            fiscal,     //1
            zReport,    //2
            cashIn,     //3
            cashOut,    //4
            xReport,    //5
            nonFiscal,  //6
            invoice,    //7
            storno,     //8
            creditNote  //9
        }

        private DocumentType foundDocumentType;
        private String date;            //DD-MM-YY Date of document
        private String time;            // hh:mm:ss  Time of document
        private int operator;           //Code of the operator who issued the document
        private int stornoOfDocument;   //Reversal doc number
        private long creditNoteNum;     //Invoice number in case of credit note ( Storno of invoice )
        private String fmNumber;        //Fiscal memory number of storno

        public DocInfo() {

        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getOperator() {
            return operator;
        }

        public void setOperator(int operator) {
            this.operator = operator;
        }

        public int getStornoOfDocument() {
            return stornoOfDocument;
        }

        public long getCreditNoteNum() {
            return creditNoteNum;
        }

        public String getFmNumber() {
            return fmNumber;
        }

        public void setFmNumber(String fmNumber) {
            this.fmNumber = fmNumber;
        }


        public DocumentType getFoundDocumentType() {
            return foundDocumentType;
        }


        public void setStornoOfDocument(int stornoOfDocument) {
            this.stornoOfDocument = stornoOfDocument;
        }


        public void setCreditNoteNum(long num) {
            creditNoteNum = num;
        }

        public void setFMNumber(String fmNumber) {
            this.fmNumber = fmNumber;

        }

        public void setDate(String date) {
            this.date = date;

        }

        public void setType(DocumentType type) {
            foundDocumentType = type;

        }
    }
}





