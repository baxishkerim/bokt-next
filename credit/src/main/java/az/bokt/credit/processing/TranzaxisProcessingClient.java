package az.bokt.credit.processing;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Реальный клиент процессинга TranzAxis (SOAP/SAAJ). Включается при
 * {@code bokt.processing.tranzaxis.enabled=true} и подменяет заглушку.
 * <p>
 * Воспроизводит операции старого сервиса:
 * <ul>
 *   <li>{@link #charge} — выдача (paymentCredit): материнская карта → карта выдачи, одна операция;</li>
 *   <li>{@link #reverse} — реверс выдачи (IsReversal=true, Link Reversed2Reversal);</li>
 *   <li>{@link #getCard} — справка по карте (GetCardByPAN).</li>
 * </ul>
 * Суммы передаются в основных единицах (минорные /100). Результат читается из
 * {@code Response/@Result}; RRN — {@code @Id}, код авторизации — {@code @ApprovalCode}.
 */
@Component
@ConditionalOnProperty(prefix = "bokt.processing.tranzaxis", name = "enabled", havingValue = "true")
public class TranzaxisProcessingClient implements ProcessingClient {

    private static final Logger log = LoggerFactory.getLogger(TranzaxisProcessingClient.class);

    // Пространства имён TranzAxis
    private static final String WSDL = "http://schemas.tranzaxis.com/tran.wsdl";
    private static final String TRAN = "http://schemas.tranzaxis.com/tran.xsd";
    private static final String COMMON_TYPES = "http://schemas.tranzaxis.com/common-types.xsd";
    private static final String TRAN_COMMON = "http://schemas.tranzaxis.com/tran-common.xsd";

    private final TranzaxisProperties props;

    public TranzaxisProcessingClient(TranzaxisProperties props) {
        this.props = props;
    }

    // ------------------------------------------------------------------ charge

    @Override
    public ProcessingResult charge(ProcessingRequest req) {
        try {
            SimpleDateFormat full = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            SimpleDateFormat mmYY = new SimpleDateFormat("MMyy");
            String amount = minorToDecimal(req.amountMinor());
            String fee = minorToDecimal(req.feeMinor());

            SOAPMessage msg = MessageFactory.newInstance().createMessage();
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            declareNamespaces(env);
            SOAPBody body = env.getBody();
            SOAPBodyElement tran = body.addBodyElement(new QName(WSDL, "Tran", "ns1"));

            SOAPElement request = tran.addChildElement(new QName(TRAN, "Request", "ns2"));
            request.setAttribute("InitiatorRid", props.initiatorRid());
            request.setAttribute("ProcessorInstId", props.processorInstId());
            request.setAttribute("Kind", props.chargeKind());
            request.setAttribute("LifePhase", "Single");
            request.setAttribute("IsAdvice", "false");

            SOAPElement parties = request.addChildElement(new QName(TRAN, "Parties", "ns2"));
            SOAPElement cust = parties.addChildElement(new QName(TRAN, "Cust", "ns2"));
            cust.setAttribute("Presence", "false");
            if (req.payerCvv() == null) {
                cust.setAttribute("AuthChecked", "true");
            }
            SOAPElement token = cust.addChildElement(new QName(TRAN, "Token", "ns2"));
            token.setAttribute("Kind", "Card");
            SOAPElement payerCard = token.addChildElement(new QName(TRAN_COMMON, "Card", "ns4"));
            payerCard.setAttribute("ExpDate", full.format(mmYY.parse(req.payerExpiry())));
            payerCard.setAttribute("Pan", req.payerPan());
            payerCard.setAttribute("EntryMode", "ECommerce");
            SOAPElement auth = payerCard.addChildElement(new QName(TRAN_COMMON, "Auth", "ns4"));
            if (req.payerCvv() != null) {
                auth.setAttribute("Cvv2", req.payerCvv());
            }

            SOAPElement payee = parties.addChildElement(new QName(TRAN, "Payee", "ns2"));
            payee.setAttribute("Type", "CustToken");
            SOAPElement payeeCard = payee.addChildElement(new QName(TRAN_COMMON, "Card", "ns4"));
            if (req.payeePan() != null) {
                payeeCard.setAttribute("Pan", req.payeePan());
            }

            SOAPElement term = parties.addChildElement(new QName(TRAN, "Term", "ns2"));
            term.setAttribute("Rid", props.terminalRid());

            SOAPElement match = request.addChildElement(new QName(TRAN, "Match", "ns2"));
            match.setAttribute("Key", req.guid());
            match.setAttribute("CheckForDuplicate", "true");

            SOAPElement moneys = request.addChildElement(new QName(TRAN, "Moneys", "ns2"));
            SOAPElement mCust = moneys.addChildElement(new QName(TRAN, "Cust", "ns2"));
            mCust.setAttribute("Ccy", req.currencyCode());
            mCust.setAttribute("Amt", amount);
            mCust.setAttribute("Fee", fee);
            SOAPElement mClear = moneys.addChildElement(new QName(TRAN, "Clear", "ns2"));
            mClear.setAttribute("Ccy", req.currencyCode());
            mClear.setAttribute("Amt", amount);
            mClear.setAttribute("AcqFeeAmt", fee);

            String response = send(msg, "CHARGE", req.guid());
            return readTranResult(response);
        } catch (Exception e) {
            log.error("TranzAxis CHARGE failed guid={}", req.guid(), e);
            return new ProcessingResult.Declined(ProcessingErrorCode.INTERNAL, e.getMessage());
        }
    }

    // ----------------------------------------------------------------- reverse

    @Override
    public ProcessingResult reverse(ReversalRequest req) {
        try {
            String amount = minorToDecimal(req.amountMinor());

            SOAPMessage msg = MessageFactory.newInstance().createMessage();
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            declareNamespaces(env);
            SOAPBody body = env.getBody();
            SOAPBodyElement tran = body.addBodyElement(new QName(WSDL, "Tran", "ns1"));

            SOAPElement request = tran.addChildElement(new QName(TRAN, "Request", "ns2"));
            request.setAttribute("InitiatorRid", props.initiatorRid());
            request.setAttribute("ProcessorInstId", props.processorInstId());
            request.setAttribute("Kind", props.reverseKind());
            request.setAttribute("LifePhase", "Single");
            request.setAttribute("IsReversal", "true");
            request.setAttribute("IsPartial", "false");

            SOAPElement parties = request.addChildElement(new QName(TRAN, "Parties", "ns2"));
            SOAPElement cust = parties.addChildElement(new QName(TRAN, "Cust", "ns2"));
            cust.setAttribute("Presence", "true");
            SOAPElement token = cust.addChildElement(new QName(TRAN, "Token", "ns2"));
            token.setAttribute("Kind", "Card");
            SOAPElement card = token.addChildElement(new QName(TRAN_COMMON, "Card", "ns4"));
            card.setAttribute("Pan", req.pan());
            card.setAttribute("EntryMode", "ECommerce");
            SOAPElement term = parties.addChildElement(new QName(TRAN, "Term", "ns2"));
            term.setAttribute("Rid", props.terminalRid());

            SOAPElement link = request.addChildElement(new QName(TRAN, "Link", "ns2"));
            link.setAttribute("Kind", "Reversed2Reversal");
            link.setAttribute("ResultOnFailure", "SystemError");
            link.setAttribute("Key", req.originalGuid());

            SOAPElement match = request.addChildElement(new QName(TRAN, "Match", "ns2"));
            match.setAttribute("CheckForDuplicate", "true");

            SOAPElement specific = request.addChildElement(new QName(TRAN, "Specific", "ns2"));
            specific.setAttribute("ReversalReason", "Cancel");

            if (req.amountMinor() > 0) {
                SOAPElement moneys = request.addChildElement(new QName(TRAN, "Moneys", "ns2"));
                SOAPElement mCust = moneys.addChildElement(new QName(TRAN, "Cust", "ns2"));
                mCust.setAttribute("Ccy", req.currencyCode());
                mCust.setAttribute("Amt", amount);
                SOAPElement mClear = moneys.addChildElement(new QName(TRAN, "Clear", "ns2"));
                mClear.setAttribute("Ccy", req.currencyCode());
                mClear.setAttribute("Amt", amount);
            }

            String response = send(msg, "REVERSE", req.originalGuid());
            return readTranResult(response);
        } catch (Exception e) {
            log.error("TranzAxis REVERSE failed guid={}", req.originalGuid(), e);
            return new ProcessingResult.Declined(ProcessingErrorCode.INTERNAL, e.getMessage());
        }
    }

    // ----------------------------------------------------------------- getCard

    @Override
    public CardInfo getCard(String pan) {
        try {
            SOAPMessage msg = MessageFactory.newInstance().createMessage();
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            env.addNamespaceDeclaration("tran", WSDL);
            env.addNamespaceDeclaration("tran1", TRAN);
            env.addNamespaceDeclaration("tran2", TRAN_COMMON);
            SOAPBody body = env.getBody();
            SOAPBodyElement tran = body.addBodyElement(new QName(WSDL, "Tran", "tran"));

            SOAPElement request = tran.addChildElement(new QName(TRAN, "Request", "tran1"));
            request.setAttribute("InitiatorRid", props.initiatorRid());
            request.setAttribute("ProcessorInstId", props.processorInstId());
            request.setAttribute("Kind", "Udt");
            request.setAttribute("LifePhase", "Single");
            request.setAttribute("UdtType", "GetCardByPAN");
            SOAPElement parties = request.addChildElement(new QName(TRAN, "Parties", "tran1"));
            SOAPElement cust = parties.addChildElement(new QName(TRAN, "Cust", "tran1"));
            SOAPElement token = cust.addChildElement(new QName(TRAN, "Token", "tran1"));
            token.setAttribute("Kind", "Card");
            SOAPElement card = token.addChildElement(new QName(TRAN_COMMON, "Card", "tran2"));
            card.setAttribute("Pan", pan);

            String response = send(msg, "GET-CARD", "pan-inquiry");
            Document doc = parse(response);
            XPath xp = XPathFactory.newInstance().newXPath();

            String avail = paramValue(xp, doc, "AvailBalance");
            String curIso = paramValue(xp, doc, "CurISO");
            String status = cardValue(xp, doc, "Status");
            String expiry = cardValue(xp, doc, "ExpTime");
            String holder = cardValue(xp, doc, "EmbossName");

            // карта не найдена / пустой ответ
            if (avail == null && status == null && expiry == null && holder == null) {
                String result = text(xp, doc, "//*[local-name()='Response']/@Result");
                log.warn("GET-CARD empty response (result={})", result);
                return null;
            }

            long availableMinor = avail == null ? 0L
                    : new BigDecimal(avail).movePointRight(2).longValueExact();
            return new CardInfo(pan, status, expiry, holder, availableMinor, curIso);
        } catch (Exception e) {
            log.error("TranzAxis GET-CARD failed", e);
            return null;
        }
    }

    // --------------------------------------------------------------- helpers

    private void declareNamespaces(SOAPEnvelope env) throws Exception {
        env.addNamespaceDeclaration("ns1", WSDL);
        env.addNamespaceDeclaration("ns2", TRAN);
        env.addNamespaceDeclaration("ns3", COMMON_TYPES);
        env.addNamespaceDeclaration("ns4", TRAN_COMMON);
    }

    private String send(SOAPMessage msg, String op, String ref) throws Exception {
        if (log.isDebugEnabled()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            msg.writeTo(out);
            log.debug("TranzAxis {} request ref={}:\n{}", op, ref, out.toString());
        }
        SOAPConnection conn = SOAPConnectionFactory.newInstance().createConnection();
        try {
            SOAPMessage resp = conn.call(msg, props.url());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            resp.writeTo(out);
            String xml = out.toString();
            if (log.isDebugEnabled()) {
                log.debug("TranzAxis {} response ref={}:\n{}", op, ref, xml);
            }
            return xml;
        } finally {
            conn.close();
        }
    }

    /** Разбор ответа Tran в ProcessingResult (Approved/Declined). */
    private ProcessingResult readTranResult(String xml) throws Exception {
        Document doc = parse(xml);
        XPath xp = XPathFactory.newInstance().newXPath();
        String result = text(xp, doc, "//*[local-name()='Response']/@Result");
        if ("Approved".equalsIgnoreCase(result)) {
            String rrn = text(xp, doc, "//*[local-name()='Response']/@Id");
            String approval = text(xp, doc, "//*[local-name()='Response']/@ApprovalCode");
            return new ProcessingResult.Approved(rrn, approval);
        }
        String reason = text(xp, doc, "//*[local-name()='Response']/@DeclineReason");
        ProcessingErrorCode code = mapResult(result);
        String raw = (result == null ? "SystemError" : result) + (reason == null ? "" : ": " + reason);
        return new ProcessingResult.Declined(code, raw);
    }

    /** Строковый Result TranzAxis → наш код ошибки (с сохранением исходной строки в rawMessage). */
    private ProcessingErrorCode mapResult(String result) {
        if (result == null) {
            return ProcessingErrorCode.INTERNAL;
        }
        String r = result.toLowerCase();
        if (r.contains("insufficientfunds")) {
            return ProcessingErrorCode.INSUFFICIENT_FUNDS;
        }
        if (r.contains("duplicate")) {
            return ProcessingErrorCode.DUPLICATE_REFERENCE;
        }
        if (r.contains("expired") || r.contains("invalidexp") || r.contains("cantfindcontract")) {
            return ProcessingErrorCode.CARD_LOCKED;
        }
        if (r.contains("routingerror") || r.contains("systemerror") || r.contains("insufficientauth")) {
            return ProcessingErrorCode.INTERNAL;
        }
        return ProcessingErrorCode.UNKNOWN;
    }

    private Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }

    private String text(XPath xp, Document doc, String expr) throws Exception {
        String v = (String) xp.evaluate(expr, doc, XPathConstants.STRING);
        return (v == null || v.isEmpty()) ? null : v;
    }

    /** Значение параметра Params по атрибуту Rid. */
    private String paramValue(XPath xp, Document doc, String rid) throws Exception {
        return text(xp, doc, "//*[local-name()='Params']/*[@Rid='" + rid + "']");
    }

    /** Значение поля карты внутри блока Card (по имени узла, без учёта префикса). */
    private String cardValue(XPath xp, Document doc, String localName) throws Exception {
        return text(xp, doc, "(//*[local-name()='Card']//*[local-name()='" + localName + "'])[1]");
    }

    private String minorToDecimal(long minor) {
        return BigDecimal.valueOf(minor).movePointLeft(2).toPlainString();
    }
}
