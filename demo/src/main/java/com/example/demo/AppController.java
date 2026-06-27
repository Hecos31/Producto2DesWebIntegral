package com.example.demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class AppController {

    @GetMapping("/paso2/{numero}")
    public String paso2(@PathVariable String numero) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. SOAP (Obtenemos el inglés)
            String soapEnvelope = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "  <soap:Body>\n" +
                    "    <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">\n" +
                    "      <ubiNum>" + numero + "</ubiNum>\n" +
                    "    </NumberToWords>\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);

            String soapUrl = "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";
            String soapResponse = restTemplate.postForObject(soapUrl, request, String.class);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(soapResponse)));
            
            String resultadoIngles = document.getElementsByTagName("m:NumberToWordsResult")
                                             .item(0).getTextContent().trim();

            // 2. Traducción (Usando API pública y gratuita sin Key)
            String textoCodificado = URLEncoder.encode(resultadoIngles, StandardCharsets.UTF_8.toString());
            String urlTraduccion = "https://api.mymemory.translated.net/get?q=" + textoCodificado + "&langpair=en|es";
            
            String jsonResponse = restTemplate.getForObject(urlTraduccion, String.class);
            
            // Usamos la librería Jackson para mapear la respuesta
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            String resultadoEspanol = root.path("responseData").path("translatedText").asText();

            return "<html>\n" +
                   "<head>\n" +
                   "    <meta charset=\"utf-8\">\n" +
                   "    <title>Paso 2 - Traducción API Pública</title>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <h2>Paso 2: SOAP y Traducción </h2>\n" +
                   "    <p><strong>Número enviado:</strong> " + numero + "</p>\n" +
                   "    <p><strong>Resultado en inglés:</strong> " + resultadoIngles + "</p>\n" +
                   "    <p><strong>Resultado final traducido a español:</strong> " + resultadoEspanol + "</p>\n" +
                   "</body>\n" +
                   "</html>";

        } catch (Exception e) {
            e.printStackTrace();
            return "Ocurrió un error en el proceso: " + e.getMessage();
        }
    }
}