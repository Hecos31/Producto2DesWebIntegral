package com.example.demo; // Cambia esto por el paquete que estés usando

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.StringReader;

@RestController
public class AppController {

    @GetMapping("/paso1/{numero}")
    public String paso1(@PathVariable String numero) {
        try {
            String soapEnvelope = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "  <soap:Body>\n" +
                    "    <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">\n" +
                    "      <ubiNum>" + numero + "</ubiNum>\n" +
                    "    </NumberToWords>\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);

            String url = "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";
            String soapResponse = restTemplate.postForObject(url, request, String.class);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(soapResponse)));
            
            String resultadoIngles = document.getElementsByTagName("m:NumberToWordsResult")
                                             .item(0)
                                             .getTextContent()
                                             .trim();

            return "<html>\n" +
                   "<head>\n" +
                   "    <meta charset=\"utf-8\">\n" +
                   "    <title>Paso 1 - SOAP Java</title>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <h2>Paso 1: Consumo SOAP (Java Spring Boot)</h2>\n" +
                   "    <p><strong>Número enviado:</strong> " + numero + "</p>\n" +
                   "    <p><strong>Resultado en inglés:</strong> " + resultadoIngles + "</p>\n" +
                   "</body>\n" +
                   "</html>";

        } catch (Exception e) {
            e.printStackTrace();
            return "Ocurrió un error al consumir el servicio SOAP: " + e.getMessage();
        }
    }
}