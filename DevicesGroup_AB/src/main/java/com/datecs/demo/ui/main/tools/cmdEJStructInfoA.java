package com.datecs.demo.ui.main.tools;

import com.datecs.fiscalprinter.SDK.FiscalResponse;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class cmdEJStructInfoA extends DatecsFiscalDevice {
    /**
     * Search EJ structural information by data time algorithm.
     *
     * @param fromDT - The beginning of the search
     * @param toDT   - The end of the search
     * @return A String list containing the data for the documents found in the time interval.
     * @throws Exception
     */
    public List<String> readDocumentsInPeriod(String fromDT, String toDT) throws Exception {
        List<String> listOfDocs = new ArrayList<>();
        String sDoc = "";
        int docNum = 0;
        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedECR())
            throw new UnsupportedOperationException("Not supported yet.");
        if (isConnectedPrinter()) {
            R = getConnectedPrinterV1().command119Variant11Version0(
                    fromDT.replace("-", "").replace(":", ""),
                    toDT.replace("-", "").replace(":", ""));
            if (R.get("errCode").equals("P")) {
                docNum = Integer.parseInt(R.get("recNumber").replace("\"", ""));
                while (!R.get("errCode").equals("F")) {

                    if (Integer.parseInt(R.get("recNumber").replace("\"", "")) > docNum) {
                        listOfDocs.add(sDoc);
                        sDoc = "";
                        docNum = Integer.parseInt(R.get("recNumber").replace("\"", ""));
                    }
                    sDoc += infoToString(R);
                    R = getConnectedPrinterV1().command119Variant11Version1(); //read EJ next line
                }
            }
        }
        return listOfDocs;
    }


    /**
     * @param r errCode - "P" The command is successful."F" - The command is unsuccessful or no data
     *          recDateTime -  first receipt date time
     *          serialNumber - fiscal device ID;
     *          recType  -  ФБ -fiscal receipt, РФБ -Invoice, СФБ -Refund receipt or РФБ -Credit notification;
     *          recNumber - global number of receipt;
     *          unp- Unique Sale ID;
     *          productName -  commodity/service - name;
     *          singlePrice - commodity/service – single price;
     *          qty - commodity/service - quantity;
     *          price - commodity/service - price;
     *          total- total price for the receipt;
     *          invoiceNumber - Invoice number/Credit notification - if the entry is for Invoice or Credit notification;
     *          uic- UIC of recipient – if the entry is for Invoice or Credit notification;
     *          docNumber - global number of the refund receipt – if the entry is for Invoice or Credit notification;
     *          stornoInvoiceNumber- number of the refunded invoice номер на сторнирана фактура – if the entry is for Invoic
     *          reasonOfStornoInvoice- reason for issue – in case if entry is for refund receipt or Credit notification.
     * @return Structured info as string
     */
    private String infoToString(FiscalResponse r) {

        return "\nfirst receipt date time: " + r.get("recDateTime") +
                "\nrecType: " + r.get("recType") +
                "\nglobal number of receipt: " + r.get("recNumber") +
                "\ncommodity/service - name: " + r.get("productName") +
                "\ncommodity/service - price: " + r.get("price");
    }

    /**
     * DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-700
     *
     * @return
     * @throws Exception
     */
    public String readStructuredInformation_FirstLine(int nDoc) throws Exception {
        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedECR()) throw new UnsupportedOperationException("Not supported yet.");
        else if (isConnectedPrinter()) {
            R = getConnectedPrinterV1().command119Variant10Version0(String.valueOf(nDoc), "");
            return R.get("Data");
        }
        return null;
    }

    /**
     * DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-700
     *
     * @return
     * @throws Exception
     */
    public String readStructuredInformation_NextLine() throws Exception {
        FiscalResponse R = new FiscalResponse(0);
        if (isConnectedECR()) throw new UnsupportedOperationException("Not supported yet.");
        else if (isConnectedPrinter()) {
            R = getConnectedPrinterV1().command119Variant10Version1();
            return R.get("Data");
        }
        return null;
    }


    private StructuredInfoRegister_DeviceGroup_B.tagRClass gteStructureType(byte[] data) {
        return StructuredInfoRegister_DeviceGroup_B.tagRClass.getById(data[0]);
    }


    public String DecodeLine(String sLine) {
/**
 *         U,        //   Structured information on the receipt. Once on the receipt.
 *         V,        //   Structured decimal digit and tax rate information. Once on the receipt
 *         R,        //   Structured sale or adjustment information. Can appear multiple times on the receipt.
 *         M,        //   Structured group discount/mark-up information. Can appear multiple times on the receipt
 *         T,        //   Structured information on the accumulated sums on the receipt. Once on the receipt.
 *         P,        //   Structured paid amount information. Once on the receipt.
 *         S,        //   Structured reversal receipt information. Not present in the fiscal receipts. Once on the receipt.
 *         I,        //   Structured invoice information. Once in invoice-type receipts.
 *         D,        //   Receipt start and end date and time
 *         Z,        //   Daily report with clearing.
 *         Q,        //   Serve In / Out sums.
 *         F,        //   No more data
 */
        switch (sLine.charAt(0)) {
            case 'U':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassU dataU = new StructuredInfoRegister_DeviceGroup_A.tagSubClassU(sLine);
                return dataU.getStringData();
            case 'V':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassV dataV = new StructuredInfoRegister_DeviceGroup_A.tagSubClassV(sLine);
                return dataV.getStringData();
            case 'R':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassR dataR = new StructuredInfoRegister_DeviceGroup_A.tagSubClassR(sLine);
                return dataR.getStringData();
            case 'M':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassM dataM = new StructuredInfoRegister_DeviceGroup_A.tagSubClassM(sLine);
                return dataM.getStringData();
            case 'T':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassT dataT = new StructuredInfoRegister_DeviceGroup_A.tagSubClassT(sLine);
                return dataT.getStringData();
            case 'P':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassP dataP = new StructuredInfoRegister_DeviceGroup_A.tagSubClassP(sLine);
                return dataP.getStringData();
            case 'S':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassS dataS = new StructuredInfoRegister_DeviceGroup_A.tagSubClassS(sLine);
                return dataS.getStringData();
            case 'I':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassI dataI = new StructuredInfoRegister_DeviceGroup_A.tagSubClassI(sLine);
                return dataI.getStringData();
            case 'D':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassD dataD = new StructuredInfoRegister_DeviceGroup_A.tagSubClassD(sLine);
                return dataD.getStringData();
            case 'Z':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassZ dataZ = new StructuredInfoRegister_DeviceGroup_A.tagSubClassZ(sLine);
                return dataZ.getStringData();
            case 'Q':
                StructuredInfoRegister_DeviceGroup_A.tagSubClassQ dataQ = new StructuredInfoRegister_DeviceGroup_A.tagSubClassQ(sLine);
                return dataQ.getStringData();
            case 'F':
                break;
            default:


        }
        return "";
    }

    public DocInfo getDocInfo(Integer globalDocNumber) throws Exception {
        DocInfo res = new DocInfo();
        String readLine = readStructuredInformation_FirstLine(globalDocNumber);
        if (readLine.startsWith("F")) return res;
        while (!readLine.startsWith("*")) {
            StructuredInfoRegister_DeviceGroup_A.tagSubClassD D;
            if (readLine.startsWith("D")) {
                D = new StructuredInfoRegister_DeviceGroup_A.tagSubClassD(readLine);
                res.docFinishedDate = D.getEndDT().split(" ")[0];
                res.docFinishedTime = D.getEndDT().split(" ")[1];
            }
            StructuredInfoRegister_DeviceGroup_A.tagSubClassU U;
            if (readLine.startsWith("U")) {
                U = new StructuredInfoRegister_DeviceGroup_A.tagSubClassU(readLine);
                res.setDocUNP(U.UNP);
                res.setDocOperNum(U.OperNum);
                res.setTillNum(U.TillNum);
                res.setRecType(U.recType);
            }
            StructuredInfoRegister_DeviceGroup_A.tagSubClassI I;
            if (readLine.startsWith("I")) {
                I = new StructuredInfoRegister_DeviceGroup_A.tagSubClassI(readLine);
                res.setInvoiceNumber(I.Invoice);
            }
            readLine = readStructuredInformation_NextLine();
        }
        return res;
    }


    public class DocInfo {
        private String docFinishedDate;
        private String docFinishedTime;
        private String docUNP;

        public long getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(long invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }

        private long invoiceNumber;

        public String getDocUNP() {
            return docUNP;
        }

        public void setDocUNP(String docUNP) {
            this.docUNP = docUNP;
        }

        public int getDocOperNum() {
            return docOperNum;
        }

        public void setDocOperNum(int docOperNum) {
            this.docOperNum = docOperNum;
        }

        public int getTillNum() {
            return TillNum;
        }

        public void setTillNum(int tillNum) {
            TillNum = tillNum;
        }

        public StructuredInfoRegister_DeviceGroup_A.RecType getRecType() {
            return recType;
        }

        public void setRecType(StructuredInfoRegister_DeviceGroup_A.RecType recType) {
            this.recType = recType;
        }

        private int docOperNum;
        private int TillNum;
        private StructuredInfoRegister_DeviceGroup_A.RecType recType;

        public String getDocFinishedDate() {
            return docFinishedDate;
        }

        public void setDocFinishedDate(String docFinishedDate) {
            this.docFinishedDate = docFinishedDate;
        }

        public String getDocFinishedTime() {
            return docFinishedTime;
        }

        public void setDocFinishedTime(String docFinishedTime) {
            this.docFinishedTime = docFinishedTime;
        }
    }
}





