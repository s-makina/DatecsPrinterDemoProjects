package com.datecs.demo.ui.main.tools;
import android.util.Base64;

import com.datecs.fiscalprinter.SDK.FiscalErrorCodesV2;
import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.FiscalResponse;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;

import java.io.*;


public class cmdEJStructInfo extends DatecsFiscalDevice {
    private volatile boolean userBreak = false;

    public String ReadDocumentByNumber(int docNumber,DocTypeToRead recType) throws Exception {
        if (SetDocumentToRead(docNumber,recType) == FiscalErrorCodesV2.OK) {
            return readEjData(docNumber);
        }
        return null;
    }

    public int[] SearchDocumentsInPeriod(String fromDT, String toDT) throws IOException, FiscalException {

        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedDeviceV2()) {
            R = getConnectedModelV2().command124Variant0Version0(fromDT, toDT, "0");
            checkErrorCode(R);
            int firstDoc = Integer.parseInt(R.getString("firstDoc"));
            int lastDoc = Integer.parseInt(R.getString("lastDoc"));
            int[] res = new int[lastDoc - firstDoc + 1];
            int j = 0;
            for (int i = firstDoc; i <= lastDoc; i++) {
                res[j] = i;
                j++;
            }
            return res;
        } else return null;

    }


    /**
     * Set document to read;
     *
     * @return
     * @throws IOException
     * @throws FiscalException
     */
    public int SetDocumentToRead(int docNum, DocTypeToRead recType) throws IOException, FiscalException {

        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedDeviceV2()) {
            try {
                R = getConnectedModelV2().command125Variant0Version0(String.valueOf(docNum), String.valueOf(recType.ordinal()));
            } catch (FiscalException e) {
                if ((e.getErrorCode() != FiscalException.ERR_EJ_NO_RECORDS) && (e.getErrorCode() != FiscalException.ERR_END_OF_DATA)) {
                    throw e;
                } else return e.getErrorCode();
            }
        }
        return R.getInt("errorCode");
    }


    /**
     * @param docNum
     * @return
     * @throws FiscalException
     * @throws IOException
     */
    private String readEjData(int docNum) throws IOException, FiscalException {
        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedDeviceV2()) {
            String res = "";
            while (!userBreak) {
                try {
                    R = getConnectedModelV2().command125Variant2Version0("", "");
                    byte[] data = Base64.decode(R.get("Data"), 0);
                    res += ParseInfoFromStructure_C(data);

                } catch (FiscalException e) {
                    int i = e.getErrorCode();
                    if ((i == FiscalException.ERR_EJ_NO_RECORDS) || (i == FiscalException.ERR_END_OF_DATA))
                        return res;
                    else throw e;
                }

            }
            return res;
        } else return null;
    }

    private StructuredInfoRegister_DeviceGroup_C.tagRClass getStructureType(byte[] data) {
        return StructuredInfoRegister_DeviceGroup_C.tagRClass.getById(data[0]);
    }


    private String ParseInfoFromStructure_C(byte[] data) throws IOException {

        StructuredInfoRegister_DeviceGroup_C.tagRClass type = getStructureType(data);
        String rtxEJ = "";
        switch (type) {
            case UNKNOWN:
                break;
            case PLUSELL:
            case DPxSELL:
            case VD_PLUSELL:
            case VD_DPxSELL: {
                //StructuredInfoRegister_DeviceGroup_C.tagRCMDsell sell =
                //rtxEJ.SelectionFont = new Font(rtxEJ.Font, FontStyle.Bold);
                StructuredInfoRegister_DeviceGroup_C.tagRCMDsell sell = new StructuredInfoRegister_DeviceGroup_C.tagRCMDsell(data);
                rtxEJ += ("Sell:\t" + "\r\n");
                rtxEJ += ("Флаг за void операция:\t" + sell.cancel + "\r\n");
                rtxEJ += ("PLU номер:\t" + sell.fMyPluDB + "\r\n");
                rtxEJ += ("Данъчна група:\t" + sell.vat + "\r\n");
                rtxEJ += ("Единична цена:\t" + sell.prc + "\r\n");
                rtxEJ += ("Сума:\t" + sell.suma + "\r\n"); // Сума = qty*prc
                rtxEJ += ("Номенклатурен код:\t" + sell.icode + "\r\n");
                rtxEJ += ("Баркод:\t" + sell.bcode + "\r\n");
                rtxEJ += ("Продадено количество:\t" + sell.qty + "\r\n");
                rtxEJ += ("Име:\t" + new String((sell.name), "cp1251").trim() + "\r\n");
                rtxEJ += ("Тип отстъпка/надбавка:\t" + sell.typeIncDec + "\r\n");
                rtxEJ += ("Процент или стойност:\t" + sell.sIncDec + "\r\n");
                rtxEJ += ("Номер на департамент:\t" + sell.dep + "\r\n");
                rtxEJ += ("Номер на стокова група:\t" + sell.grp + "\r\n");
                rtxEJ += ("Мерна единица:\t" + sell.unit + "\r\n");
                rtxEJ += ("Сторно:\t" + sell.fVDstorno + "\r\n");
                rtxEJ += ("\r\n");
                break;
            }
            case PAYMENT: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDpay pay = new StructuredInfoRegister_DeviceGroup_C.tagRCMDpay(data);
                //rtxEJ.SelectionFont = new Font(rtxEJ.Font, FontStyle.Bold);
                rtxEJ += ("Payment:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + pay.subtype + "\r\n");
                rtxEJ += ("Име на валута:\t" + new String((pay.name_currency), "cp1251").replace((char) 0, ' ') + "\r\n");
                rtxEJ += ("Въведена сума:\t" + pay.sInput + "\r\n");
                rtxEJ += ("Ресто:\t" + pay.sResto + "\r\n");
                rtxEJ += ("Ресто в алтернативна валута:\t" + pay.sRestoForeign + "\r\n");
                rtxEJ += ("Сума подадена от клиента в алт.валута:\t" + pay.sPYN + "\r\n");
                rtxEJ += ("Сума преизчислена в осн.валута" + pay.sPYosnov + "\r\n");
                rtxEJ += ("Сума преизчислена според курса:\t" + pay.sTLneosn + "\r\n");
                rtxEJ += ("RRN (Уникален номер на транзакция):\t" + new String((pay.RRN), "cp1251").trim() + "\r\n");
                rtxEJ += ("Курс на основна -> алт.валута:\t" + pay.sExchRate + "\r\n");
                rtxEJ += ("Име на плащането:\t" + new String(pay.pay_name, "cp1251").trim() + " " + "\r\n" + "\r\n");
                break;
            }
            case OTS_NDB:
            case VD_OTS_NDB: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDperc discSurc = new StructuredInfoRegister_DeviceGroup_C.tagRCMDperc(data);
                //rtxEJ.SelectionFont = new Font(rtxEJ.Font, FontStyle.Bold);
                rtxEJ += ("Discount/Surcharge:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + discSurc.subtype + "\r\n");
                rtxEJ += ("Флаг за void операция:\t" + discSurc.cancel + "\r\n");
                rtxEJ += ("Флаг за операция над STL:\t" + discSurc.flagSTL + "\r\n");
                rtxEJ += ("Данъчна група:\t" + discSurc.nVAT + "\r\n");
                rtxEJ += ("Департамент:\t" + discSurc.dep + "\r\n");
                rtxEJ += ("Номер на стокова група:\t" + discSurc.grp + "\r\n");
                rtxEJ += ("Сума на транзакцията:\t" + discSurc.sSuma + "\r\n");
                rtxEJ += ("Обща сума по дан. групи преди %STL:\t" + discSurc.sSTL + "\r\n");
                String sBrut = null;
                for (long s : discSurc.sBRUT) sBrut += s + " ";
                rtxEJ += ("Сума бруто:\t" + sBrut + "\r\n");
                rtxEJ += ("Стойност на отстъпка/надбавка:\t" + discSurc.percIncDec + "\r\n");
                rtxEJ += ("Сторно:\t" + discSurc.fVDstorno + "\r\n" + "\r\n");
                break;
            }
            case RA_PO: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDrapo rapo = new StructuredInfoRegister_DeviceGroup_C.tagRCMDrapo(data);
                rtxEJ += ("Rapo:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + rapo.subtype + "\r\n");
                rtxEJ += ("Име на валута:\t" + new String((rapo.name_currency), "cp1251").replace((char) 0, ' ') + "\r\n");
                rtxEJ += ("Въведена сума:\t" + rapo.sInput + "\r\n");
                rtxEJ += ("Курс на основна -> алт.валута:\t" + rapo.sExchRate + "\r\n" + "\r\n");
                break;
            }
            case ABONAT: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDAbonat abonat = new StructuredInfoRegister_DeviceGroup_C.tagRCMDAbonat(data);
                rtxEJ += ("Abonat:\t" + "\r\n");
                rtxEJ += ("Абонатен номер:\t" + new String(abonat.AbonatNomer, "cp1251").trim() + "\r\n");
                rtxEJ += ("Абонатна карта:\t" + new String(abonat.AbonatKarta, "cp1251").trim() + "\r\n" + "\r\n");
                break;
            }
            case BEGPAY: {
                String r = "";
                StructuredInfoRegister_DeviceGroup_C.tagRCMDtotal total = new StructuredInfoRegister_DeviceGroup_C.tagRCMDtotal(data);
                rtxEJ += ("Total:\t" + "\r\n");
                rtxEJ += ("Име на валута:\t" + new String((total.name_currency), "cp1251").replace((char) 0, ' ') + "\r\n");
                rtxEJ += ("Обща сума:\t" + total.sSTL + "\r\n");

                for (long val : total.sBRUT) {
                    r += val + " ";

                }
                rtxEJ += ("Брутна сума:\t" + r + "\r\n");
                rtxEJ += ("Курс на основна -> алт.валута:\t" + total.sExchRate + "\r\n" + "\r\n");
                break;
            }
            case ALL_VOID: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDallvoid allVoid = new StructuredInfoRegister_DeviceGroup_C.tagRCMDallvoid(data);
                rtxEJ += ("All void:\t" + "\r\n");
                rtxEJ += ("Обща сума:\t" + allVoid.sTL + "\r\n");
                rtxEJ += ("Брутна сума:\t" + allVoid.sBRUT + "\r\n");
                break;
            }
            case TEXT_LINE: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDtextline textLine = new StructuredInfoRegister_DeviceGroup_C.tagRCMDtextline(data);
                rtxEJ += ("Text line:\t" + "\r\n");
                rtxEJ += ("Текст:\t" + new String(textLine.text, "cp1251").trim() + "\r\n" + "\r\n"); //GetStringFromByteArr
                break;
            }
            case OPEN_BON: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDopen open = new StructuredInfoRegister_DeviceGroup_C.tagRCMDopen(data);
                rtxEJ += ("Open receipt:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + open.subtype + "\r\n");
                rtxEJ += ("Номер на оператор:\t" + open.nClerk + "\r\n");
                rtxEJ += ("Тип сторно:\t" + open.stornoType + "\r\n");
                rtxEJ += ("Позиция на десетичната точка:\t" + open.dec_point + "\r\n");
                rtxEJ += ("Номер на фактура, към която е сторното:\t" + open.nInvoice + "\r\n");
                rtxEJ += ("Номер на фактура, по който е сторното:\t" + open.nInvoiceStorno + "\r\n");
                rtxEJ += ("Номер на бона, по който е сторното:\t" + open.strNumberDoc + "\r\n");
                rtxEJ += ("Дата и час:\t" + StructuredInfoRegister_DeviceGroup_C.DateToString(open.bonDateTime) + "\r\n");
                rtxEJ += ("Дата и час на сторното:\t" + StructuredInfoRegister_DeviceGroup_C.DateToString(open.strDateTime) + "\r\n");
                rtxEJ += ("Карта на плащанията:\t");
                for (int i = 0; i < open.paymentRemap.length; i++) {
                    rtxEJ += (open.paymentRemap[i] + " ");
                }
                rtxEJ += ("\r\n");
                rtxEJ += ("Номер на фискалната памет на бона:\t" + new String(open.strFMnumber, "cp1251").trim() + "\r\n"); //GetStringFromCharArr
                rtxEJ += ("Уникален номер на продажба:\t" + new String(open.nSale).trim() + "\r\n" + "\r\n");

                break;
            }

            case CLOSE_BON: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDClose close = new StructuredInfoRegister_DeviceGroup_C.tagRCMDClose(data);
                rtxEJ += ("Close receipt:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + close.subtype + "\r\n");
                rtxEJ += ("Тип лична информация:\t" + close.ClientEikType + "\r\n");
                rtxEJ += ("Номер на фактура:\t" + close.nInvoice + "\r\n");
                rtxEJ += ("Дата и час:\t" + StructuredInfoRegister_DeviceGroup_C.DateToString(close.bonDateTime) + "\r\n");
                rtxEJ += ("Лична клиентска информация:\t" + new String(close.clientEIK).trim() + "\r\n"); //GetStringFromCharArr
                rtxEJ += ("Идентификационен номер:\t" + new String(close.IDnumber).trim() + "\r\n" + "\r\n"); //GetStringFromCharArr
                break;
            }

            case INVOICE: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDInvoice invoice = new StructuredInfoRegister_DeviceGroup_C.tagRCMDInvoice(data);
                rtxEJ += ("Invoice:\t" + "\r\n");
                rtxEJ += ("Номер на фактура:\t" + invoice.invoiceNumber + "\r\n" + "\r\n");
                break;
            }

            case ADD_INFO: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDAddInfo addInfo = new StructuredInfoRegister_DeviceGroup_C.tagRCMDAddInfo(data);
                rtxEJ += ("Additional information:\t" + "\r\n");
                rtxEJ += ("Бонус:\t" + new String(addInfo.bonus, "cp1251").trim() + "\r\n"); //GetStringFromCharArr
                rtxEJ += ("Име:\t" + new String(addInfo.name, "cp1251").trim() + "\r\n");//GetStringFromCharArr
                rtxEJ += ("Брой данъчни групи:\t" + new String(addInfo.TaxN, "cp1251").trim() + "\r\n");
                rtxEJ += ("Тип данъчни групи:\t" + addInfo.typeTaxN + "\r\n");
                rtxEJ += ("Име на получател:\t" + new String(addInfo.RECname, "cp1251").trim() + "\r\n");
                rtxEJ += ("Брой данъчни групи:\t" + new String(addInfo.VATN, "cp1251").trim() + "\r\n" + "\r\n");
                break;
            }
            case CARGO: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDCargo cargo = new StructuredInfoRegister_DeviceGroup_C.tagRCMDCargo(data);
                rtxEJ += ("Cargo:\t" + "\r\n");
                rtxEJ += ("Товарителница:\t" + new String(cargo.tovaritelnica, "cp1251").trim() + "\r\n" + "\r\n");
                break;
            }
            case BARCODE_LINE: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDBarcodeLine barcode = new StructuredInfoRegister_DeviceGroup_C.tagRCMDBarcodeLine(data);
                rtxEJ += ("Barcode line:\t" + "\r\n");
                rtxEJ += ("Подтип:\t" + barcode.subtype + "\r\n");
                rtxEJ += ("Текст base64 формат:\t" + new String(barcode.base64Text) + "\r\n" + "\r\n");
                break;
            }
                    /*
                case StructuredInfoRegister_DeviceGroup_C.tagRClass.HEADER_FOOTER:
                    {
                        var headerFooter = c.CmdHeaderFooter;
                        rtxEJ.SelectionFont = new Font(rtxEJ.Font, FontStyle.Bold);
                        rtxEJ.add("Header and footer:\t" + "\r\n");
                        rtxEJ.add("Подтип:\t" + headerFooter.subtype.ToString() + "\r\n");
                        rtxEJ.add("Номер на ред:\t" + headerFooter.nLine.ToString() + "\r\n");
                        rtxEJ.add("Текст за отпечатване:\t" + GetStringFromCharArr(headerFooter.text) + "\r\n");
                        rtxEJ.add("Контролна сума:\t" + headerFooter.checksum.ToString() + "\r\n" + "\r\n");
                        break;
                    }
                case StructuredInfoRegister_DeviceGroup_C.tagRClass.PINPAD_INFO:
                    {
                        //не се използва
                        break;
                    }
                    */
        }
        return rtxEJ;
    }

    /**
     * Search EJ for a document by number of the listed types.
     *
     * @param docNumber
     * @return
     * @throws IOException
     * @throws FiscalException
     * @note If the search is by invoice type.Parameter docNumber is invoice number,
     * not by document number
     */
    public FiscalDocInfo ReadReceiptInfoFromEJ(Integer docNumber, DocTypeToRead... dt) throws IOException, FiscalException {

        for (DocTypeToRead tmp : dt)
            if (SetDocumentToRead(docNumber, tmp) == FiscalErrorCodesV2.OK) {//Document found
                return readFiscalReceiptInfo(docNumber);
            }
        return null;
    }

    /**
     * Read doc content
     *
     * @param docNum
     * @return
     * @throws IOException
     * @throws FiscalException
     */
    private FiscalDocInfo readFiscalReceiptInfo(int docNum) throws IOException, FiscalException {
        FiscalResponse R = new FiscalResponse(0);
        FiscalDocInfo res = new FiscalDocInfo(false);//Not data yet
        if (!isConnectedDeviceV2()) return null;
        while (true) {
            try {
                R = getConnectedModelV2().command125Variant2Version0("", "");
                byte[] data = Base64.decode(R.get("Data"),0);
                ParseDocInfo(data, res);
            } catch (FiscalException e) {
                int i = e.getErrorCode();
                if ((i == FiscalException.ERR_EJ_NO_RECORDS) || (i == FiscalException.ERR_END_OF_DATA))
                    return res;
                else throw e;
            }

        }
    }

    /**
     * Parse data
     *
     * @param data
     * @return
     */
    private void ParseDocInfo(byte[] data, FiscalDocInfo result) {
        result.setfDocData(data.length>0);
        StructuredInfoRegister_DeviceGroup_C.tagRClass type = getStructureType(data);
        switch (type) {
            case UNKNOWN:
                break;
            case ALL_VOID:
            {
                result.setAllVoid(true);
                break;
            }
            case OPEN_BON:
                StructuredInfoRegister_DeviceGroup_C.tagRCMDopen open = new StructuredInfoRegister_DeviceGroup_C.tagRCMDopen(data);
                result.setOperator(open.nClerk);
                result.setInvoiceNumber(open.nInvoice);
                result.setOther(open.subtype == StructuredInfoRegister_DeviceGroup_C.tagRSubClassOpen.OTHER);
                result.setStornoOfDocument(open.strNumberDoc);
                break;
            case CLOSE_BON: {
                StructuredInfoRegister_DeviceGroup_C.tagRCMDClose close = new StructuredInfoRegister_DeviceGroup_C.tagRCMDClose(data);
                result.setSubType(close.subtype);
                result.setInvoice(close.subtype == StructuredInfoRegister_DeviceGroup_C.tagRSubClassOpen.INVOICE);
                result.setClientEIK(new String(close.clientEIK).trim());
                result.setDateTime(StructuredInfoRegister_DeviceGroup_C.DateToString(close.bonDateTime));
                result.setIDnumber(new String(close.IDnumber).trim());
            }
        }
    }


    public class FiscalDocInfo {
        private StructuredInfoRegister_DeviceGroup_C.tagRSubClassOpen subType; //Type of document: sale, invoice or others-Storno, Void , Reports etc.
        private String dateTime; //DD-MM-YY hh:mm:ss DST  - Date and time of document closing(completion)
        private int operator;    //Code of the operator who issued the document 1-30
        private boolean invoice;
        private long invoiceNumber;
        //private String fmNumber;
        private String clientEIK;
        private boolean other;
        private String IDNumber;
        private int stornoOfDocument;
        private boolean allVoid=false;

        /**
         * @return true if there any data in the found document
         */
        public boolean isfDocData() {
            return fDocData;
        }

        public void setfDocData(boolean fDocData) {
            this.fDocData = fDocData;
        }

        private boolean fDocData = false;

        public FiscalDocInfo(boolean bDocData) {
            this.fDocData = bDocData;
        }

        public long getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(long invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }

        public boolean isInvoice() {
            return invoice;
        }

        public void setInvoice(boolean invoice) {
            this.invoice = invoice;
        }

        public int getOperator() {
            return operator;
        }

        public void setOperator(int operator) {
            this.operator = operator;
        }

        public FiscalDocInfo(StructuredInfoRegister_DeviceGroup_C.tagRSubClassOpen subType, String dateTime) {
            this.subType = subType;
            this.dateTime = dateTime;
        }


        public StructuredInfoRegister_DeviceGroup_C.tagRSubClassOpen getSubType() {
            return subType;
        }

        public void setSubType(StructuredInfoRegister_DeviceGroup_C.tagRSubClassOpen subType) {
            this.subType = subType;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public void setIDnumber(String IDNumber) {
            this.IDNumber = IDNumber;
        }

        public String getIDnumber() {
            return this.IDNumber;
        }


        public void setClientEIK(String clientEIK) {
            this.clientEIK = clientEIK;
        }

        public String getClientEIK() {
            return clientEIK;
        }

        public void setOther(boolean other) {
            this.other = other;
        }

        public boolean isOther() {
            return other;
        }


        public void setStornoOfDocument(int stornoOfDocument) {
            this.stornoOfDocument = stornoOfDocument;
        }

        public int getStornoOfDocument() {
            return stornoOfDocument;
        }

        public void setAllVoid(boolean allVoid) {
            this.allVoid = allVoid;
        }

        public boolean isAllVoid() {
            return allVoid;
        }
    }
}

