package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.icu.text.RuleBasedNumberFormat;
import java.util.Locale;

@RestController
public class AppController {

    @GetMapping("/paso3/{numero}")
    public String paso3(@PathVariable Integer numero) {
        try {
            RuleBasedNumberFormat formateador = new RuleBasedNumberFormat(new Locale("es"), RuleBasedNumberFormat.SPELLOUT);
            
            String resultadoLetras = formateador.format(numero);

            if (resultadoLetras != null && !resultadoLetras.isEmpty()) {
                resultadoLetras = resultadoLetras.substring(0, 1).toUpperCase() + resultadoLetras.substring(1);
            }

            return "<html>\n" +
                   "<head>\n" +
                   "    <meta charset=\"utf-8\">\n" +
                   "    <title>Paso 3 - Java con Librería</title>\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <h2>Paso 3: Conversión de Número a Letras</h2>\n" +
                   "    <p><strong>Número ingresado:</strong> " + numero + "</p>\n" +
                   "    <p><strong>Resultado con 'icu4j':</strong> " + resultadoLetras + "</p>\n" +
                   "</body>\n" +
                   "</html>";

        } catch (Exception e) {
            e.printStackTrace();
            return "Ocurrió un error en el proceso: " + e.getMessage();
        }
    }
}