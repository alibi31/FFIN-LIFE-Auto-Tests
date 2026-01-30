package life.utils;

public class XmlRequestBuilder {

    public static String buildSessionIdRequest() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ffinlife/ws">
                <soapenv:Header/>
                <soapenv:Body>
                    <ws:authorizationWSRequest>
                        <ws:login>ffinlife_site</ws:login>
                        <ws:password>1q3wr2</ws:password>
                    </ws:authorizationWSRequest>
                </soapenv:Body>
            </soapenv:Envelope>
            """;
    }

    public static String buildActivatePolicyRequest(String sessionId, String contractId) {
        return String.format("""
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ffinlife/ws">
               <soapenv:Header/>
               <soapenv:Body>
                  <ws:proofPaymentRequest>
                     <ws:sessionId>%s</ws:sessionId>
                     <ws:id>%s</ws:id>
                     <ws:idPaybox>123</ws:idPaybox>
                     <ws:Sum>100</ws:Sum>
                  </ws:proofPaymentRequest>
               </soapenv:Body>
            </soapenv:Envelope>
            """, sessionId, contractId);
    }
}

